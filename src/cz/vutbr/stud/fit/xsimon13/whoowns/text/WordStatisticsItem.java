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

import cz.vutbr.stud.fit.xsimon13.whoowns.db.DBConverter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

/**
 * A statistics for a word stored in DB.
 */
class WordStatisticsItem implements Serializable {
    private static final long MILLISECONDS_IN_MONTH = 1000L * 3600L * 24L * 30L;

    public int nDocuments; // number of documents containing the word
    public int nCategorizedDocuments; // number of categorized documents containing the word
    public Map<String, Integer> nDocumentsPerCategory; // The number of documents containing the word within the category

    public int nOccurrences; // number of occurrences
    public Map<String, Integer> nOccurrencesPerCategory; // The occurrences within the category

    private Map<String, Set<Integer>> monthsPerCategory = new HashMap<>();

    public long totalFragmentLength; // total length (counting words) of fragments the word appeared in

    public WordStatisticsItem() {
    }

    public WordStatisticsItem(int nDocuments, int nOccurrences, long totalFragmentLength) {
        this.nDocuments = nDocuments;
        this.nOccurrences = nOccurrences;
        this.totalFragmentLength = totalFragmentLength;
        nDocumentsPerCategory = new HashMap<>();
        nOccurrencesPerCategory = new HashMap<>();
    }

    public void incrementNDocumentsPerCategory(String category) {
        nDocumentsPerCategory.put(category, getNDocumentsPerCategory(category) + 1);
        nCategorizedDocuments++;
    }

    public int getNDocumentsPerCategory(String category) {
        Integer n = nDocumentsPerCategory.get(category);
        return n!=null ? n : 0;
    }

    public void incrementOccurrencesInCategory(String category) {
        nOccurrencesPerCategory.put(category, getOccurrencesInCategory(category) + 1);
    }

    public int getOccurrencesInCategory(String category) {
        Integer n = nOccurrencesPerCategory.get(category);
        return n!=null ? n : 0;
    }

    public int getMonthsTouched(String category) {
        Set<Integer> months = monthsPerCategory.get(category);
        return months == null ? 0 : months.size();
    }

    public void addMonth(String category, Date time) {
        Set<Integer> months = monthsPerCategory.get(category);
        if (months == null) {
            months = new HashSet<Integer>();
            monthsPerCategory.put(category, months);
        }

        int timeInMonths = (int) (time.getTime() / MILLISECONDS_IN_MONTH);
        months.add(timeInMonths);
    }

    public static class Converter implements DBConverter<WordStatisticsItem> {

        private static final String N_DOCUMENTS = "nDocuments";
        private static final String N_CATEGORIZED_DOCUMENTS = "nCategorizedDocuments";
		private static final String N_DOCUMENTS_PER_CATEGORY = "nDocumentsPerCategory";
		private static final String N_OCCURRENCES = "nOccurrences";
		private static final String N_OCCURRENCES_PER_CATEGORY = "nOccurrencesPerCategory";
		private static final String MONTHS_PER_CATEGORY = "monthsPerCategory";
		private static final String TOTAL_FRAGMENT_LENGTH = "totalFragmentLength";


        @Override
        public String toDbString(WordStatisticsItem item) {
            try {
                JSONObject object = new JSONObject();
                object.put(N_DOCUMENTS, item.nDocuments);
                object.put(N_CATEGORIZED_DOCUMENTS, item.nCategorizedDocuments);
                object.put(N_DOCUMENTS_PER_CATEGORY, item.nDocumentsPerCategory);
                object.put(N_OCCURRENCES, item.nOccurrences);
                object.put(N_OCCURRENCES_PER_CATEGORY, item.nOccurrencesPerCategory);
                object.put(MONTHS_PER_CATEGORY, item.monthsPerCategory);
                object.put(TOTAL_FRAGMENT_LENGTH, item.totalFragmentLength);
                return object.toString();
            }
            catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public WordStatisticsItem fromDbString(String dbString) {
            try {
                WordStatisticsItem item = new WordStatisticsItem();
                JSONObject object = new JSONObject(dbString);

                item.nDocuments = object.getInt(N_DOCUMENTS);
                item.nCategorizedDocuments = object.getInt(N_CATEGORIZED_DOCUMENTS);
                item.nDocumentsPerCategory = loadCountsPerCategory(object.getJSONObject(N_DOCUMENTS_PER_CATEGORY));
                item.nOccurrences = object.getInt(N_OCCURRENCES);
                item.nOccurrencesPerCategory = loadCountsPerCategory(object.getJSONObject(N_OCCURRENCES_PER_CATEGORY));
                item.monthsPerCategory = loadMonhtsPerCategory(object.getJSONObject(MONTHS_PER_CATEGORY));
                item.totalFragmentLength = object.getLong(TOTAL_FRAGMENT_LENGTH);
                return item;
            }
            catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        private Map<String, Integer> loadCountsPerCategory(JSONObject map) throws JSONException {
            Map<String, Integer> countPerCategory = new HashMap<>();

            Iterator<String> keys = map.keys();
            while(keys.hasNext()) {
                String category = keys.next();
                countPerCategory.put(category, map.getInt(category));
            }

            return countPerCategory;
        }

        private Map<String, Set<Integer>> loadMonhtsPerCategory(JSONObject map) throws JSONException {
            HashMap<String, Set<Integer>> monthsPerCategory = new HashMap<>();

            Iterator<String> keys = map.keys();
            while(keys.hasNext()) {
                String category = keys.next();

                Set<Integer> months = new HashSet<>();

                JSONArray monthsArray = map.getJSONArray(category);
                for (int i = 0; i < monthsArray.length(); ++i)
                    months.add(monthsArray.getInt(i));

                monthsPerCategory.put(category, months);
            }

            return monthsPerCategory;
        }


    }
}
