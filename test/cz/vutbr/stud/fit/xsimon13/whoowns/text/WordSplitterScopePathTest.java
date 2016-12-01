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

import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class WordSplitterScopePathTest {

    @Parameterized.Parameters
    public static Collection<Object[]> scopePathParameters() {
        return Arrays.asList(new Object[][] {
                { "test", new String[] { "test" } },
                { "", new String[] { } },
                { "  ", new String[] { } },
                { ".", new String[] { } },
                { ". . .", new String[] { } },
                { "test", new String[] { "test" } },
                { "TEST", new String[] { "test" } },
                { "testTest", new String[] { "test", "test", } },
                { "ASTLoader", new String[] { "ast", "loader" } },
                { "AstLoader", new String[] { "ast", "loader" } },
                { "test.Test", new String[] { "test", "test" } },
                { "test.Test", new String[] { "test", "test" } },
                { "a.b.c", new String[] { "a", "b", "c" } },
                { "ISee.TreesOf.green.Red,rosesToo.", new String[] { "i", "see", "trees", "of", "green", "red", "roses", "too" } }
        });
    }

    private final String name;
    private final String[] expectedResult;

    public WordSplitterScopePathTest(String name, String[] expectedResult) {
        this.name = name;
        this.expectedResult = expectedResult;
    }

    @Test
    public void testScopePath() {
        Assert.assertEquals(
                WordSplitter.split(new ScopePath(name)).toArray(),
                expectedResult
        );
    }
}
