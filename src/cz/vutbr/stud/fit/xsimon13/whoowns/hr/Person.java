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

import cz.vutbr.stud.fit.xsimon13.whoowns.Entity;

import java.util.Date;

/**
 * A person.
 */
public class Person implements Entity {

    private String login;

    public Person(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }


    @Override
    public String toEntityId() {
        return PERSON_PREFIX + login;
    }

    @Override
    public String getType() {
        return PERSON_PREFIX;
    }

    @Override
    public String getData() {
        return login;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;

        if (!login.equals(person.login)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }
}
