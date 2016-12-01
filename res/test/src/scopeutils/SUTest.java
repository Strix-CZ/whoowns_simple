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

package scopeutils;

public class SUTest {

    private SUTest() {
        int a = 0;
    }

    public void method() throws Exception {
        List<String> list = new ArrayList<String>() {{
            add(("someString"));
        }};
    }

    private SUTest(String description, Class<? extends ValuesSource> valuesSourceType) {
       this.description = description;
    }

    public enum testEnum {
        A(0);

        private static final testEnum[] VALUES = new testEnum[testEnum.values().length];

        static {
            for (testEnum value : testEnum.values()) {
                assert value.id() < VALUES.length && value.id() >= 0;
                VALUES[value.id()] = value;
            }
        }

        private final int id;

        testEnum(int id) {
            this.id = id;
        }

        public int id() {
            return this.id;
        }
    }
}