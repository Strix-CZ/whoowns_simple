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

import cz.vutbr.stud.fit.xsimon13.whoowns.Utils;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

public class FileAccessorTest {
    Set<String> javaClasses;
    Path root;

    private static final String PACKAGE_PATH = "cz/vutbr/stud/fit/xsimon13";
    private static final String PACKAGE_NAME = "cz.vutbr.stud.fit.xsimon13";

    @Before
    public void createTestDirectory() throws Exception {
        root = Files.createTempDirectory("testProject");
        Path pkg = Files.createDirectories(root.resolve(PACKAGE_PATH));
        if (pkg == null)
            throw new RuntimeException("Directory not created");

        javaClasses = new HashSet<String>() {{
            add("SomeClass");
            add("OtheClass");
            add("c");
            add("kuhfd8866jb");
        }};

        for (String c : javaClasses)
            Files.createFile(pkg.resolve(c + ".jAvA"));

        Files.createFile(pkg.resolve("Non java file"));
        Files.createFile(pkg.resolve("AnotherNonJavaFileJava"));
    }

    @After
    public void tearDown() throws Exception {
        Utils.deleteRecursive(root);
    }

    @Test
    public void visitorTest() throws Exception {
        FileAccessor accessor = new FileAccessor(root);

        final Set<ScopePath> paths = new HashSet<ScopePath>(){{
            add(new ScopePath(PACKAGE_NAME + ".SomeClass"));
            add(new ScopePath(PACKAGE_NAME + ".OtheClass"));
            add(new ScopePath(PACKAGE_NAME + ".c"));
            add(new ScopePath(PACKAGE_NAME + ".kuhfd8866jb"));
        }};

        accessor.visitJavaFiles(new FileAccessor.FileVisitor() {
            @Override
            public void visit(ScopePath classPath) {
                Assert.assertTrue("Unexpected path " + classPath, paths.contains(classPath));
                paths.remove(classPath);
            }
        });

        Assert.assertTrue("Paths not visited: " + paths, paths.isEmpty());
    }

    @Test
    public void emptyPackageContentTest() throws Exception {
        FileAccessor accessor = new FileAccessor(root);
        Assert.assertTrue(
                "The package cz should not contain any files",
                accessor.getPackageFiles(new ScopePath("cz")).isEmpty()
        );
    }

    @Test
    public void packageContentTest() throws Exception {
        FileAccessor accessor = new FileAccessor(root);

        Set<ScopePath> names = accessor.getPackageFiles(new ScopePath(PACKAGE_NAME));

        Assert.assertEquals(
                "The count of items in the package is different than expected",
                names.size(), javaClasses.size()
        );

        for (ScopePath name : names) {
            Assert.assertTrue(name.getName() + " is not an expected item name", javaClasses.contains(name.getName().toString()));
            Assert.assertEquals(name.getQualifier().toString(), PACKAGE_NAME);
        }
    }

    @Test
    public void packageNameToPathTest() throws Exception {
        FileAccessor accessor = new FileAccessor(root);
        Path result = accessor.packageNameToPath(new ScopePath(PACKAGE_NAME));
        Assert.assertEquals(result, root.resolve(PACKAGE_PATH));
    }

    @Test
    public void classNameToPathTest() throws Exception {
        FileAccessor accessor = new FileAccessor(root);
        Path result = accessor.classNameToPath(new ScopePath(PACKAGE_NAME + ".SomeClass"));
        Assert.assertEquals(result, root.resolve(PACKAGE_PATH).resolve("SomeClass.java"));
    }
}
