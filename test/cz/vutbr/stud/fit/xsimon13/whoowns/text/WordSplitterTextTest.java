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

package cz.vutbr.stud.fit.xsimon13.whoowns.text;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class WordSplitterTextTest {
    @Parameterized.Parameters
    public static Collection<Object[]> textParameters() {
        return Arrays.asList(new Object[][] {
                { "", new String[] { } },
                { "/** I see trees of green */", new String[] { "i", "see", "trees", "of", "green" } },
                { "// Red roses", new String[] { "red", "roses" } },
                { "/** */", new String[] { } },
                { "09", new String[] { "09" } },
                { "JaromirJagr68", new String[] { "jaromir", "jagr", "68" } },
                { "red_roses", new String[] { "red", "roses" } },
                { "\t'I see trees of green, red roses too\n\r" +
                        "\tI see them bloom for me and you;'\n" +
                        "\t\t~ Louis Armstrong ~",
                        new String[] { "i", "see", "trees", "of", "green", "red", "roses", "too",
                                "i", "see", "them", "bloom", "for", "me", "and", "you",
                                "louis", "armstrong"
                        } },
                { ",./;'[]\\123456789@#$%^&*()", new String[] { "123456789" } },
                { "the GN&SR and B&PCR routes", new String[] { "the", "gn", "sr", "and", "b", "pcr", "routes" }},
                { "A Ax Axx BB BBx BBxx CCC CCCx CCCxx", new String[] { "a", "ax", "axx", "bb", "b", "bx", "b", "bxx", "ccc", "cc", "cx", "cc", "cxx" }}
        });
    }

    private final String text;
    private final String[] expectedResult;

    public WordSplitterTextTest(String text, String[] expectedResult) {
        this.text = text;
        this.expectedResult = expectedResult;
    }

    @Test
    public void testText() {
        Assert.assertEquals(
                WordSplitter.split(text).toArray(),
                expectedResult
        );
    }
}
