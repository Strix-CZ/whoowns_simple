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

import cz.vutbr.stud.fit.xsimon13.whoowns.Factory;
import cz.vutbr.stud.fit.xsimon13.whoowns.TestUtils;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Person;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Team;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.TeamAssignment;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.ChangedLines;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.EditedChangedLines;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

public class TextAnalyzerTest
{
    private TextAnalyzer analyzer;
    private WordStatistics wordStats;
    private Person author;
    private Team team;
    private Date time;

    @Before
    public void setUp() throws Exception {
        ParsedClassProvider parsedClassProvider = Factory.createParsedClassProvider(TestUtils.getTestProjectRoot());
        author = new Person("honza");
        team = new Team("team");
        TeamAssignment teamAssignment = new TeamAssignment(TestUtils.getTestJedis());
        teamAssignment.addAssignment("team", "honza", null, null);
        time = new Date();

        wordStats = new WordStatistics(TestUtils.getTestJedis());
        wordStats.clear();

        analyzer = new TextAnalyzer(wordStats, teamAssignment);
        analyzer.setChangelistInformation(time, author, "CL description", parsedClassProvider);
    }

    @After
    public void tearDown() throws Exception {
        wordStats.clear();
    }


    @Test
    public void testLineComment() throws Exception {
        analyze(new int[]{42, 42});

        Assert.assertEquals(wordStats.getCategorizedDocumentCount(), 2);
        Assert.assertEquals(wordStats.getDocumentCount(), 2);
        Assert.assertTrue(wordStats.getIdfFw("aldwych") != 0);
        Assert.assertTrue(wordStats.getIdfFw("cl") != 0);
        Assert.assertTrue(wordStats.getIdfFw("description") != 0);
    }

    @Test
    public void testJavaDocComment() throws Exception {
        analyze(new int[]{31, 35});

        Assert.assertEquals(wordStats.getCategorizedDocumentCount(), 2);
        Assert.assertEquals(wordStats.getDocumentCount(), 2);
        Assert.assertTrue(wordStats.getIdfFw("license") != 0);
        Assert.assertTrue(wordStats.getIdfFw("parties") != 0);
        Assert.assertTrue(wordStats.getIdfFw("material") != 0);
    }

    @Test
    public void testPartialBlockComment() throws Exception {
        analyze(new int[]{53, 54});

        Assert.assertEquals(wordStats.getCategorizedDocumentCount(), 2);
        Assert.assertEquals(wordStats.getDocumentCount(), 2);
        Assert.assertTrue(wordStats.getIdfFw("october") == 0);
        Assert.assertTrue(wordStats.getIdfFw("station") != 0);
        Assert.assertTrue(wordStats.getIdfFw("branch") != 0);
        Assert.assertTrue(wordStats.getIdfFw("1933") == 0);
    }

    @Test
    public void testClassName() throws Exception {
        analyze(new int[]{22, 22});

        Assert.assertEquals(wordStats.getCategorizedDocumentCount(), 2);
        Assert.assertEquals(wordStats.getDocumentCount(), 2);
        Assert.assertTrue(wordStats.getIdfFw("text") != 0);
        Assert.assertTrue(wordStats.getIdfFw("author") == 0);
    }

    @Test
    public void testVariableName() throws Exception {
        analyze(new int[]{39, 39});

        Assert.assertEquals(wordStats.getCategorizedDocumentCount(), 3);
        Assert.assertEquals(wordStats.getDocumentCount(), 3);
        Assert.assertTrue(wordStats.getIdfFw("unique") != 0);
    }

    private void analyze(int[] changedLines) throws Exception {
        ScopePath filePath = new ScopePath("text.Text");
        ChangedLines cl = EditedChangedLines.fromArray(changedLines);
        analyzer.analyze(filePath, cl);
    }


}
