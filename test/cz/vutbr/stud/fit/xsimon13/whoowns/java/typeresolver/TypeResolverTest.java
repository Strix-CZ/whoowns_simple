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

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import cz.vutbr.stud.fit.xsimon13.whoowns.TestUtils;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.filesystem.ASTLoader;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.filesystem.FileAccessor;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;

import static cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.TypeResolver.SymbolType.*;

@RunWith(Parameterized.class)
public class TypeResolverTest {
    private Path root;
    private FileAccessor fileAccessor;
    private ParsedClassProvider parsedClassProvider;
    private final static ScopePath typeResolverPkg = new ScopePath("typeResolver");

    @Parameterized.Parameters
    public static Collection<Object[]> createData1() {
        return Arrays.asList(new Object[][] {
                { "Simple", METHOD, "methodB", "Simple.methodA", "typeResolver.Simple.methodB" },
                { "Simple", CLASS_OR_INTERFACE, "Nested", "Simple.methodC", "typeResolver.Simple.Nested" },
                { "Simple", VARIABLE_TYPE, "variable", "Simple.methodC.[0].[0]", "typeResolver.Simple.Nested" },
                { "Simple", VARIABLE_TYPE, "field", "Simple.methodB.[0]", "typeResolver.Simple.Nested" },
                { "Simple", VARIABLE_TYPE, "Nested.nestedField", "Simple.methodD.[0]", "typeResolver.Simple" },

                { "Importing", VARIABLE_TYPE, "Simple.field", "Importing.methodA.[0]", "typeResolver.Simple.Nested" },
                { "Importing", VARIABLE_TYPE, "foo", "Importing.methodA.[0]", "typeResolver.Simple.Nested" },
                { "Importing", METHOD, "Simple.methodC", "Importing.methodA.[0]", "typeResolver.Simple.methodC" },
                { "Importing", METHOD, "typeResolver.Simple.methodC", "Importing.methodA.[0]", "typeResolver.Simple.methodC" },

                { "Inheritance", VARIABLE_TYPE, "superField", "Child.method.[0]", "typeResolver.Simple" },
                { "Inheritance", METHOD, "superMethod", "Child.method.[0]", "typeResolver.Inheritance.superMethod" },
                { "Inheritance", VARIABLE_TYPE, "a", "Child.method.[0]", "typeResolver.Inheritance.superType" },
                { "Inheritance", METHOD, "superMethod", "Child2.method.[0]", "typeResolver.Child2.superMethod" },
                { "Inheritance", METHOD, "super.superMethod", "Child2.method.[0]", "typeResolver.Inheritance.superMethod" },
                { "Inheritance", METHOD, "this.superMethod", "Child2.method.[0]", "typeResolver.Child2.superMethod" },

                { "Inheritance4", VARIABLE_TYPE, "field", "Inheritance4.method.[0]", "pkgA.pkgB.pkgC.deeplyNested.deeplyNested1.deeplyNested2.deeplyNested3" },
        });
    }

    private final String className;
    private final TypeResolver.SymbolType symbolType;
    private final String symbol;
    private final String scopeName;
    private final String expectation;

    public TypeResolverTest(String className, TypeResolver.SymbolType symbolType, String symbol, String scopeName, String expectation) {
        this.className = className;
        this.symbolType = symbolType;
        this.symbol = symbol;
        this.scopeName = scopeName;
        this.expectation = expectation;
    }

    @Before
    public void setupTestProject() throws Exception {
        root = TestUtils.getTestProjectRoot();

        fileAccessor = new FileAccessor(root);
        parsedClassProvider = new ParsedClassProvider(fileAccessor, new FullyQualifiedImportResolver(fileAccessor), new ASTLoader());
    }

    @Test
    public void simpleTest() throws Exception {
        ScopePath context = ScopePath.append(typeResolverPkg, className);
        TypeResolver typeResolver = new TypeResolver(parsedClassProvider);

        ScopePath scope = ScopePath.append(typeResolverPkg, scopeName);
        CompilationUnit ast = parsedClassProvider.get(context).getAst();
        Node scopeNode = ScopeUtils.locateNode(ast, new ScopePath(scopeName));
        Node node = scopeNode.getChildrenNodes().get(0);

        ScopePath fullyQualifiedName = typeResolver.resolve(parsedClassProvider.get(context).getImportResolver(), symbolType, scope, node, new ScopePath(symbol));

        TestUtils.assertNoMessages();
        Assert.assertEquals(fullyQualifiedName, new ScopePath(expectation));
    }
 }
