/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
 */

package cz.vutbr.stud.fit.xsimon13.whoowns.tasks;

import cz.vutbr.stud.fit.xsimon13.whoowns.Entity;
import cz.vutbr.stud.fit.xsimon13.whoowns.Request;
import cz.vutbr.stud.fit.xsimon13.whoowns.StandardOutputCommunicator;
import cz.vutbr.stud.fit.xsimon13.whoowns.Utils;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Person;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Team;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.TeamAssignment;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.OwnersTree;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.OwnershipStatistics;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.text.WordEntity;
import cz.vutbr.stud.fit.xsimon13.whoowns.text.WordStatistics;
import org.json.JSONArray;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ServerTask implements Task {
    private OwnershipStatistics teamStats;
    private WordStatistics wordStats;

    private OwnersTree ownersTree;

    private Set<String> words;
    private Path staticFile;
    private Set<Team> teams;

    @Override
    public String getName() {
        return "server";
    }

    @Override
    public void run(TeamAssignment teamAssignment, Jedis jedis, List<String> params) throws Exception {
        validateParams(params);

        // Load list of teams into memory
        teams = new TeamAssignment(jedis).getTeams();

        // Load list of words into memory
        {
            List<String> categories = new ArrayList<String>(teams.size());
            for (Team team : teams)
                categories.add(team.toEntityId());

            wordStats = new WordStatistics(jedis);
            words = wordStats.getAllWordsWithOwner(categories);
        }

        // Load ownership stats into memory
        teamStats = new OwnershipStatistics(jedis, ProcessChangelistsTask.TEAM_STATISTICS_NAME);
        ownersTree = new OwnersTree(teamStats, 1.2);

        new StandardOutputCommunicator(this::makeResponseTo);
    }

    public String makeResponseTo(String requestString) {
        Request request = new Request(requestString);
        String uri = request.getPath();

        try {
            if ("".equals(uri) || "/".equals(uri) || "/index.html".equals(uri)) {
                String staticContent = Utils.readFromFile(staticFile);
                return staticContent;
            }
            else if ("/autocomplete".equals(uri)) {
                String text = request.getParameter("q");
                if (text == null)
                    return error("not supported any more");

                JSONArray array = new JSONArray();
                for (Entity suggestion : autoComplete(text)) {
                    JSONObject item = new JSONObject();
                    item.put("type", suggestion.getType());
                    item.put("data", suggestion.getData());
                    array.put(item);
                }

                return array.toString();
            }
            else if ("/query".equals(uri)) {
                String q = request.getParameter("q");
                String type = request.getParameter("t");
                if (q == null || type == null)
                    return error("Missing parameter q or t");

                String r;
                if (type.equals(Entity.JAVA_PREFIX))
                    r = getOwners(new ScopePath(q)).toString();
                else if (type.equals(Entity.WORD_PREFIX))
                    r = getOwners(new WordEntity(q)).toString();
                else if (type.equals(Entity.PERSON_PREFIX))
                    r = getOwnedBy(new Person(q)).toString();
                else if (type.equals(Entity.TEAM_PREFIX))
                    r = getOwnedBy(new Team(q)).toString();
                else
                    return error("Incorrect type");

                return r;
            }
            else
                return error("Page not found");
        }
        catch (Exception e) {
            return error(e.toString());
        }
    }

    private String error(String reason) {
        return "Error: " + reason;
    }

    @Override
    public void validateParams(List<String> params) throws WrongParameterException {
        if (params.size() != 1)
            throw new WrongParameterException(this, "Wrong number of parameters - expected index file.");

        staticFile = Paths.get(params.get(0));
        if (!Files.isRegularFile(staticFile))
            throw new WrongParameterException(this, "File " + staticFile + " not found");
    }

    public List<Entity> autoComplete(final String text) {
        final String upperCaseText = text.toUpperCase();

        // Paths
        final List<Entity> suggestions = ownersTree.autoComplete(upperCaseText, 10);

        // Keywords
        List<Entity> startsWith = new ArrayList<Entity>(10);
        List<Entity> contains = new ArrayList<Entity>(20);
        for (String word : words) {
            if (word.toUpperCase().startsWith(upperCaseText))
                startsWith.add(new WordEntity(word));
            else if (word.toUpperCase().contains(upperCaseText))
                contains.add(new WordEntity(word));

            if (startsWith.size() > 10)
                break;
        }
        suggestions.addAll(startsWith);
        suggestions.addAll(contains.subList(0, contains.size() > 5 ? 5 : contains.size()));

        // Teams
        for (Team team : teams) {
            if (team.toString().toUpperCase().contains(upperCaseText))
                suggestions.add(team);
        }

        return suggestions;
    }

    public JSONObject getOwners(ScopePath path) throws Exception {
        return ownersTree.getOwners(path, 10);
    }

    public JSONObject getOwners(WordEntity word) throws Exception {
        JSONArray owners = new JSONArray();

        double sum = 0.0d;
        for (Team team : teams)
            sum += wordStats.getWeight(word.getData(), team.toEntityId(), teamStats.getMonths());

        for (Team team : teams) {
            double w = wordStats.getWeight(word.getData(), team.toEntityId(), teamStats.getMonths());
            w /= sum;

            JSONObject item = new JSONObject();
            item.put("name", team.toEntityId());
            item.put("w", w);
            item.put("type", Entity.TEAM_PREFIX);
            owners.put(item);
        }

        JSONObject result = new JSONObject();
        result.put("type", Entity.WORD_PREFIX);
        result.put("name", word.getData());
        result.put("owners", owners);

        return result;
    }

    public JSONArray getOwnedBy(final Team team) throws Exception {

        JSONObject keywords = new JSONObject();
        JSONArray keywordArray = new JSONArray();
        keywords.put("type", Entity.TEAM_PREFIX);
        keywords.put("name", team.toEntityId());
        keywords.put("other", "Keywords");
        keywords.put("children", keywordArray);

        int projectMonths = teamStats.getMonths();
        for (WordStatistics.Keyword keyword : wordStats.extractKeywordsForCategoryWithWeights(team.toEntityId(), 20, projectMonths)) {
            JSONObject keywordJson = getOwners(new WordEntity(keyword.word));
            keywordJson.put("w", keyword.weight * 10);
            keywordArray.put(keywordJson);
        }

        JSONObject code = new JSONObject();
        JSONArray codeArray = new JSONArray();
        code.put("type", Entity.TEAM_PREFIX);
        code.put("name", team.toEntityId());
        code.put("other", "Code areas");
        code.put("children", codeArray);

        for (OwnersTree.OwnedScopePath item : ownersTree.getCodeOwnedBy(team.toEntityId(), 10)) {
            JSONObject codeJson = ownersTree.getOwners(item.scopePath, 1);
            codeJson.put("w", item.score);
            codeArray.put(codeJson);
        }

        JSONArray result = new JSONArray();
        result.put(keywords);
        result.put(code);
        return result;
    }

    public JSONObject getOwnedBy(final Person team) throws Exception {
        JSONObject result = new JSONObject();
        result.put("type", Entity.WORD_PREFIX);
        result.put("name", "");
        result.put("other", "Sorry, extracting ownership for a person is not supported yet. Try team ownership instead.");
        return result;
    }
}
