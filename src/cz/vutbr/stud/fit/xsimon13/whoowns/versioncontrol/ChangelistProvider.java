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

import cz.vutbr.stud.fit.xsimon13.whoowns.Analyzer;
import cz.vutbr.stud.fit.xsimon13.whoowns.db.DBSet;
import cz.vutbr.stud.fit.xsimon13.whoowns.db.StringDbConverter;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.tasks.Logger;
import redis.clients.jedis.Jedis;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class ChangelistProvider {
    public static final String KEY_PROCESSED_CLS = "Cls-Processed";

    private final DBSet<String> processedCls;

    public ChangelistProvider(Jedis jedis) {
        processedCls = new DBSet<>(jedis, KEY_PROCESSED_CLS, new StringDbConverter());
    }

    public void run(Path repository, ParsedClassProvider parsedClassProvider, String branch, Date beginDate, Date endDate, Analyzer analyzer) throws VersionControlException {
        for (Changelist.ChangelistFactory cl : getChangelists(repository, branch, parsedClassProvider, beginDate, endDate)) {
            Map<ScopePath, ChangedLines> changes = getChangesInCl(repository, branch, parsedClassProvider, cl.getId());
            if (changes != null)
                cl.setChanges(changes);

            Changelist actualCl = cl.create();
            int chunkCount = actualCl.getNumberOfChunks();

            if (chunkCount > 0) {
                sync(repository, branch, cl.getId());
                actualCl.goThrough(analyzer);
                Logger.log("Processed CL from " + Logger.dateFormat.format(actualCl.getTime()) + " (" + cl.getId() + "). Chunks: " + chunkCount);
            }
            else
                Logger.log("Skipping empty CL from " + Logger.dateFormat.format(actualCl.getTime()) + " (" + cl.getId() + "). Chunks: " + chunkCount);

            setChangelistProcessed(actualCl.getId());
        }
    }

    protected abstract List<Changelist.ChangelistFactory> getChangelists(Path repository, String branch, ParsedClassProvider parsedClassProvider, Date beginDate, Date endDate) throws VersionControlException;

    protected abstract void sync(Path repository, String branch, String cl) throws VersionControlException;

    /**
     * Returns changed lines of all files in a changelist. Only original changes are captured.
     * If a file was integrated, it is skipped.
     */
    protected abstract Map<ScopePath, ChangedLines> getChangesInCl(Path repository, String branch, ParsedClassProvider parsedClassProvider, String cl) throws VersionControlException;

    protected void setChangelistProcessed(String id) {
        processedCls.add(id);
    }

    protected boolean isChangelistProcessed(String id) {
        return processedCls.contains(id);
    }

    public static class VersionControlException extends Exception {
        public VersionControlException(String message) {
            super(message);
        }

        public VersionControlException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
