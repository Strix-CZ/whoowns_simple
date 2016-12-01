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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Splits a Java identifier into components. CamelCaseWords get split into its parts and
 * appended afterwards as a whole as well.
 *
 * "IdentifierSplitter test"-> ["identifier", "splitter", "identifiersplitter", "test"]
 */
public class WordSplitter {

    private static Pattern separator = Pattern.compile("[-_\\.,;\\\\/ \n\r\t\\*\\+]+");
    private static Pattern word = Pattern.compile("[0-9]+|([A-Z]?([A-Z]+|[a-z]+))");
    private static Pattern upperCase = Pattern.compile("^[A-Z]+$");
    private static Pattern lowerCase = Pattern.compile("^[a-z]+$");

    private static Pattern stopWordPattern = Pattern.compile("[,!?\\.;:]|((?<![a-zA-Z])(and|or|both)(?![a-zA-Z]))");

    public static List<String> split(ScopePath name) {
        return split(name.toString());
    }

    public static List<Word> splitWithStopWords(String text) {
        List<Word> words = new ArrayList<Word>();

        for (String fragment : stopWordPattern.split(text)) {

            List<String> fragmentWords = split(fragment);
            for (String word : fragmentWords)
                words.add(new Word(word, fragmentWords.size()));
        }

        return words;
    }

    public static List<String> split(String text) {
        List<String> result = new ArrayList<String>();
        //int previousEnd = -1;
        //boolean isCompoundWord = false;
        //String compoundWord = "";

        for (String part : separator.split(text)) {
            Matcher matcher = word.matcher(part);
            while (matcher.find()) {
                String group = matcher.group();

                /*if (matcher.start() == previousEnd) {
                    compoundWord += group;
                    isCompoundWord = true;
                }
                else {
                    if (isCompoundWord)
                        result.add(compoundWord.toLowerCase());
                    compoundWord = group;
                    isCompoundWord = false;
                }
                previousEnd = matcher.end();*/

                if (upperCase.matcher(group).matches()) {
                    // ASTLoader will be matched as ASTL, we want to push the L to be together as Loader
                    int afterGroup = matcher.end();
                    if (afterGroup < part.length() && lowerCase.matcher(part.substring(afterGroup, afterGroup + 1)).matches()) {
                        group = group.substring(0, group.length() - 1);
                        matcher.region(afterGroup - 1, part.length());
                        //previousEnd--;
                        //compoundWord = compoundWord.substring(0, compoundWord.length() - 1);
                    }
                }

                result.add(group.toLowerCase());
            }
        }

        //if (isCompoundWord)
        //    result.add(compoundWord.toLowerCase());

        return result;
    }

    public static class Word {
        public String text;
        public int fragmentLength;

        public Word(String text, int fragmentLength) {
            this.text = text;
            this.fragmentLength = fragmentLength;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Word word1 = (Word) o;
            return fragmentLength == word1.fragmentLength && text.equals(word1.text);
        }

        @Override
        public int hashCode() {
            int result = word.hashCode();
            result = 31 * result + fragmentLength;
            return result;
        }

        @Override
        public String toString() {
            return text + "(" + Integer.toString(fragmentLength) + ")";
        }
    }
}