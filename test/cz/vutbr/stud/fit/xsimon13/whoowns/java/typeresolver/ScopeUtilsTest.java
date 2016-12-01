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
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import cz.vutbr.stud.fit.xsimon13.whoowns.Factory;
import cz.vutbr.stud.fit.xsimon13.whoowns.TestUtils;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import org.junit.Assert;
import org.junit.Test;

public class ScopeUtilsTest {
    @Test
    public void testWithoutBlocks() throws Exception {
        ScopePath s = new ScopePath("a.b.[0].c.[1].[2]");
        Assert.assertEquals(s.withoutTrailingBlocks(), new ScopePath("a.b.[0].c"));
    }

    @Test
    public void scopeInsideNodeTest() throws Exception {
        ParsedClassProvider classProvider = Factory.createParsedClassProvider(TestUtils.getTestProjectRoot());
        CompilationUnit cu = classProvider.get(new ScopePath("scopeutils.SUTest")).getAst();

        // Class declaration in VariableDeclarator
        ScopePath methodScope = new ScopePath("SUTest.method.[0]");
        Node method = ScopeUtils.locateNode(cu, methodScope);
        BlockStmt block = (BlockStmt)method.getChildrenNodes().get(0).getChildrenNodes().get(0).getChildrenNodes().get(1).getChildrenNodes().get(1).getChildrenNodes().get(1).getChildrenNodes().get(0);
        Assert.assertEquals(ScopeUtils.getScopeInsideNode(methodScope, block), ScopePath.append(methodScope, "[0]"));

        // Constructor
        ScopePath constructorScope = new ScopePath("SUTest.SUTest");
        Node constructor = ScopeUtils.locateNode(cu, constructorScope);
        Assert.assertEquals(ScopeUtils.getScopeInsideNode(constructorScope, constructor.getChildrenNodes().get(0)), ScopePath.append(constructorScope, "[0]"));

        // Enum + Initializer inside
        ScopePath initializerScope = new ScopePath("SUTest.testEnum.initializer");
        Node initializer = ScopeUtils.locateNode(cu, initializerScope);
        Assert.assertTrue(initializer instanceof InitializerDeclaration);
        Assert.assertEquals(ScopeUtils.getScopeInsideNode(initializerScope, initializer.getChildrenNodes().get(0)), ScopePath.append(initializerScope, "[0]"));
    }
}
