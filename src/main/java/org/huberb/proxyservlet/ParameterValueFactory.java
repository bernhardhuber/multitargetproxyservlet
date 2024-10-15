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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javax.servlet.ServletConfig;

/**
 *
 * @author pi
 */
public class ParameterValueFactory {

    private static final String TARGET_URI = "targetUri";
    private static final String TARGET_URIS = "targetUris";
    private static final String TARGET_URI_DOT = "targetUri.";

    /**
     * Extract parameters from servlet init parameters.
     *
     * @param servletConfig
     * @return
     */
    public List<String> setupTargetUrisFromServletInitParameter(ServletConfig servletConfig) {
        UnaryOperator<String> f1 = servletConfig::getInitParameter;
        Supplier<List<String>> supp1 = () -> {
            List<String> l = new ArrayList<>();
            servletConfig.getInitParameterNames().asIterator().forEachRemaining(key -> {
                if (key != null && !key.isEmpty()) {
                    l.add(key);
                }
            });
            return l;
        };
        return setupTargetUrisFromFunction(f1, supp1);
    }

    /**
     * Extract parameters from properties.
     *
     * @param props
     * @return
     */
    public List<String> setupTargetUrisFromProperties(Properties props) {
        UnaryOperator<String> f1 = props::getProperty;
        Supplier<List<String>> supp1 = () -> {
            List<String> l = new ArrayList<>();
            props.keySet().forEach(key -> {
                if (key != null && key instanceof String) {
                    l.add((String) key);
                }
            });
            return l;
        };
        return setupTargetUrisFromFunction(f1, supp1);
    }

    protected List<String> setupTargetUrisFromFunction(UnaryOperator<String> f1, Supplier<List<String>> supp1) {
        List<String> targetUris = new ArrayList<>();

        // single targetUri value
        Optional.ofNullable(f1.apply(TARGET_URI))
                .ifPresent(targetUris::add);

        // multi targetUri values in single parameter
        // like targetUri, targetUri, ...
        Optional.ofNullable(f1.apply(TARGET_URIS))
                .ifPresent(paramValue -> {
                    String[] values = paramValue.split(",");
                    Stream.of(values).forEach(singleParamValue -> {
                        String trimmed = singleParamValue.trim();
                        if (!trimmed.isEmpty()) {
                            targetUris.add(trimmed);
                        }
                    });
                });

        // multi targetUri values in multiple parameter names,
        // like targetUri.NNN
        List<OrderElement> l = new ArrayList<>();
        for (String parameterName : supp1.get()) {
            if (parameterName.startsWith(TARGET_URI_DOT)) {
                try {
                    int order = Integer.parseInt(parameterName.substring(TARGET_URI_DOT.length()));
                    String value = f1.apply(parameterName);
                    if (value != null && !value.isBlank()) {
                        l.add(new OrderElement(order, value));
                    }
                } catch (NumberFormatException nfex) {
                    // continue
                }
            }
        }

        targetUris.addAll(OrderElement.buildSortedList(l));
        return targetUris;

    }

    static class OrderElement {

        final int order;
        final String value;

        public OrderElement(int order, String value) {
            this.order = order;
            this.value = value.trim();
        }

        static List<String> buildSortedList(List<OrderElement> l) {
            Collections.sort(l, (e1, e2) -> e1.order - e2.order);
            List<String> l2 = new ArrayList<>();
            l.forEach(e -> l2.add(e.value));
            return l2;
        }
    }
}
