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

package cz.vutbr.stud.fit.xsimon13.whoowns;

import java.util.HashMap;
import java.util.Map;

/**
 * Common message board for reporting warnings - singleton.
 */
public class Request {

    private Map<String, String> params = new HashMap<>();
    private String path;

    public Request(String request) {
        int delimiterPosition = request.indexOf('?');
        if (delimiterPosition > 0) {
            path = request.substring(0, delimiterPosition);
            String paramsString = request.substring(delimiterPosition + 1);
            params = parseParams(paramsString);
        }
        else
            path = request;
    }

    public String getPath() {
        return path;
    }

    public String getParameter(String name) {
        return params.get(name);
    }

    public boolean isParameterSet(String name) {
        return params.containsKey(name);
    }

    private Map<String, String> parseParams(String paramsString) {
        Map<String, String> params = new HashMap<>();

        String[] pairs = paramsString.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=");
            if (parts.length != 2)
                throw new RuntimeException("Unbalanced equal sign in parameter");
            params.put(parts[0], parts[1]);
        }

        return params;
    }
}
