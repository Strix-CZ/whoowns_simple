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
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.ReferenceType;
import com.github.javaparser.ast.type.Type;
import cz.vutbr.stud.fit.xsimon13.whoowns.MessageBoard;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.visitors.DeclarationVisitor;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.visitors.Tour;
import javafx.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TypeResolver {

    ParsedClassProvider parsedClassProvider;

    public TypeResolver(ParsedClassProvider parsedClassProvider) {
        this.parsedClassProvider = parsedClassProvider;
    }

    public static enum SymbolType {
        VARIABLE_TYPE,     // Resolves name of a variable to a fully qualified type of the variable. First finds the variable declaration, then it's type
        METHOD,            // Resolves name of a method to be fully qualified
        CLASS_OR_INTERFACE // Resolves name of a class/interface to be fully qualified
    }

    /**
     * Resolves a name to it's fully qualified form. For variables the result is fully qualified name of its type.
     * Built-in types (int, boolean) and java.lang.* types are not resolved and null is returned.
     *
     * @param importResolver - Import resolver for the current file.
     * @param symbolType     - Type of the resolving - type of variable / method / class&interface
     * @param currentScope   - Current scope
     * @param node           - Current node in AST
     * @param name           - Name being resolved
     * @return Fully qualified name or null if the symbol could not be resolved.
     */
    public ScopePath resolve(ImportResolver importResolver, SymbolType symbolType, ScopePath currentScope, Node node, final ScopePath name) {
        if (name.getQualifier().toString().equals("super")) {
            // Resolve in superclass
            Pair<ScopePath, ClassOrInterfaceDeclaration> enclosingClass = ScopeUtils.getEnclosingType(currentScope, node);
            if (enclosingClass == null) {
                MessageBoard.getInstance().sendRecoverableError("No enclosing class/interface found.", new UnknownName((name)));
                return null;
            }
            List<ClassOrInterfaceType> superClasses = enclosingClass.getValue().getExtends();
            if (superClasses.size() == 0) {
                MessageBoard.getInstance().sendRecoverableError("The enclosing class/interface doesn't inherit anything.", new UnknownName((name)));
                return null;
            }
            if (superClasses.size() > 1) {
                MessageBoard.getInstance().sendRecoverableError("Can't use 'super' in interface inheritance.", new UnknownName((name)));
                return null;
            }

            ScopePath superPath = resolve(importResolver, SymbolType.CLASS_OR_INTERFACE, enclosingClass.getKey(), enclosingClass.getValue(), new ScopePath(superClasses.get(0).toString()));
            return resolveInDifferentScope(importResolver, symbolType, superPath, name.getName());
        }

        if (name.getQualifier().toString().equals("this")) {
            // Resolve in enclosing class
            Pair<ScopePath, ClassOrInterfaceDeclaration> enclosingClass = ScopeUtils.getEnclosingType(currentScope, node);
            if (enclosingClass == null) {
                MessageBoard.getInstance().sendRecoverableError("No enclosing class/interface found.", new UnknownName((name)));
                return null;
            }
            return resolve(importResolver, symbolType, enclosingClass.getKey(), enclosingClass.getValue(), name.withoutFirst());
        }

        if (name.hasQualifier()) {
            // Try to resolve the scope as a name in current scope.
            ScopePath innerScopePath = resolve(importResolver, SymbolType.CLASS_OR_INTERFACE, currentScope, node, name.firstPart());
            if (innerScopePath != null)
                return resolveInDifferentScope(importResolver, symbolType, innerScopePath, name.withoutFirst());
            else {
                // Resolve it normally - it is probably fully qualified name
            }
        }

        ScopePath result = null;
        if (symbolType == SymbolType.CLASS_OR_INTERFACE)
            result = resolveType(importResolver, currentScope, node, name);
        else if (symbolType == SymbolType.METHOD)
            result = resolveMethod(importResolver, currentScope, node, name);
        else if (symbolType == SymbolType.VARIABLE_TYPE) {
            // Find the place, where the variable is declared
            QualifiedVariableDeclaration variableDeclaration = resolveVariableDeclaration(importResolver, currentScope, node, name);

            // If the variable type is a class then resolve it to a fully qualified name.
            if (variableDeclaration != null) {
                if (variableDeclaration.type instanceof ClassOrInterfaceType || variableDeclaration.type instanceof ReferenceType) {
                    return resolve(
                            getImportResolverForScope(importResolver, variableDeclaration.currentScope), // ImportResolver for the new file
                            SymbolType.CLASS_OR_INTERFACE,                                               // We want the type of the variable
                            variableDeclaration.currentScope,                                            // Scope where the variable is declared
                            variableDeclaration.node,                                                    // Node where the variable is declared
                            new ScopePath(variableDeclaration.type.toString())                           // Type being resolved
                    );
                }
                else {
                    return null;
                }
            }
        }
        else
            throw new RuntimeException("Unknown SymbolType");

        if (result == null) {
            try {
                result = importResolver.resolveName(name); // Symbol was not found in the scope - try imports
            }
            catch (ImportResolver.UnknownImport e) {
                // We are just returning null on failure
            }
        }

        if (result != null && symbolType == SymbolType.VARIABLE_TYPE) {
            // We have imported a field - we have to resolve it's type
            ScopePath scope = result.getQualifier();
            result = resolveInDifferentScope(importResolver, SymbolType.VARIABLE_TYPE, scope, result.getName());
        }

        return result;
    }

    private ScopePath resolveInDifferentScope(ImportResolver oldImportResolver, SymbolType symbolType, ScopePath newScope, final ScopePath name) {
        try {
            ImportResolver newImportResolver = getImportResolverForScope(oldImportResolver, newScope);
            Node node = oldImportResolver.resolveNameToAST(newScope);
            return resolve(newImportResolver, symbolType, newScope, node, name);
        }
        catch (ImportResolver.UnknownImport e) {
            MessageBoard.getInstance().sendRecoverableError(e);
        }
        catch (ParseException e) {
            MessageBoard.getInstance().sendRecoverableError(e);
        }
        catch (IOException e) {
            MessageBoard.getInstance().sendRecoverableError(e);
        }

        return null;
    }

    private ImportResolver getImportResolverForScope(ImportResolver oldImportResolver, ScopePath newScope) {
        try {
            ScopePath fileName = oldImportResolver.getFileNameFromScope(newScope);
            return parsedClassProvider.get(fileName).getImportResolver();
        }
        catch (ImportResolver.UnknownImport e) {
            MessageBoard.getInstance().sendRecoverableError(e);
        }
        catch (IOException e) {
            MessageBoard.getInstance().sendRecoverableError(e);
        }
        catch (ParseException e) {
            MessageBoard.getInstance().sendRecoverableError(e);
        }

        return null;
    }

    private class QualifiedVariableDeclaration {
        /**
         * We have identified the place, where the variable is defined.
         */
        public QualifiedVariableDeclaration(ScopePath currentScope, Type type, Node node) {
            this.currentScope = currentScope;
            this.type = type;
            this.node = node;
        }

        public ScopePath currentScope = null;
        public Type type = null;
        public Node node = null;
    }


    private ScopePath resolveType(ImportResolver importResolver, ScopePath currentScope, Node node, final ScopePath name) {

        final DeclarationVisitor<ScopePath> declarationVisitor = new DeclarationVisitor<ScopePath>() {
            @Override
            public ScopePath visitTypeDeclaration(ScopePath scope, ClassOrInterfaceDeclaration type) {
                ScopePath matchedName = ScopePath.append(scope, type.getName());
                return name.matchesQualifiedName(matchedName)? matchedName : null;
            }
        };

        return visitParentScopes(importResolver, currentScope, node, new ScopeVisitor<ScopePath>() {
            @Override
            public ScopePath visitClassOrInterface(ScopePath name, ClassOrInterfaceDeclaration type) {
                return Tour.visitDeclarationsInScope(name, type, declarationVisitor);
            }

            @Override
            ScopePath visitCompilationUnit(ScopePath name, CompilationUnit cu) {
                return Tour.visitDeclarationsInScope(name, cu, declarationVisitor);
            }
        });

    }

    private ScopePath resolveMethod(ImportResolver importResolver, ScopePath currentScope, Node node, final ScopePath name) {

        final DeclarationVisitor<ScopePath> declarationVisitor = new DeclarationVisitor<ScopePath>() {
            @Override
            public ScopePath visitMethodDeclaration(ScopePath scope, MethodDeclaration method) {
                ScopePath matchedName = ScopePath.append(scope, method.getName());
                return name.matchesQualifiedName(matchedName) ? matchedName : null;
            }
        };

        return visitParentScopes(importResolver, currentScope, node, new ScopeVisitor<ScopePath>() {
            @Override
            public ScopePath visitClassOrInterface(ScopePath nodeName, ClassOrInterfaceDeclaration type) {
                return Tour.visitDeclarationsInScope(nodeName, type, declarationVisitor);
            }
        });

    }

    private QualifiedVariableDeclaration resolveVariableDeclaration(ImportResolver importResolver, ScopePath currentScope, Node node, final ScopePath name) {

        final DeclarationVisitor<QualifiedVariableDeclaration> declarationVisitor = new DeclarationVisitor<QualifiedVariableDeclaration>() {
            @Override
            public QualifiedVariableDeclaration visitVariableDeclaration(ScopePath scope, VariableDeclarationExpr var) {
                if (search(new ScopePath(""), var.getVars()))
                    return new QualifiedVariableDeclaration(scope, var.getType(), var);
                else
                    return null;
            }

            @Override
            public QualifiedVariableDeclaration visitFieldDeclaration(ScopePath scope, FieldDeclaration field) {
                if (search(scope, field.getVariables()))
                    return new QualifiedVariableDeclaration(scope, field.getType(), field);
                else
                    return null;
            }

            private boolean search(ScopePath scope, List<VariableDeclarator> declarations) {
                for (VariableDeclarator item : declarations) {
                    ScopePath matchedName = ScopePath.append(scope, item.getId().getName());
                    if (name.matchesQualifiedName(matchedName))
                        return true;
                }

                return false;
            }
        };

        return visitParentScopes(importResolver, currentScope, node, new ScopeVisitor<QualifiedVariableDeclaration>() {
            @Override
            public QualifiedVariableDeclaration visitBlock(ScopePath scope, BlockStmt block) {
                if (name.hasQualifier())
                    return null; // Only member variables can be accessed through qualified names
                return Tour.visitDeclarationsInScope(scope, block, declarationVisitor);
            }

            @Override
            public QualifiedVariableDeclaration visitMethod(ScopePath scope, MethodDeclaration method) {
                if (name.hasQualifier())
                    return null; // Only member variables can be accessed through qualified names

                // Variable can be defined as a parameter
                List<Parameter> parameterList = method.getParameters();
                if (parameterList != null) {
                    for (Parameter parameter : parameterList) {
                        if (name.matchesQualifiedName(new ScopePath(parameter.getId().getName())))
                            return new QualifiedVariableDeclaration(scope, parameter.getType(), parameter);
                    }
                }

                return Tour.visitDeclarationsInScope(scope, method.getBody(), declarationVisitor);
            }

            @Override
            public QualifiedVariableDeclaration visitClassOrInterface(ScopePath scope, ClassOrInterfaceDeclaration type) {
                return Tour.visitDeclarationsInScope(scope, type, declarationVisitor);
            }
        });
    }

    private static abstract class ScopeVisitor<T> {
        T visitBlock(ScopePath scope, BlockStmt block) {
            return null;
        }

        T visitMethod(ScopePath name, MethodDeclaration method) {
            return null;
        }

        T visitClassOrInterface(ScopePath name, ClassOrInterfaceDeclaration type) {
            return null;
        }

        T visitCompilationUnit(ScopePath name, CompilationUnit cu) { return null; }
    }

    /**
     * Visits scopes in which node is enclosed including inheritance.
     * The direction of the trip is upward.
     */
    private <T> T visitParentScopes(ImportResolver importResolver, ScopePath currentName, Node node, ScopeVisitor<T> visitor) {
        T result = null;

        while (result == null && node != null && currentName != null) {
            ScopePath nextName = currentName;

            if (node instanceof BlockStmt) {
                result = visitor.visitBlock(currentName, (BlockStmt) node);
                nextName = currentName.getQualifier();
            }
            else if (node instanceof MethodDeclaration) {
                result = visitor.visitMethod(currentName, (MethodDeclaration) node);
                nextName = currentName.getQualifier();
            }
            else if (node instanceof ClassOrInterfaceDeclaration) {
                ClassOrInterfaceDeclaration type = (ClassOrInterfaceDeclaration) node;
                result = visitor.visitClassOrInterface(currentName, type);
                nextName = currentName.getQualifier();

                if (result == null) {
                    // Visit super classes and interfaces
                    List<ClassOrInterfaceType> superTypes = type.getExtends();
                    if (superTypes == null)
                        superTypes = new ArrayList<ClassOrInterfaceType>();

                    List<ClassOrInterfaceType> implementTypes = type.getImplements();
                    if (implementTypes != null)
                        superTypes.addAll(implementTypes);

                    for (ClassOrInterfaceType superType : superTypes) {
                        ScopePath superTypeName = resolve(importResolver, SymbolType.CLASS_OR_INTERFACE, currentName.getQualifier(), node.getParentNode(), new ScopePath(superType.toString()));
                        ImportResolver newImportResolver = getImportResolverForScope(importResolver, superTypeName);

                        try {
                            Node superClassNode = importResolver.resolveNameToAST(superTypeName);

                            result = visitParentScopes(newImportResolver, superTypeName, superClassNode, visitor);

                            if (result != null)
                                return result;
                        }
                        catch (ImportResolver.UnknownImport e) {
                            MessageBoard.getInstance().sendRecoverableError(e);
                        }
                        catch (ParseException e) {
                            MessageBoard.getInstance().sendRecoverableError(e);
                        }
                        catch (IOException e) {
                            MessageBoard.getInstance().sendRecoverableError(e);
                        }
                    }
                }
            }
            else if (node instanceof CompilationUnit) {
                result = visitor.visitCompilationUnit(currentName, (CompilationUnit) node);
                nextName = null;
            }

            node = node.getParentNode();
            currentName = nextName;
        }

        return result;
    }

    private static class UnknownName extends Exception {
        public UnknownName(ScopePath path) {
            this(path, "");
        }

        public UnknownName(ScopePath path, String message) {
            super("Qualified name for " + path + " could not be resolved: " + message);
        }
    }

}
