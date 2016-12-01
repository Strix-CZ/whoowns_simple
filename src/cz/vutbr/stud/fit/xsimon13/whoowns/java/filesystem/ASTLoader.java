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

package cz.vutbr.stud.fit.xsimon13.whoowns.java.filesystem;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Parsing Java files into AST.
 */
public class ASTLoader {

    public ASTLoader() {
        JavaParser.setDoNotAssignCommentsPreceedingEmptyLines(false);
    }

    public CompilationUnit load(Path file) throws IOException, ParseException {
        InputStream in = Files.newInputStream(file);

        try {
            return JavaParser.parse(in, null, true);
        }
        finally {
            in.close();
        }
    }
}
