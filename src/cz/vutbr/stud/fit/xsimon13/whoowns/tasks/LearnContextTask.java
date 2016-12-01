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

import cz.vutbr.stud.fit.xsimon13.whoowns.Factory;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.TeamAssignment;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.text.ContextLearner;
import cz.vutbr.stud.fit.xsimon13.whoowns.text.WordStatistics;
import redis.clients.jedis.Jedis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class LearnContextTask implements Task
{
    @Override
    public String getName()
    {
        return "learn context";
    }

    @Override
    public void run(TeamAssignment teamAssignment, Jedis jedis, List<String> params) throws Exception
    {
        validateParams(params);

        Path root = Paths.get(params.get(0));
        ParsedClassProvider classProvider = Factory.createParsedClassProvider(root);
        WordStatistics stats = new WordStatistics(jedis);

        ContextLearner learner = new ContextLearner(classProvider.getFileAccessor(), classProvider, stats);
        learner.learn();

        Logger.log("Saving");
        stats.save();
    }

    @Override
    public void validateParams(List<String> params) throws WrongParameterException
    {
        if (params.size() != 1)
            throw new WrongParameterException(this, "Missing parameter 1 - project root");

        Path root = Paths.get(params.get(0));
        if (!Files.isDirectory(root))
            throw new WrongParameterException(this, "Directory " + root + " not found");
    }

}
