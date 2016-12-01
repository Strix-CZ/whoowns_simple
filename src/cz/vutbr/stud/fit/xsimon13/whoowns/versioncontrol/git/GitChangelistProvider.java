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

package cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.git;

import cz.vutbr.stud.fit.xsimon13.whoowns.diff.Diff;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Person;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.filesystem.FileAccessor;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.*;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GitChangelistProvider extends ChangelistProvider {

    private static final Pattern commitPattern = Pattern.compile("^commit ([a-fA-F0-9]+)$");
    private static final Pattern authorPattern = Pattern.compile("^author.*<([^>]+)> ([0-9]+)( [-+][0-9]+)?$");
    private static final Pattern descriptionPatter = Pattern.compile("^    (.*)$");
    private static final Pattern newFilePattern = Pattern.compile("^\\+\\+\\+ (?:b/)?(.*)$");
    private static final Pattern diffPattern = Pattern.compile("^[-+@].*$");

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    GitRunner runner;

    public GitChangelistProvider(GitRunner runner, Jedis jedis) {
        super(jedis);
        this.runner = runner;
    }

    private enum State {
        LOOK_FOR_COMMIT,
        LOOK_FOR_AUTHOR,
        LOOK_FOR_DESCRIPTION,
        READ_DESCRIPTION,
        LOOK_FOR_NEW_FILE_OR_COMMIT,
        LOOK_FOR_DIFF_OR_COMMIT,
        READ_DIFF
    }

    @Override
    protected List<Changelist.ChangelistFactory> getChangelists(Path repository, String branch, ParsedClassProvider parsedClassProvider, Date beginDate, Date endDate) throws VersionControlException {

        String response = runner.execute(repository, "checkout", branch).readAll();
        if (!response.contains("Switched to branch '" + branch + "'") && !response.contains("Already on '" + branch + "'"))
            throw new VersionControlException("Unexpected git response when checkout " + branch + ":\n" + response);

        LineStream lines = runner.execute(repository,
                           "log",
                           //"--branches=" + branch,
                           "--since", dateFormat.format(beginDate),
                           "--until", dateFormat.format(endDate),
                           "--no-merges",
                           "--format=raw",
                           "-p",
                           "--unified=0");

        try {

            List<Changelist.ChangelistFactory> cls = new ArrayList<Changelist.ChangelistFactory>();

            State state = State.LOOK_FOR_COMMIT;
            Changelist.ChangelistFactory cl = null;
            ScopePath file = null;
            String diff = null;

            for (String line : lines) {
                Matcher commitMatcher = commitPattern.matcher(line);

                if ((state == State.LOOK_FOR_COMMIT || state == State.LOOK_FOR_NEW_FILE_OR_COMMIT || state == State.LOOK_FOR_DIFF_OR_COMMIT) && commitMatcher.matches()) {
                    if (state == State.LOOK_FOR_NEW_FILE_OR_COMMIT)
                        cls.add(cl);

                    cl = new Changelist.ChangelistFactory();
                    cl.setParsedClassProvider(parsedClassProvider);
                    cl.setId(commitMatcher.group(1));
                    state = State.LOOK_FOR_AUTHOR;
                } else if (state == State.LOOK_FOR_AUTHOR) {
                    Matcher m = authorPattern.matcher(line);
                    if (m.matches()) {
                        cl.setAuthor(new Person(m.group(1)));
                        cl.setTime(new Date(Long.parseLong(m.group(2)) * 1000L));
                        state = State.LOOK_FOR_DESCRIPTION;
                    }
                } else if (state == State.LOOK_FOR_DESCRIPTION || state == State.READ_DESCRIPTION) {
                    Matcher m = descriptionPatter.matcher(line);
                    if (m.matches()) {
                        state = State.READ_DESCRIPTION;
                        cl.appendDescription(m.group(1));
                    } else if (state == State.READ_DESCRIPTION)
                        state = State.LOOK_FOR_NEW_FILE_OR_COMMIT;
                } else if (state == State.LOOK_FOR_NEW_FILE_OR_COMMIT) {
                    Matcher m = newFilePattern.matcher(line);
                    if (m.matches()) {
                        Path newFile = Paths.get(m.group(1));
                        FileAccessor fileAccessor = parsedClassProvider.getFileAccessor();
                        if (!fileAccessor.isValidClass(newFile))
                            state = State.LOOK_FOR_NEW_FILE_OR_COMMIT; // Skip non java file
                        else {
                            file = fileAccessor.pathToClassName(newFile);
                            diff = "";
                            state = State.LOOK_FOR_DIFF_OR_COMMIT;
                        }
                    }
                } else if (state == State.LOOK_FOR_DIFF_OR_COMMIT || state == State.READ_DIFF) {
                    Matcher m = diffPattern.matcher(line);
                    if (m.matches()) {
                        state = State.READ_DIFF;
                        diff += "\n" + line;
                    } else if (state == State.READ_DIFF) {
                        cl.insertChanges(file, EditedChangedLines.fromPatch(Diff.parse(diff)));
                        state = State.LOOK_FOR_NEW_FILE_OR_COMMIT;
                    }
                }
            }

            if (state == State.READ_DIFF)
                cl.insertChanges(file, EditedChangedLines.fromPatch(Diff.parse(diff)));
            if (cl != null)
                cls.add(cl);

            return cls;
        }
        finally {
            try {
                lines.close();
            }
            catch (IOException e) {
                throw new VersionControlException("Can't close LineStream", e);
            }

        }
    }

    @Override
    protected void sync(Path repository, String branch, String cl) throws VersionControlException {
        String response = runner.execute(repository, "checkout", cl).readAll();
        if (!response.contains("HEAD is now at"))
            throw new VersionControlException("Unexpected git response when checkout " + cl + ":\n" + response);
    }

    @Override
    protected Map<ScopePath, ChangedLines> getChangesInCl(Path repository, String branch, ParsedClassProvider parsedClassProvider, String cl) throws VersionControlException {
        return null;
    }
}