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

import org.junit.Assert;
import org.junit.Test;

public class EditedChangedLinesTest {

    @Test
    public void testIsChanged() throws Exception {
        EditedChangedLines lines = EditedChangedLines.fromArray(new int[]{1,5, 10,15});

        for (int line : new int[] { 1, 3, 5, 10, 11, 14, 15 })
            Assert.assertTrue("Line " + Integer.toString(line) + " should be contained", lines.isChanged(line));

        for (int line : new int[] { -199, 0, 6, 7, 9, 16, 1000 })
            Assert.assertFalse("Line " + Integer.toString(line) + " should not be contained", lines.isChanged(line));
    }

    @Test
    public void testContains() throws Exception {
        EditedChangedLines lines = new EditedChangedLines();
        lines.add(1, 5);
        lines.add(10, 15);

        int[] contains = new int[] { 1,1, 2,4, 3,3, 5,5, 1,5, 0,6, 0,1, 5,6, 0,6, -100,6, 3,13, -100,100 };
        for (int i = 0; i < contains.length - 1; i+=2)
            Assert.assertTrue("Range " + Integer.toString(contains[i]) + " - " + Integer.toString(contains[i+1]) + " should be contained", lines.containsChangedLines(contains[i], contains[i+1]));

        int[] doesNotContain = new int[] { -100,0, 6,6, 6,9, 16,16, 16,20 };
        for (int i = 0; i < doesNotContain.length - 1; i+=2)
            Assert.assertFalse("Range " + Integer.toString(doesNotContain[i]) + " - " + Integer.toString(doesNotContain[i+1]) + " should not be contained", lines.containsChangedLines(doesNotContain[i], doesNotContain[i+1]));
    }

    @Test
    public void testCount() throws Exception {
        EditedChangedLines lines = new EditedChangedLines();
        lines.add(1, 5);
        lines.add(10, 15);

        int[] counts = new int[] {
                -1, 0, 0,
                0, 0, 0,
                0, 1, 1,
                1, 1, 1,
                0, 3, 3,
                1, 3, 3,
                0, 5, 5,
                1, 5, 5,
                1, 6, 5,
                0, 6, 5,
                6, 9, 0,
                9, 10, 1,
                10, 10, 1,
                9, 13, 4,
                10, 13, 4,
                10, 15, 6,
                9, 15, 6,
                9, 16, 6,
                16, 16, 0,
                1, 10, 6,
                5, 10, 2,
                4, 11, 4,
                1, 15, 11,
                0, 16, 11,
                -100, 100, 11,
                50, 60, 0
        };
        for (int i = 0; i < counts.length - 2; i+=3) {
            Integer from = counts[i];
            Integer to = counts[i+1];
            Integer expectation = counts[i+2];
            Integer result = lines.countChangedLinesWithin(from, to);
            Assert.assertEquals("Changed lines count in " + from.toString() + " - " + to.toString() + " should be " + expectation.toString() + ", got " + result.toString() + " instead", result, expectation);
        }

    }

}
