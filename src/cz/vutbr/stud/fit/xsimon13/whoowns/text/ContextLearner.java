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

package cz.vutbr.stud.fit.xsimon13.whoowns.text;

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import cz.vutbr.stud.fit.xsimon13.whoowns.MessageBoard;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.visitors.LazyVoidVisitor;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.filesystem.FileAccessor;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopeUtils;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

/**
 * Learn context (corpus) of a project by going through all the texts in it.
 */
public class ContextLearner {

    private FileAccessor fileAccessor;
    private ParsedClassProvider classProvider;
    private WordStatistics wordStatistics;

    public ContextLearner(FileAccessor fileAccessor, ParsedClassProvider classProvider, WordStatistics wordStatistics) {
        this.fileAccessor = fileAccessor;
        this.classProvider = classProvider;
        this.wordStatistics = wordStatistics;
    }

    public void learn() throws IOException {
        MyFileVisitor visitor = new MyFileVisitor();
        fileAccessor.visitJavaFiles(visitor);
    }

    private class MyFileVisitor implements FileAccessor.FileVisitor {
        @Override
        public void visit(ScopePath file) {

            EverythingVisitor visitor = new EverythingVisitor();

            try {
                visitEverything(file.getQualifier(), classProvider.get(file).getAst(), visitor);
            } catch (ParseException e) {
                MessageBoard.getInstance().sendRecoverableError("Can't learn texts from file", e);
            } catch (IOException e) {
                MessageBoard.getInstance().sendRecoverableError("Can't learn texts from file", e);
            }

        }
    }

    private class EverythingVisitor extends LazyVoidVisitor<ScopePath> {
        @Override
        public void visit(BlockComment n, ScopePath scope) {
            wordStatistics.analyzeDocument(n.getContent());
        }

        @Override
        public void visit(StringLiteralExpr n, ScopePath scope) {
            wordStatistics.analyzeDocument(n.getValue());
        }

        @Override
        public void visit(JavadocComment n, ScopePath scope) {
            wordStatistics.analyzeDocument(n.getContent());
        }

        public void visit(Comment n, ScopePath scope) {
            wordStatistics.analyzeDocument(n.getContent());
        }

        @Override
        public void visit(LineComment n, ScopePath scope) {
            wordStatistics.analyzeDocument(n.getContent());
        }

        @Override
        public void visit(NameExpr n, ScopePath scope) {
            wordStatistics.analyzeDocument(n.toString());
        }

        @Override
        public void visit(QualifiedNameExpr n, ScopePath scope) {
            wordStatistics.analyzeDocument(n.toString());
        }

        @Override
        public void visit(VariableDeclaratorId variableDeclaratorId, ScopePath scopePath) {
            wordStatistics.analyzeDocument(variableDeclaratorId.getName());
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, ScopePath scopePath) {
            wordStatistics.analyzeDocument(classOrInterfaceDeclaration.getName());
        }

        @Override
        public void visit(MethodDeclaration methodDeclaration, ScopePath scopePath) {
            wordStatistics.analyzeDocument(methodDeclaration.getName());
        }
    }

    private void visitEverything(ScopePath startingScope, Node startingNode, EverythingVisitor visitor) {
        Queue<Node> nodes = new ArrayDeque<Node>();
        Queue<ScopePath> scopes = new ArrayDeque<ScopePath>();
        nodes.add(startingNode);
        scopes.add(startingScope);

        while (!nodes.isEmpty()) {
            Node node = nodes.poll();
            ScopePath scope = scopes.poll();

            node.accept(visitor, scope);

            // Visit comment associated with the node - this type of comment is not part of the children tree
            Comment comment = node.getComment();
            if (comment != null)
                visitor.visit(comment, null);

            List<Node> children = node.getChildrenNodes();
            if (children != null) {
                for (Node child : children) {
                    try {
                        scopes.add(ScopeUtils.getScopeInsideNode(scope, child));
                        nodes.add(child);
                    }
                    catch (ParseException e) {
                        MessageBoard.getInstance().sendRecoverableError("Can't resolve scope in " + scope.toString() + " of child: " + child.toString() , e);
                    }
                }
            }

        }
    }


}
