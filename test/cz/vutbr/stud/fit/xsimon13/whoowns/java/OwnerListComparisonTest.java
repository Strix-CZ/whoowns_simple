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

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class OwnerListComparisonTest {

    @Parameterized.Parameters
    public static Collection<Object[]> comparison() {
        return Arrays.asList(new Object[][] {
                { true, // the same
                        new Object[] {"a", 0.8, "b", 0.2, null,
                        "a", 0.8, "b", 0.2}
                },
                { true, // missing second item
                        new Object[] {"a", 0.09, null}
                },
                { true, // missing first item
                        new Object[] {null,
                        "a", 0.09,}
                },
                { true, // switched order
                        new Object[] {"b", 0.2, "a", 0.8, null,
                        "a", 0.8, "b", 0.2}
                },
                { true, // extra element in list 1
                        new Object[] {"a", 0.71, "b", 0.09, "c", 0.2, null,
                        "a", 0.8, "c", 0.2}
                },
                { true, // extra element in list 2
                        new Object[] {"a", 0.8, "c", 0.2, null,
                        "a", 0.71, "b", 0.09, "c", 0.2}
                },
                { true, // extra element in the end
                        new Object[] {"a", 0.71, "b", 0.2, "c", 0.09, null,
                        "a", 0.8, "b", 0.2}
                },
                { true, // extra element in list 2
                        new Object[] {"a", 0.05, "b", 0.05, "c", 0.05, "d", 0.85, null,
                        "d", 0.85, "e", 0.05, "f", 0.05, "g", 0.05}
                },
                { false, // not in tolerance
                        new Object[] {"a", 0.05, "b", 0.95, null,
                        "a", 0.05, "b", 0.65, "c", 0.05, "d", 0.05, "e", 0.05, "f", 0.05, "g", 0.05,}
                },
        });
    }

    private final boolean expectation;
    private final Object[] data;

    public OwnerListComparisonTest(boolean expectation, Object[] data) {
        this.expectation = expectation;
        this.data = data;
    }

    @Test
    public void testComparison() throws Exception {
        String name = "";
        List<OwnershipStatistics.Owner> owners = new ArrayList<OwnershipStatistics.Owner>();
        OwnerList list1 = new OwnerList(owners);

        int i = 0;
        for (Object d : data) {
            if (d == null) {
                list1 = new OwnerList(owners);
                owners = new ArrayList<OwnershipStatistics.Owner>();
                i = 0;
                continue;
            }

            if (i%2 == 0)
                name = (String)d;
            else
                owners.add(new OwnershipStatistics.Owner(name, (Double)d, 0));

            i++;
        }
        OwnerList list2 = new OwnerList(owners);


        Assert.assertEquals(expectation, list1.equals(list2));
    }
}
