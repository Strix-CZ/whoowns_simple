package cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.p4;

import com.github.javaparser.ParseException;
import cz.vutbr.stud.fit.xsimon13.whoowns.Analyzer;
import cz.vutbr.stud.fit.xsimon13.whoowns.Factory;
import cz.vutbr.stud.fit.xsimon13.whoowns.TestUtils;
import cz.vutbr.stud.fit.xsimon13.whoowns.Utils;
import cz.vutbr.stud.fit.xsimon13.whoowns.db.DBSet;
import cz.vutbr.stud.fit.xsimon13.whoowns.db.StringDbConverter;
import cz.vutbr.stud.fit.xsimon13.whoowns.hr.Person;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.ChangedLines;
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

import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.ChangelistProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.EditedChangedLines;
import cz.vutbr.stud.fit.xsimon13.whoowns.versioncontrol.LineStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeMap;

public class P4ChangelistProviderTest {

    private String branch;
    private String cl;

    @Test
    public void testSingleCl() throws Exception {
        final ParsedClassProvider parsedClassProvider = Factory.createParsedClassProvider(TestUtils.getTestProjectRoot());

        branch = "branchA";

        DBSet<String> processedCls = new DBSet<>(TestUtils.getTestJedis(), ChangelistProvider.KEY_PROCESSED_CLS, new StringDbConverter());
        processedCls.add("11");
        processedCls.add("12");
        processedCls.add("13");

        P4Runner p4 = new FakeP4Runner();
        cl = "14";

        P4ChangelistProvider clProvider = new P4ChangelistProvider(p4, TestUtils.getTestJedis());

        MyAnalyzer analyzer = new MyAnalyzer();

        clProvider.run(TestUtils.getTestProjectRoot(),
                       parsedClassProvider,
                       branch,
                       new GregorianCalendar(2015, 3, 24, 17, 0, 0).getTime(),
                       new GregorianCalendar(2015, 3, 25, 23, 0, 0).getTime(),
                       analyzer);

        analyzer.assertCalledProperly();
    }

    private class FakeP4Runner extends P4Runner {
        @Override
        public JSONArray executeJson(final String... arguments) throws ChangelistProvider.VersionControlException {
            String command = Utils.join(Arrays.asList(arguments), " ");

            try
            {
                if (command.equals("changes -t -s submitted -l //depot/" + branch + "/...@2015/04/24,@2015/04/25"))
                    return new JSONArray(CHANGELISTS);
                else if (command.equals("sync //depot/" + branch + "/...@" + cl))
                    return new JSONArray(SYNC);
                else if (command.equals("describe " + cl))
                    return new JSONArray(DESCRIBE);
                else
                    throw new RuntimeException("Unexpected command '" + command + "'");
            }
            catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public LineStream execute(List<String> arguments) throws ChangelistProvider.VersionControlException
        {
            String command = Utils.join(arguments, " ");

            if (command.equals("diff2 -dbu0 //depot/branchA/typeResolver/Simple.java#1 //depot/branchA/typeResolver/Simple.java#2"))
                return createLineStream(DIFF);
            else
                throw new RuntimeException("Unexpected command '" + command + "'");
        }

        private LineStream createLineStream(String str) {
            InputStream inputStream = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
            return new LineStream(inputStream);
        }
    }


    private static final String CHANGELISTS =
                "[\n" +
                "  {\n" +
                "    \"status\": \"submitted\", \n" +
                "    \"code\": \"stat\", \n" +
                "    \"changeType\": \"public\", \n" +
                "    \"client\": \"TestRepo\", \n" +
                "    \"user\": \"username\", \n" +
                "    \"time\": \"1429953947\", \n" +
                "    \"path\": \"//depot/branchA/*\", \n" +
                "    \"change\": \"14\", \n" +
                "    \"desc\": \"Changed file to test diff\\n\"\n" +
                "  }, \n" +
                "  {\n" +
                "    \"status\": \"submitted\", \n" +
                "    \"code\": \"stat\", \n" +
                "    \"changeType\": \"public\", \n" +
                "    \"client\": \"TestRepo\", \n" +
                "    \"user\": \"honza\", \n" +
                "    \"time\": \"1429953627\", \n" +
                "    \"path\": \"//depot/branchA/*\", \n" +
                "    \"change\": \"13\", \n" +
                "    \"desc\": \"TypeResolver\\n\"\n" +
                "  }, \n" +
                "  {\n" +
                "    \"status\": \"submitted\", \n" +
                "    \"code\": \"stat\", \n" +
                "    \"changeType\": \"public\", \n" +
                "    \"client\": \"TestRepo\", \n" +
                "    \"user\": \"honza\", \n" +
                "    \"time\": \"1429896150\", \n" +
                "    \"path\": \"//depot/branchA/*\", \n" +
                "    \"change\": \"12\", \n" +
                "    \"desc\": \"Change\\n\"\n" +
                "  }, \n" +
                "  {\n" +
                "    \"status\": \"submitted\", \n" +
                "    \"code\": \"stat\", \n" +
                "    \"changeType\": \"public\", \n" +
                "    \"client\": \"TestRepo\", \n" +
                "    \"user\": \"honza\", \n" +
                "    \"time\": \"1429877630\", \n" +
                "    \"path\": \"//depot/branchA/*\", \n" +
                "    \"change\": \"10\", \n" +
                "    \"desc\": \"A very long changelist description that has several lines.\\nThis is next line.\\nLorem ipsum dolor sit amet, consectetur adipiscing elit. Fusce nunc neque, rhoncus vel fermentum vitae, dignissim sed nisi. Sed sollicitudin, sem a vulputate porta, nisi magna auctor lorem, in hendrerit enim nunc ac urna. Praesent molestie at lacus vitae commodo. Quisque placerat sem at facilisis aliquam. Maecenas a egestas odio, porttitor ullamcorper arcu. Pellentesque porttitor urna quis risus fringilla, ut finibus sem lobortis. Suspendisse rutrum arcu lorem, quis elementum tellus faucibus in. Fusce euismod, nulla in sodales aliquam, felis dui volutpat lacus, at lacinia sem nunc in eros. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Sed vehicula turpis ac lacinia posuere. Donec id velit faucibus, eleifend urna ut, porttitor justo. Pellentesque aliquet leo in odio rhoncus elementum. Nam ut semper libero. \\n\"\n" +
                "  }, \n" +
                "  {\n" +
                "    \"status\": \"submitted\", \n" +
                "    \"code\": \"stat\", \n" +
                "    \"changeType\": \"public\", \n" +
                "    \"client\": \"TestRepo\", \n" +
                "    \"user\": \"honza\", \n" +
                "    \"time\": \"1429877285\", \n" +
                "    \"path\": \"//depot/branchA/*\", \n" +
                "    \"change\": \"8\", \n" +
                "    \"desc\": \"new file\\n\"\n" +
                "  }, \n" +
                "  {\n" +
                "    \"status\": \"submitted\", \n" +
                "    \"code\": \"stat\", \n" +
                "    \"changeType\": \"public\", \n" +
                "    \"client\": \"TestRepo\", \n" +
                "    \"user\": \"honza\", \n" +
                "    \"time\": \"1429876824\", \n" +
                "    \"path\": \"//depot/branchA/*\", \n" +
                "    \"change\": \"7\", \n" +
                "    \"desc\": \"Add: Yet another line\\n\"\n" +
                "  }, \n" +
                "  {\n" +
                "    \"status\": \"submitted\", \n" +
                "    \"code\": \"stat\", \n" +
                "    \"changeType\": \"public\", \n" +
                "    \"client\": \"TestRepo\", \n" +
                "    \"user\": \"honza\", \n" +
                "    \"time\": \"1429876436\", \n" +
                "    \"path\": \"//depot/branchA/*\", \n" +
                "    \"change\": \"4\", \n" +
                "    \"desc\": \"Added a new line in file A\\n\"\n" +
                "  }, \n" +
                "  {\n" +
                "    \"status\": \"submitted\", \n" +
                "    \"code\": \"stat\", \n" +
                "    \"changeType\": \"public\", \n" +
                "    \"client\": \"TestRepo\", \n" +
                "    \"user\": \"honza\", \n" +
                "    \"time\": \"1429876255\", \n" +
                "    \"path\": \"//depot/branchA/*\", \n" +
                "    \"change\": \"1\", \n" +
                "    \"desc\": \"fileA\\n\"\n" +
                "  }\n" +
                "]\n";

    private static final String SYNC =
                "[\n" +
                "  {\n" +
                "    \"code\": \"stat\", \n" +
                "    \"totalFileSize\": \"18234\", \n" +
                "    \"rev\": \"5\", \n" +
                "    \"totalFileCount\": \"2\", \n" +
                "    \"clientFile\": \"C:\\\\programovani\\\\diplomka\\\\testrepo\\\\branchA\\\\fileA.txt\", \n" +
                "    \"fileSize\": \"90\", \n" +
                "    \"action\": \"updated\", \n" +
                "    \"depotFile\": \"//depot/branchA/fileA.txt\", \n" +
                "    \"change\": \"14\"\n" +
                "  }, \n" +
                "  {\n" +
                "    \"code\": \"stat\", \n" +
                "    \"rev\": \"2\", \n" +
                "    \"clientFile\": \"C:\\\\programovani\\\\diplomka\\\\testrepo\\\\branchA\\\\TypeResolver.java\", \n" +
                "    \"fileSize\": \"18144\", \n" +
                "    \"action\": \"added\", \n" +
                "    \"depotFile\": \"//depot/branchA/typeResolver/Simple.java\"\n" +
                "  }\n" +
                "]\n";

    private static final String DESCRIBE = "[\n" +
                "  {\n" +
                "    \"status\": \"submitted\", \n" +
                "    \"code\": \"stat\", \n" +
                "    \"depotFile0\": \"//depot/branchA/typeResolver/Simple.java\", \n" +
                "    \"rev0\": \"2\", \n" +
                "    \"changeType\": \"public\", \n" +
                "    \"action0\": \"edit\", \n" +
                "    \"fileSize0\": \"18144\", \n" +
                "    \"client\": \"TestRepo\", \n" +
                "    \"user\": \"honza\", \n" +
                "    \"time\": \"1429953947\", \n" +
                "    \"path\": \"//depot/branchA/*\", \n" +
                "    \"digest0\": \"1E05C03F235F72F1DEE56C2D6223BEF2\", \n" +
                "    \"type0\": \"text\", \n" +
                "    \"change\": \"14\", \n" +
                "    \"desc\": \"Changed file to test diff\\n\"\n" +
                "  }\n" +
                "]\n";

    private static final String DIFF =
                "==== //depot/branchA/typeResolver/Simple.java#1 (text) - //depot/branchA/typeResolver/Simple.java#2 (text) ==== content\n" +
                "@@ -52,3 +52,6 @@\n" +
                "-        VARIABLE_TYPE,     // Resolves name of a variable to a fully qualified type of the variable. First finds the variable declaration, then it's type\n" +
                "-        METHOD,            // Resolves name of a method to be fully qualified\n" +
                "-        CLASS_OR_INTERFACE // Resolves name of a class/interface to be fully qualified\n" +
                "+        VARIABLE_TYPE,     // blab Resolves name of a variable to a fully qualified type of the variable. First finds the variable declaration, then it's type\n" +
                "+        METHOD,            // blab Resolves name of a method to be fully qualified\n" +
                "+        CLASS_OR_INTERFACE // blab Resolves name of a class/interface to be fully qualified\n" +
                "+        // newline blab\n" +
                "+        // another newline blab\n" +
                "+        // and yet another newline blab\n" +
                "@@ -66,1 +69,1 @@\n" +
                "-     * @return Fully qualified name.\n" +
                "+     * @return Fully qualified name. Small change blab\n" +
                "@@ -69,15 +72,0 @@\n" +
                "-        if (name.getQualifier().toString().equals(\"super\")) {\n" +
                "-            // Resolve in superclass\n" +
                "-            Pair<ScopePath, ClassOrInterfaceDeclaration> enclosingClass = ScopeUtils.getEnclosingType(currentScope, node);\n" +
                "-            if (enclosingClass == null)\n" +
                "-                throw new UnknownName(name, \"No enclosing class/interface found.\");\n" +
                "-            List<ClassOrInterfaceType> superClasses = enclosingClass.getValue().getExtends();\n" +
                "-            if (superClasses.size() == 0)\n" +
                "-                throw new UnknownName(name, \"The enclosing class/interface doesn't inherit anything.\");\n" +
                "-            if (superClasses.size() > 1)\n" +
                "-                throw new UnknownName(name, \"Can't use 'super' in interface inheritance.\");\n" +
                "-\n" +
                "-            ScopePath superPath = resolve(importResolver, SymbolType.CLASS_OR_INTERFACE, enclosingClass.getKey(), enclosingClass.getValue(), new ScopePath(superClasses.get(0).toString()));\n" +
                "-            return resolveInDifferentScope(importResolver, symbolType, superPath, name.getName());\n" +
                "-        }\n" +
                "-\n" +
                "@@ -117,0 +105,3 @@\n" +
                "+                if (variableDeclaration.resultingType != null)\n" +
                "+                    return variableDeclaration.resultingType;\n" +
                "+\n" +
                "@@ -118,1 +109,7 @@\n" +
                "-                    return resolve(importResolver, SymbolType.CLASS_OR_INTERFACE, variableDeclaration.currentScope, variableDeclaration.node, new ScopePath(variableDeclaration.type.toString()));\n" +
                "+                    return resolve(\n" +
                "+                            getImportResolverForScope(importResolver, variableDeclaration.currentScope), // ImportResolver for the new file\n" +
                "+                            SymbolType.CLASS_OR_INTERFACE,                                               // We want the type of the variable\n" +
                "+                            variableDeclaration.currentScope,                                            // Scope where the variable is declared\n" +
                "+                            variableDeclaration.node,                                                    // Node where the variable is declared\n" +
                "+                            new ScopePath(variableDeclaration.type.toString())                           // Type being resolved\n" +
                "+                    );\n" +
                "@@ -153,0 +150,6 @@\n" +
                "+        /**\n" +
                "+         * We have identified the place, where the variable is defined.\n" +
                "+         * @param currentScope\n" +
                "+         * @param type\n" +
                "+         * @param node\n" +
                "+         */\n" +
                "@@ -159,3 +162,11 @@\n" +
                "-        public ScopePath currentScope;\n" +
                "-        public Type type;\n" +
                "-        public Node node;\n" +
                "+        /**\n" +
                "+         * We have resolved the variable type right away.\n" +
                "+         */\n" +
                "+        public QualifiedVariableDeclaration(ScopePath resultingType) {\n" +
                "+            this.resultingType = resultingType;\n" +
                "+        }\n" +
                "+\n" +
                "+        public ScopePath currentScope = null;\n" +
                "+        public ScopePath resultingType = null;\n" +
                "+        public Type type = null;\n" +
                "+        public Node node = null;\n" +
                "@@ -185,0 +196,5 @@\n" +
                "+\n" +
                "+            @Override\n" +
                "+            ScopePath resolveInSuperClass(ImportResolver importResolver, ScopePath currentScope, Node node) throws UnknownName, ImportResolver.UnknownImport, ParseException, IOException {\n" +
                "+                return resolve(importResolver, SymbolType.VARIABLE_TYPE, currentScope, node, name);\n" +
                "+            }\n" +
                "@@ -204,0 +220,5 @@\n" +
                "+\n" +
                "+            @Override\n" +
                "+            ScopePath resolveInSuperClass(ImportResolver importResolver, ScopePath currentScope, Node node) throws UnknownName, ImportResolver.UnknownImport, ParseException, IOException {\n" +
                "+                return resolve(importResolver, SymbolType.METHOD, currentScope, node, name);\n" +
                "+            }\n" +
                "@@ -267,2 +288,0 @@\n" +
                "-        });\n" +
                "-    }\n" +
                "@@ -270,3 +289,4 @@\n" +
                "-    private static class ScopeVisitor<T> {\n" +
                "-        T visitBlock(ScopePath scope, BlockStmt block) {\n" +
                "-            return null;\n" +
                "+            @Override\n" +
                "+            QualifiedVariableDeclaration resolveInSuperClass(ImportResolver importResolver, ScopePath currentScope, Node node) throws UnknownName, ImportResolver.UnknownImport, ParseException, IOException {\n" +
                "+                ScopePath variableType = resolve(importResolver, SymbolType.VARIABLE_TYPE, currentScope, node, name);\n" +
                "+                return new QualifiedVariableDeclaration(variableType);\n" +
                "@@ -274,3 +294,1 @@\n" +
                "-\n" +
                "-        T visitMethod(ScopePath name, MethodDeclaration method) {\n" +
                "-            return null;\n" +
                "+        });\n" +
                "@@ -279,7 +297,13 @@\n" +
                "-        T visitClassOrInterface(ScopePath name, ClassOrInterfaceDeclaration type) {\n" +
                "-            return null;\n" +
                "-        }\n" +
                "-\n" +
                "-        T visitCompilationUnit(ScopePath name, CompilationUnit cu) { return null; }\n" +
                "-    }\n" +
                "-\n" +
                "+    private static abstract class ScopeVisitor<T> {\n" +
                "+        T visitBlock(ScopePath scope, BlockStmt block) { // newline blab\n" +
                "+            return null; // newline blab\n" +
                "+        } // newline blab\n" +
                "+        T visitMethod(ScopePath name, MethodDeclaration method) { // newline blab\n" +
                "+            return null; // newline blab\n" +
                "+        } // newline blab\n" +
                "+        T visitClassOrInterface(ScopePath name, ClassOrInterfaceDeclaration type) { // newline blab\n" +
                "+            return null; // newline blab\n" +
                "+        } // newline blab\n" +
                "+        T visitCompilationUnit(ScopePath name, CompilationUnit cu) { return null; } // newline blab\n" +
                "+        abstract T resolveInSuperClass(ImportResolver importResolver, ScopePath currentScope, Node node) throws UnknownName, ImportResolver.UnknownImport, ParseException, IOException; // newline blab\n" +
                "+    } // newline blab\n" +
                "@@ -322,1 +346,2 @@\n" +
                "-                        result = visitParentScopes(newImportResolver, superTypeName, importResolver.resolveNameToAST(superTypeName), visitor);\n" +
                "+\n" +
                "+                        result = visitor.resolveInSuperClass(newImportResolver, superTypeName, importResolver.resolveNameToAST(superTypeName));\n" +
                "@@ -341,2 +366,4 @@\n" +
                "-\n" +
                "-\n" +
                "+    // and a final change\n" +
                "+    private void withANewTotallyUselessMethod() {\n" +
                "+    \tString a = \"doing nothing\";\n" +
                "+    }\n";

    private static class MyAnalyzer implements Analyzer {
        private int calledSetChangelistInformation = 0;
        private int calledAnalyze = 0;
        private int calledAfter = 0;

        @Override
        public void setChangelistInformation(Date time, Person author, String description, ParsedClassProvider parsedClassProvider) {
            calledSetChangelistInformation++;

            Assert.assertEquals(time, new Date(1429953947000L));
            Assert.assertEquals(author, new Person("username"));
            Assert.assertEquals(description, "Changed file to test diff");
            Assert.assertTrue(parsedClassProvider != null);
        }

        @Override
        public void analyze(ScopePath file, ChangedLines changedLines) throws IOException, ParseException {
            calledAnalyze++;
            Assert.assertEquals(calledSetChangelistInformation, 1);

            Assert.assertEquals(file, new ScopePath("typeResolver.Simple"));
            Assert.assertTrue(changedLines instanceof EditedChangedLines);

            TreeMap<Integer, Integer> cls = ((EditedChangedLines)changedLines).getChangedLines();

            TreeMap<Integer, Integer> expectedCls = new TreeMap<Integer, Integer>(){{
                put(52, 57);
                put(69, 69);
                put(105, 107);
                put(109, 115);
                put(150, 155);
                put(162, 172);
                put(196, 200);
                put(220, 224);
                put(289, 292);
                put(294, 294);
                put(297, 309);
                put(346, 347);
                put(366, 369);
            }};

            Assert.assertEquals(cls, expectedCls);
        }

        @Override
        public void afterChangelist() {
            calledAfter++;
        }

        public void assertCalledProperly() {
            Assert.assertEquals(calledSetChangelistInformation, 1);
            Assert.assertEquals(calledAnalyze, 1);
            Assert.assertEquals(calledAfter, 1);
        }
    }


}
