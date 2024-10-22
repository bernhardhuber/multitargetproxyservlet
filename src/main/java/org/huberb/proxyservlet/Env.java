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

import java.net.URI;

import org.apache.http.HttpHost;
import org.apache.http.client.utils.URIUtils;

/**
 *
 * @author pi
 */
public class Env {

    private static final String MULTIPROXYSERVLET = "multi-proxy-servlet";

    private final String targetUri;
    private final HttpHost targetHost;

    Env(HttpHost targetHost, String targetUri) {
        this.targetHost = targetHost;
        this.targetUri = targetUri;
    }

    public String getTargetUri() {
        return targetUri;
    }

    public HttpHost getTargetHost() {
        return targetHost;
    }

    /**
     * The string prefixing rewritten cookies.
     *
     * @return
     */
    public String getCookieNamePrefix() {
        return "!Proxy!" + MULTIPROXYSERVLET;
    }

    static class Builder {

        String targetUri;

        Builder targetUri(String targetUri) {
            this.targetUri = targetUri;
            return this;
        }

        Env build() {
            if (targetUri == null) {
                throw new IllegalArgumentException("targetUri is required.");
            }

            //test it's valid
            try {
                URI targetUriObj = new URI(targetUri);
                HttpHost targetHost = URIUtils.extractHost(targetUriObj);

                if (targetHost == null) {
                    String exMessage = String.format("Cannot extract targetHost from targetUri %s", targetUri);
                    throw new IllegalArgumentException(exMessage);
                }

                return new Env(targetHost, targetUri);
            } catch (Exception e) {
                String exMessage = String.format("Cannot extract targetHost from targetUri %s", targetUri);
                throw new IllegalArgumentException(exMessage, e);
            }

        }
    }
}
