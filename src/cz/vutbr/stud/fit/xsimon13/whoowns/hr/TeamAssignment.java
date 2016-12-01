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

package cz.vutbr.stud.fit.xsimon13.whoowns.hr;

import cz.vutbr.stud.fit.xsimon13.whoowns.Utils;
import cz.vutbr.stud.fit.xsimon13.whoowns.db.DBSet;
import redis.clients.jedis.Jedis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Links people to teams.
 * Capable of moving people across teams in time.
 * And also capable of getting list of all teams.
 */
public class TeamAssignment
{
    private static final Pattern defaultTeamPattern = Pattern.compile("^default team ([a-zA-Z]+)$");
    private static final Pattern ignoreTeamPattern = Pattern.compile("^ignore changes of team ([a-zA-Z]+)$");
    private static final Pattern teamPattern = Pattern.compile("^team ([a-zA-Z]+)$");
    private static final Pattern simplePersonPattern = Pattern.compile("^([^ ]+)$");
    private static final Pattern personPattern = Pattern.compile("^([^ ]+)\\s*([-0-9]*)\\s*to\\s*([-0-9]*)$");

    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private Map<String, TreeMap<Date, Assignment>> personAssignments = new HashMap<String, TreeMap<Date, Assignment>>();
    private Team defaultTeam = null;
    private Team ignoreTeam = null;

    // Set of all teams used
    private static final String KEY_TEAMS = "Teams";
    DBSet<Team> dbTeams;
    Set<Team> teams; // local copy to speed it up

    public TeamAssignment(Jedis jedis) {
        dbTeams = new DBSet<Team>(jedis, KEY_TEAMS, new Team.Converter());
        teams = new HashSet<Team>();
        teams.addAll(dbTeams.getAll());
    }

    public void clearTeams() {
        dbTeams.clear();
        teams.clear();
    }

    /**
     * Parse the file of team assignment and return an instance.
     */
    public static TeamAssignment parse(Jedis jedis, String def) throws ParseException {
        TeamAssignment p = new TeamAssignment(jedis);

        String team = null;

        for (String line : Utils.splitLines(def)) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith("#"))
                continue;

            Matcher teamMatcher = teamPattern.matcher(line);
            Matcher personMatcher = personPattern.matcher(line);
            Matcher simplePersonMatcher = simplePersonPattern.matcher(line);
            Matcher defaultTeamMatcher = defaultTeamPattern.matcher(line);
            Matcher ignoreTeamMatcher = ignoreTeamPattern.matcher(line);

            if (ignoreTeamMatcher.matches()) {
                if (p.ignoreTeam != null)
                    throw new ParseException("Ignore changes of team declaration appeared twice", 0);
                p.ignoreTeam = new Team(ignoreTeamMatcher.group(1));
            }
            else if (defaultTeamMatcher.matches()) {
                if (p.defaultTeam != null)
                    throw new ParseException("Default team declaration appeared twice", 0);
                p.defaultTeam = new Team(defaultTeamMatcher.group(1));
            }
            else if (teamMatcher.matches())
                team = teamMatcher.group(1);
            else if (personMatcher.matches()) {
                String login = personMatcher.group(1);
                String startDateStr = personMatcher.group(2);
                String endDateStr = personMatcher.group(3);

                Date startDate = startDateStr.isEmpty() ? null : dateFormat.parse(startDateStr);
                Date endDate = endDateStr.isEmpty() ? null : dateFormat.parse(endDateStr);

                p.addAssignment(team, login, startDate, endDate);
            }
            else if (simplePersonMatcher.matches())
                p.addAssignment(team, simplePersonMatcher.group(1), null, null);
            else
                throw new ParseException("Unknown line: '" + line + "'", 0);
        }

        return p;
    }

    /**
     * Adds assignment of a person `login` to `team`.
     */
    public void addAssignment(String team, String login, Date startDate, Date endDate) throws ParseException {

        if (startDate == null)
            startDate = new GregorianCalendar(1900, 0, 1).getTime();
        if (endDate == null)
            endDate = new GregorianCalendar(2100, 0, 1).getTime();
        if (!startDate.before(endDate))
            throw new ParseException("Start date must be before end date (" + login + "@" + team + ")", 0);

        TreeMap<Date, Assignment> assignments = personAssignments.get(login);
        if (assignments == null) {
            assignments = new TreeMap<Date, Assignment>();
            personAssignments.put(login, assignments);
        }

        Map.Entry<Date, Assignment> nextAssignment = assignments.ceilingEntry(startDate);
        if (nextAssignment != null && nextAssignment.getKey().before(endDate))
            throw new ParseException("Ending date " + dateFormat.format(endDate) + " (" + team + ") overlaps with next assignment (" + login + "@" + nextAssignment.getValue().team + ")", 0);

        Map.Entry<Date, Assignment> prevAssignment = assignments.floorEntry(endDate);
        if (prevAssignment != null && prevAssignment.getValue().endDate.after(startDate))
            throw new ParseException("Starting date " + dateFormat.format(startDate) + " (" + team + ") overlaps with previous assignment (" + login + "@" + prevAssignment.getValue().team + ")", 0);

        assignments.put(startDate, new Assignment(startDate, endDate, team));
    }

    /**
     * Gets person's team on a specific date.
     */
    public Team getTeam(Person person, Date time) {
        Team team;
        TreeMap<Date, Assignment> assignments = personAssignments.get(person.getLogin());
        if (assignments == null) {
            if (defaultTeam != null)
                team = defaultTeam;
            else
                team = new Team(person.getLogin()); // Not assigned to any team - self contained team :-)
        }
        else {
            Map.Entry<Date, Assignment> assignment = assignments.floorEntry(time);
            if (assignment == null || assignment.getValue().endDate.before(time)) {
                if (defaultTeam != null)
                    team = defaultTeam;
                else
                    team = new Team(person.getLogin()); // Not assigned any more - self contained team :-)
            }
            else
                team = new Team(assignment.getValue().team);
        }

        if (!team.equals(ignoreTeam) && !teams.contains(team)) {
            dbTeams.add(team);
            teams.add(team);
        }
        return team;
    }

    public Set<Team> getTeams() {
        return teams;
    }

    public boolean isTeamIgnored(Team team) {
        return team.equals(ignoreTeam);
    }


    public static class Assignment implements Comparable<Assignment> {
        public final Date startDate;
        public final Date endDate;
        public final String team;

        public Assignment(Date startDate, Date endDate, String team)
        {
            this.startDate = startDate;
            this.endDate = endDate;
            this.team = team;
        }

        @Override
        public int compareTo(Assignment o) {
            int c = startDate.compareTo(o.startDate);
            if (c != 0)
                return c;
            return team.compareTo(team);
        }
    }
}
