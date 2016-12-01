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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Holds a list of owners. Capable of tolerant comparison with following meaning:
 * Two lists of owners are considered equal only if the difference between
 * weights of owners from two lists are in tolerance range. The tolerance range
 * depends on the average of the weights - it goes from 25% if both weights are 1
 * down to 10% if both weights are 0.
 */
public class OwnerList {
    private List<OwnershipStatistics.Owner> owners;

    public OwnerList(List<OwnershipStatistics.Owner> owners) {
        this.owners = owners;

        Collections.sort(this.owners, new Comparator<OwnershipStatistics.Owner>() {
            @Override
            public int compare(OwnershipStatistics.Owner o1, OwnershipStatistics.Owner o2) {
                return o1.entityId.compareTo(o2.entityId);
            }
        });
    }

    public List<OwnershipStatistics.Owner> getOwners() {
        return owners;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OwnerList other = (OwnerList) o;
        Iterator<OwnershipStatistics.Owner> it1 = owners.iterator();
        Iterator<OwnershipStatistics.Owner> it2 = other.owners.iterator();
        OwnershipStatistics.Owner o1 = it1.hasNext() ? it1.next() : null;
        OwnershipStatistics.Owner o2 = it2.hasNext() ? it2.next() : null;

        boolean inTolerance = true;
        while (inTolerance && (o1 != null || o2 != null)) {
            int comparison;
            if (o1 == null)
                comparison = 1;
            else if (o2 == null)
                comparison = -1;
            else
                comparison = o1.entityId.compareTo(o2.entityId);

            if (comparison == 0) {
                // Both lists have the same value
                inTolerance = isInTolerance(o1, o2);
                o1 = it1.hasNext() ? it1.next() : null;
                o2 = it2.hasNext() ? it2.next() : null;
            }
            else if(comparison < 0) {
                inTolerance = isInTolerance(o1, null); // o2 is ahead, so list it1 doesn't have a corresponding item o1.
                o1 = it1.hasNext() ? it1.next() : null; // Increase o1, we are trying to catch-up with o2.
            }
            else {
                // The other way round.
                inTolerance = isInTolerance(null, o2);
                o2 = it2.hasNext() ? it2.next() : null;
            }

        }

        return inTolerance;
    }

    public static boolean isInTolerance(OwnershipStatistics.Owner o1,  OwnershipStatistics.Owner o2) {
        double w1 = o1 != null ? o1.weight : 0.0d;
        double w2 = o2 != null ? o2.weight : 0.0d;
        return Math.abs(w1 - w2) < (w1 + w2) * 0.1d + 0.1d;
    }

    @Override
    public int hashCode() {
        return owners.hashCode();
    }
}
