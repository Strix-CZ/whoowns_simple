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

import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.p4.P4Runner;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;

public class FileAccessor {

    private Path root;
    private static final String JAVA_EXTENSION = ".java";
    private final static JavaFileFilter fileFilter = new JavaFileFilter();
    private final static PackageFilter packageFilter = new PackageFilter();

    public FileAccessor(Path root) {
        this.root = root;
    }

    public static interface FileVisitor {
        public void visit(ScopePath classPath);
    }

    public void visitJavaFiles(FileVisitor visitor) throws IOException {
        Queue<ScopePath> packages = new ArrayDeque<ScopePath>();
        packages.add(new ScopePath(""));

        while (!packages.isEmpty()) {
            ScopePath packageName = packages.poll();
            Path packagePath = packageNameToPath(packageName);

            DirectoryStream<Path> dir = Files.newDirectoryStream(packagePath, fileFilter);
            try {
                for (Path file : dir) {
                    if (Files.isRegularFile(file)) {
                        String name = file.getFileName().toString();
                        name = removeExtension(name);

                        visitor.visit(ScopePath.append(packageName, name));
                    } else if (Files.isDirectory(file)) {
                        packages.add(ScopePath.append(packageName, file.getFileName().toString()));
                    }
                }
            }
            finally {
                dir.close();
            }

            dir = Files.newDirectoryStream(packagePath, packageFilter);
            try {
                for (Path file : dir)
                    packages.add(ScopePath.append(packageName, file.getFileName().toString()));
            }
            finally {
                dir.close();
            }
        }
    }

    public Set<ScopePath> getPackageFiles(ScopePath packageName) throws IOException {
        Path packagePath = packageNameToPath(packageName);

        Set<ScopePath> result = new HashSet<ScopePath>();

        DirectoryStream<Path> dir = Files.newDirectoryStream(packagePath, fileFilter);
        try {
            for (Path file : dir) {
                if (Files.isRegularFile(file)) {
                    String name = file.getFileName().toString();
                    name = removeExtension(name);

                    result.add(ScopePath.append(packageName, name));
                }
            }
        }
        finally {
            dir.close();
        }

        return result;
    }

    public boolean isValidClass(Path file) {
        return fileFilter.accept(file);
    }

    public ScopePath pathToClassName(Path file) {
        if (!isValidClass(file))
            return null;

        Path relativePath = root.relativize(root.resolve(file));
        String withoutExtension = removeExtension(relativePath.toString());
        return new ScopePath(withoutExtension.replace(File.separator, "."));
    }


    public Path packageNameToPath(ScopePath packageName) {
        Path file = root.resolve(packageName.toString().replace(".", File.separator));
        if (Files.exists(file))
            return file;

        file = root;
        ScopePath pkg = packageName.withoutFirst();
        String segment = packageName.firstPart().toString();

        while (!pkg.isEmpty()) {
            Path nextFile = file.resolve(segment);
            if (Files.exists(nextFile)) {
                file = nextFile;
                segment = pkg.firstPart().toString();
                pkg = pkg.withoutFirst();
            }
            else {
                segment += "." + pkg.firstPart().toString();
                pkg = pkg.withoutFirst();
            }
        }

        return file.resolve(segment);
    }

    public Path classNameToPath(ScopePath name) {
        return packageNameToPath(name.getQualifier()).resolve(name.getName() + JAVA_EXTENSION);
    }

    private static String removeExtension(String filename) {
        return filename.substring(0, filename.length() - JAVA_EXTENSION.length());
    }

    /**
     * Filter by .java file extension
     */
    private static class JavaFileFilter implements DirectoryStream.Filter<Path> {
        @Override
        public boolean accept(Path file) {
            return file.toString().toLowerCase().endsWith(JAVA_EXTENSION);
        }
    }

    private static class PackageFilter implements DirectoryStream.Filter<Path> {

        private static final Pattern noDots = Pattern.compile("^[^.].*$");

        @Override
        public boolean accept(Path file) {
            return (
                    noDots.matcher(file.getFileName().toString()).matches() &&
                    Files.isDirectory(file)
            );
        }
    }
}
