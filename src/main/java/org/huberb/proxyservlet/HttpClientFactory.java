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

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClientBuilder;

/**
 * Create a {@link HttpClient} instance.
 *
 * @author pi
 */
public class HttpClientFactory {

    //---
    protected boolean doHandleCompression = false;
    protected boolean doHandleRedirects = false;
    //---
    protected boolean useSystemProperties = false;
    protected int connectTimeout = -1;
    protected int readTimeout = -1;
    protected int connectionRequestTimeout = -1;
    protected int maxConnections = -1;
    private final Config config;

    public HttpClientFactory(Config config) {
        this.config = config;
        //---
        this.doHandleCompression = config.isDoHandleCompression();
        this.doHandleRedirects = config.isDoHandleRedirects();
        //---
        this.useSystemProperties = config.isUseSystemProperties();
        this.connectTimeout = config.getConnectTimeout();
        this.readTimeout = config.getReadTimeout();
        this.connectionRequestTimeout = config.getConnectionRequestTimeout();
        this.maxConnections = config.getMaxConnections();
    }

    /**
     * HttpClient offers many opportunities for customization.
     * <p>
     * In any case, it should be thread-safe.
     *
     * @return
     */
    public HttpClient createHttpClient() {
        HttpClientBuilder clientBuilder = HttpClientBuilder
                .create()
                .setDefaultRequestConfig(buildRequestConfig())
                .setDefaultSocketConfig(buildSocketConfig());

        clientBuilder.setMaxConnTotal(maxConnections);
        clientBuilder.setMaxConnPerRoute(maxConnections);
        if (!doHandleCompression) {
            clientBuilder.disableContentCompression();
        }

        if (useSystemProperties) {
            clientBuilder = clientBuilder.useSystemProperties();
        }
        return clientBuilder.build();
    }

    /**
     * Sub-classes can override specific behaviour of
     * {@link org.apache.http.client.config.RequestConfig}.
     *
     * @return
     */
    protected RequestConfig buildRequestConfig() {
        return RequestConfig.custom()
                .setRedirectsEnabled(doHandleRedirects)
                .setCookieSpec(CookieSpecs.IGNORE_COOKIES) // we handle them in the servlet instead
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(readTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .build();
    }

    /**
     * Sub-classes can override specific behaviour of
     * {@link org.apache.http.config.SocketConfig}.
     *
     * @return
     */
    protected SocketConfig buildSocketConfig() {

        if (readTimeout < 1) {
            return null;
        }

        return SocketConfig.custom()
                .setSoTimeout(readTimeout)
                .build();
    }

}
