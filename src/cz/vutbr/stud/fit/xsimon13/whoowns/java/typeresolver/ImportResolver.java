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
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.*;
import com.sun.istack.internal.NotNull;
import cz.vutbr.stud.fit.xsimon13.whoowns.MessageBoard;
import cz.vutbr.stud.fit.xsimon13.whoowns.Utils;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.ParsedClassProvider;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.filesystem.FileAccessor;

import javax.xml.ws.handler.MessageContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;


/**
 * Resolves imports contained in a single file.
 */
public class ImportResolver {
    FileAccessor fileAccessor;
    ParsedClassProvider parsedClassProvider;
    PackageDeclaration localPackage;
    List<ImportDeclaration> imports;

    boolean importsAnalyzed = false;
    SortedSet<AnalyzedImport> analyzedImports = new TreeSet<AnalyzedImport>();

    public static class UnknownImport extends Exception {
        public UnknownImport(ScopePath path) {
            this(path, "");
        }

        public UnknownImport(ScopePath path, String message) {
            super("Name " + path + " could not be resolved as an imported name: " + message);
        }
    }

    public ImportResolver(FileAccessor fileAccessor, ParsedClassProvider parsedClassProvider, PackageDeclaration localPackage, List<ImportDeclaration> imports) {
        this.fileAccessor = fileAccessor;
        this.parsedClassProvider = parsedClassProvider;
        this.localPackage = localPackage;
        this.imports = imports;
    }

    public SortedSet<AnalyzedImport> getAnalyzedImports() {
        analyzeImportsIfNecessary();
        return analyzedImports;
    }

    /**
     * Locates the file which contains the scope, parses the file, locates the scope inside the file.
     */
    public Node resolveNameToAST(ScopePath name) throws ImportResolver.UnknownImport, ParseException, IOException {
        analyzeImportsIfNecessary();

        ScopePath fileName = getFileNameFromScope(resolveName(name));
        Node node = parsedClassProvider.get(fileName).getAst();

        ScopePath innerName = name.getPathRemainingAfter(fileName.getQualifier());
        return ScopeUtils.locateNode(node, innerName);
    }

    public ScopePath getFileNameFromScope(ScopePath qualifiedName) throws UnknownImport {
        analyzeImportsIfNecessary();

        try {
            return getDetails(qualifiedName).fileName;
        }
        catch (UnknownImport e) {
            return parsedClassProvider.getFullyQualifiedImportResolver().getFileFromScope(qualifiedName);
        }
    }

    /**
     * Resolves a name with optional scope to a fully qualified name.
     *
     * The method has two String as parameters, because the name can come from number of classes in the AST:
     *
     *        Class in AST               Scope            Name
     *     ClassOrInterfaceType  getScope().toString()  getName()
     *     MethodCallExpr        getScope().toString()  getName()
     *     QualifiedNameExpr     getQualifier()         getName()
     *
     * @param name
     * @return Fully qualified name, where the symbol is located.
     * @throws UnknownImport If the name can not be found, a UnknownName exception is thrown.
     */
    public ScopePath resolveName(ScopePath name) throws UnknownImport {
        if (name.isEmpty())
            throw new UnknownImport(name, "Name can't be empty.");

        analyzeImportsIfNecessary();

        if (!name.hasQualifier()) {
            // Just compare the name
            for (AnalyzedImport analyzedImport : analyzedImports) {
                if (name.equals(analyzedImport.name.getName()))
                    return analyzedImport.name;
            }
        }
        else {
            for (AnalyzedImport analyzedImport : analyzedImports) {
                if (analyzedImport.name.contains(name))
                    return name; // Fully qualified name is the same or goes deeper
                if (analyzedImport.name.continuesWith(name))
                    return ScopePath.createPathThatContinuesWith(analyzedImport.name, name); // Goes deeper from non qualified name
            }

            ScopePath fileName = parsedClassProvider.getFullyQualifiedImportResolver().getFileFromScope(name);
            if (fileName != null)
                return name; // We have verified, that the name is fully qualified - return it
        }

        throw new UnknownImport(name);
    }

    private void analyzeImportsIfNecessary() {
        if (importsAnalyzed)
            return;

        // Add local package imports
        addTypeDefinitionsFromPackage(new ScopePath(localPackage.getName().toString()), true);

        // Add imports
        if (imports != null) {
            for (ImportDeclaration importDeclaration : imports) {
                try {
                    analyzeImport(importDeclaration);
                } catch (ParseException e) {
                    MessageBoard.getInstance().sendRecoverableError("Resolving import failed", e);
                } catch (IOException e) {
                    MessageBoard.getInstance().sendRecoverableError("Resolving import failed", e);
                }
            }
        }

        importsAnalyzed = true;
    }

    private AnalyzedImport getDetails(ScopePath qualifiedName) throws UnknownImport {
        for (AnalyzedImport analyzedImport : analyzedImports) {
            if (analyzedImport.name.contains(qualifiedName))
                return analyzedImport;
        }
        throw new UnknownImport(qualifiedName);
    }

    /**
     * Adds items to analyzedImports. The import declaration analyzes what is the name of containing file,
     * and figures out all imported identifiers for asterisk imports.
     */
    private void analyzeImport(ImportDeclaration imp) throws ParseException, IOException {
        boolean isStatic = imp.isStatic();
        boolean isAsterisk = imp.isAsterisk();

        // Split the import into components
        ScopePath path = new ScopePath(imp.getName().toString());
        String[] parts = path.getParts().toArray(new String[0]);

        if (parts.length < 2)
            throw new ParseException("Imports must have at least two compounds - package name and imported item");

        // Find out what part of the import is package (directory)
        int packagePrefix = parts.length - 1;
        ScopePath packageName = null;

        for (; packagePrefix>=0; --packagePrefix) {
            packageName = new ScopePath(Utils.joinStringArray(parts, packagePrefix, "."));
            Path packagePath = fileAccessor.packageNameToPath(packageName);

            if (Files.isDirectory(packagePath))
                break;
        }

        if (packagePrefix==0)
            throw new ParseException("Imported package not found");


        if (isAsterisk && packagePrefix == parts.length - 1) {
            // Importing from multiple files - all publicly visible classes/interfaces in a package
            addTypeDefinitionsFromPackage(packageName, false);
        }
        else {
            // Importing from a single file
            ScopePath fileQualifiedName = ScopePath.append(packageName, parts[packagePrefix]);

            if (!isAsterisk) {
                // Import fully qualified name
                analyzedImports.add(new AnalyzedImport(path, fileQualifiedName, false, true));
            }
            else {
                // Import all items under a parent's node which is inside a file
                ScopePath parentName = path.getQualifier();

                // Parse the file and look-up the parent TypeDeclaration node
                Node node = parsedClassProvider.get(fileQualifiedName).getAst();
                for (int i = packagePrefix; i < parts.length - 1; ++i) {
                    TypeDeclaration typeDeclaration = (TypeDeclaration) Utils.find(node.getChildrenNodes(), new Utils.TypeDeclarationMatcher<Node>(parts[i]));
                    if (typeDeclaration == null)
                        throw new ParseException("Imported type declaration " + parts[i] + "not found in " + Utils.joinStringArray(parts, i, "."));
                    if (!ModifierSet.isPublic(typeDeclaration.getModifiers()))
                        throw new ParseException("Imported type declaration " + Utils.joinStringArray(parts, i + 1, ".") + " is not public");
                    if (i > packagePrefix && !ModifierSet.isStatic(typeDeclaration.getModifiers()))
                        throw new ParseException("Imported type declaration " + Utils.joinStringArray(parts, i + 1, ".") + " is not static");
                    node = typeDeclaration;
                }

                for (Node item : node.getChildrenNodes()) {

                    if (item instanceof TypeDeclaration) {
                        // Classes or interfaces

                        TypeDeclaration td = (TypeDeclaration) item;
                        if (!ModifierSet.isPublic(td.getModifiers()) || !ModifierSet.isStatic(td.getModifiers()))
                            continue;
                        analyzedImports.add(new AnalyzedImport(ScopePath.append(parentName, td.getName()), fileQualifiedName, true, true));

                    }
                    else if (isStatic && item instanceof MethodDeclaration) {
                        // Methods (static import only)

                        MethodDeclaration md = (MethodDeclaration) item;
                        if (!ModifierSet.isPublic(md.getModifiers()) || !ModifierSet.isStatic(md.getModifiers()))
                            continue;
                        analyzedImports.add(new AnalyzedImport(ScopePath.append(parentName, md.getName()), fileQualifiedName, true, false));

                    }
                    else if (isStatic && item instanceof FieldDeclaration) {
                        // Member variables (static import only)

                        FieldDeclaration fd = (FieldDeclaration)item;
                        if (!ModifierSet.isPublic(fd.getModifiers()) || !ModifierSet.isStatic(fd.getModifiers()))
                            continue;
                        for (VariableDeclarator var : fd.getVariables())
                            analyzedImports.add(new AnalyzedImport(ScopePath.append(parentName, var.getId().getName()), fileQualifiedName, true, false));

                    }

                }
            }
        }
    }

    /**
     * Add all classes/interfaces contained in a package.
     * @param packageName     Path to the package.
     * @param includePrivate
     */
    private void addTypeDefinitionsFromPackage(ScopePath packageName, boolean includePrivate) {
        try {
            Set<ScopePath> fileNames = fileAccessor.getPackageFiles(packageName);

            for (ScopePath fileName : fileNames) {
                try {
                    CompilationUnit node = parsedClassProvider.get(fileName).getAst();

                    for (TypeDeclaration type : node.getTypes()) {
                        ScopePath name = ScopePath.append(fileName.getQualifier(), type.getName());
                        if (includePrivate || ModifierSet.isPublic(type.getModifiers()))
                            analyzedImports.add(new AnalyzedImport(name, fileName, true, true));
                    }
                }
                catch (ParseException e) {
                    MessageBoard.getInstance().sendRecoverableError("Parsing file " + fileName + " failed", e);
                }
            }
        }
        catch (IOException e) {
            MessageBoard.getInstance().sendRecoverableError("Importing package " + packageName + " failed", e);
        }
    }

    public static class AnalyzedImport implements Comparable<AnalyzedImport> {
        public ScopePath name;
        public ScopePath fileName; // Name inside the file
        public boolean isAsterisk;
        public boolean isType; // True = class/interface, False = method/variable

        public AnalyzedImport(ScopePath name,  ScopePath fileName, boolean isAsterisk, boolean isType) {
            this.name = name;
            this.fileName = fileName;
            this.isAsterisk = isAsterisk;
            this.isType = isType;
        }

        /**
         * Note: this class has a natural ordering that is inconsistent with equals.
         * The order is determined by the priority of imports (non-asterisk are first),
         * then by name.
         */
        @Override
        public int compareTo(@NotNull AnalyzedImport o) {
            int result = (isAsterisk ? 1 : 0) - (o.isAsterisk ? 1 : 0);
            if (result == 0)
                result = name.toString().compareTo(o.toString());
            return result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AnalyzedImport that = (AnalyzedImport) o;

            if (isAsterisk != that.isAsterisk) return false;
            if (!name.equals(that.name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + (isAsterisk ? 1 : 0);
            return result;
        }
    }


    /*

        Simple name:
            1) In the same package
            2) Imported

     */
}