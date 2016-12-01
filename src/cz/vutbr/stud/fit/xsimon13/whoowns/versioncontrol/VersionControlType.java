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

import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.git.GitChangelistProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.git.GitRunner;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.p4.P4ChangelistProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.p4.P4Runner;
import redis.clients.jedis.Jedis;

public enum VersionControlType {
    P4("p4"),
    GIT("git");

    private String name;
    VersionControlType(String name) {
        this.name = name;
    }

    public static VersionControlType fromName(String name) {
        for (VersionControlType type : VersionControlType.values()) {
            if (name.equalsIgnoreCase(type.name))
                return type;
        }

        throw new RuntimeException("Unknown version control type " + name);
    }

    public ChangelistProvider createChangelistProvider(Jedis jedis) {
        if (this == P4)
            return new P4ChangelistProvider(new P4Runner(), jedis);
        else
            return new GitChangelistProvider(new GitRunner(), jedis);
    }

}
