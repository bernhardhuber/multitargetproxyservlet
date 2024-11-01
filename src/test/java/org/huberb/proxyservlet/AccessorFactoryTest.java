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

import org.huberb.proxyservlet.Config.AccessorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author pi
 */
public class AccessorFactoryTest {

    private Config config;
    private AccessorFactory instance;

    @BeforeEach
    /* default */ void createInstance() {
        config = new Config();
        instance = new AccessorFactory(config);
    }

    @Test
    /* default */ void testUnknownKey() {
        instance.setKeyValue("x", "y");
    }

    @ParameterizedTest
    @CsvSource({
        "'true',  true",
        "'false', false",
        "'TRUE',  true",
        "'FALSE', false",
        "''   , false",
        "     , false"
    })
    /* default */ void testDoLogBooleanValue(String value, boolean expected) {
        instance.setKeyValue("doLog", value);
        assertEquals(expected, config.isDoLog());
    }

    @ParameterizedTest
    @CsvSource({
        "'0',  0",
        "'100', 100",
        "'-1',  -1",
        "'10000', 10000",
        "'ABC', -1",
        "'', -1"
    })
    /* default */ void testConnectionTimeoutIntegerValue(String value, int expected) {
        instance.setKeyValue("connectTimeout", value);
        assertEquals(expected, config.getConnectTimeout());
    }

}
