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

import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public class OwnerListInToleranceTest {

    @Parameterized.Parameters
    public static Collection<Object[]> tolerances() {
        return Arrays.asList(new Object[][]{
                {1.00, 0.00, false},
                {1.00, 1.00, true},
                {1.00, 0.90, true},
                {1.00, 0.76, true},
                {1.00, 0.70, false},
                {0.90, 0.70, true},
                {0.00, 0.15, false},
                {0.00, 0.09, true},
        });
    }

    private final double w1;
    private final  double w2;
    private final boolean result;

    public OwnerListInToleranceTest(double w1, double w2, boolean result) {
        this.w1 = w1;
        this.w2 = w2;
        this.result = result;
    }

    @Test
    public void testInTolerance() throws Exception {
        OwnershipStatistics.Owner o1 = new OwnershipStatistics.Owner("a", w1, 0);
        OwnershipStatistics.Owner o2 = new OwnershipStatistics.Owner("a", w2, 0);

        Assert.assertEquals(result, OwnerList.isInTolerance(o1, o2));
    }

}
