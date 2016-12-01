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

import com.github.javaparser.ParseException;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import cz.vutbr.stud.fit.xsimon13.whoowns.Analyzer;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Person;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Team;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.TeamAssignment;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.visitors.LazyVoidVisitor;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.visitors.Tour;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.ChangedLines;

import java.io.IOException;
import java.util.Date;

/**
 * Analyzes changed lines in a Java code.
 */
public class JavaAnalyzer implements Analyzer {

    private ParsedClassProvider classProvider = null;
    private OwnershipStatistics authorStatistics;
    private OwnershipStatistics teamStatistics;
    private TeamAssignment teamAssignment;
    private Date time;
    private Person author;
    private Team team;
    private boolean ignore;

    public JavaAnalyzer(OwnershipStatistics authorStatistics, OwnershipStatistics teamStatistics, TeamAssignment teamAssignment) {
        this.authorStatistics = authorStatistics;
        this.teamStatistics = teamStatistics;
        this.teamAssignment = teamAssignment;
    }

    @Override
    public void setChangelistInformation(Date time, Person author, String description, ParsedClassProvider parsedClassProvider) {
        this.time = time;
        this.author = author;
        team = teamAssignment.getTeam(author, time);
        classProvider = parsedClassProvider;

        ignore = teamAssignment.isTeamIgnored(team);
    }

    public void analyze(ScopePath file, final ChangedLines changedLines) throws IOException, ParseException {
        if (ignore)
            return;

        // Go through all changed lines and use author and team statistics to record what has been modified.

        LazyVoidVisitor<ScopePath> visitor = new LazyVoidVisitor<ScopePath>() {

            @Override
            public void visit(Node n, ScopePath scopePath, ScopePath scopePath2) {
                authorStatistics.recordModification(scopePath, author, time)
                        .changedLines(changedLines.countChangedLinesWithin(n.getBeginLine(), n.getEndLine()), false)
                        .commit();
                teamStatistics.recordModification(scopePath, team, time)
                        .changedLines(changedLines.countChangedLinesWithin(n.getBeginLine(), n.getEndLine()), false)
                        .commit();
            }

            @Override
            public void visit(ClassOrInterfaceDeclaration n, ScopePath scope) {
                authorStatistics.recordModification(scope, author, time).definedClass().commit();
                teamStatistics.recordModification(scope, team, time).definedClass().commit();
            }



            @Override
            public void visit(ConstructorDeclaration n, ScopePath scope) {
                authorStatistics.recordModification(scope, author, time).definedMethod().commit();
                teamStatistics.recordModification(scope, team, time).definedMethod().commit();
            }

            @Override
            public void visit(EnumConstantDeclaration n, ScopePath scope) {
                authorStatistics.recordModification(scope, author, time).definedField().commit();
                teamStatistics.recordModification(scope, team, time).definedField().commit();
            }

            @Override
            public void visit(EnumDeclaration n, ScopePath scope) {
                authorStatistics.recordModification(scope, author, time).definedClass().commit();
                teamStatistics.recordModification(scope, team, time).definedClass().commit();
            }

            @Override
            public void visit(FieldDeclaration n, ScopePath scope) {
                authorStatistics.recordModification(scope, author, time).definedField().commit();
                teamStatistics.recordModification(scope, team, time).definedField().commit();
            }

            @Override
            public void visit(MethodDeclaration n, ScopePath scope) {
                authorStatistics.recordModification(scope, author, time).definedMethod().commit();
                teamStatistics.recordModification(scope, team, time).definedMethod().commit();
            }

            @Override
            public void visit(VariableDeclarationExpr variableDeclarationExpr, ScopePath scope) {
                authorStatistics.recordModification(scope, author, time).definedVariable().commit();
                teamStatistics.recordModification(scope, team, time).definedVariable().commit();
            }
        };

        CompilationUnit cu = classProvider.get(file).getAst();
        ScopePath startingScopePath = file.getQualifier();

        authorStatistics.recordModification(startingScopePath, author, time)
                .changedLines(changedLines.countChangedLinesWithin(cu.getBeginLine(), cu.getEndLine()), true)
                .commit();
        teamStatistics.recordModification(startingScopePath, team, time)
                .changedLines(changedLines.countChangedLinesWithin(cu.getBeginLine(), cu.getEndLine()), true)
                .commit();

        Tour.visitChangedLines(startingScopePath, cu, changedLines, visitor);
    }

    @Override
    public void afterChangelist() {
    }
}
