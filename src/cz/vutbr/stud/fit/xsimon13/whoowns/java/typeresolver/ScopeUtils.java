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

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import cz.vutbr.stud.fit.xsimon13.whoowns.Utils;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.visitors.DeclarationVisitor;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.visitors.Tour;
import javafx.util.Pair;

import java.util.List;

public class ScopeUtils {
    public static final String INITIALIZER_NAME = "initializer";

    /**
     * Locates the scope inside the node. Scope is relative path starting from node.
     * If the part name is is in the form [N] where N is the order of enclosed blocks (without recursion).
     */
    public static Node locateNode(Node node, ScopePath path) throws ImportResolver.UnknownImport {
        ScopePath currentPath = new ScopePath("");
        List<String> parts = path.getParts();

        int i = 0;
        while (!currentPath.equals(path)) {
            String name = parts.get(i);

            Integer blockId = getBlockId(name);
            if (blockId != null)
                node = findBlock(node, blockId);
            else
                node = Utils.find(node.getChildrenNodes(), new ScopeMatcher<Node>(name));

            if (node == null)
                throw new ImportResolver.UnknownImport(path, "Can't locate AST node inside the file. Stuck with " + currentPath);

            currentPath.add(name);
            i++;
        }

        return node;
    }

    /**
     * Returns Id of the block from extracted from the part of a scope or null if the part is not a block id.
     */
    public static Integer getBlockId(String part) {
        if (part.matches("^\\[[0-9]+\\]$"))
            return Integer.parseInt(part.substring(1, part.length() - 1));
        else
            return null;
    }

    public static ScopePath getScopeInsideNode(ScopePath scope, Node node) throws ParseException {
        String innerScope = null;

        if (node instanceof BlockStmt)
            innerScope = "[" + getBlockOrder((BlockStmt) node).toString() + "]";
        else if (node instanceof MethodDeclaration)
            innerScope = ((MethodDeclaration) node).getName();
        else if (node instanceof ConstructorDeclaration)
            innerScope = ((ConstructorDeclaration) node).getName();
        else if (node instanceof ClassOrInterfaceDeclaration)
            innerScope = ((ClassOrInterfaceDeclaration) node).getName();
        else if (node instanceof EnumDeclaration)
            innerScope = ((EnumDeclaration) node).getName();
        else if (node instanceof InitializerDeclaration)
            innerScope = INITIALIZER_NAME;

        if (innerScope == null)
            return scope;
        else
            return ScopePath.append(scope, innerScope);

    }

    public static Pair<ScopePath, ClassOrInterfaceDeclaration> getEnclosingType(ScopePath path, Node node) {
        while (node != null) {
            if (node instanceof ClassOrInterfaceDeclaration)
                return new Pair<ScopePath, ClassOrInterfaceDeclaration>(path, (ClassOrInterfaceDeclaration) node);
            if (enclosesScope(node))
                path = path.getQualifier();
            node = node.getParentNode();
        }
        return null;
    }

    public static boolean enclosesScope(Node node) {
        return     node instanceof MethodDeclaration
                || node instanceof ConstructorDeclaration
                || node instanceof ClassOrInterfaceDeclaration
                || node instanceof BlockStmt
                || node instanceof EnumDeclaration
                || node instanceof InitializerDeclaration;
    }

    private static class ScopeMatcher<T extends Node> implements Utils.Matcher<T> {
        String name;
        public ScopeMatcher(String name) {
            this.name = name;
        }

        @Override
        public boolean match(T item) {
            return (
                    (item instanceof TypeDeclaration && name.equals(((TypeDeclaration)item).getName())) ||
                    (item instanceof ClassOrInterfaceDeclaration && name.equals(((ClassOrInterfaceDeclaration)item).getName())) ||
                    (item instanceof MethodDeclaration && name.equals(((MethodDeclaration)item).getName())) ||
                    (item instanceof ConstructorDeclaration && name.equals(((ConstructorDeclaration)item).getName())) ||
                    (item instanceof EnumDeclaration && name.equals(((EnumDeclaration)item).getName())) ||
                    (item instanceof InitializerDeclaration && name.equals(INITIALIZER_NAME))
            );
        }
    }

    private static BlockStmt findBlock(Node node, int n) {
        return Tour.visitDeclarationsInScope(null, node, new CountingDeclarationVisitor(n));
    }

    private static class CountingDeclarationVisitor extends DeclarationVisitor<BlockStmt> {
        private int n;
        public CountingDeclarationVisitor(int n) {
            this.n = n;
        }

        @Override
        public BlockStmt visitBlock(ScopePath scope, BlockStmt block) {
            if (n == 0)
                return block;
            else {
                n--;
                return null;
            }
        }
    }

    private static Integer getBlockOrder(BlockStmt block) throws ParseException {
        Node parent = block.getParentNode();
        while (parent != null && !enclosesScope(parent))
            parent = parent.getParentNode();
        if (parent == null)
            throw new ParseException("Can't find enclosing scope");

        Integer n = Tour.visitDeclarationsInScope(null, parent, new BlockFindingDeclarationVisitor(block));
        if (n == null)
            throw new ParseException("Can't find block inside its own parent");

        return n;
    }

    private static class BlockFindingDeclarationVisitor extends DeclarationVisitor<Integer> {
        private BlockStmt block;
        private Integer n = 0;
        public BlockFindingDeclarationVisitor(BlockStmt block) {
            this.block = block;
        }

        @Override
        public Integer visitBlock(ScopePath scope, BlockStmt block) {
            if (block == this.block)
                return n;
            else {
                n++;
                return null;
            }
        }
    }
}
