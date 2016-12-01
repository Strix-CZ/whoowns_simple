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

import java.util.List;

public class WordSplitterFragmentTest {
    @Test
    public void fragmentTest() throws Exception {

        List<WordSplitter.Word> words = WordSplitter.splitWithStopWords("or ab,c ,.;? d e holbor and f or ff band both fbothf g and h , i ; j ? k ! l m and");

        WordSplitter.Word[] expectation = new WordSplitter.Word[] {
                new WordSplitter.Word("ab", 1),
                new WordSplitter.Word("c", 1),
                new WordSplitter.Word("d", 3),
                new WordSplitter.Word("e", 3),
                new WordSplitter.Word("holbor", 3),
                new WordSplitter.Word("f", 1),
                new WordSplitter.Word("ff", 2),
                new WordSplitter.Word("band", 2),
                new WordSplitter.Word("fbothf", 2),
                new WordSplitter.Word("g", 2),
                new WordSplitter.Word("h", 1),
                new WordSplitter.Word("i", 1),
                new WordSplitter.Word("j", 1),
                new WordSplitter.Word("k", 1),
                new WordSplitter.Word("l", 2),
                new WordSplitter.Word("m", 2),
        };

        Assert.assertEquals(words.toString(), words.toArray(), expectation);
    }
}
