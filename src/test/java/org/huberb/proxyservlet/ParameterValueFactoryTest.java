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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import javax.servlet.ServletConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 *
 * @author pi
 */
public class ParameterValueFactoryTest {

    /**
     * Test of setupTargetUrisFromServletInitParameter method, of class
     * ParameterValueFactory.
     */
    @Test
    public void testSetupTargetUrisFromServletInitParameter() {
        ServletConfig servletConfig = Mockito.mock(ServletConfig.class);
        when(servletConfig.getInitParameter("targetUri")).thenReturn("targetUriValue1");
        // targetUris
        when(servletConfig.getInitParameter("targetUris")).thenReturn("targetUrisValue1, targetUrisValue2 ,targetUrisValue3");
        // targetUri.
        when(servletConfig.getInitParameter("targetUri.1")).thenReturn("targetUriDotValue1");
        when(servletConfig.getInitParameter("targetUri.2")).thenReturn("targetUriDotValue2");
        when(servletConfig.getInitParameter("targetUri.3")).thenReturn("targetUriDotValue3");
        when(servletConfig.getInitParameterNames()).thenReturn(
                new Vector<String>(
                        Arrays.asList("targetUri", "targetUris", "targetUri.1", "targetUri.2", "targetUri.3"))
                        .elements()
        );
        ParameterValueFactory instance = new ParameterValueFactory();
        List<String> result = instance.setupTargetUrisFromServletInitParameter(servletConfig);
        assertAll(
                () -> assertEquals(7, result.size()),
                () -> assertEquals("targetUriValue1", result.get(0)),
                // targetUris
                () -> assertEquals("targetUrisValue1", result.get(1)),
                () -> assertEquals("targetUrisValue2", result.get(2)),
                () -> assertEquals("targetUrisValue3", result.get(3)),
                // targetUri.
                () -> assertEquals("targetUriDotValue1", result.get(4)),
                () -> assertEquals("targetUriDotValue2", result.get(5)),
                () -> assertEquals("targetUriDotValue3", result.get(6))
        );
    }

    /**
     * Test of setupTargetUrisFromProperties method, of class
     * ParameterValueFactory.
     */
    @Test
    public void testSetupTargetUrisFromProperties() {
        Properties props = new PropertiesBuilder(new Properties())
                .put("targetUri", "targetUriValue1")
                // targetUris
                .put("targetUris", "targetUrisValue1, targetUrisValue2 ,targetUrisValue3")
                // targetUri.
                .put("targetUri.1", "targetUriDotValue1")
                .put("targetUri.2", "targetUriDotValue2")
                .put("targetUri.3", "targetUriDotValue3")
                .build();
        ParameterValueFactory instance = new ParameterValueFactory();
        List<String> result = instance.setupTargetUrisFromProperties(props);
        assertAll(
                () -> assertEquals(7, result.size()),
                () -> assertEquals("targetUriValue1", result.get(0)),
                // targetUris
                () -> assertEquals("targetUrisValue1", result.get(1)),
                () -> assertEquals("targetUrisValue2", result.get(2)),
                () -> assertEquals("targetUrisValue3", result.get(3)),
                // targetUri.
                () -> assertEquals("targetUriDotValue1", result.get(4)),
                () -> assertEquals("targetUriDotValue2", result.get(5)),
                () -> assertEquals("targetUriDotValue3", result.get(6))
        );

    }

    /**
     * Test of setupTargetUrisFromFunction method, of class
     * ParameterValueFactory.
     */
    @Test
    public void testSetupTargetUrisFromFunction_targeUri() {
        UnaryOperator<String> f1 = key -> {
            if ("targetUri".equals(key)) {
                return "targetUriValue";
            } else {
                return null;
            }
        };
        Supplier<List<String>> supp1 = () -> Collections.singletonList("targetUri");
        ParameterValueFactory instance = new ParameterValueFactory();
        List<String> result = instance.setupTargetUrisFromFunction(f1, supp1);
        assertAll(
                () -> assertEquals(1, result.size()),
                () -> assertEquals("targetUriValue", result.get(0))
        );
    }

    /**
     * Test of setupTargetUrisFromFunction method, of class
     * ParameterValueFactory.
     */
    @Test
    public void testSetupTargetUrisFromFunction_targeUris() {
        UnaryOperator<String> f1 = key -> {
            if ("targetUris".equals(key)) {
                return "targetUriValue1,targetUriValue2,targetUriValue3";
            } else {
                return null;
            }
        };
        Supplier<List<String>> supp1 = () -> Collections.singletonList("targetUris");
        ParameterValueFactory instance = new ParameterValueFactory();
        List<String> result = instance.setupTargetUrisFromFunction(f1, supp1);
        assertAll(
                () -> assertEquals(3, result.size()),
                () -> assertEquals("targetUriValue1", result.get(0)),
                () -> assertEquals("targetUriValue2", result.get(1)),
                () -> assertEquals("targetUriValue3", result.get(2))
        );
    }

    /**
     * Test of setupTargetUrisFromFunction method, of class
     * ParameterValueFactory.
     */
    @Test
    public void testSetupTargetUrisFromFunction_targeUrisDot() {
        UnaryOperator<String> f1 = key -> {
            if (null == key) {
                return null;
            } else {
                switch (key) {
                    case "targetUri.1":
                        return "targetUriValue1";
                    case "targetUri.2":
                        return "targetUriValue2";
                    case "targetUri.3":
                        return "targetUriValue3";
                    default:
                        return null;
                }
            }
        };
        Supplier<List<String>> supp1 = () -> Arrays.asList(
                "targetUri.1",
                "targetUri.2",
                "targetUri.3"
        );
        ParameterValueFactory instance = new ParameterValueFactory();
        List<String> result = instance.setupTargetUrisFromFunction(f1, supp1);
        assertAll(
                () -> assertEquals(3, result.size()),
                () -> assertEquals("targetUriValue1", result.get(0)),
                () -> assertEquals("targetUriValue2", result.get(1)),
                () -> assertEquals("targetUriValue3", result.get(2))
        );
    }

}
