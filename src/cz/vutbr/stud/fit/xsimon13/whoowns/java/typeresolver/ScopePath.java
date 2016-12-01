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

import com.github.javaparser.ast.type.Type;
import cz.vutbr.stud.fit.xsimon13.whoowns.Entity;
import cz.vutbr.stud.fit.xsimon13.whoowns.Utils;
import cz.vutbr.stud.fit.xsimon13.whoowns.db.DBConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ScopePath implements Entity {
    private List<String> parts;

    public ScopePath() {
    }

    public ScopePath(String qualifier, String name) {
        boolean qualifierExists = !Utils.isEmpty(qualifier);
        boolean nameExists = !Utils.isEmpty(name);
        String path = (
                (qualifierExists ? qualifier : "") +
                (qualifierExists && nameExists ? "." : "") +
                (nameExists ? name : "")
        );
        parts = parse(path);
    }

    public ScopePath(Type type) {
        this(type.toString());
    }

    public ScopePath(String path) {
        parts = parse(path);
    }

    @Override
    public String toEntityId() {
        return JAVA_PREFIX + withoutTrailingBlocks().toString();
    }

    @Override
    public String getType() {
        return JAVA_PREFIX;
    }

    @Override
    public String getData() {
        return withoutTrailingBlocks().toString();
    }

    private List<String> parse(String path) {
        if (Utils.isEmpty(path))
            return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(path.split("\\.")));
    }

    private ScopePath(List<String> parts) {
        this.parts = parts;
    }

    public void add(String path) {
        parts.addAll(parse(path));
    }

    public ScopePath withoutFirst() {
        return new ScopePath(Utils.partialCopy(parts, 1));
    }

    public ScopePath withoutTrailingBlocks() {
        ArrayList<String> newParts = new ArrayList<String>();

        for (int i = parts.size() - 1; i >= 0; --i) {
            if (ScopeUtils.getBlockId(parts.get(i)) == null) {
                newParts.ensureCapacity(i + 1);
                for (int j = 0; j <= i; ++j)
                    newParts.add(parts.get(j));
                break;
            }
        }

        return new ScopePath(newParts);
    }

    public ScopePath firstPart() {
        return new ScopePath(parts.get(0));
    }

    public boolean hasQualifier() {
        return parts.size() > 1;
    }

    public ScopePath getQualifier() {
        if (!hasQualifier())
            return new ScopePath(new ArrayList<String>());
        else
            return new ScopePath(Utils.partialCopy(parts, 0, parts.size() - 1));
    }

    public ScopePath getName() {
        if (parts.size() == 0)
            return new ScopePath(new ArrayList<String>());
        else
            return new ScopePath(parts.get(parts.size() - 1));
    }

    public int getPartsCount() {
        return parts.size();
    }

    public ScopePath getPathRemainingAfter(ScopePath other) {
        if (!other.contains(this))
            throw new RuntimeException("Can't extract remaining parts if the path is not contained within the other path: " + this + " - " + other);

        ArrayList<String> newParts = new ArrayList<String>();
        newParts.ensureCapacity(parts.size() - other.parts.size());

        for (int i = other.parts.size(); i < parts.size(); ++i)
            newParts.add(parts.get(i));

        return new ScopePath(newParts);
    }

    public boolean contains(ScopePath other) {
        if (parts.size() > other.parts.size())
            return false;

        for (int i = 0; i < parts.size(); ++i) {
            if (!parts.get(i).equals(other.parts.get(i)))
                return false;
        }

        return true;
    }

    public boolean continuesWith(ScopePath other) {
        if (isEmpty() || other.isEmpty())
            return false;
        return parts.get(parts.size() - 1).equals(other.parts.get(0));
    }

    /**
     * Appends two paths that share the middle part.
     */
    public static ScopePath createPathThatContinuesWith(ScopePath first, ScopePath second) {
        if (first.parts.size() == 0 || second.parts.size() == 0)
            throw new RuntimeException("Can't continue path if one of them is empty");
        if (!first.parts.get(first.parts.size() - 1).equals(second.parts.get(0)))
            throw new RuntimeException("The shared part is not equal.");

        ArrayList<String> parts = new ArrayList<String>();
        parts.ensureCapacity(first.parts.size() + second.parts.size() - 1);

        for (int i = 0; i < first.parts.size(); ++i)
            parts.add(first.parts.get(i));
        for (int i = 1; i < second.parts.size(); ++i)
            parts.add(second.parts.get(i));

        return new ScopePath(parts);
    }

    public static ScopePath append(ScopePath first, ScopePath second) {
        ArrayList<String> parts = new ArrayList<String>();
        parts.ensureCapacity(first.parts.size() + second.parts.size());

        for (int i = 0; i < first.parts.size(); ++i)
            parts.add(first.parts.get(i));
        for (int i = 0; i < second.parts.size(); ++i)
            parts.add(second.parts.get(i));

        return new ScopePath(parts);
    }

    public static ScopePath append(ScopePath path, String appendix) {
        return append(path, new ScopePath(appendix));
    }

    public boolean matchesQualifiedName(ScopePath qualifiedName) {
        if (parts.size() == 0)
            return qualifiedName.parts.size() == 0;
        else if (parts.size() == 1)
            return parts.get(0).equals(qualifiedName.getName().toString());
        else
            return equals(qualifiedName);

    }

    public boolean isEmpty() {
        return parts.size() == 0;
    }

    @Override
    public String toString() {
        return Utils.joinStringArray(parts.toArray(new String[parts.size()]), ".");
    }

    public List<String> getParts() {
        return parts;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScopePath scopePath = (ScopePath) o;

        if (!parts.equals(scopePath.parts)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return parts.hashCode();
    }

    public static class Converter implements DBConverter<ScopePath> {
        @Override
        public String toDbString(ScopePath item) {
            return item.toString();
        }

        @Override
        public ScopePath fromDbString(String dbString) {
            return new ScopePath(dbString);
        }
    }
}
