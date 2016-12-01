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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ScopePathTreeTest {
    ScopePathTree<String> tree;

    @Before
    public void setUp() {
        tree = new ScopePathTree<String>(new ScopePath(""));
    }

    @Test
    public void setAndGetTest() throws Exception {
        ScopePath paths[] = new ScopePath[] {
                new ScopePath("a"),
                new ScopePath("b"),
                new ScopePath("a.b.c"),
                new ScopePath("a.b.d"),
                new ScopePath("b.c.d.e.f")
        };

        for (ScopePath path : paths) {
            tree.set(path, "val" + path);
            Assert.assertEquals(tree.findClosest(path).getPath(), path);
            Assert.assertEquals(tree.findClosest(path).get(), "val" + path);
        }

        Assert.assertEquals(tree.findClosest(new ScopePath("a.b.f")).getPath(), new ScopePath("a.b"));
        Assert.assertEquals(tree.findClosest(new ScopePath("x")).getPath(), new ScopePath(""));

        Assert.assertEquals(tree.findClosest(new ScopePath("b.c")).get(), null);

        Assert.assertEquals(tree.findClosest(new ScopePath("a.b.c")).countChildren(), 0);
        Assert.assertEquals(tree.findClosest(new ScopePath("a.b")).countChildren(), 2);
        Assert.assertEquals(tree.findClosest(new ScopePath("a")).countChildren(), 3);
        Assert.assertEquals(tree.findClosest(new ScopePath("b.c.d.e")).countChildren(), 1);
        Assert.assertEquals(tree.findClosest(new ScopePath("b")).countChildren(), 4);
        Assert.assertEquals(tree.countChildren(), 9);
    }

    @Test
    public void removeTest() throws Exception {
        ScopePath paths[] = new ScopePath[] {
                new ScopePath("a"),
                new ScopePath("a.b"),
                new ScopePath("c")
        };

        for (ScopePath path : paths)
            tree.set(path, "val" + path);

        ScopePath path = new ScopePath("c");
        tree.findClosest(path).remove();
        Assert.assertEquals(tree.findClosest(path).getPath(), new ScopePath(""));

        path = new ScopePath("a");
        tree.findClosest(path).remove();
        Assert.assertEquals(tree.findClosest(path).getPath(), new ScopePath("a"));
        Assert.assertEquals(tree.findClosest(path).get(), null);

        path = new ScopePath("a.b");
        tree.findClosest(path).remove();
        Assert.assertEquals(tree.findClosest(path).getPath(), new ScopePath(""));
        Assert.assertEquals(tree.findClosest(path).get(), null);
    }
}
