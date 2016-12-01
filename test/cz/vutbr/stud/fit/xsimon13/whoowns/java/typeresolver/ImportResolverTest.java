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

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.expr.NameExpr;
import cz.vutbr.stud.fit.xsimon13.whoowns.MessageBoard;
import cz.vutbr.stud.fit.xsimon13.whoowns.TestUtils;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.filesystem.ASTLoader;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.filesystem.FileAccessor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

public class ImportResolverTest {

    Path root, pkg, blahPkg;
    FileAccessor fileAccessor;
    ParsedClassProvider parsedClassProvider;
    
    @Before
    public void setupTestProject() throws Exception {
        root = TestUtils.getTestProjectRoot();
        pkg = root.resolve("pkgA\\pkgB\\pkgC");
        blahPkg = root.resolve("blah");

        fileAccessor = new FileAccessor(root);
        parsedClassProvider = new ParsedClassProvider(fileAccessor, new FullyQualifiedImportResolver(fileAccessor), new ASTLoader());
    }

    @Before
    public void setUpTest() throws Exception {
        MessageBoard.getInstance().clearLastMessages();
    }

    @Test
    public void simpleImportTest() throws Exception {
        SortedSet<ImportResolver.AnalyzedImport> result = analyzeImport("pkgA.pkgB.pkgC.classEmpty", false).getAnalyzedImports();

        Assert.assertEquals(result.size(), 1);
        Assert.assertFalse(result.first().isAsterisk);
        Assert.assertTrue(result.first().isType);
        Assert.assertEquals(result.first().name, new ScopePath("pkgA.pkgB.pkgC.classEmpty"));
        Assert.assertEquals(result.first().fileName, new ScopePath("pkgA.pkgB.pkgC.classEmpty"));
    }

    @Test
    public void nestedClassTest() throws Exception {
        SortedSet<ImportResolver.AnalyzedImport> result = analyzeImport("pkgA.pkgB.pkgC.classNested.nested", false).getAnalyzedImports();

        Assert.assertEquals(result.size(), 1);
        Assert.assertFalse(result.first().isAsterisk);
        Assert.assertTrue(result.first().isType);
        Assert.assertEquals(result.first().name, new ScopePath("pkgA.pkgB.pkgC.classNested.nested"));
        Assert.assertEquals(result.first().fileName, new ScopePath("pkgA.pkgB.pkgC.classNested"));
    }

    @Test
    public void simpleStaticImportTest() throws Exception {
        SortedSet<ImportResolver.AnalyzedImport> result = analyzeImport("pkgA.pkgB.pkgC.classStatic.visibleField", true).getAnalyzedImports();

        Assert.assertEquals(result.size(), 1);
        Assert.assertFalse(result.first().isAsterisk);
        Assert.assertTrue(result.first().isType);
        Assert.assertEquals(result.first().name, new ScopePath("pkgA.pkgB.pkgC.classStatic.visibleField"));
        Assert.assertEquals(result.first().fileName, new ScopePath("pkgA.pkgB.pkgC.classStatic"));
    }

    @Test
    public void staticClassTest() throws Exception {
        SortedSet<ImportResolver.AnalyzedImport> result = analyzeImport("pkgA.pkgB.pkgC.classStatic.*", true).getAnalyzedImports();

        Assert.assertEquals(result.size(), 3);

        for (ImportResolver.AnalyzedImport item : result) {
            Assert.assertTrue(item.isAsterisk);
            Assert.assertEquals(result.first().fileName, new ScopePath("pkgA.pkgB.pkgC.classStatic"));
            if (item.isType) {
                Assert.assertEquals(item.name, new ScopePath("pkgA.pkgB.pkgC.classStatic.visibleClass"));
            }
            else {
                ScopePath name1 = new ScopePath("pkgA.pkgB.pkgC.classStatic.visibleField");
                ScopePath name2 = new ScopePath("pkgA.pkgB.pkgC.classStatic.visibleMethod");
                Assert.assertTrue(item.name.equals(name1) || item.name.equals(name2));

                ScopePath innerName1 = new ScopePath("classStatic.visibleField");
                ScopePath innerName2 = new ScopePath("classStatic.visibleMethod");
            }
        }
    }

    @Test
    public void localPackageTest() throws Exception {
        List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>();

        ImportResolver resolver = analyzeImports(new ScopePath("pkgA.pkgB.pkgC"), imports);
        SortedSet<ImportResolver.AnalyzedImport> result = resolver.getAnalyzedImports();

        Assert.assertEquals("Found some board messages: " + MessageBoard.getInstance().getLastMessages().toString(), MessageBoard.getInstance().getLastMessages().size(), 0);
        Assert.assertEquals(result.size(), 6);
    }

    @Test
    public void deepNestingTest() throws Exception {
        SortedSet<ImportResolver.AnalyzedImport> result = analyzeImport("pkgA.pkgB.pkgC.deeplyNested.deeplyNested1.deeplyNested2.deeplyNested3", false).getAnalyzedImports();

        Assert.assertEquals(result.size(), 1);
        Assert.assertEquals(result.first().name, new ScopePath("pkgA.pkgB.pkgC.deeplyNested.deeplyNested1.deeplyNested2.deeplyNested3"));
        Assert.assertEquals(result.first().fileName, new ScopePath("pkgA.pkgB.pkgC.deeplyNested"));
        Assert.assertFalse(result.first().isAsterisk);
        Assert.assertTrue(result.first().isType);
    }

    @Test
    public void publicPackageImport() throws Exception {
        SortedSet<ImportResolver.AnalyzedImport> result = analyzeImport("pkgA.pkgB.pkgC.*", false).getAnalyzedImports();

        Assert.assertEquals(result.size(), 4);
    }

    @Test
    public void resolveNameTest() throws Exception {
        ImportResolver resolver = analyzeImport("pkgA.pkgB.pkgC.*", false);

        ScopePath context = new ScopePath("pkgA.pkgB.pkgC");

        List<ScopePath> importedNames = new ArrayList<ScopePath>(){{
            add(new ScopePath("classEmpty"));
            add(new ScopePath("classNested"));
            add(new ScopePath("classStatic"));
            add(new ScopePath("deeplyNested"));
            add(new ScopePath("pkgA.pkgB.pkgC.classEmpty"));
            add(new ScopePath("pkgA.pkgB.pkgC.classNested"));
            add(new ScopePath("pkgA.pkgB.pkgC.classStatic"));
            add(new ScopePath("pkgA.pkgB.pkgC.deeplyNested"));
        }};

        for (ScopePath importedName : importedNames)
            Assert.assertEquals(resolver.resolveName(importedName), ScopePath.append(context, importedName.getName()));

        try {
            String result = resolver.resolveName(new ScopePath("UnknownName")).toString();
            Assert.fail("Unknown name was successfully resolved to " + result);
        }
        catch (ImportResolver.UnknownImport e) {
            // expected exception
        }
    }

    @Test
    public void resolvePrecedenceTest() throws Exception {
        List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>(){{
            add(new ImportDeclaration(new NameExpr("pkgA.pkgB.pkgC.*"), false, true));
            add(new ImportDeclaration(new NameExpr("blah.classEmpty"), false, true));
        }};
        ImportResolver resolver = analyzeImports(new ScopePath("foo.bar"), imports);

        ScopePath blah = new ScopePath("blah");
        ScopePath pkgABC = new ScopePath("pkgA.pkgB.pkgC");

        // Specific imports should take precedence over asterisk imports
        Assert.assertEquals(resolver.resolveName(new ScopePath("classEmpty")), ScopePath.append(blah, "classEmpty"));

        // Qualified names should be imported in it's package
        Assert.assertEquals(resolver.resolveName(new ScopePath("pkgA.pkgB.pkgC.classEmpty")), ScopePath.append(pkgABC, "classEmpty"));

        // Asterisk imports should work normally for other names
        Assert.assertEquals(resolver.resolveName(new ScopePath("classNested")), ScopePath.append(pkgABC, "classNested"));
    }

    @Test
    public void resolveStaticTest() throws Exception {
        ImportResolver resolver = analyzeImport("pkgA.pkgB.pkgC.classStatic", false);

        ScopePath context = new ScopePath("pkgA.pkgB.pkgC.classStatic");

        for (String name : new String[] {"visibleField", "visibleMethod", "visibleClass"}) {
            Assert.assertEquals(resolver.resolveName(new ScopePath("classStatic." + name)), ScopePath.append(context, name));
            Assert.assertEquals(resolver.resolveName(ScopePath.append(context.getName(), name)), ScopePath.append(context, name));
        }
    }

    @Test
    public void resolveStaticAsteriskTest() throws Exception {
        ImportResolver resolver = analyzeImport("pkgA.pkgB.pkgC.classStatic.*", true);

        ScopePath context = new ScopePath("pkgA.pkgB.pkgC.classStatic");

        for (String name : new String[] {"visibleField", "visibleMethod", "visibleClass"}) {
            ScopePath expected =  ScopePath.append(context, name);
            Assert.assertEquals(resolver.resolveName(new ScopePath(name)), expected);
            Assert.assertEquals(resolver.resolveName(ScopePath.append(context, name)), expected);
        }
    }

    @Test
    public void fullyQualifiedNameTest() throws Exception {
        ImportResolver importResolver = analyzeImports(new ScopePath("foo.bar"), new ArrayList<ImportDeclaration>());

        ScopePath file = importResolver.getFileNameFromScope(new ScopePath("pkgA.pkgB.pkgC.deeplyNested.deeplyNested1.deeplyNested2"));
        Assert.assertEquals(file, new ScopePath("pkgA.pkgB.pkgC.deeplyNested"));
    }

    /*@Test
    public void getFileTest() throws Exception {
        ImportResolver resolver = analyzeImport("pkgA.pkgB.pkgC.classStatic.*", true);
        ScopePath name = new ScopePath("pkgA.pkgB.pkgC.classStatic.visibleField");

        Assert.assertEquals(resolver.getDetails(name).file, fileAccessor.classNameToPath(Utils.getParentName(name)));
    }*/


    private ImportResolver analyzeImport(final String importedName, final boolean isStatic) throws Exception {
        List<ImportDeclaration> imports = new ArrayList<ImportDeclaration>(){{
            add(new ImportDeclaration(new NameExpr(importedName), isStatic, importedName.endsWith("*")));
        }};

       return analyzeImports(new ScopePath("foo.bar"), imports);
    }

    private ImportResolver analyzeImports(ScopePath localPackage, final List<ImportDeclaration> imports) throws Exception {
        ImportResolver resolver = new ImportResolver(fileAccessor, parsedClassProvider, new PackageDeclaration(new NameExpr(localPackage.toString())), imports);

        TestUtils.assertNoMessages();

        return resolver;
    }
}
