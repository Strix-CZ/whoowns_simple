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

package cz.vutbr.stud.fit.xsimon13.whoowns.diff;

import cz.vutbr.stud.fit.xsimon13.whoowns.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Capable of parsing a diff in unified format. Extract just the interesting information
 * - what lines were added or modified.
 */
public class Diff
{
    public static class Chunk {
        public int oldPosition;
        public int oldSize;
        public int newPosition;
        public int newSize;
    }

    private List<Chunk> chunks = new ArrayList<Chunk>();

    public List<Chunk> getChunks() {
        return chunks;
    }

    private static final Pattern chunkPattern = Pattern.compile("^@@ -([0-9]+)((?:,[0-9]+)?) \\+([0-9]+)((?:,[0-9]+)?) @@.*$");

    /**
     * Parse the unified diff and return instance Diff containing changed or new lines.
     */
    public static Diff parse(String str) {
        Diff diff = new Diff();

        for (String line : Utils.splitLines(str)) {
            Matcher m = chunkPattern.matcher(line);
            if (m.matches()) {
                Chunk chunk = new Chunk();

                chunk.oldPosition = Integer.parseInt(m.group(1));
                if (m.group(2).length() > 1)
                    chunk.oldSize = Integer.parseInt(m.group(2).substring(1));
                else
                    chunk.oldSize = 1;

                chunk.newPosition = Integer.parseInt(m.group(3));
                if (m.group(4).length() > 1)
                    chunk.newSize = Integer.parseInt(m.group(4).substring(1));
                else
                    chunk.newSize = 1;

                diff.chunks.add(chunk);
            }
        }

        return diff;
    }

}
