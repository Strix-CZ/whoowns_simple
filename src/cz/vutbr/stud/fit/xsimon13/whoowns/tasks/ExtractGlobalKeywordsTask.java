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

import cz.vutbr.stud.fit.xsimon13.whoowns.hr.TeamAssignment;
import cz.vutbr.stud.fit.xsimon13.whoowns.text.WordStatistics;
import redis.clients.jedis.Jedis;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

public class ExtractGlobalKeywordsTask implements Task {
    @Override
    public String getName() {
        return "extract global keywords";
    }

    int maxKeywords;

    @Override
    public void run(TeamAssignment teamAssignment, Jedis jedis, List<String> params) throws Exception {
        validateParams(params);

        NumberFormat formatter = new DecimalFormat("#0.00");
        WordStatistics wordStatistics = new WordStatistics(jedis);

        for (WordStatistics.Keyword keyword : wordStatistics.extractKeywordsWithWeights(maxKeywords))
            System.out.println(formatter.format(keyword.weight) + ' ' + keyword.word);
    }

    @Override
    public void validateParams(List<String> params) throws WrongParameterException {
        if (params.size() != 1)
            throw new WrongParameterException(this, "Wrong number of parameters - should be number of keywords extracted per team.");

        try {
            maxKeywords = Integer.parseInt(params.get(0));
        }
        catch (NumberFormatException e) {
            throw new WrongParameterException(this, "The parameter must be a integer number.");
        }

        if (maxKeywords < 1)
            throw new WrongParameterException(this, "The parameter must be positive.");
    }
}
