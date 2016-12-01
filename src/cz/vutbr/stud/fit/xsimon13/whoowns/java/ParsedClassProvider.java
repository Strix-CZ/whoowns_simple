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
import cz.vutbr.stud.fit.xsimon13.whoowns.java.filesystem.ASTLoader;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.filesystem.FileAccessor;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.FullyQualifiedImportResolver;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ImportResolver;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.TypeResolver;

import javax.xml.ws.handler.MessageContext;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Provides parsed class for a file with caching. Lazy constructs all the items.
 */
public class ParsedClassProvider {

    private FileAccessor fileAccessor;
    private ASTLoader astLoader;
    private FullyQualifiedImportResolver fullyQualifiedImportResolver;

    private static final int CACHED_ITEMS_LIMIT = 5000;
    private Map<ScopePath, MyAnalyzedFile> cache = new HashMap<ScopePath, MyAnalyzedFile>();
    Random random = new Random(0);

    public ParsedClassProvider(FileAccessor fileAccessor, FullyQualifiedImportResolver fullyQualifiedImportResolver, ASTLoader astLoader) {
        this.fileAccessor = fileAccessor;
        this.astLoader = astLoader;
        this.fullyQualifiedImportResolver = fullyQualifiedImportResolver;
    }

    public AnalyzedFile get(ScopePath className) throws ParseException, IOException {

        MyAnalyzedFile item = cache.get(className);
        FileTime modificationTime = item != null ? Files.getLastModifiedTime(item.file) : null;

        if (item == null || !item.modificationTime.equals(modificationTime)) {
            item = analyzeClass(className);

            cache.put(className, item);
            if (cache.size() > CACHED_ITEMS_LIMIT)
                removeFromCache();

            return item;
        }
        else
            return item;
    }

    public FullyQualifiedImportResolver getFullyQualifiedImportResolver() {
        return fullyQualifiedImportResolver;
    }

    public FileAccessor getFileAccessor() {
        return fileAccessor;
    }

    private MyAnalyzedFile analyzeClass(ScopePath className) throws ParseException, IOException {
        MyAnalyzedFile analyzed = new MyAnalyzedFile();

        analyzed.file = fileAccessor.classNameToPath(className);
        analyzed.modificationTime = Files.getLastModifiedTime(analyzed.file);
        analyzed.ast = astLoader.load(analyzed.file);

        return analyzed;
    }

    private void removeFromCache() {
        // Remove randomly chosen item.
        int i = 0;
        int removeItem = random.nextInt(cache.size());
        for (ScopePath className : cache.keySet()) {
            if (i == removeItem) {
                cache.remove(className);
                break;
            }
            ++i;
        }
    }

    public static interface AnalyzedFile {
        public CompilationUnit getAst();
        public ImportResolver getImportResolver();
        public TypeResolver getTypeResolver();
    }

    private class MyAnalyzedFile implements AnalyzedFile {
        public Path file;
        public FileTime modificationTime;
        public CompilationUnit ast;
        public ImportResolver importResolver = null;
        public TypeResolver typeResolver = null;

        public CompilationUnit getAst() {
            return ast;
        }

        public ImportResolver getImportResolver() {
            if (importResolver == null)
                importResolver = new ImportResolver(fileAccessor, ParsedClassProvider.this, ast.getPackage(), ast.getImports());
            return importResolver;
        }

        public TypeResolver getTypeResolver() {
            if (typeResolver == null)
                typeResolver = new TypeResolver(ParsedClassProvider.this);
            return typeResolver;
        }
    }
}
