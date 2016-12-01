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

import com.github.javaparser.ParseException;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Person;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.ChangedLines;

import java.io.IOException;
import java.util.Date;

/**
 * Class capable of analyzing a changelist.
 */
public interface Analyzer {
    /**
     * Set's general CL information. Guaranteed to be called before analyze() calls.
     */
    public void setChangelistInformation(Date time, Person author, String description, ParsedClassProvider parsedClassProvider);

    /**
     * Analyze a modified file.
     */
    public void analyze(ScopePath file, ChangedLines changedLines) throws IOException, ParseException;

    /**
     * Called after whole CL is processed.
     */
    public void afterChangelist();
}
