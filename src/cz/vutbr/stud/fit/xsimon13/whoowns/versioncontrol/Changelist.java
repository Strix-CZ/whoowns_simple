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

import com.github.javaparser.ParseException;
import cz.vutbr.stud.fit.xsimon13.whoowns.Analyzer;
import cz.vutbr.stud.fit.xsimon13.whoowns.MessageBoard;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Person;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Changelist {
    private String id = null;
    private Person author = null;
    private String description = "";
    private Date time = null;
    private ParsedClassProvider parsedClassProvider = null;
    private Map<ScopePath, ChangedLines> changes = new HashMap<ScopePath, ChangedLines>();

    public String getId() {
        return id;
    }

    public Person getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public int getNumberOfChunks() {
        int i = 0;
        for (ScopePath file : changes.keySet())
            i += changes.get(file).getCount();
        return i;
    }

    public Date getTime() {
        return time;
    }

    public void goThrough(Analyzer analyzer) {
        analyzer.setChangelistInformation(time, author, description, parsedClassProvider);

        for (ScopePath file : changes.keySet()) {
            if (!Files.exists(parsedClassProvider.getFileAccessor().classNameToPath(file))) {
                MessageBoard.getInstance().sendMessage(MessageBoard.Types.RECOVERABLE_ERROR, "File not found: '" + file + "'");
                continue;
            }
            try {
                analyzer.analyze(file, changes.get(file));
            }
            catch(ParseException e) {
                MessageBoard.getInstance().sendRecoverableError("Can't analyze file " + file + " in CL " + id, e);
            }
            catch(IOException e) {
                MessageBoard.getInstance().sendRecoverableError("Can't analyze file " + file + " in CL " + id, e);
            }
        }

        analyzer.afterChangelist();
    }

    public static class ChangelistFactory {
        private Changelist cl = new Changelist();

        public Changelist create() {
            return cl;
        }

        public String getId() {
            return cl.id;
        }

        public ChangelistFactory setId(String id) {
            cl.id = id;
            return this;
        }

        public ChangelistFactory setAuthor(Person author) {
            cl.author = author;
            return this;
        }

        public ChangelistFactory setDescription(String description) {
            cl.description = description;
            return this;
        }

        public ChangelistFactory appendDescription(String description) {
            if (!cl.description.isEmpty())
                cl.description += "\n";
            cl.description += description;
            return this;
        }

        public ChangelistFactory setTime(Date time) {
            cl.time = time;
            return this;
        }

        public ChangelistFactory setParsedClassProvider(ParsedClassProvider parsedClassProvider) {
            cl.parsedClassProvider = parsedClassProvider;
            return this;
        }

        public ChangelistFactory setChanges(Map<ScopePath, ChangedLines> changes) {
            cl.changes = changes;
            return this;
        }

        public ChangelistFactory insertChanges(ScopePath file, ChangedLines content) {
            cl.changes.put(file, content);
            return this;
        }
    }

}
