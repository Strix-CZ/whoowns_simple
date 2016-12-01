package cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.git;

import com.github.javaparser.ParseException;
import cz.vutbr.stud.fit.xsimon13.whoowns.Analyzer;
import cz.vutbr.stud.fit.xsimon13.whoowns.Factory;
import cz.vutbr.stud.fit.xsimon13.whoowns.TestUtils;
import cz.vutbr.stud.fit.xsimon13.whoowns.Utils;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Person;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
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

import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.ChangedLines;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.ChangelistProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.EditedChangedLines;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.LineStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeMap;

public class GitChangelistProviderTest {
    @Test
    public void test() throws Exception {

        Path root = TestUtils.getTestProjectRoot();
        ParsedClassProvider parsedClassProvider = Factory.createParsedClassProvider(root);
        Date startDate = new GregorianCalendar(2015, 4, 1).getTime();
        Date endDate = new GregorianCalendar(2015, 4, 2).getTime();
        MyAnalyzer analyzer = new MyAnalyzer();

        GitChangelistProvider provider = new GitChangelistProvider(new MyGitRunner(), TestUtils.getTestJedis());
        provider.run(root, parsedClassProvider, "master", startDate, endDate, analyzer);

        analyzer.assertCalledProperly();
    }

    private static class MyGitRunner extends GitRunner {
        @Override
        public LineStream execute(Path repository, List<String> argumentList) throws ChangelistProvider.VersionControlException {
            String args = Utils.join(argumentList, " ");

            if (args.equals("checkout master"))
                return createLineStream("Switched to branch 'master'");
            else if (args.equals("log --since 2015-05-01 --until 2015-05-02 --no-merges --format=raw -p --unified=0"))
                return createLineStream(diff);
            else if (args.equals("checkout df1914cb21d696790875d11c79c521132f424304"))
                return createLineStream("HEAD is now at df1914c...");
            else if (args.equals("checkout c28bf3bb3f5c46ad7e20dcf0d6251db02a9e1697"))
                return createLineStream("HEAD is now at c28bf3b...");

            throw new RuntimeException("Command not expected '" + args + "'");
        }

        private LineStream createLineStream(String str) {
            InputStream inputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
            return new LineStream(inputStream);
        }
    }

    private static final String diff =
            "commit df1914cb21d696790875d11c79c521132f424304\n" +
            "tree e4049a09feecec8a716cd06f2917c7f1a336684d\n" +
            "parent c28bf3bb3f9c46ad7e20dcf1d6251db02a9e1697\n" +
            "author Jan Simonek <xsimon13@stud.fit.vutbr.cz> 1430508624 +0200\n" +
            "committer Jan Simonek <xsimon13@stud.fit.vutbr.cz> 1430508722 +0200\n" +
            "\n" +
            "    Description123\n" +
            "    still the same description\n" +
            "\n" +
            "diff --git blabtext/Text.java text/Text.java\n" +
            "index afded7d..92e0982 100644\n" +
            "--- blabtext/Text.java\n" +
            "+++ text/Text.java\n" +
            "@@ -237 +237 @@ SomeJunk sj = createSomeJunk();\n" +
            "-=== old trash\n" +
            "+=== replaced with new trash\n" +
            "\n" +
            "commit c28bf3bb3f5c46ad7e20dcf0d6251db02a9e1697\n" +
            "tree c28bf3bb3f5c46ad7e20dcf0d6251db02a9e1697\n" +
            "parent b2e022bd943f884f85b4de365c9595a6a656b1ed\n" +
            "author Jan Simonek <xsimon13@stud.fit.vutbr.cz> 1430505446 +0200\n" +
            "committer Jan Simonek <xsimon13@stud.fit.vutbr.cz> 1430505972 +0200\n" +
            "\n" +
            "    Description 2\n" +
            "\n" +
            "diff --git blah/classEmpty.java blah/classEmpty.java\n" +
            "index 5455440..e020581 100644\n" +
            "--- blah/classEmpty.java\n" +
            "+++ blah/classEmpty.java\n" +
            "@@ -53 +53 @@\n" +
            "-  \n" +
            "+\n" +
            "@@ -56 +56 @@\n" +
            "-  \n" +
            "+\n" +
            "diff --git \"non java file.txt\" \"non java file.txt\"\n" +
            "index 88135d8..48b2f2a 100644\n" +
            "--- non java file.txt\n" +
            "+++ non java file.txt\n" +
            "@@ -3 +3 @@\n" +
            "-a\n" +
            "+b\n" +
            "diff --git \"old with space.java\" \"scopeutils/SUTes.java\"\n" +
            "index 88135d8..48b2f2a 100644\n" +
            "--- old with space.java\n" +
            "+++ scopeutils/SUTest.java\n" +
            "@@ -10 +10,2 @@\n" +
            "-c\n" +
            "+d\n" +
            "+e\n";

    private static class MyAnalyzer implements Analyzer {
        private int calledSetChangelistInformation = 0;
        private int calledAnalyze = 0;
        private int calledAfter = 0;

        @Override
        public void setChangelistInformation(Date time, Person author, String description, ParsedClassProvider parsedClassProvider) {
            if (calledSetChangelistInformation == 0) {
                Assert.assertEquals(time, new Date(1430508624000L));
                Assert.assertEquals(author, new Person("xsimon13@stud.fit.vutbr.cz"));
                Assert.assertEquals(description, "Description123\nstill the same description");
                Assert.assertTrue(parsedClassProvider != null);
            }
            else {
                Assert.assertEquals(time, new Date(1430505446000L));
                Assert.assertEquals(author, new Person("xsimon13@stud.fit.vutbr.cz"));
                Assert.assertEquals(description, "Description 2");
                Assert.assertTrue(parsedClassProvider != null);
            }

            calledSetChangelistInformation++;
        }

        @Override
        public void analyze(ScopePath file, ChangedLines changedLines) throws IOException, ParseException {
            if (calledAnalyze == 0) {
                Assert.assertEquals(calledSetChangelistInformation, 1);
                Assert.assertEquals(file, new ScopePath("text.Text"));
                Assert.assertTrue(changedLines instanceof EditedChangedLines);

                TreeMap<Integer, Integer> cls = ((EditedChangedLines) changedLines).getChangedLines();
                TreeMap<Integer, Integer> expectedCls = new TreeMap<Integer, Integer>() {{
                    put(237, 237);
                }};
                Assert.assertEquals(cls, expectedCls);
            }
            else if (calledAnalyze == 2) {
                Assert.assertEquals(calledSetChangelistInformation, 2);
                Assert.assertEquals(file, new ScopePath("blah.classEmpty"));
                Assert.assertTrue(changedLines instanceof EditedChangedLines);

                TreeMap<Integer, Integer> cls = ((EditedChangedLines) changedLines).getChangedLines();
                TreeMap<Integer, Integer> expectedCls = new TreeMap<Integer, Integer>() {{
                    put(53, 53);
                    put(56, 56);
                }};
                Assert.assertEquals(cls, expectedCls);
            }
            else if (calledAnalyze == 1) {
                Assert.assertEquals(calledSetChangelistInformation, 2);
                Assert.assertEquals(file, new ScopePath("scopeutils.SUTest"));
                Assert.assertTrue(changedLines instanceof EditedChangedLines);

                TreeMap<Integer, Integer> cls = ((EditedChangedLines) changedLines).getChangedLines();
                TreeMap<Integer, Integer> expectedCls = new TreeMap<Integer, Integer>() {{
                    put(10, 11);
                }};
                Assert.assertEquals(cls, expectedCls);
            }

            calledAnalyze++;
        }

        @Override
        public void afterChangelist() {
            calledAfter++;
        }

        public void assertCalledProperly() {
            Assert.assertEquals(calledSetChangelistInformation, 2);
            Assert.assertEquals(calledAnalyze, 3);
            Assert.assertEquals(calledAfter, 2);
        }
    }
}
