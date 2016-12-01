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

import cz.vutbr.stud.fit.xsimon13.whoowns.Entity;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePathTree;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * Contains ScopePathTree of Owners.
 */
public class OwnersTree {

    public static class Item  {
        public OwnerList ownerList; // Complete owner list

        public String owner; // The definite owner (if there is one, otherwise null)
        public double score; // The owner's score

        public Item(List<OwnershipStatistics.Owner> owners, String owner, double score) {
            ownerList = new OwnerList(owners);
            this.owner = owner;
            this.score = score;
        }
    }

    private ScopePathTree<Item> ownersTree;

    /**
     * Construct the tree from statistics. The only owner must be over threshold.
     */
    public OwnersTree(OwnershipStatistics teamStats, double threshold) {
        ownersTree = new ScopePathTree<Item>(new ScopePath(""));
        for (ScopePath scopePath : teamStats.getScopePaths()) {
            List<OwnershipStatistics.Owner> owners = teamStats.getAllOwners(scopePath);
            if (owners.size() == 0)
                continue;

            OwnershipStatistics.Owner theOnlyOwner = teamStats.getTheOnlyOwner(scopePath, threshold);
            if (theOnlyOwner == null)
                ownersTree.set(scopePath, new Item(owners, null, 0));
            else
                ownersTree.set(scopePath, new Item(owners, theOnlyOwner.entityId, theOnlyOwner.score));
        }
    }

    public ScopePathTree<Item> getTree() {
        return ownersTree;
    }

    /**
     * Autocomplete a scopePath.
     */
    public List<Entity> autoComplete(String text, final int limit) {
        final String upperCaseText = text.toUpperCase();

        final List<Entity> paths = new ArrayList<Entity>(limit);
        try {
            ownersTree.visitChildrenBreadthFirst(new ScopePathTree.ValuePassingVisitor<Item, Integer>() {
                @Override
                public Integer visit(ScopePathTree<Item> node, Integer n) {
                    if (paths.size() >= limit)
                        return null;

                    ScopePath path = node.getPath();
                    if (path.toString().toUpperCase().contains(upperCaseText)) {
                        paths.add(path);
                        return null;
                    }
                    else
                        return 1;
                }
            }, 1);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }

        return paths;
    }

    /**
     * Returns Owners for the scopePath including excpetions on lower levels.
     * The total count of items is within `limit`.
     */
    public JSONObject getOwners(ScopePath path, int limit) throws Exception {
        ScopePathTree<Item> startingNode = ownersTree.findClosest(path);
        if (!startingNode.getPath().equals(path))
            throw new ScopePathNotFound();

        Item item = startingNode.get();

        JSONObject result = new JSONObject();
        result.put("type", Entity.JAVA_PREFIX);
        result.put("name", path.toString());
        result.put("path", path.toString());
        result.put("owners", itemToJson(item));
        JSONArray children = new JSONArray();
        result.put("children", children);

        final int[] limitArray = new int[]{ limit };
        VisitorsBaggage baggage = new VisitorsBaggage(startingNode, children);

        startingNode.visitChildrenBreadthFirst(
                new ScopePathTree.ValuePassingVisitor<Item, VisitorsBaggage>() {
                    @Override
                    public VisitorsBaggage visit(ScopePathTree<Item> node, VisitorsBaggage parent) throws Exception {
                        if (limitArray[0] <= 0) {
                            if (limitArray[0] == 0) {
                                JSONObject obj = new JSONObject();
                                obj.put("name", "...");
                                obj.put("path", "none");
                                obj.put("owners", new JSONArray());
                                obj.put("children", new JSONArray());
                                obj.put("type", "none");
                                parent.array.put(obj);

                                limitArray[0] = -1;
                            }

                            return null;
                        }

                        if (!node.get().ownerList.equals(parent.node.get().ownerList)) {
                            JSONObject obj = new JSONObject();
                            JSONArray children  = new JSONArray();
                            obj.put("name", node.getPath().getPathRemainingAfter(parent.node.getPath()).toString());
                            obj.put("path", node.getPath().toString());
                            obj.put("owners", itemToJson(node.get()));
                            obj.put("children", children);
                            obj.put("type", Entity.JAVA_PREFIX);

                            parent.array.put(obj);

                            limitArray[0]--;
                            return new VisitorsBaggage(node, children);
                        }
                        else {
                            // I have the same owners as my parent, skip me. My children will not reference me, but my parent.
                            return parent;
                        }
                    }
                },
                baggage
        );

        return result;
    }

    public static class OwnedScopePath implements Comparable<OwnedScopePath> {
        public ScopePath scopePath;
        public double score;

        public OwnedScopePath(ScopePath scopePath, double score) {
            this.scopePath = scopePath;
            this.score = score;
        }

        @Override
        public int compareTo(OwnedScopePath o) {
            if (score > o.score)
                return -1;
            else if (score < o.score)
                return 1;
            else
                return scopePath.toEntityId().compareTo(scopePath.toEntityId());
        }
    }

    public SortedSet<OwnedScopePath> getCodeOwnedBy(final String entityId, final int limit) {
        final SortedSet<OwnedScopePath> owners = new TreeSet<OwnedScopePath>();

        try {
            ownersTree.visitChildrenBreadthFirst(new ScopePathTree.ValuePassingVisitor<Item, Integer>() {
                @Override
                public Integer visit(ScopePathTree<Item> node, Integer parent) throws Exception {
                    if (node.get() != null && entityId.equals(node.get().owner)) {
                        owners.add(new OwnedScopePath(node.getPath(), node.get().score));
                        if (owners.size() > limit)
                            owners.remove(owners.last());
                    }

                    return 1;
                }
            }, 1);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        return owners;
    }

    private static class VisitorsBaggage {
        public ScopePathTree<Item> node;
        public JSONArray array;

        public VisitorsBaggage(ScopePathTree<Item> node, JSONArray array) {
            this.node = node;
            this.array = array;
        }
    }

    private JSONArray itemToJson(Item item) throws JSONException {
        JSONArray array = new JSONArray();
        for (OwnershipStatistics.Owner owner : item.ownerList.getOwners()) {
            JSONObject object = new JSONObject();
            object.put("name", owner.entityId);
            object.put("w", owner.weight);
            object.put("type", Entity.TEAM_PREFIX);
            array.put(object);
        }

        return array;
    }

    public static class ScopePathNotFound extends Exception {
    }
}
