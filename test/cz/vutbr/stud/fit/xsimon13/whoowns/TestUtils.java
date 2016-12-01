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

import org.junit.Assert;
import redis.clients.jedis.Jedis;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class TestUtils {
    /**
     * Name of environment variable holding the path to test project.
     */
    private static final String TEST_PROJECT_PATH_VARIABLE_NAME = "ProjectTestBench";

    /**
     * Locates the test project. It first tries to use the environment variable and then
     * different sub-folders of working directory and all parent directories.
     */
    public static Path getTestProjectRoot() {
        Path path;


        String environmentVariable = System.getenv(TEST_PROJECT_PATH_VARIABLE_NAME);
        if (!Utils.isEmpty(environmentVariable)) {
            path = Paths.get(".").resolve(environmentVariable);
            if (containsTestProject(path))
                return path;
        }

        String[] subFolders = new String[] {
                ".",
                "res/test/src",
                "test/src",
                "src",
                "test",
                "res/test",
                "parser",
                "parser/res/test/src",
                "parser/test/src",
                "parser/src",
                "parser/test",
                "parser/res/test",
        };

        List<String> triedPaths = new ArrayList<String>();
        path = Paths.get(".").toAbsolutePath();

        while (path != null && Files.isDirectory(path)) {
            for (String subFolder : subFolders) {
                Path tryPath = path.resolve(subFolder);
                if (containsTestProject(tryPath))
                    return tryPath;
                triedPaths.add(tryPath.toString());
            }
            path = path.getParent();
        }

        throw new RuntimeException(
                "Directory with test project could not be found.\n" +
                "You can set " + TEST_PROJECT_PATH_VARIABLE_NAME + " environment variable to set the location yourself.\n" +
                "I have tried these locations:\n" + triedPaths.toString()
        );
    }

    private static Jedis jedis = null;
    public static Jedis getTestJedis() {
        if (jedis == null) {
            jedis = new Jedis("192.168.253.106", 6382);
            jedis.flushDB();
        }

        return jedis;
    }

    /**
     * Decides if a path contains the test project.
     */
    private static boolean containsTestProject(Path path) {
        return Files.isDirectory(path) && Files.isDirectory(path.resolve("pkgA")) && Files.isDirectory(path.resolve("blah"));
    }

    public static void assertNoMessages() {
        MessageBoard board = MessageBoard.getInstance();
        try {
            Assert.assertEquals("Found some board messages:\n" + board.getLastMessages().toString(),
                    board.getLastMessages().size(), 0);
        }
        finally {
            // Clear the board for following tests
            board.clearLastMessages();
        }
    }
}
