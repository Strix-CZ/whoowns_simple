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

package cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.p4;

import cz.vutbr.stud.fit.xsimon13.whoowns.MessageBoard;
import cz.vutbr.stud.fit.xsimon13.whoowns.Utils;
import cz.vutbr.stud.fit.xsimon13.whoowns.diff.Diff;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Person;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.filesystem.FileAccessor;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;


public class P4ChangelistProvider extends ChangelistProvider {
    private P4Runner p4Runner;
    private Date lastSync = new GregorianCalendar(2000, 0, 1).getTime();
    private static final long intervalBetweenSync = 20000;


    public P4ChangelistProvider(P4Runner p4Runner, Jedis jedis) {
        super(jedis);
        this.p4Runner = p4Runner;
    }

    @Override
    protected List<Changelist.ChangelistFactory> getChangelists(Path repository, String branch, ParsedClassProvider parsedClassProvider, Date beginDate, Date endDate) throws VersionControlException {
        try {
            String dateLimiter = "";
            if (beginDate != null && endDate != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                dateLimiter = "@" + dateFormat.format(beginDate) + ",@" + dateFormat.format(endDate);
            }

            JSONArray response = p4Runner.executeJson("changes", "-t", "-s", "submitted", "-l",
                                                      "//depot/" + branch + "/..." + dateLimiter);

            List<Changelist.ChangelistFactory> cls = new ArrayList<Changelist.ChangelistFactory>();
            for (int i = 0; i < response.length(); ++i) {
                JSONObject line = response.getJSONObject(i);

                Changelist.ChangelistFactory cl = new Changelist.ChangelistFactory();
                cl.setParsedClassProvider(parsedClassProvider);

                Date time = new Date(line.getLong("time") * 1000L);
                if (beginDate != null && beginDate.compareTo(time) > 0)
                    continue;
                if (endDate != null && endDate.compareTo(time) < 0)
                    continue;

                String id = line.getString("change");
                if (isChangelistProcessed(id))
                    continue;

                cls.add(cl.setId(id) // Changelist number
                        .setTime(time)
                        .setAuthor(new Person(line.getString("user")))
                        .setDescription(line.optString("desc", "").trim()));
            }

            Collections.sort(cls, new Comparator<Changelist.ChangelistFactory>() {
                @Override
                public int compare(Changelist.ChangelistFactory o1, Changelist.ChangelistFactory o2) {
                    int id1 = Integer.parseInt(o1.getId());
                    int id2 = Integer.parseInt(o2.getId());
                    if (id1 > id2)
                        return 1;
                    else if (id1 < id2)
                        return -1;
                    else
                        return 0;
                }
            });

            return cls;
        }
        catch(JSONException e) {
            throw new RuntimeException("Unexpected P4 response", e);
        }
    }

    @Override
    protected void sync(Path repository, String branch, String cl) throws VersionControlException {
        long timeSinceSync = new Date().getTime() - lastSync.getTime();
        if (timeSinceSync < intervalBetweenSync) {
            try {
                Thread.sleep(intervalBetweenSync - timeSinceSync);
            }
            catch (InterruptedException e) {
            }
        }

        lastSync = new Date();
        p4Runner.executeJson("sync", "//depot/" + branch + "/...@" + cl);
    }

    @Override
    protected Map<ScopePath, ChangedLines> getChangesInCl(Path repository, String branch, ParsedClassProvider parsedClassProvider, String cl) throws VersionControlException {
        String depotFilePrefix = "//depot/" + branch + "/";

        Map<ScopePath, ChangedLines> result = new HashMap<ScopePath, ChangedLines>();

        try {
            JSONObject r = p4Runner.executeJson("describe", cl).getJSONObject(0);
            List<String> actions = getP4Array(r, "action");
            List<String> depotFiles = getP4Array(r, "depotFile");
            List<String> fileRevisions = getP4Array(r, "rev");

            if (actions.size() != depotFiles.size() || depotFiles.size() != fileRevisions.size())
                throw new VersionControlException("Lists of changed files, actions or file revisions has different lengths.");

            for (int i = 0; i < depotFiles.size(); ++i) {
                // Convert depot file to scope path of the file
                String depotFile = depotFiles.get(i);
                if (!depotFile.startsWith(depotFilePrefix))
                    throw new VersionControlException("Depot file doesn't start with prefix '" + depotFilePrefix + "'. It is: '" + depotFile + "'");
                Path depotFilePath = Paths.get(depotFile.substring(depotFilePrefix.length()));
                FileAccessor fileAccessor = parsedClassProvider.getFileAccessor();
                if (!fileAccessor.isValidClass(depotFilePath))
                    continue; // Skip non java file
                ScopePath scopePath = fileAccessor.pathToClassName(depotFilePath);

                ChangedLines changedLines;
                String action = actions.get(i);
                if ("delete".equals(action) || "branch".equals(action) || "import".equals(action) || "integrate".equals(action))
                    continue; // All branch, import and integrate are integration actions.
                else if ("add".equals(action))
                    changedLines = new CreatedFileChangedLines();
                else if ("edit".equals(action))
                    changedLines = getDiff(depotFile, fileRevisions.get(i));
                else {
                    MessageBoard.getInstance().sendRecoverableError(new VersionControlException("Unknown file action '" + action + "'"));
                    continue;
                }

                result.put(scopePath, changedLines);
            }

        }
        catch(JSONException e) {
            throw new VersionControlException("Unexpected P4 response", e);
        }

        return result;
    }

    private ChangedLines getDiff(String depotFile, String fileRevision) throws VersionControlException {
        Integer prevFileRevision = Integer.parseInt(fileRevision) - 1;

        // unified diff with 0 lines of context, ignores changes made within whitespace
        LineStream ls = p4Runner.execute("diff2", "-dbu0", depotFile + "#" + prevFileRevision, depotFile + "#" + fileRevision);
        try {
            return EditedChangedLines.fromPatch(Diff.parse(ls.readAll()));
        }
        finally {
            try {
                ls.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<String> getP4Array(JSONObject r, String prefix) {
        List<String> result = new ArrayList<String>();
        for (int i = 0; true; ++i) {
            String item = r.optString(prefix + Integer.toString(i));
            if (Utils.isEmpty(item))
                return result;
            result.add(item);
        }
    }
}
