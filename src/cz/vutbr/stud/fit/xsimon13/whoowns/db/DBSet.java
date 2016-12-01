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

package cz.vutbr.stud.fit.xsimon13.whoowns.db;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Collection;


public class DBSet<T> {
    private final Jedis jedis;
    private final String name;
    private final DBConverter<T> dbConverter;

    public DBSet(Jedis jedis, String name, DBConverter<T> dbConverter) {
        this.jedis = jedis;
        this.name = name;
        this.dbConverter = dbConverter;
    }

    public void add(T value) {
        jedis.sadd(name, dbConverter.toDbString(value));
    }

    public boolean contains(T value) {
        return jedis.sismember(name, dbConverter.toDbString(value));
    }

    public void clear() {
        jedis.del(name);
    }

    public Collection<T> getAll() {
        return DBUtility.collectionFromString(jedis.smembers(name), new ArrayList<>(), dbConverter);
    }

}
