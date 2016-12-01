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

package cz.vutbr.stud.fit.xsimon13.whoowns.java;

import cz.vutbr.stud.fit.xsimon13.whoowns.Factory;
import cz.vutbr.stud.fit.xsimon13.whoowns.TestUtils;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Person;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Team;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.TeamAssignment;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.ChangedLines;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.CreatedFileChangedLines;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.EditedChangedLines;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static cz.vutbr.stud.fit.xsimon13.whoowns.java.OwnershipStatistics.*;

public class JavaAnalyzerTest {

    public static final double DELTA = 0.00001;
    private JavaAnalyzer analyzer;
    private OwnershipStatistics personStats;
    private OwnershipStatistics teamStats;
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

        personStats = new OwnershipStatistics(TestUtils.getTestJedis(), "Person");
        personStats.clear();
        teamStats = new OwnershipStatistics(TestUtils.getTestJedis(), "Team");
        teamStats.clear();

        analyzer = new JavaAnalyzer(personStats, teamStats, teamAssignment);
        analyzer.setChangelistInformation(time, author, "CL description", parsedClassProvider);
    }

    @After
    public void tearDown() throws Exception {
        personStats.clear();
        teamStats.clear();
    }

    @Test
    public void testClassCreated() throws Exception {
        ScopePath filePath = new ScopePath("pkgA.pkgB.pkgC.classEmpty");
        ChangedLines cl = EditedChangedLines.fromArray(new int[]{ 26, 28 });
        analyzer.analyze(filePath, cl);

        double tc = personStats.getTimeCoefficient(time);
        Assert.assertTrue(tc > 0.9995 && tc < 1.0005);
        Assert.assertEquals(personStats.getMonths(), 1);

        makeItemAssertions(filePath, 3 + CLASS_DEFINITION_COEF, true);
    }

    @Test
    public void testMethodCreated() throws Exception {
        ScopePath filePath = new ScopePath("typeResolver.Inheritance");
        ChangedLines cl = EditedChangedLines.fromArray(new int[]{ 35, 39 });
        analyzer.analyze(filePath, cl);

        makeItemAssertions(new ScopePath("typeResolver.Child.method.[0]"), 5 + VARIABLE_DEFINITION_COEF, false);
        makeItemAssertions(new ScopePath("typeResolver.Child.method"), 5 + METHOD_DEFINITION_COEF + VARIABLE_DEFINITION_COEF, true);
    }

    @Test
    public void testFieldCreated() throws Exception {
        ScopePath filePath = new ScopePath("typeResolver.Inheritance");
        ChangedLines cl = EditedChangedLines.fromArray(new int[]{ 27, 27 });
        analyzer.analyze(filePath, cl);

        makeItemAssertions(filePath, 1 + FIELD_DEFINITION_COEF, true);
    }

    @Test
    public void testVariableCreated() throws Exception {
        ScopePath filePath = new ScopePath("typeResolver.Simple");
        ChangedLines cl = EditedChangedLines.fromArray(new int[]{ 32, 35 });
        analyzer.analyze(filePath, cl);

        makeItemAssertions(ScopePath.append(filePath, "methodC.[0].[0]"), 3, false);
        makeItemAssertions(ScopePath.append(filePath, "methodC.[0]"), 4 + VARIABLE_DEFINITION_COEF, true);
    }

    @Test
    public void testJustLines() throws Exception {
        ScopePath filePath = new ScopePath("typeResolver.Inheritance");
        ChangedLines cl = EditedChangedLines.fromArray(new int[]{ 36, 37 });
        analyzer.analyze(filePath, cl);

        makeItemAssertions(new ScopePath("typeResolver.Child.method.[0]"), 2, true);
    }

    @Test
    public void testNewFile() throws Exception {
        ScopePath filePath = new ScopePath("typeResolver.Inheritance");
        ChangedLines cl = new CreatedFileChangedLines();
        analyzer.analyze(filePath, cl);

        // Class Inheritance
        double superMethod = 3 + METHOD_DEFINITION_COEF;
        double superField = 1 + FIELD_DEFINITION_COEF;
        double superType = 3 + CLASS_DEFINITION_COEF;
        double inheritance = 4 + CLASS_DEFINITION_COEF + superMethod + superField + superType;
        makeItemAssertions(new ScopePath("typeResolver.Inheritance.superMethod"), superMethod, false);
        makeItemAssertions(new ScopePath("typeResolver.Inheritance.superType"), superType, false);
        makeItemAssertions(new ScopePath("typeResolver.Inheritance"), inheritance, false);

        // Class Child
        double methodBody = 5 + VARIABLE_DEFINITION_COEF;
        double method = METHOD_DEFINITION_COEF + methodBody;
        double child = 2 + CLASS_DEFINITION_COEF + method;
        makeItemAssertions(new ScopePath("typeResolver.Child.method.[0]"), methodBody, false);
        makeItemAssertions(new ScopePath("typeResolver.Child.method"), method, false);
        makeItemAssertions(new ScopePath("typeResolver.Child"), child, false);

        // Class Child2
        double superMethod2 = 3 + METHOD_DEFINITION_COEF;
        double method2 = 5 + METHOD_DEFINITION_COEF;
        double child2 = 3 + CLASS_DEFINITION_COEF + superMethod2 + method2;
        makeItemAssertions(new ScopePath("typeResolver.Child2.superMethod"), superMethod2, false);
        makeItemAssertions(new ScopePath("typeResolver.Child2.method"), method2, false);
        makeItemAssertions(new ScopePath("typeResolver.Child2"), child2, false);

        double file = 4 + inheritance + child + child2;
        makeItemAssertions(new ScopePath("typeResolver"), file, true);
    }

    @Test
    public void testCompound() throws Exception {
        ScopePath filePath = new ScopePath("typeResolver.Inheritance");
        ChangedLines cl = EditedChangedLines.fromArray(new int[]{ 37, 44 });
        analyzer.analyze(filePath, cl);

        // Class Child
        double methodBody = 3 + VARIABLE_DEFINITION_COEF;
        double method = methodBody;
        double child = 1 + method;
        makeItemAssertions(new ScopePath("typeResolver.Child.method.[0]"), methodBody, false);
        makeItemAssertions(new ScopePath("typeResolver.Child.method"), method, false);
        makeItemAssertions(new ScopePath("typeResolver.Child"), child, false);

        // Class Child2
        double superMethod2 = 2 + METHOD_DEFINITION_COEF;
        double child2 = 1 + CLASS_DEFINITION_COEF + superMethod2;
        makeItemAssertions(new ScopePath("typeResolver.Child2.superMethod"), superMethod2, false);
        makeItemAssertions(new ScopePath("typeResolver.Child2"), child2, false);

        double file = 1 + child + child2;
        makeItemAssertions(new ScopePath("typeResolver"), file, true);
    }

    private void makeItemAssertions(ScopePath path, double modificationVolume, boolean recurse) throws Exception {
        for (; !path.isEmpty(); path = path.getQualifier()) {
            OwnershipStatistics.Item item = personStats.getItem(path, author);
            Assert.assertTrue("Returned empty item for " + path, item != null);
            Assert.assertEquals(item.getMonthsTouched(), 1);
            Assert.assertEquals("Author - Weight differs from modification volume", item.getWeight(personStats.getMonths()), item.getModificationVolume(), DELTA);
            Assert.assertEquals("Author - Wrong modificationVolume", item.getModificationVolume(), modificationVolume * personStats.getTimeCoefficient(time), DELTA);

            item = teamStats.getItem(path, team);
            Assert.assertTrue("Returned empty item for " + path, item != null);
            Assert.assertEquals(item.getMonthsTouched(), 1);
            Assert.assertEquals("Team - Weight differs from modification volume", item.getWeight(personStats.getMonths()), item.getModificationVolume(), DELTA);
            Assert.assertEquals("Team - Wrong modificationVolume", item.getModificationVolume(), modificationVolume * personStats.getTimeCoefficient(time), DELTA);

            if (!recurse)
                return;
        }
    }
}
