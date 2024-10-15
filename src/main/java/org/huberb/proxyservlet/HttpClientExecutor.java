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

import java.io.Closeable;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;

/**
 * Execute an HTTP request/response via {@link HttpClient}.
 *
 * @author pi
 */
public class HttpClientExecutor {

    private static final Logger LOG = Logger.getLogger(HttpClientExecutor.class.getName());
    protected boolean doLog = true;
    private final Config config;
    private final Env env;

    public HttpClientExecutor(Config config, Env env) {
        this.config = config;
        this.doLog = config.isDoLog();
        this.env = env;
    }

    public HttpResponse doExecute(HttpServletRequest servletRequest, HttpRequest proxyRequest) throws IOException {
        if (doLog) {
            String msg = String.format("httpclient execute: method: %s" + " uri: %s -- proxyRequest: %s",
                    servletRequest.getMethod(),
                    servletRequest.getRequestURI(),
                    proxyRequest.getRequestLine().getUri());
            LOG.info(msg);
        }
        HttpClient proxyClient = new HttpClientFactory(config).createHttpClient();
        try {
            HttpResponse httpResponse = proxyClient.execute(env.getTargetHost(), proxyRequest);
            if (doLog) {
                String msg = String.format("httpclient execute: status line %s", httpResponse.getStatusLine());
                LOG.info(msg);
            }
            return httpResponse;
        } finally {
            //Usually, clients implement Closeable:
            if (proxyClient instanceof Closeable) {
                try {
                    ((Closeable) proxyClient).close();
                } catch (IOException e) {
                    LOG.log(Level.WARNING, "shutting down HttpClient", e);
                }
            } else {
                //Older releases require we do this:
                if (proxyClient != null) {
                    proxyClient.getConnectionManager().shutdown();
                }
            }
        }
    }

}
