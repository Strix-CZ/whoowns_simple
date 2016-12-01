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

package cz.vutbr.stud.fit.xsimon13.whoowns.tasks;

import cz.vutbr.stud.fit.xsimon13.whoowns.hr.TeamAssignment;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.OwnershipStatistics;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePath;
import cz.vutbr.stud.fit.xsimon13.whoowns.java.typeresolver.ScopePathTree;
import redis.clients.jedis.Jedis;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

import static cz.vutbr.stud.fit.xsimon13.whoowns.java.OwnershipStatistics.Owner;
import static cz.vutbr.stud.fit.xsimon13.whoowns.java.OwnershipStatistics.OwnerWithPath;

public class ExtractOwnershipTask implements Task {

    private boolean team;
    private int thresholdPercentage;

    @Override
    public String getName() {
        return "extract ownership";
    }

    @Override
    public void run(TeamAssignment teamAssignment, Jedis jedis, List<String> params) throws Exception {
        validateParams(params);

        OwnershipStatistics stats = new OwnershipStatistics(jedis,team ? ProcessChangelistsTask.TEAM_STATISTICS_NAME : ProcessChangelistsTask.AUTHOR_STATISTICS_NAME);

        ScopePathTree<Owner> tree = new ScopePathTree<Owner>(new ScopePath(""));

        double threshold = 1.0d + ((double) thresholdPercentage / 100.0d);

        for (ScopePath scopePath : stats.getScopePaths()) {
            Owner owner = stats.getTheOnlyOwner(scopePath, threshold);
            if (owner == null)
                continue;

            if (isThereParentScopeWithSameOwner(tree, scopePath, owner))
                continue;

            ScopePathTree<Owner> node = tree.set(scopePath, owner);
            removeChildrenWithSameOwner(node, owner);
        }

        //printResults(tree);
        printSortedResults(tree);
    }

    private boolean isThereParentScopeWithSameOwner(ScopePathTree<Owner> tree, ScopePath path, final Owner owner) {
        final Result result = new Result();

        tree.findClosest(path).visitParents(new ScopePathTree.Visitor<Owner>() {
            @Override
            public boolean visit(ScopePathTree<Owner> node) {
                if (node.get() == null)
                    return true;
                result.result = node.get().equals(owner);
                return false;
            }
        });

        return result.result;
    }

    private void removeChildrenWithSameOwner(final ScopePathTree<Owner> startNode, final Owner owner) {
        final Set<ScopePathTree<Owner>> nodesToRemove = new HashSet<ScopePathTree<Owner>>();

        startNode.visitChildren(new ScopePathTree.Visitor<Owner>() {
            @Override
            public boolean visit(ScopePathTree<Owner> node) {
                if (startNode == node || node.get() == null)
                    return true;
                if (!node.get().equals(owner))
                    return false;
                nodesToRemove.add(node);
                return true;
            }
        });

        for (ScopePathTree<Owner> nodeToRemove : nodesToRemove)
            nodeToRemove.remove();
    }

    private void printResults(ScopePathTree<Owner> tree) {
        final NumberFormat formatter = new DecimalFormat("#0.00");

        tree.visitChildren(new ScopePathTree.Visitor<Owner>() {
            @Override
            public boolean visit(ScopePathTree<Owner> node) {
                Owner owner = node.get();
                if (owner != null)
                    System.out.println(node.getPath() + " " + owner.entityId + " " + formatter.format(owner.weight));
                return true;
            }
        });
    }

    private void printSortedResults(ScopePathTree<Owner> tree) {
        final NumberFormat formatter = new DecimalFormat("#0.00");
        final List<OwnershipStatistics.OwnerWithPath> owners = new ArrayList<OwnerWithPath>();

        tree.visitChildren(new ScopePathTree.Visitor<Owner>() {
            @Override
            public boolean visit(ScopePathTree<Owner> node) {
                Owner owner = node.get();
                if (owner != null)
                    owners.add(new OwnerWithPath(owner.entityId, owner.weight, owner.score, node.getPath()));
                return true;
            }
        });

        Collections.sort(owners, new Comparator<OwnerWithPath>() {
            @Override
            public int compare(OwnerWithPath o1, OwnerWithPath o2) {
                int c = o1.entityId.compareTo(o2.entityId);
                if (c != 0)
                    return c;
                if (o1.score > o2.score)
                    return -1;
                else if (o1.score == o2.score)
                    return 0;
                else
                    return 1;
            }
        });

        for (OwnerWithPath owner : owners) {
            System.out.println(owner.path + " " + owner.entityId + " " + formatter.format(owner.weight));
        }
    }

    @Override
    public void validateParams(List<String> params) throws WrongParameterException {
        // team|author, threshold
        if (params.size() != 2)
            throw new WrongParameterException(this, "Wrong number of params, must be: team|author, threshold");

        if (params.get(0).equals("team"))
            team = true;
        else if (params.get(0).equals("author"))
            team = false;
        else
            throw new WrongParameterException(this, "First parameter must be either 'team' or 'author'");

        try {
            thresholdPercentage = Integer.parseInt(params.get(1));
            if (thresholdPercentage <= 0 || thresholdPercentage > 100)
                throw new NumberFormatException();
        }
        catch(NumberFormatException e) {
            throw new WrongParameterException(this, "Second parameter must be a number in the range 1 - 100 indicating percentage of minimal ownership");
        }
    }

    private static class Result {
        public boolean result = false;
    }

}
