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

package cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol;

import cz.vutbr.stud.fit.xsimon13.whoowns.diff.Diff;

import java.util.Map;
import java.util.TreeMap;

public class EditedChangedLines implements ChangedLines {
    TreeMap<Integer, Integer> lines = new TreeMap<Integer, Integer>();


    public static EditedChangedLines fromPatch(Diff patch) {
        EditedChangedLines cl = new EditedChangedLines();

        for (Diff.Chunk chunk : patch.getChunks()) {
            int position = chunk.newPosition;
            int size = chunk.newSize;
            if (size > 0)
                cl.add(position, position + size - 1);
        }

        return cl;
    }

    /**
     * Create EditedChangedLines from an array of starting and ending line numbers.
     * @param lines Pairs of line numbers [startLine0, endLine0, startLine1, endLine1, ...]
     */
    public static EditedChangedLines fromArray(int[] lines) {
        if (lines.length % 2 != 0)
            throw new RuntimeException("Number of entries in the array must be even");

        EditedChangedLines cl = new EditedChangedLines();
        for (int i = 0; i < lines.length - 1; i+=2)
            cl.add(lines[i], lines[i+1]);
        return cl;
    }

    public void add(int from, int to) {
        if (from > to) {
            int swap = from;
            from = to;
            to = swap;
        }

        lines.put(from, to);
    }

    public TreeMap<Integer, Integer> getChangedLines() {
        return lines;
    }

    @Override
    public boolean isChanged(int n) {
        Map.Entry<Integer, Integer> entry = lines.floorEntry(n);
        return entry != null && (entry.getKey() == n || entry.getValue() >= n);
    }

    @Override
    public boolean containsChangedLines(int from, int to) {
        Map.Entry<Integer, Integer> entry = lines.floorEntry(to);
        return entry != null && entry.getValue() >= from;

    }

    @Override
    public int countChangedLinesWithin(int from, int to) {
        Map.Entry<Integer, Integer> entry = lines.floorEntry(from);
        if (entry == null)
            entry = lines.firstEntry();

        int count = 0;
        for (; entry != null && entry.getKey() <= to; entry = lines.higherEntry(entry.getKey())) {
            int entryFrom = entry.getKey();
            int entryTo = entry.getValue();
            if (entryTo < from)
                continue;
            if (entryFrom < from)
                entryFrom = from;
            if (entryTo > to)
                entryTo = to;

            count += entryTo - entryFrom + 1;
        }

        return count;
    }

    @Override
    public int getCount() {
        return lines.size();
    }
}
