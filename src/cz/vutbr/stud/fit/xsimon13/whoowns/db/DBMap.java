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
import redis.clients.jedis.Pipeline;

import java.io.IOException;
import java.util.*;

public class DBMap<K, V> implements Map<K, V> {
    private final Jedis jedis;
    private final String name;
    private final DBConverter<K> keyConverter;
    private final DBConverter<V> valueConverter;

    public DBMap(Jedis jedis, String name, DBConverter<K> keyConverter, DBConverter<V> valueConverter) {
        this.jedis = jedis;
        this.name = name;
        this.keyConverter = keyConverter;
        this.valueConverter = valueConverter;
    }

    @Override
    public int size() {
        return jedis.hlen(name).intValue();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return jedis.hexists(name, key2db((K) key));
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException("This operation is not supported as it would be too expensive");
    }

    @Override
    public V get(Object key) {
        String stringValue = jedis.hget(name, key2db((K)key));
        return db2value(stringValue);
    }

    @Override
    public V put(K key, V value) {
        jedis.hset(name, key2db(key), value2db(value));
        return value;
    }

    @Override
    public V remove(Object key) {
        V value = get(key);
        jedis.hdel(name, key2db((K)key));
        return value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        try (Pipeline pipeline = jedis.pipelined()) {
            for (Map.Entry<?, ?> entry : m.entrySet()) {
                pipeline.hset(
                        name,
                        key2db((K) entry.getKey()),
                        valueConverter.toDbString((V)entry.getValue()));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clear() {
        jedis.del(name);
    }

    @Override
    public Set<K> keySet() {
        return DBUtility.collectionFromString(jedis.hkeys(name), new HashSet<>(), keyConverter);
    }

    @Override
    public Collection<V> values() {
        return DBUtility.collectionFromString(jedis.hvals(name), new ArrayList<>(), valueConverter);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
       return loadAll().entrySet();
    }

    public Map<K, V> loadAll() {
        Map<K, V> map = new HashMap<>();

        for (Entry<String, String> entry : jedis.hgetAll(name).entrySet())
            map.put(db2key(entry.getKey()), db2value(entry.getValue()));

        return map;
    }


    private String key2db(K key) {
        return keyConverter.toDbString(key);
    }

    private K db2key(String dbString) {
        return keyConverter.fromDbString(dbString);
    }

    private String value2db(V value) {
        return valueConverter.toDbString(value);
    }

    private V db2value(String dbString) {
        return valueConverter.fromDbString(dbString);
    }
}
