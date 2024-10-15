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
import java.util.Set;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.HeaderGroup;

/**
 * Header that should not be copied.
 *
 * @author pi
 */
class HobByHopHeaders {

    private HobByHopHeaders() {
    }
    
    /**
     * These are the "hop-by-hop" headers that should not be copied.
     * <p>
     * I use an HttpClient @{link HeaderGroup} class instead of
     * {@link Set<String>} because this approach does case insensitive lookup
     * faster.
     *
     * @see http://www.w3.org/Protocols/rfc2616/rfc2616-sec13.html.
     */
    private static final HeaderGroup hopByHopHeaders;

    static {
        hopByHopHeaders = new HeaderGroup();
        Arrays.asList(
                "Connection",
                "Keep-Alive",
                "Proxy-Authenticate",
                "Proxy-Authorization",
                "TE",
                "Trailers",
                "Transfer-Encoding",
                "Upgrade")
                .forEach(header -> hopByHopHeaders.addHeader(new BasicHeader(header, null)));
    }

    static boolean containsHeader(String name) {
        return hopByHopHeaders.containsHeader(name);
    }

}
