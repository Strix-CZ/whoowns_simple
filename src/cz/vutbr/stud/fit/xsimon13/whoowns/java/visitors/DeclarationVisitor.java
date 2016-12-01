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

package cz.vutbr.stud.fit.xsimon13.whoowns.java.visitors;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;

public class DeclarationVisitor<T> {
    public T visitVariableDeclaration(ScopePath scope, VariableDeclarationExpr var) {
        return null;
    }

    public T visitFieldDeclaration(ScopePath scope, FieldDeclaration field) {
        return null;
    }

    public T visitMethodDeclaration(ScopePath scope, MethodDeclaration method) {
        return null;
    }

    public T visitTypeDeclaration(ScopePath scope, ClassOrInterfaceDeclaration type) {
        return null;
    }

    public T visitBlock(ScopePath scope, BlockStmt block) {
        return null;
    }
}
