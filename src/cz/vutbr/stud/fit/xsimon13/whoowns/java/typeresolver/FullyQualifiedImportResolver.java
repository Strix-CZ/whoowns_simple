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

package cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver;

import cz.vutbr.stud.fit.xsimon13.whoowns.Utils;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.filesystem.FileAccessor;

import java.nio.file.Files;
import java.nio.file.Path;

public class FullyQualifiedImportResolver {

    FileAccessor fileAccessor;

    public FullyQualifiedImportResolver(FileAccessor fileAccessor) {
        this.fileAccessor = fileAccessor;
    }

    ScopePath getFileFromScope(ScopePath scope) throws ImportResolver.UnknownImport {
        String[] parts = scope.getParts().toArray(new String[0]);

        if (parts.length < 2)
            throw new ImportResolver.UnknownImport(scope, "Fully qualified name must have at least two components");

        // Find out what part of the import is package (directory)
        int packagePrefix = parts.length - 1;
        ScopePath packageName = null;

        for (; packagePrefix>=0; --packagePrefix) {
            packageName = new ScopePath(Utils.joinStringArray(parts, packagePrefix, "."));
            Path packagePath = fileAccessor.packageNameToPath(packageName);

            if (Files.isDirectory(packagePath))
                break;
        }

        if (packagePrefix==0)
            throw new ImportResolver.UnknownImport(scope, "Imported package not found");

        ScopePath fileName = new ScopePath(Utils.joinStringArray(parts, packagePrefix, "."), parts[packagePrefix]);

        if (Files.isRegularFile(fileAccessor.classNameToPath(fileName)))
            return fileName;
        else
            throw new ImportResolver.UnknownImport(scope, "Imported java file not found");
    }
}
