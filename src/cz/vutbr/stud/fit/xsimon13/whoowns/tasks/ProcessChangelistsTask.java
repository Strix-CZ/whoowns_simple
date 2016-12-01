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

import cz.vutbr.stud.fit.xsimon13.whoowns.CompoundAnalyzer;
import cz.vutbr.stud.fit.xsimon13.whoowns.Factory;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.TeamAssignment;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.JavaAnalyzer;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.OwnershipStatistics;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.text.TextAnalyzer;
import cz.vutbr.stud.fit.xsimon13.whoowns.text.WordStatistics;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.ChangelistProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.VersionControlType;
import redis.clients.jedis.Jedis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ProcessChangelistsTask implements Task {

    private final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    public final static String TEAM_STATISTICS_NAME = "Team";
    public final static String AUTHOR_STATISTICS_NAME = "Author";

    private Path root;
    private String branch;
    private Date startDate;
    private Date endDate;
    private VersionControlType versionControlType;


    @Override
    public String getName()
    {
        return "process changes";
    }

    @Override
    public void run(TeamAssignment teamAssignment, Jedis jedis, List<String> params) throws Exception
    {
        validateParams(params);

        ParsedClassProvider classProvider = Factory.createParsedClassProvider(root);

        OwnershipStatistics teamStats = new OwnershipStatistics(jedis, TEAM_STATISTICS_NAME);
        OwnershipStatistics authorStats = new OwnershipStatistics(jedis, AUTHOR_STATISTICS_NAME);
        WordStatistics wordStats = new WordStatistics(jedis);

        CompoundAnalyzer analyzer = new CompoundAnalyzer();
        analyzer.addAnalyzer(new JavaAnalyzer(authorStats, teamStats, teamAssignment));
        analyzer.addAnalyzer(new TextAnalyzer(wordStats, teamAssignment));

        ChangelistProvider provider = versionControlType.createChangelistProvider(jedis);
        provider.run(root, classProvider, branch, startDate, endDate, analyzer);
    }

    @Override
    public void validateParams(List<String> params) throws WrongParameterException
    {
        if (params.size() != 5)
            throw new WrongParameterException(this, "Wrong number of params - must be p4|git, root, branch, start date, end date. Got:\n" + params);

        versionControlType = VersionControlType.fromName(params.get(0));

        root = Paths.get(params.get(1));
        if (!Files.isDirectory(root))
            throw new WrongParameterException(this, "Wrong root " + root);

        branch = params.get(2);

        try
        {
            startDate = dateFormat.parse(params.get(3));
            endDate = dateFormat.parse(params.get(4));
        }
        catch (ParseException p) {
            throw new WrongParameterException(this, "Date must be in format " + dateFormat.toPattern());
        }
    }
}
