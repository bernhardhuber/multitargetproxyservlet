/*
 * Copyright 2024 pi.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.huberb.proxyservlet;

import org.huberb.proxyservlet.Config.Accessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author pi
 */
public class AccessorTest {

    static class ValuesHolder {

        int intValue;
        Accessor<Integer> accessorIntValue;

        Integer integerValue;
        Accessor<Integer> accessorIntegerValue;

        boolean booleanValue;
        Accessor<Boolean> accessorBooleanValue;

        Boolean booleanObjectValue;
        Accessor<Boolean> accessorBooleanObjectValue;

        static ValuesHolder create() {
            ValuesHolder valuesHolder = new ValuesHolder();

            // intValue, integerValue
            valuesHolder.accessorIntValue = new Accessor<>(
                    "intValue",
                    Integer.class,
                    v -> valuesHolder.intValue = v
            );
            valuesHolder.accessorIntegerValue = new Accessor<>(
                    "integerValue",
                    Integer.class,
                    v -> valuesHolder.integerValue = v
            );

            // booleanValue, booleanObjectValue
            valuesHolder.accessorBooleanValue = new Accessor<>(
                    "booleanValue",
                    Boolean.class,
                    v -> valuesHolder.booleanValue = v
            );
            valuesHolder.accessorBooleanObjectValue = new Accessor<>(
                    "booleanObjectValue",
                    Boolean.class,
                    v -> valuesHolder.booleanObjectValue = v
            );

            return valuesHolder;
        }
    }

    @Test
    /*default*/ void testIntAndInteger() {
        ValuesHolder valuesHolder = ValuesHolder.create();

        valuesHolder.accessorIntValue.consumer.accept(100);
        assertEquals(100, valuesHolder.intValue);

        valuesHolder.accessorIntegerValue.consumer.accept(101);
        assertEquals(101, valuesHolder.integerValue);
    }

    @Test
    /*default*/ void testBooleanAndBooleanObject() {
        ValuesHolder valuesHolder = ValuesHolder.create();

        valuesHolder.accessorBooleanValue.consumer.accept(true);
        assertEquals(true, valuesHolder.booleanValue);

        valuesHolder.accessorBooleanObjectValue.consumer.accept(true);
        assertEquals(true, valuesHolder.booleanObjectValue);
    }
}
