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

import cz.vutbr.stud.fit.xsimon13.whoowns.Utils;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.LineStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.ChangelistProvider.VersionControlException;

public class P4Runner {

    private static final String MARSHALING_SWITCH = "-G";

    public JSONArray executeJson(final String... arguments) throws VersionControlException {

        List<String> args = new ArrayList<String>(){{
            add(MARSHALING_SWITCH);
            addAll(Arrays.asList(arguments));
        }};
        String argsString = Utils.join(args, " ");
        String errorMessage = "Can't execute P4 command p4 " + argsString;

        try
        {
            Path tempFile = Files.createTempFile("p4", ".data");
            try
            {
                // Save p4 output to a temp file
                Process p4 = startProcess(args, ProcessBuilder.Redirect.to(tempFile.toFile()));
                p4.waitFor();

                // Unmarshal it to JSON by a Python script
                ProcessBuilder pb = new ProcessBuilder("python", "script\\marshal_to_json.py");
                pb.redirectErrorStream(true);
                pb.redirectInput(tempFile.toFile());
                LineStream ls = LineStream.readProcess(pb.start());

                try {
                    JSONArray array = new JSONArray(ls.readAll());
                    if (array.length() > 0) {
                        JSONObject json = array.optJSONObject(0);
                        String code = json.optString("code", "");
                        if ("error".equals(code))
                            throw new VersionControlException("P4 returned error while executing command:\n " + argsString + "\nError:\n" + json.optString("data", "no error message"));
                    }

                    return array;
                }
                finally {
                    ls.close();
                }
            }
            finally
            {
                if (tempFile != null)
                    Files.delete(tempFile);
            }
        }
        catch (IOException e) {
            throw new VersionControlException(errorMessage, e);
        }
        catch (InterruptedException e) {
            throw new VersionControlException(errorMessage, e);
        }
        catch (JSONException e) {
            throw new VersionControlException(errorMessage, e);
        }
    }

    public LineStream execute(String... arguments) throws VersionControlException {
        return execute(Arrays.asList(arguments));
    }


    public LineStream execute(final List<String> arguments) throws VersionControlException {
        final Process p = startProcess(arguments, null);
        return LineStream.readProcess(p);
    }

    private Process startProcess(final List<String> arguments, ProcessBuilder.Redirect redirect) throws VersionControlException {
        List<String> args = new ArrayList<String>(){{
            add("p4");
            addAll(arguments);
        }};
        String argumentsAsString = Utils.join(args, " ");

        try {
            ProcessBuilder ps = new ProcessBuilder(args);
            ps.redirectErrorStream(true);
            if (redirect != null)
                ps.redirectOutput(redirect);

            return ps.start();
        }
        catch(IOException e) {
            throw new VersionControlException("Can't execute P4 command " + argumentsAsString, e);
        }
    }

}
