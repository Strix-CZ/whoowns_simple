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

import cz.vutbr.stud.fit.xsimon13.whoowns.Utils;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.TeamAssignment;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TasksRunner
{
    public Map<String, Task> tasks = new HashMap<String, Task>();
    private static Pattern taskPattern = Pattern.compile("^task (.*)$");

    public TasksRunner() {
        addTask(new ClearDatabaseTask());
        addTask(new LearnContextTask());
        addTask(new ProcessChangelistsTask());
        addTask(new ExtractKeywordsTask());
        addTask(new ExtractGlobalKeywordsTask());
        addTask(new ExtractOwnershipTask());
        addTask(new ServerTask());
    }

    private void addTask(Task task) {
        tasks.put(task.getName(), task);
    }

    public void execute(TeamAssignment teamAssignment, Jedis jedis, String commands) throws Exception {
        List<TaskWithParams> sequence = new ArrayList<TaskWithParams>();
        Integer l = 0;

        for (String line : Utils.splitLines(commands)) {
            ++l;
            line = line.trim();
            if (line.isEmpty() || line.startsWith("#"))
                continue;

            Matcher matcher = taskPattern.matcher(line);
            if (matcher.matches())
            {
                Task task = tasks.get(matcher.group(1));
                if (task == null)
                    throw new Exception("Line " + l + ": Unknown task '" + matcher.group(1) + "'");
                sequence.add(new TaskWithParams(task));
            }
            else if (sequence.size() > 0) {
                sequence.get(sequence.size() - 1).params.add(line);
            }
            else
                throw new Exception("Line " + l + " is not command nor parameter");
        }

        for (TaskWithParams t : sequence) {
            t.task.validateParams(t.params);
        }

        for (TaskWithParams t : sequence) {
            Logger.log("Executing task " + t.task.getName() + "(" + t.params + ")");
            t.task.run(teamAssignment, jedis, t.params);
            Logger.log("Done");
        }
    }

    private static class TaskWithParams {
        public Task task;
        public List<String> params;

        public TaskWithParams(Task task) {
            this.task = task;
            params = new ArrayList<String>();
        }
    }

}
