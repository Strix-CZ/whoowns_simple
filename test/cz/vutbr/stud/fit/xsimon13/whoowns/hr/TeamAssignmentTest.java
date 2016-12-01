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

package cz.vutbr.stud.fit.xsimon13.whoowns.hr;

import cz.vutbr.stud.fit.xsimon13.whoowns.TestUtils;
import junit.framework.Assert;
import org.junit.Test;


import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

public class TeamAssignmentTest {
    private TeamAssignment a;

    @Test
    public void allTeamsTest() throws Exception {
        a = TeamAssignment.parse(TestUtils.getTestJedis(), ASSIGNEMNT);
        a.clearTeams();

        Set<Team> expect = new HashSet<Team>();

        Assert.assertEquals(expect, a.getTeams());

        testAssignment("Rychnak", "vypravy", "2000-01-01");
        expect.add(new Team("vypravy"));
        Assert.assertEquals(expect, a.getTeams());

        testAssignment("Rychnak", "vypravy", "2020-01-01");
        Assert.assertEquals(expect, a.getTeams());

        testAssignment("Bozka", "maslo",   "2010-01-01");
        expect.add(new Team("maslo"));
        Assert.assertEquals(expect, a.getTeams());

        testAssignment("Pavel", "Pavel",   "2009-12-31");
        expect.add(new Team("Pavel"));
        Assert.assertEquals(expect, a.getTeams());

        testAssignment("Zoro", "Zoro", "2020-01-01");
        expect.add(new Team("Zoro"));
        Assert.assertEquals(expect, a.getTeams());

        a = new TeamAssignment(TestUtils.getTestJedis());
        Assert.assertEquals(expect, a.getTeams());
    }

    @Test
    public void assignmentTest() throws Exception {
        a = TeamAssignment.parse(TestUtils.getTestJedis(), ASSIGNEMNT);

        testAssignment("Rychnak", "vypravy", "2000-01-01");
        testAssignment("Rychnak", "vypravy", "2010-01-01");
        testAssignment("Rychnak", "vypravy", "2011-01-01");
        testAssignment("Rychnak", "vypravy", "2014-01-01");
        testAssignment("Rychnak", "vypravy", "2020-01-01");

        testAssignment("Bozka", "vypravy", "2000-01-01");
        testAssignment("Bozka", "vypravy", "2009-12-31");
        testAssignment("Bozka", "maslo",   "2010-01-01");
        testAssignment("Bozka", "maslo",   "2010-01-02");
        testAssignment("Bozka", "maslo",   "2013-12-31");
        testAssignment("Bozka", "prace",   "2014-01-01");
        testAssignment("Bozka", "prace",   "2020-01-01");

        testAssignment("Simi", "vypravy", "2010-01-01");
        testAssignment("Simi", "vypravy", "2014-07-31");
        testAssignment("Simi", "prace",   "2014-08-01");
        testAssignment("Simi", "prace",   "2020-08-01");

        testAssignment("Pavel", "Pavel",   "2009-12-31");
        testAssignment("Pavel", "vypravy", "2010-01-01");
        testAssignment("Pavel", "vypravy", "2020-01-01");

        testAssignment("Jonas", "Jonas",   "2010-01-01");
        testAssignment("Jonas", "vypravy", "2010-06-01");
        testAssignment("Jonas", "vypravy", "2010-07-01");
        testAssignment("Jonas", "vypravy", "2010-09-01");
        testAssignment("Jonas", "Jonas",   "2010-09-02");

        testAssignment("Zoro", "Zoro", "2010-01-01");
    }

    @Test
    public void testDefaultTeam() throws Exception{
        a = TeamAssignment.parse(TestUtils.getTestJedis(),
                ASSIGNEMNT + "\n" +
                "default team defaultTeam\n"
        );

        testAssignment("Jonas", "defaultTeam", "2010-01-01");
        testAssignment("Jonas", "defaultTeam", "2010-09-02");
        testAssignment("Zoro", "defaultTeam", "2010-01-01");
    }

    @Test
    public void testIgnoreTeam() throws Exception{
        a = TeamAssignment.parse(TestUtils.getTestJedis(),
                ASSIGNEMNT + "\n" +
                "ignore changes of team maslo\n"
        );

        Assert.assertTrue(a.isTeamIgnored(new Team("maslo")));
    }

    private void testAssignment(String login, String team, String dateStr) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Team actualTeam = a.getTeam(new Person(login), dateFormat.parse(dateStr));
        Assert.assertEquals("Team is different than expected for " + login + " on " + dateStr, team, actualTeam.getId());
    }

    private static final String ASSIGNEMNT =
            "# comment\n" +
            " # comment \n" +
            " \n" +
            "\t#comment\r\n" +
            "\n" +
            "team vypravy\n" +
            "Simi to 2014-08-01\n" +
            "Bozka to 2010-01-01\n" +
            "Rychnak\n" +
            "Pavel 2010-01-01 to\n" +
            "Jonas 2010-06-01 to 2010-09-01\n" +
            "\n" +
            "team maslo\n" +
            "Bozka 2010-01-01 to 2014-01-01\n" +
            "\n" +
            "team prace\n" +
            "Simi 2014-08-01 to\n" +
            "Bozka 2014-01-01 to\n";

}
