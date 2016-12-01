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

package cz.vutbr.stud.fit.xsimon13.whoowns;

import cz.vutbr.stud.fit.xsimon13.whoowns.hr.TeamAssignment;
import cz.vutbr.stud.fit.xsimon13.whoowns.tasks.Logger;
import cz.vutbr.stud.fit.xsimon13.whoowns.tasks.TasksRunner;
import redis.clients.jedis.Jedis;

import java.nio.file.Paths;

/**
 * Main entry point to the app.
 */
public class Main {

    public static void main(String[] args) throws Exception {

        if (args.length != 4)
        {
            System.err.println("Wrong number of arguments.");
            System.err.println("  First argument is filename of task file");
            System.err.println("  Second argument is filename of team assignment");
            System.err.println("  Third argument is address of Redis server. with port (localhost)");
            System.err.println("  Fourth argument is Redis port (6379)");
            return;
        }

        String tasksFile = args[0];
        String teamFile = args[1];
        String redisAddress = args[2];
        int redisPort = Integer.parseUnsignedInt(args[3]);

        Jedis jedis = new Jedis(redisAddress, redisPort);

        try {
            String teamDef = Utils.readFromFile(Paths.get(teamFile));
            TeamAssignment teamAssignment = TeamAssignment.parse(jedis, teamDef);

            String tasks = Utils.readFromFile(Paths.get(tasksFile));
            TasksRunner tasksRunner = new TasksRunner();
            tasksRunner.execute(teamAssignment, jedis, tasks);
        }
        catch(Exception e) {
            System.err.println("Unexpected exception");
            e.printStackTrace(System.err);
        }
        finally {
            Logger.log("Shutting down");
            jedis.save();
        }
    }
}
