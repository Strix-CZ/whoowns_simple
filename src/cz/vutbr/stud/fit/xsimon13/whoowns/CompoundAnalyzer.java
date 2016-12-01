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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An analyzer that wrap more analyzers into one.
 */
public class CompoundAnalyzer implements Analyzer
{
    List<Analyzer> analyzers = new ArrayList<Analyzer>();

    public void addAnalyzer(Analyzer analyzer) {
        analyzers.add(analyzer);
    }

    @Override
    public void setChangelistInformation(Date time, Person author, String description, ParsedClassProvider parsedClassProvider)
    {
        for (Analyzer analyzer : analyzers)
            analyzer.setChangelistInformation(time, author, description, parsedClassProvider);
    }

    @Override
    public void analyze(ScopePath file, ChangedLines changedLines) throws IOException, ParseException
    {
        for (Analyzer analyzer : analyzers)
            analyzer.analyze(file, changedLines);
    }

    @Override
    public void afterChangelist()
    {
        for (Analyzer analyzer : analyzers)
            analyzer.afterChangelist();
    }
}
