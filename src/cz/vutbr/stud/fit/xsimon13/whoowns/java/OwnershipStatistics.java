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

import cz.vutbr.stud.fit.xsimon13.whoowns.Entity;
import cz.vutbr.stud.fit.xsimon13.whoowns.db.*;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * Holds statistics of ownership.
 */
public class OwnershipStatistics {

    public static final double OWNER_WEIGHT_THRESHOLD = 0.5d;

    public static final double CLASS_DEFINITION_COEF = 500;
    public static final double FIELD_DEFINITION_COEF = 50;
    public static final double METHOD_DEFINITION_COEF = 20;
    public static final double VARIABLE_DEFINITION_COEF = 2;

    private static final long MILLISECONDS_IN_MONTH = 1000L * 3600L * 24L * 30L;
    private static final long MILLISECONDS_IN_YEAR = 1000L * 3600L * 24L * 365L;

    private static final String KEY_PATH_STATS = "OS-PathStats";
    private DBMap<ScopePath, Map<String, Item>> pathStats;

    private static final String KEY_START_TIME = "OS-StartTime";
    private DBValue<Date> startTimeBucket;
    private Date startTime;

    private static final String KEY_END_TIME = "OS-EndTime";
    private DBValue<Date> endTimeBucket;
    private Date endTime;

    private static final String KEY_EPOCH = "OS-Epoch";
    private DBValue<Date> epochBucket;
    private Date epoch;

    public OwnershipStatistics(Jedis jedis, String jedisName) {
        pathStats = new DBMap<>(jedis, KEY_PATH_STATS + "-" + jedisName, new ScopePath.Converter(), new PathStatConverter());

        startTimeBucket = new DBValue<>(jedis, KEY_START_TIME + "-" + jedisName, new DateConverter());
        startTime = startTimeBucket.get(); // assumes no concurrent access
        endTimeBucket = new DBValue<>(jedis, KEY_END_TIME + "-" + jedisName, new DateConverter());
        endTime = endTimeBucket.get(); // assumes no concurrent access
        epochBucket = new DBValue<>(jedis, KEY_EPOCH + "-" + jedisName, new DateConverter());
        epoch = epochBucket.get(); // assumes no concurrent access
    }

    public void clear() {
        pathStats.clear();

        startTimeBucket.delete();
        startTime = null;
        endTimeBucket.delete();
        endTime = null;
        epochBucket.delete();
        epoch = null;
    }

    /**
     * Fluently record what is modified in the `scopePath`.
     */
    public OngoingAnalysis recordModification(ScopePath scopePath, Entity author, Date time) {
        if (startTime == null || startTime.compareTo(time) > 0) {
            startTime = time;
            startTimeBucket.set(startTime);
        }

        if (endTime == null || endTime.compareTo(time) < 0) {
            endTime = time;
            endTimeBucket.set(endTime);
        }

        return new OngoingAnalysis(scopePath, author, time);
    }

    public Item getItem(ScopePath scopePath, Entity entity) {
        Map<String, Item> row = pathStats.get(scopePath);
        if (row == null)
            return null;
        else
            return row.get(entity.toEntityId());
    }

    public static class Owner implements Comparable<Owner> {
        public String entityId;
        public double weight;
        public double score;

        public Owner(String entityId, double weight, double score) {
            this.entityId = entityId;
            this.weight = weight;
            this.score = score;
        }

        @Override
        public int compareTo(Owner o) {
            if (weight > o.weight)
                return -1;
            else if (weight < o.weight)
                return 1;
            else
                return entityId.compareTo(o.entityId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Owner owner = (Owner) o;

            if (!entityId.equals(owner.entityId)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return entityId.hashCode();
        }

        @Override
        public String toString() {
            return entityId + " (" + weight + ")";
        }
    }

    public static class OwnerWithPath extends Owner {
        public ScopePath path;

        public OwnerWithPath(String entityId, double weight, double score, ScopePath path) {
            super(entityId, weight, score);
            this.path = path;
        }
    }

    public List<Owner> getAllOwners(ScopePath scopePath) {
        return getOwners(scopePath, true);
    }

    /**
     * Returns list of owners for the scope path.
     */
    public List<Owner> getOwners(ScopePath scopePath) {
        return getOwners(scopePath, false);
    }

    /**
     *  Returns list of owners for the scope path. If allOwners is true
     *  the list will not be truncated.
     */
    private List<Owner> getOwners(ScopePath scopePath, boolean allOwners) {
        SortedSet<Owner> owners = new TreeSet<Owner>();

        Map<String, Item> row = pathStats.get(scopePath);
        if (row == null)
            return new ArrayList<Owner>();

        int projectMonths = getMonths();

        double weightSum = 0;

        for (String entityId : row.keySet()) {
            double weight = row.get(entityId).getWeight(projectMonths);
            if (weight > 0.0d) {
                weightSum += weight;
                owners.add(new Owner(entityId, weight, weight));
            }
        }

        if (!allOwners && owners.size() > 1) {
            // Remove all owners below threshold
            double threshold = owners.first().weight * OWNER_WEIGHT_THRESHOLD;
            Iterator<Owner> toRemove = owners.tailSet(new Owner("", threshold, 0)).iterator();
            while (toRemove.hasNext()) {
                toRemove.next();
                toRemove.remove();
            }
        }

        List<Owner> result = new ArrayList<Owner>(owners.size());
        for (Owner owner : owners) {
            owner.weight /= weightSum;
            result.add(owner);
        }

        return result;
    }

    /**
     * Extract the only owner if he exists or null.
     */
    public Owner getTheOnlyOwner(ScopePath scopePath, double threshold) {
        SortedSet<Owner> owners = new TreeSet<Owner>();

        Map<String, Item> row = pathStats.get(scopePath);
        if (row == null)
            return null;

        int projectMonths = getMonths();

        double weightSum = 0;

        for (String entityId : row.keySet()) {
            double weight = row.get(entityId).getWeight(projectMonths);
            if (weight > 0.0d) {
                weightSum += weight;
                owners.add(new Owner(entityId, weight, weight));
                if (owners.size() > 2)
                    owners.remove(owners.last());
            }
        }

        if (owners.size() == 0)
            return null;
        if (owners.size() == 1)
            return owners.first();

        Iterator<Owner> it = owners.iterator();
        Owner first = it.next();
        Owner second = it.next();
        if (first.weight / second.weight < threshold)
            return null; // the owner is not distinctive enough
        else {
            first.weight /= weightSum;
            return first;
        }
    }

    public Set<ScopePath> getScopePaths() {
        return pathStats.keySet();
    }

    /**
     * Returns number of months between minimal and maximal seen time
     */
    public int getMonths() {
        if (startTime == null || endTime == null)
            return 0;
        return (int) (1 + (endTime.getTime() - startTime.getTime()) / MILLISECONDS_IN_MONTH);
    }

    public double getTimeCoefficient(Date time) {
        if (epoch == null) {
            // Initialize the epoch as the current time.
            // The epoch actually doesn't matter too much, it only needs to be more or less recent in order to keep
            // the time coefficient small.
            epoch = new Date();
            epochBucket.set(epoch);
        }

        return Math.pow(2d, (time.getTime() - epoch.getTime()) / MILLISECONDS_IN_YEAR);
    }

    /**
     * Fluent class to record modification.
     */
    public class OngoingAnalysis {

        private class ItemOnScopePath {
            private Item stat;
            private Map<String, Item> row;
        }

        private Map<ScopePath, ItemOnScopePath> enclosingScopePaths;
        private ScopePath mostDetailedScopePath;
        private double timeCoefficient;

        private OngoingAnalysis(ScopePath scopePath, Entity author, Date time) {
            mostDetailedScopePath = scopePath;
            timeCoefficient = getTimeCoefficient(time);
            int timeInMonths = (int) (time.getTime() / MILLISECONDS_IN_MONTH);
            enclosingScopePaths = new HashMap<ScopePath, ItemOnScopePath>();

            while (!scopePath.isEmpty()) {
                ItemOnScopePath s = new ItemOnScopePath();
                enclosingScopePaths.put(scopePath, s);

                s.row = pathStats.get(scopePath);
                if (s.row == null)
                    s.row = new HashMap<String, Item>();

                s.stat = s.row.get(author.toEntityId());
                if (s.stat == null) {
                    s.stat = new Item();
                    s.row.put(author.toEntityId(), s.stat);
                }

                s.stat.months.add(timeInMonths);

                scopePath = scopePath.getQualifier();
            }

        }

        public OngoingAnalysis changedLines(int touchedLines, boolean recurseEnclosing) {
            if (recurseEnclosing) {
                for (ScopePath sp : enclosingScopePaths.keySet()) {
                    ItemOnScopePath item = enclosingScopePaths.get(sp);
                    item.stat.modificationVolume += timeCoefficient * touchedLines;
                    item.stat.nTouched++; // lines are counted exactly once for every change
                }
            }
            else {
                ItemOnScopePath item = enclosingScopePaths.get(mostDetailedScopePath);
                item.stat.modificationVolume += timeCoefficient * touchedLines;
                item.stat.nTouched++;
            }
            return this;
        }

        public OngoingAnalysis definedClass() {
            for (ScopePath sp : enclosingScopePaths.keySet())
                enclosingScopePaths.get(sp).stat.modificationVolume += timeCoefficient * CLASS_DEFINITION_COEF;
            return this;
        }

        public OngoingAnalysis definedMethod() {
            for (ScopePath sp : enclosingScopePaths.keySet())
                enclosingScopePaths.get(sp).stat.modificationVolume += timeCoefficient * METHOD_DEFINITION_COEF;
            return this;
        }

        public OngoingAnalysis definedField() {
            for (ScopePath sp : enclosingScopePaths.keySet())
                enclosingScopePaths.get(sp).stat.modificationVolume += timeCoefficient * FIELD_DEFINITION_COEF;
            return this;
        }

        public OngoingAnalysis definedVariable() {
            for (ScopePath sp : enclosingScopePaths.keySet())
                enclosingScopePaths.get(sp).stat.modificationVolume += timeCoefficient * VARIABLE_DEFINITION_COEF;
            return this;
        }

        public void commit() {
            for (ScopePath sp : enclosingScopePaths.keySet())
                pathStats.put(sp, enclosingScopePaths.get(sp).row); // TODO: Asynchronous put
        }
    }

    public static class Item {
        private double modificationVolume = 0d;
        private int nTouched = 0;
        private Set<Integer> months = new HashSet<>();

        public int getMonthsTouched() {
            return months.size();
        }

        public double getModificationVolume() {
            return modificationVolume;
        }

        public double getWeight(int months) {
            return ((double)getMonthsTouched() / (double)months) * modificationVolume;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Item item = (Item) o;

            if (nTouched != item.nTouched) return false;
            if (Double.compare(item.modificationVolume, modificationVolume) != 0) return false;
            if (!months.equals(item.months)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            temp = Double.doubleToLongBits(modificationVolume);
            result = (int) (temp ^ (temp >>> 32));
            result = 31 * result + nTouched;
            result = 31 * result + months.hashCode();
            return result;
        }
    }

    public static class PathStatConverter implements DBConverter<Map<String, Item>> {

        public static final String MODIFICATION_VOLUME = "modificationVolume";
        public static final String N_TOUCHED = "nTouched";
        public static final String MONTHS = "months";

        @Override
        public String toDbString(Map<String, Item> item) {
            try {
                JSONObject object = new JSONObject();

                for (Map.Entry<String, Item> entry : item.entrySet())
                    object.put(entry.getKey(), item2db(entry.getValue()));

                return object.toString();
            }
            catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Map<String, Item> fromDbString(String dbString) {
            if (dbString == null)
                return new HashMap<>();

            try {
                JSONObject map = new JSONObject(dbString);

                Map<String, Item> itemMap = new HashMap<>();

                Iterator<String> keys = map.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    Item item = db2item(map.getJSONObject(key));
                    itemMap.put(key, item);
                }

                return itemMap;
            }
            catch(JSONException e) {
                throw new RuntimeException(e);
            }
        }

        private Item db2item(JSONObject object) throws JSONException {
            Item item = new Item();
            item.modificationVolume = object.getDouble(MODIFICATION_VOLUME);
            item.nTouched = object.getInt(N_TOUCHED);

            JSONArray months = object.getJSONArray(MONTHS);
            for (int i = 0; i < months.length(); ++i)
                item.months.add(months.getInt(i));

            return item;
        }

        private JSONObject item2db(Item item) throws JSONException {
            return new JSONObject()
                    .put(MODIFICATION_VOLUME, item.modificationVolume)
                    .put(N_TOUCHED, item.nTouched)
                    .put(MONTHS, item.months);
        }
    }
}
