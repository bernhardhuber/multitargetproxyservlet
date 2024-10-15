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

import java.util.Properties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author pi
 */
public class ConfigTest {

    @Test
    public void testConfigFromPropertiesBuilder() {
        Properties props = new PropertiesBuilder(new Properties())
                //---
                .put("doLog", "false")
                .put("doHandleCompression", "true")
                .put("doPreserveHost", "true")
                .put("doPreserveCookies", "true")
                .put("doForwardIP", "true")
                .put("doSendUrlFragment", "true")
                .put("doHandleRedirects", "true")
                //---
                .put("useSystemProperties", "true")
                .put("connectTimeout", "1")
                .put("readTimeout", "2")
                .put("connectionRequestTimeout", "3")
                .put("maxConnections", "4")
                .build();

        Config config = new Config.ConfigFromPropertiesBuilder()
                .props(props)
                .build();
        assertAll(
                () -> assertFalse(config.isDoLog()),
                () -> assertTrue(config.isDoHandleCompression()),
                () -> assertTrue(config.isDoPreserveHost()),
                () -> assertTrue(config.isDoPreserveCookies()),
                () -> assertTrue(config.isDoForwardIP()),
                () -> assertTrue(config.isDoHandleRedirects()),
                //---
                () -> assertTrue(config.isUseSystemProperties()),
                () -> assertEquals(1, config.getConnectTimeout()),
                () -> assertEquals(2, config.getReadTimeout()),
                () -> assertEquals(3, config.getConnectionRequestTimeout()),
                () -> assertEquals(4, config.getMaxConnections())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "doLog: true",
        "doHandleCompression: false",
        "doPreserveHost: false",
        "doPreserveCookies: false",
        "doForwardIP: false",
        "doSendUrlFragment: false",
        "doHandleRedirects: false",
        //---
        "useSystemProperties: false",
        "connectTimeout: -1",
        "readTimeout: -1",
        "connectionRequestTimeout: -1",
        "maxConnections: -1",})
    public void testFormatConfigValues(String expectedConfigNameValue) {
        Config config = new Config();
        String s = config.formatConfigValues();
        assertNotNull(s);
        assertTrue(!s.isEmpty(), s);
        assertTrue(!s.isBlank(), s);
        assertTrue(s.contains(expectedConfigNameValue), s);
    }

}
