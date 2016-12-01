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

import com.github.javaparser.ast.*;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.*;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;

public class LazyVoidVisitor<T> implements VoidVisitorWithScope<T> {

    @Override
    public void visit(Node n, ScopePath scopePath, T t) {

    }

    @Override
    public void visit(CompilationUnit compilationUnit, T t) {

    }

    @Override
    public void visit(PackageDeclaration packageDeclaration, T t) {

    }

    @Override
    public void visit(ImportDeclaration importDeclaration, T t) {

    }

    @Override
    public void visit(TypeParameter typeParameter, T t) {

    }

    @Override
    public void visit(LineComment lineComment, T t) {

    }

    @Override
    public void visit(BlockComment blockComment, T t) {

    }

    @Override
    public void visit(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, T t) {

    }

    @Override
    public void visit(EnumDeclaration enumDeclaration, T t) {

    }

    @Override
    public void visit(EmptyTypeDeclaration emptyTypeDeclaration, T t) {

    }

    @Override
    public void visit(EnumConstantDeclaration enumConstantDeclaration, T t) {

    }

    @Override
    public void visit(AnnotationDeclaration annotationDeclaration, T t) {

    }

    @Override
    public void visit(AnnotationMemberDeclaration annotationMemberDeclaration, T t) {

    }

    @Override
    public void visit(FieldDeclaration fieldDeclaration, T t) {

    }

    @Override
    public void visit(VariableDeclarator variableDeclarator, T t) {

    }

    @Override
    public void visit(VariableDeclaratorId variableDeclaratorId, T t) {

    }

    @Override
    public void visit(ConstructorDeclaration constructorDeclaration, T t) {

    }

    @Override
    public void visit(MethodDeclaration methodDeclaration, T t) {

    }

    @Override
    public void visit(Parameter parameter, T t) {

    }

    @Override
    public void visit(MultiTypeParameter multiTypeParameter, T t) {

    }

    @Override
    public void visit(EmptyMemberDeclaration emptyMemberDeclaration, T t) {

    }

    @Override
    public void visit(InitializerDeclaration initializerDeclaration, T t) {

    }

    @Override
    public void visit(JavadocComment javadocComment, T t) {

    }

    @Override
    public void visit(ClassOrInterfaceType classOrInterfaceType, T t) {

    }

    @Override
    public void visit(PrimitiveType primitiveType, T t) {

    }

    @Override
    public void visit(ReferenceType referenceType, T t) {

    }

    @Override
    public void visit(VoidType voidType, T t) {

    }

    @Override
    public void visit(WildcardType wildcardType, T t) {

    }

    @Override
    public void visit(ArrayAccessExpr arrayAccessExpr, T t) {

    }

    @Override
    public void visit(ArrayCreationExpr arrayCreationExpr, T t) {

    }

    @Override
    public void visit(ArrayInitializerExpr arrayInitializerExpr, T t) {

    }

    @Override
    public void visit(AssignExpr assignExpr, T t) {

    }

    @Override
    public void visit(BinaryExpr binaryExpr, T t) {

    }

    @Override
    public void visit(CastExpr castExpr, T t) {

    }

    @Override
    public void visit(ClassExpr classExpr, T t) {

    }

    @Override
    public void visit(ConditionalExpr conditionalExpr, T t) {

    }

    @Override
    public void visit(EnclosedExpr enclosedExpr, T t) {

    }

    @Override
    public void visit(FieldAccessExpr fieldAccessExpr, T t) {

    }

    @Override
    public void visit(InstanceOfExpr instanceOfExpr, T t) {

    }

    @Override
    public void visit(StringLiteralExpr stringLiteralExpr, T t) {

    }

    @Override
    public void visit(IntegerLiteralExpr integerLiteralExpr, T t) {

    }

    @Override
    public void visit(LongLiteralExpr longLiteralExpr, T t) {

    }

    @Override
    public void visit(IntegerLiteralMinValueExpr integerLiteralMinValueExpr, T t) {

    }

    @Override
    public void visit(LongLiteralMinValueExpr longLiteralMinValueExpr, T t) {

    }

    @Override
    public void visit(CharLiteralExpr charLiteralExpr, T t) {

    }

    @Override
    public void visit(DoubleLiteralExpr doubleLiteralExpr, T t) {

    }

    @Override
    public void visit(BooleanLiteralExpr booleanLiteralExpr, T t) {

    }

    @Override
    public void visit(NullLiteralExpr nullLiteralExpr, T t) {

    }

    @Override
    public void visit(MethodCallExpr methodCallExpr, T t) {

    }

    @Override
    public void visit(NameExpr nameExpr, T t) {

    }

    @Override
    public void visit(ObjectCreationExpr objectCreationExpr, T t) {

    }

    @Override
    public void visit(QualifiedNameExpr qualifiedNameExpr, T t) {

    }

    @Override
    public void visit(ThisExpr thisExpr, T t) {

    }

    @Override
    public void visit(SuperExpr superExpr, T t) {

    }

    @Override
    public void visit(UnaryExpr unaryExpr, T t) {

    }

    @Override
    public void visit(VariableDeclarationExpr variableDeclarationExpr, T t) {

    }

    @Override
    public void visit(MarkerAnnotationExpr markerAnnotationExpr, T t) {

    }

    @Override
    public void visit(SingleMemberAnnotationExpr singleMemberAnnotationExpr, T t) {

    }

    @Override
    public void visit(NormalAnnotationExpr normalAnnotationExpr, T t) {

    }

    @Override
    public void visit(MemberValuePair memberValuePair, T t) {

    }

    @Override
    public void visit(ExplicitConstructorInvocationStmt explicitConstructorInvocationStmt, T t) {

    }

    @Override
    public void visit(TypeDeclarationStmt typeDeclarationStmt, T t) {

    }

    @Override
    public void visit(AssertStmt assertStmt, T t) {

    }

    @Override
    public void visit(BlockStmt blockStmt, T t) {

    }

    @Override
    public void visit(LabeledStmt labeledStmt, T t) {

    }

    @Override
    public void visit(EmptyStmt emptyStmt, T t) {

    }

    @Override
    public void visit(ExpressionStmt expressionStmt, T t) {

    }

    @Override
    public void visit(SwitchStmt switchStmt, T t) {

    }

    @Override
    public void visit(SwitchEntryStmt switchEntryStmt, T t) {

    }

    @Override
    public void visit(BreakStmt breakStmt, T t) {

    }

    @Override
    public void visit(ReturnStmt returnStmt, T t) {

    }

    @Override
    public void visit(IfStmt ifStmt, T t) {

    }

    @Override
    public void visit(WhileStmt whileStmt, T t) {

    }

    @Override
    public void visit(ContinueStmt continueStmt, T t) {

    }

    @Override
    public void visit(DoStmt doStmt, T t) {

    }

    @Override
    public void visit(ForeachStmt foreachStmt, T t) {

    }

    @Override
    public void visit(ForStmt forStmt, T t) {

    }

    @Override
    public void visit(ThrowStmt throwStmt, T t) {

    }

    @Override
    public void visit(SynchronizedStmt synchronizedStmt, T t) {

    }

    @Override
    public void visit(TryStmt tryStmt, T t) {

    }

    @Override
    public void visit(CatchClause catchClause, T t) {

    }

    @Override
    public void visit(LambdaExpr lambdaExpr, T t) {

    }

    @Override
    public void visit(MethodReferenceExpr methodReferenceExpr, T t) {

    }

    @Override
    public void visit(TypeExpr typeExpr, T t) {

    }
}
