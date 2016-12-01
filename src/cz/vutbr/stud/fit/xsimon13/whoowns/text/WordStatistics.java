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

import cz.vutbr.stud.fit.xsimon13.whoowns.ChangeTrackingHashMap;
import cz.vutbr.stud.fit.xsimon13.whoowns.db.DBMap;
import cz.vutbr.stud.fit.xsimon13.whoowns.db.LongDbConverter;
import cz.vutbr.stud.fit.xsimon13.whoowns.db.StringDbConverter;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.OwnershipStatistics;
import redis.clients.jedis.Jedis;
import statutil.StatUtil;

import java.io.IOException;
import java.util.*;

/**
 * Word ownership statistics.
 * Has no notion of Entity - just category.
 */
public class WordStatistics {

    private static final double FW_ALPHA = 1.1d;
    private static final double OPTIMAL_TERM_FREQUENCY = 0.035d;
    private static final double KEYWORD_THRESHOLD = 0.5d; // the worst keyword must have weight at lest KEYWORD_TRESHOLD * <best keyword>

    private static final String KEY_PROPERTIES = "WS-Properties";
    private static final String DOCUMENT_COUNT = "nDocuments"; //The total number of documents
    private static final String CATEGORIZED_DOCUMENT = "nCategorizedDocuments"; // The number of documents, that have been analyzed with an assigned category

    private Map<String, Long> properties = new HashMap<String, Long>();
    private DBMap<String, Long> dbProperties;

    private static final String KEY_DOCUMENT_COUNT_PER_CATEGORY = "WS-DocsInCategory";
    private Map<String, Long> documentCountPerCategory = new HashMap<String, Long>(); // The number of documents in categories
    private DBMap<String, Long> dbDocumentCountPerCategory; // The number of documents in categories

    private static final String KEY_WORDS_STATISTICS = "WS-Words";
    private ChangeTrackingHashMap<String, WordStatisticsItem> wordStats = new ChangeTrackingHashMap<String, WordStatisticsItem>();
    private DBMap<String, WordStatisticsItem> dbWordStats;

    public WordStatistics(Jedis jedis) throws IOException, ClassNotFoundException {
        dbProperties = new DBMap<>(jedis, KEY_PROPERTIES, new StringDbConverter(), new LongDbConverter());
        dbWordStats = new DBMap<>(jedis, KEY_WORDS_STATISTICS, new StringDbConverter(), new WordStatisticsItem.Converter());
        dbDocumentCountPerCategory =  new DBMap<>(jedis, KEY_DOCUMENT_COUNT_PER_CATEGORY, new StringDbConverter(), new LongDbConverter());

        load();
    }

    public void clear() throws IOException {
        properties.clear();
        dbProperties.clear();
        wordStats.clear();
        dbWordStats.clear();
        documentCountPerCategory.clear();
        dbDocumentCountPerCategory.clear();
    }

    private void load() {
        properties.clear();
        properties.putAll(dbProperties);

        wordStats.clear();
        wordStats.putAllWithoutTrackingChanges(dbWordStats);

        documentCountPerCategory.clear();
        documentCountPerCategory.putAll(dbDocumentCountPerCategory);
    }

    public void save() {
        if (!properties.isEmpty())
            dbProperties.putAll(properties);

        if (!wordStats.getChangedItems().isEmpty())
            dbWordStats.putAll(wordStats.getChangedItems());

        if (!documentCountPerCategory.isEmpty())
            dbDocumentCountPerCategory.putAll(documentCountPerCategory);
    }

    public void analyzeDocument(String document) {
        analyzeCategorizedDocument(document, null, null);
    }

    public void analyzeCategorizedDocument(String document, String category, Date time) {
        properties.put(DOCUMENT_COUNT, getDocumentCount() + 1);
        if (category != null) {
            properties.put(CATEGORIZED_DOCUMENT, getCategorizedDocumentCount() + 1);
            Long n = documentCountPerCategory.get(category);
            documentCountPerCategory.put(category, n != null ? n + 1 : 1);
        }

        List<WordSplitter.Word> sequenceOfWords = WordSplitter.splitWithStopWords(document);

        // Count words inside a document
        HashMap<String, WordStatisticsItem> uniqueWords = new HashMap<String, WordStatisticsItem>();
        for (WordSplitter.Word word : sequenceOfWords) {
            WordStatisticsItem stat = uniqueWords.get(word.text); // Avoid loading from Redis-backed wordsStats. Otherwise we would override changes to the object.
            if (stat == null)
                stat = wordStats.get(word.text);

            if (stat == null)
                stat = new WordStatisticsItem(1, 1, word.fragmentLength);
            else {
                stat.nOccurrences++;
                stat.totalFragmentLength += word.fragmentLength;
            }

            if (category != null)
                stat.incrementOccurrencesInCategory(category);

            uniqueWords.put(word.text, stat);
        }

        for (String word : uniqueWords.keySet()) {
            WordStatisticsItem stat = uniqueWords.get(word);

            stat.nDocuments++;

            if (category != null) {
                stat.incrementNDocumentsPerCategory(category);
                if (time != null)
                    stat.addMonth(category, time);
            }

            // The changes are not written through on modification automatically, so
            // we have to actually write the item back to the map to save it into Redis.
            wordStats.put(word, stat);
        }
    }

    public long getDocumentCount() {
        Long v = properties.get(DOCUMENT_COUNT);
        return v != null ? v : 0;
    }

    public long getCategorizedDocumentCount() {
        Long v = properties.get(CATEGORIZED_DOCUMENT);
        return v != null ? v : 0;
    }

    /**
     * Returns IDF-FW of a word.
     */
    public double getIdfFw(String word) {
        WordStatisticsItem stats = wordStats.get(word);
        return stats == null ? 0 : getIdfFw(stats);
    }

    private double getIdfFw(WordStatisticsItem stats) {
        double idf = - Math.log((double)stats.nDocuments / (double)getDocumentCount());
        double fw = FW_ALPHA * Math.abs(Math.log(stats.nOccurrences / (getDocumentCount() * OPTIMAL_TERM_FREQUENCY)));
        return idf - fw;
    }

    private double getInverseAverageFragmentLength(WordStatisticsItem stats) {
        return (double)stats.nOccurrences / (double)stats.totalFragmentLength;
    }

    private double getCategoryProbability(WordStatisticsItem stats, String category) {
        Integer n = stats.nDocumentsPerCategory.get(category);
        if (n == null)
            return 0;

        return (double)n / (double)stats.nCategorizedDocuments;
    }

    private double getDocumentProbability(WordStatisticsItem stats, String category) {
        Long docsInCategory = documentCountPerCategory.get(category);
        if (docsInCategory == null)
            return 0;

        return (double)stats.getNDocumentsPerCategory(category) / docsInCategory;
    }

    private double getBns(WordStatisticsItem stats, String category) {
        int docsWithWordInCategory = stats.getNDocumentsPerCategory(category);
        Long docsInCategory = documentCountPerCategory.get(category);
        if (docsInCategory == null)
            return 0.0d;

        double positiveExamples = ((double)docsWithWordInCategory) / ((double)docsInCategory);
        if (positiveExamples > 0.99995d)
            positiveExamples = 0.99995d;
        else if (positiveExamples < 0.00005d)
            positiveExamples = 0.00005d;

        double negativeExamples = ((double)stats.nCategorizedDocuments - (double)docsWithWordInCategory) / ((double)getCategorizedDocumentCount() - (double)docsInCategory);
        if (negativeExamples > 0.99995d)
            negativeExamples = 0.99995d;
        else if (negativeExamples < 0.00005d)
            negativeExamples = 0.00005d;

        if (positiveExamples < negativeExamples)
            return 0;

        return Math.abs(StatUtil.getInvCDF(positiveExamples, true) - StatUtil.getInvCDF(negativeExamples, true));
    }

    /**
     * Factor how frequent the term appeared through time.
     * 1 if the term appeared at least in every month during the lifetime
     * linearly goes down to 0 if it never appeared.
     */
    private double getMonthsWeight(WordStatisticsItem stats, String category, int projectMonth) {
        return (double)stats.getMonthsTouched(category) / (double)projectMonth;
    }

    public double getWeight(String word, String category, int projectMonths) {
        WordStatisticsItem stats = wordStats.get(word);
        if (stats == null)
            return 0;
        else
            return getWeight(stats, category, projectMonths);
    }

    private double getWeight(WordStatisticsItem stats, String category, int projectMonths) {
        double corpusWeight = getIdfFw(stats);
        double bns = getBns(stats, category);
        double probability = getCategoryProbability(stats, category) + getDocumentProbability(stats, category);
        double iAFL = getInverseAverageFragmentLength(stats);
        double months = getMonthsWeight(stats, category, projectMonths);

        double categoryWeight = bns * (0.4 + probability) * (0.2d + months) * (3.0d + iAFL);

        // Used to normalize score to match owners
        Long docsInCategory = documentCountPerCategory.get(category);
        docsInCategory = docsInCategory != null ? docsInCategory : 0;

        return (corpusWeight + 20.0d * categoryWeight) * Math.log(docsInCategory);
    }

    public static class Keyword implements Comparable<Keyword> {
        public final String word;
        public final double weight;

        public Keyword(String word, double weight) {
            this.word = word;
            this.weight = weight;
        }

        @Override
        public int compareTo(Keyword o) {
            if (weight > o.weight)
                return -1;
            else if (weight < o.weight)
                return 1;
            else
                return word.compareTo(o.word);
        }

        @Override
        public String toString() {
            return word + " (" + weight + ")";
        }
    }

    /**
     * Returns best keywords from the category.
     */
    public List<Keyword> extractKeywordsForCategoryWithWeights(String category, int maximalCount, int projectMonths) {
        SortedSet<Keyword> keywords = new TreeSet<Keyword>();

        for (String word : wordStats.keySet()) {
            WordStatisticsItem stats = wordStats.get(word);

            if (stats.getOccurrencesInCategory(category) == 0)
                continue; // Word doesn't occur in this category at all
            if (stats.getNDocumentsPerCategory(category) < 10)
                continue;

            keywords.add(new Keyword(word, getWeight(stats, category, projectMonths)));
            if (keywords.size() > maximalCount)
                keywords.remove(keywords.last());
        }

        removeKeywordsBelowThreshold(keywords);

        List<Keyword> result = new ArrayList<Keyword>(keywords.size());
        for (Keyword keyword : keywords)
            result.add(keyword);

        return result;
    }

    public long getWordsInCategory(String category) {
        long count = 0L;

        for (String word : wordStats.keySet()) {
            WordStatisticsItem stats = wordStats.get(word);
            count += stats.getOccurrencesInCategory(category);
        }

        return count;
    }

    /**
     * Extract all words that have at least one owner.
     */
    public Set<String> getAllWordsWithOwner(List<String> categories) {
        Set<String> words = new HashSet<String>();

        for (String word : wordStats.keySet()) {

            for (String category : categories) {
                if (getWeight(word, category, 1) > 0.0d) {
                    words.add(word);
                    break;
                }
            }

        }

        return wordStats.keySet();
    }

    /**
     * @return The only definite owner (if such exist)
     */
    public String getTheOnlyOwner(String word, List<String> categories, double threshold) {
        SortedSet<OwnershipStatistics.Owner> owners = new TreeSet<OwnershipStatistics.Owner>();

        for (String category : categories) {
            double weight = getWeight(word, category, 1);
            if (weight > 0.0d) {
                owners.add(new OwnershipStatistics.Owner(category, weight, weight));
                if (owners.size() > 2)
                    owners.remove(owners.last());
            }
        }

        if (owners.size() == 0)
            return null;
        if (owners.size() == 1)
            return owners.first().entityId;

        Iterator<OwnershipStatistics.Owner> it = owners.iterator();
        OwnershipStatistics.Owner first = it.next();
        OwnershipStatistics.Owner second = it.next();
        if (first.weight / second.weight < threshold)
            return null; // the owner is not distinctive enough
        else
            return first.entityId;
    }

    public List<String> extractKeywordsForCategory(String category, int maximalCount, int projectMonths) {
        List<String> result = new ArrayList<String>();
        for (Keyword keyword : extractKeywordsForCategoryWithWeights(category, maximalCount, projectMonths))
            result.add(keyword.word);

        return result;
    }

    /**
     * Returns best keywords from the category.
     */
    public List<Keyword> extractKeywordsWithWeights(int maximalCount) {
        SortedSet<Keyword> keywords = new TreeSet<Keyword>();

        for (String word : wordStats.keySet()) {
            WordStatisticsItem stats = wordStats.get(word);

            keywords.add(new Keyword(word, getIdfFw(stats)));
            if (keywords.size() > maximalCount)
                keywords.remove(keywords.last());
        }

        removeKeywordsBelowThreshold(keywords);

        List<Keyword> result = new ArrayList<Keyword>(keywords.size());
        for (Keyword keyword : keywords)
            result.add(keyword);

        return result;
    }

    public List<String> extractKeywords(int maximalCount) {
        List<String> result = new ArrayList<String>();
        for (Keyword keyword : extractKeywordsWithWeights(maximalCount))
            result.add(keyword.word);

        return result;
    }

    private void removeKeywordsBelowThreshold(SortedSet<Keyword> keywords) {
        if (keywords.size() > 1) {
            // Remove all keywords below threshold
            double threshold = keywords.first().weight * KEYWORD_THRESHOLD;
            Iterator<Keyword> toRemove = keywords.tailSet(new Keyword("", threshold)).iterator();
            while (toRemove.hasNext()) {
                toRemove.next();
                toRemove.remove();
            }
        }
    }
}
