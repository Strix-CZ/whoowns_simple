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

import java.util.*;

public class ScopePathTree <T> {
    private ScopePath path; // Path to this node
    private Map<String, ScopePathTree<T>> children = new HashMap<String, ScopePathTree<T>>();
    private T value = null;
    private ScopePathTree<T> parent;
    private Integer childrenCount = null;

    public ScopePathTree(ScopePath path) {
        this(path, null);
    }

    private ScopePathTree(ScopePath path, ScopePathTree<T> parent) {
        this.path = path;
        this.parent = parent;
    }

    public ScopePathTree set(ScopePath nPath, T nValue) {
        if (!path.contains(nPath))
            throw new RuntimeException("Can't add scopePath " + nPath + " to a node with scopePath " + path);

        childrenCount = null;

        ScopePath childPath = nPath.getPathRemainingAfter(path);

        if (childPath.isEmpty()) {
            value = nValue;
            return this;
        }
        else {
            String childId = childPath.firstPart().toString();

            ScopePathTree<T> subTree = children.get(childId);
            if (subTree == null) {
                subTree = new ScopePathTree<T>(ScopePath.append(path, childId), this);
                children.put(childId, subTree);
            }

            return subTree.set(nPath, nValue);
        }
    }

    public ScopePathTree<T> findClosest(ScopePath nPath) {
        if (!path.contains(nPath))
            throw new RuntimeException("Can't findClosest scopePath " + nPath + " inside a node with scopePath " + path);

        ScopePath childPath = nPath.getPathRemainingAfter(path);

        if (childPath.isEmpty())
            return this;
        else {
            String childId = childPath.firstPart().toString();

            ScopePathTree<T> subTree = children.get(childId);
            if (subTree == null)
                return this;
            else
                return subTree.findClosest(nPath);
        }
    }

    public void remove() {
        value = null;
        childrenCount = null;
        if (children.isEmpty() && parent != null)
            parent.removeChild(path);
    }

    private void removeChild(ScopePath childPath) {
        String childId = childPath.getPathRemainingAfter(path).firstPart().toString();
        ScopePathTree<T> child = children.remove(childId);
        if (child == null)
            throw new RuntimeException("Child " + childPath + " not found inside " + path);

        childrenCount = null;

        if (value == null)
            remove();
    }

    public T get() {
        return value;
    }

    public ScopePath getPath() {
        return path;
    }

    public Collection<ScopePathTree<T>> getChildren() {
        return children.values();
    }

    public interface Visitor <T> {
        // Return true if you want to recur deeper, false if you want to stop
        public boolean visit(ScopePathTree<T> node);
    }

    public interface ValuePassingVisitor <T, P> {
        /**
         * Return non null object if you want to recur deeper, false if you want to stop.
         * The returned object will be passed in as the parameter when the function is invoked on children.
         */
        public P visit(ScopePathTree<T> node, P parent) throws Exception;
    }

    public void visitChildren(Visitor<T> visitor) {
        if (visitor.visit(this)) {
            for (String child : children.keySet())
                children.get(child).visitChildren(visitor);
        }
    }

    public void visitParents(Visitor<T> visitor) {
        if (visitor.visit(this) && parent != null)
            parent.visitParents(visitor);
    }

    public <P> void visitChildrenBreadthFirst(ValuePassingVisitor<T, P> visitor, P startingValue) throws Exception {
        Queue<ScopePathTree<T>> nodes = new ArrayDeque<ScopePathTree<T>>();
        Queue<P> params = new ArrayDeque<P>();
        nodes.add(this);
        params.add(startingValue);

        while (!nodes.isEmpty()) {
            ScopePathTree<T> node = nodes.poll();
            P param = params.poll();

            P result = visitor.visit(node, param);
            if (result != null) {
                for (ScopePathTree<T> child : node.children.values()) {
                    nodes.add(child);
                    params.add(result);
                }
            }
        }
    }

    public int countChildren() {
        if (childrenCount == null) {
            childrenCount = 0;
            for (ScopePathTree<T> child : children.values())
                childrenCount += child.countChildren() + 1;
        }

        return childrenCount;
    }
}
