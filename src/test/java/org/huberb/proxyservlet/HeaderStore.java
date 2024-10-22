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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * utility for mimicking Header API
 */
class HeaderStore {

    Map<String, String> values = new HashMap<>();

    HeaderStore put(String k, String v) {
        values.put(k, v);
        return this;
    }

    Enumeration<String> getHeaderNames() {
        Vector<String> vec = new Vector<>();
        values.keySet().forEach(k -> vec.add(k));
        return vec.elements();
    }

    String getHeader(String key) {
        return values.get(key);
    }

    Enumeration<String> getHeaderNames(String key) {
        Vector<String> vec = new Vector<>();
        String value = values.get(key);
        if (value != null) {
            vec.add(value);
        }
        return vec.elements();
    }

}
