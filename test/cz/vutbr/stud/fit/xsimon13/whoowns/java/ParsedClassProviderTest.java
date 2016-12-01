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

package cz.vutbr.stud.fit.xsimon13.whoowns.java;

import com.github.javaparser.ast.CompilationUnit;
import cz.vutbr.stud.fit.xsimon13.whoowns.MessageBoard;
import cz.vutbr.stud.fit.xsimon13.whoowns.TestUtils;
import cz.vutbr.stud.fit.xsimon13.whoowns.Utils;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.filesystem.ASTLoader;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.filesystem.FileAccessor;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.FullyQualifiedImportResolver;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;

public class ParsedClassProviderTest {

    Path root, pkg;
    FileAccessor fileAccessor;
    ParsedClassProvider parsedClassProvider;

    @Before
    public void setUpTest() throws Exception {
        MessageBoard.getInstance().clearLastMessages();

        root = TestUtils.getTestProjectRoot();
        pkg = root.resolve("parsedClassProvider");

        fileAccessor = new FileAccessor(root);
        parsedClassProvider = new ParsedClassProvider(fileAccessor, new FullyQualifiedImportResolver(fileAccessor), new ASTLoader());
    }

    @Test
    public void parsingTest() throws Exception {
        CompilationUnit cu = parsedClassProvider.get(new ScopePath("parsedClassProvider.Test")).getAst();

        Assert.assertEquals(cu.getComments().size(), 1);
        Assert.assertEquals(cu.getImports().size(), 4);
        Assert.assertEquals(cu.getPackage().getName().toString(), "parsedClassProvider");
        Assert.assertEquals(cu.getTypes().size(), 1);
        Assert.assertEquals(cu.getTypes().get(0).getName(), "Test");
    }

    @Test
    public void cacheTest() throws Exception {
        ScopePath fileName = new ScopePath("parsedClassProvider.Test");
        Path file = fileAccessor.classNameToPath(fileName);

        CompilationUnit cu1 = parsedClassProvider.get(fileName).getAst();
        CompilationUnit cu2 = parsedClassProvider.get(fileName).getAst();
        Assert.assertTrue("ASTLoader haven't cached the file.", cu1 == cu2);

        Utils.writeIntoFile(file, Utils.readFromFile(file)); // Write to the file again to change last modification time
        CompilationUnit cu3 = parsedClassProvider.get(fileName).getAst();

        Assert.assertTrue("ASTLoader haven't realized that the file was modified.", cu2 != cu3);
    }
}
