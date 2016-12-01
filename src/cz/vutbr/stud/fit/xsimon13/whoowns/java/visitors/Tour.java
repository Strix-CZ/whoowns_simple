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

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import cz.vutbr.stud.fit.xsimon13.whoowns.MessageBoard;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopeUtils;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.ChangedLines;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

public class Tour {
    /**
     * Visits declarations enclosed in a scope. It doesn't go inside nested scopes.
     */
    public static <T> T visitDeclarationsInScope(ScopePath scope, Node node, DeclarationVisitor<T> visitor) {
        T result = null;
        for (Node child : node.getChildrenNodes()) {
            boolean recurse = true;

            if (child instanceof VariableDeclarationExpr) {
                result = visitor.visitVariableDeclaration(scope, (VariableDeclarationExpr) child);
                recurse = true; // InitializerDeclaration with a block can be enclosed (see ScopeUtilsTest.scopeInsideNodeTest)
            }
            else if (child instanceof FieldDeclaration) {
                result = visitor.visitFieldDeclaration(scope, (FieldDeclaration) child);
                recurse = true; // InitializerDeclaration with a block can be enclosed (see ScopeUtilsTest.scopeInsideNodeTest)
            }
            else if (child instanceof MethodDeclaration) {
                result = visitor.visitMethodDeclaration(scope, (MethodDeclaration) child);
                recurse = false;
            }
            else if (child instanceof ClassOrInterfaceDeclaration) {
                result = visitor.visitTypeDeclaration(scope, (ClassOrInterfaceDeclaration) child);
                recurse = false;
            }
            else if (child instanceof BlockStmt) {
                result = visitor.visitBlock(scope, (BlockStmt) child);
                recurse = false;
            }

            if (result != null)
                return result;

            if (recurse)
                result = visitDeclarationsInScope(scope, child, visitor);

            if (result != null)
                return result;
        }

        return result;
    }


    private static class ChangedLinesPlaceToVisit {
        public Node node;
        public ScopePath scope;
        public boolean enclosesScope;

        public ChangedLinesPlaceToVisit(Node node, ScopePath scope, boolean enclosesScope) {
            this.node = node;
            this.scope = scope;
            this.enclosesScope = enclosesScope;
        }
    }

    public static void visitChangedLines(ScopePath startingScope, Node startingNode, ChangedLines changedLines, VoidVisitorWithScope<ScopePath> visitor) {
        Queue<ChangedLinesPlaceToVisit> places = new ArrayDeque<ChangedLinesPlaceToVisit>();

        if (changedLines.containsChangedLines(startingNode.getBeginLine(), startingNode.getEndLine()))
            places.add(new ChangedLinesPlaceToVisit(startingNode, startingScope, false));

        while (!places.isEmpty()) {
            ChangedLinesPlaceToVisit place = places.poll();
            Node node = place.node;
            ScopePath scope = place.scope;

            if (place.enclosesScope)
                visitor.visit(node, scope, scope);

            // If the node has a child, then the first line has to be modified in order to visit it.
            // If the children doesn't have any children, it is enough if it is modified anywhere inside.
            // This still isn't ideal - The best possible approach is to visit the node, if the first line is changed
            // or if there is a non-empty changed line enclosed in the node, that is not enclosed in any child.
            boolean hasChildren = node.getChildrenNodes() != null && node.getChildrenNodes().size() > 0;
            if (hasChildren && changedLines.isChanged(node.getBeginLine()))
                node.accept(visitor, scope);
            else if (!hasChildren && changedLines.containsChangedLines(node.getBeginLine(), node.getEndLine()))
                node.accept(visitor, scope);

            Comment comment = node.getComment();
            if (comment != null && changedLines.containsChangedLines(comment.getBeginLine(), comment.getEndLine()))
                comment.accept(visitor, scope);

            if (hasChildren) {
                for (Node child : node.getChildrenNodes()) {
                    if (!changedLines.containsChangedLines(child.getBeginLine(), child.getEndLine()))
                    {
                        Comment childComment = child.getComment();
                        if (childComment != null && changedLines.containsChangedLines(childComment.getBeginLine(), childComment.getEndLine()))
                            childComment.accept(visitor, scope);

                        continue;
                    }

                    if (ScopeUtils.enclosesScope(child)) {
                        try {
                            places.add(new ChangedLinesPlaceToVisit(child, ScopeUtils.getScopeInsideNode(scope, child), true));
                        }
                        catch (ParseException e) {
                            MessageBoard.getInstance().sendRecoverableError("Can't resolve scope in " + scope.toString() + " of child: " + child.toString(), e);
                        }
                    }
                    else
                        places.add(new ChangedLinesPlaceToVisit(child, scope, false));

                }
            }

        }
    }
}
