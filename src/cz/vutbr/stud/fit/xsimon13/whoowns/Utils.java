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

package cz.vutbr.stud.fit.xsimon13.whoowns;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class.
 */
public class Utils {

    /**
     * Recursively deletes directory including all its content.
     */
    public static void deleteRecursive(Path path) throws IOException {

        if (Files.isDirectory(path)) {
            DirectoryStream<Path> dir = Files.newDirectoryStream(path);
            try {
                for (Path child : dir)
                    deleteRecursive(child);
            }
            finally {
                dir.close();
            }
        }

        //noinspection ResultOfMethodCallIgnored
        path.toFile().delete();
        //Files.delete(path); // Can't use Files.delete because it doesn't delete the file immediately and upper folder deletion would fail
    }

    /**
     * Writes the content into the file. Create the file if it doesn't exist or truncates it if it does.
     */
    public static void writeIntoFile(Path file, String content) throws IOException {
        OutputStream stream = Files.newOutputStream(file, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        try {
            OutputStreamWriter writer = new OutputStreamWriter(stream, "UTF-8");
            try {
                writer.write(content);
            }
            finally {
                writer.close();
            }
        }
        finally {
            stream.close();
        }
    }

    /**
     * Reads and returns the content of the file.
     */
    public static String readFromFile(Path file) throws IOException {
        InputStream stream = Files.newInputStream(file, StandardOpenOption.READ);
        StringBuilder strBuilder = new StringBuilder();
        try {
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            try {
                char[] charBuffer = new char[1023];
                int read = 0;
                while (read >= 0) {
                    read = reader.read(charBuffer);
                    if (read > 0)
                        strBuilder.append(charBuffer, 0, read);
                }
            }
            finally {
                reader.close();
            }
        }
        finally {
            stream.close();
        }

        return strBuilder.toString();
    }

    public static String joinStringArray(String[] strings, String glue) {
        return joinStringArray(strings, strings.length, glue);
    }

    public static String joinStringArray(String[] strings, int arraySize, String glue) {
        StringBuilder sb = new StringBuilder();

        if (arraySize>0)
            sb.append(strings[0]);

        for (int i=1; i<arraySize; ++i) {
            sb.append(glue);
            sb.append(strings[i]);
        }

        return sb.toString();
    }

    public static <T> String join(List<T> list, String separator)
    {
        StringBuilder sb = new StringBuilder();
        boolean first = true;

        for (T item : list) {
            if (first)
                first = false;
            else
                sb.append(separator);

            sb.append(item);
        }

        return sb.toString();
    }

    public static <T> List<T> partialCopy(List<T> list, int begin) {
        return partialCopy(list, begin, list.size());
    }

    public static <T> List<T> partialCopy(List<T> list, int begin, int end) {
        ArrayList<T> copy = new ArrayList<T>();

        int i = 0;
        Iterator<T> it = list.iterator();
        while (it.hasNext()) {
            T item = it.next();
            if (i >= begin && i < end)
                copy.add(item);
            ++i;
        }

        return copy;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static interface Matcher<T> {
        public boolean match(T item);
    }

    public static <T extends Node> T find(List<T> list, Matcher<T> matcher) {
        if (list == null)
            return null;


        for (T item : list) {
            if (matcher.match(item))
                return item;
        }

        return null;
    }

    public static class TypeDeclarationMatcher<T extends Node> implements Matcher<T> {
        String name;
        public TypeDeclarationMatcher(String name) {
            this.name = name;
        }

        @Override
        public boolean match(T item) {
            return item instanceof TypeDeclaration && name.equals(((TypeDeclaration)item).getName());
        }
    }

    public static String formatChildrenOfNode(Node node) {
        return formatChildrenOfNode(node, 0);
    }

    private static String formatChildrenOfNode(Node node, int indentation) {
        String t = "";
        for (int i = 0; i < indentation; ++i)
            t += " ";
        t += node.getClass().getSimpleName();

        List<Node> children = node.getChildrenNodes();
        if (children != null) {
            for (Node child : children)
                t += "\n" + formatChildrenOfNode(child, indentation + 4);
        }

        return t;
    }

    public static List<String> splitLines(String str) {
        return Arrays.asList(str.split("\r\n|\n"));
    }
}
