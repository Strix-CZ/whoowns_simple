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
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclaratorId;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.QualifiedNameExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import cz.vutbr.stud.fit.xsimon13.whoowns.Analyzer;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Person;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Team;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.TeamAssignment;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.visitors.LazyVoidVisitor;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.visitors.Tour;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.ChangedLines;

import java.io.IOException;
import java.util.Date;

/**
 * Analyzes texts in the CL.
 */
public class TextAnalyzer implements Analyzer {

    private ParsedClassProvider classProvider = null;
    private WordStatistics wordStatistics;
    private String category = null;
    private TeamAssignment teamAssignment;
    private boolean ignore;
    private Date time;

    public TextAnalyzer(WordStatistics wordStatistics, TeamAssignment teamAssignment) {
        this.wordStatistics = wordStatistics;
        this.teamAssignment = teamAssignment;
    }

    @Override
    public void setChangelistInformation(Date time, Person author, String description, ParsedClassProvider parsedClassProvider) {
        Team team = teamAssignment.getTeam(author, time);
        ignore = teamAssignment.isTeamIgnored(team);

        this.time = time;
        category = team.toEntityId();
        classProvider = parsedClassProvider;

        wordStatistics.analyzeCategorizedDocument(description, category, time);
    }

    @Override
    public void analyze(final ScopePath file, ChangedLines changedLines) throws IOException, ParseException {
        if (ignore)
            return;

        CompilationUnit cu = classProvider.get(file).getAst();
        ScopePath startingScopePath = file.getQualifier();

        MyVisitor visitor = new MyVisitor(changedLines);

        Tour.visitChangedLines(startingScopePath, cu, changedLines, visitor);
    }

    @Override
    public void afterChangelist() {
        wordStatistics.save();
    }

    private class MyVisitor extends LazyVoidVisitor<ScopePath> {

        private ChangedLines changedLines;

        private MyVisitor(ChangedLines changedLines) {
            this.changedLines = changedLines;
        }

        @Override
        public void visit(BlockComment n, ScopePath scope) {
            analyzeChangedLines(n.getContent(), n.getBeginLine());
        }

        @Override
        public void visit(JavadocComment n, ScopePath scope) {
            analyzeChangedLines(n.getContent(), n.getBeginLine());
        }

        @Override
        public void visit(LineComment n, ScopePath scope) {
            wordStatistics.analyzeCategorizedDocument(n.getContent(), category, time);
        }

        @Override
        public void visit(StringLiteralExpr n, ScopePath scope) {
            wordStatistics.analyzeCategorizedDocument(n.getValue(), category, time);
        }

        @Override
        public void visit(NameExpr n, ScopePath scope) {
            wordStatistics.analyzeCategorizedDocument(n.toString(), category, time);
        }

        @Override
        public void visit(QualifiedNameExpr n, ScopePath scope) {
            wordStatistics.analyzeCategorizedDocument(n.toString(), category, time);
        }

        @Override
        public void visit(VariableDeclaratorId variableDeclaratorId, ScopePath scopePath) {
            wordStatistics.analyzeCategorizedDocument(variableDeclaratorId.getName(), category, time);
        }

        @Override
        public void visit(ClassOrInterfaceDeclaration classOrInterfaceDeclaration, ScopePath scopePath) {
            wordStatistics.analyzeCategorizedDocument(classOrInterfaceDeclaration.getName(), category, time);
        }

        @Override
        public void visit(EnumDeclaration enumDeclaration, ScopePath scopePath) {
            wordStatistics.analyzeCategorizedDocument(enumDeclaration.getName(), category, time);
        }

        @Override
        public void visit(MethodDeclaration methodDeclaration, ScopePath scopePath) {
            wordStatistics.analyzeCategorizedDocument(methodDeclaration.getName(), category, time);
        }

        private void analyzeChangedLines(String document, int beginLine) {
            StringBuilder filteredDocument = new StringBuilder();

            String[] lines = document.split("\r\n|\n");
            for (int i = 0; i < lines.length; ++i) {
                if (changedLines.isChanged(beginLine + i))
                    filteredDocument.append(lines[i]);
            }

            wordStatistics.analyzeCategorizedDocument(filteredDocument.toString(), category, time);
        }
    }
}
