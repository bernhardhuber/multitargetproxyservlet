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
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.AbortableHttpRequest;
import org.apache.http.util.EntityUtils;

/**
 * Coordinate processing proxy request, and proxy response.
 *
 * @author pi
 */
public class Mediator {

    private final Config config;
    private final Env env;
    private final int i;

    public Mediator(Config config, Env env, int i) {
        this.config = config;
        this.env = env;
        this.i = i;
    }

    /**
     * Send a proxy request to target, and process the response.
     *
     * @param servletRequest
     * @param servletResponse
     * @throws IOException
     * @throws ServletException
     */
    public void service(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException, ServletException {

        // 1 Create the proxy request
        HttpRequest proxyRequest = new HttpRequestProxyFactory(config, env).createHttpRequest(servletRequest);

        HttpResponse proxyResponse = null;
        try {
            // 2 Execute the proxy request
            proxyResponse = new HttpClientExecutor(config, env).doExecute(servletRequest, proxyRequest);

            // 3 Send the response 
            if (sendTheHttpResponse(i)) {
                new HttpResponseProxyFactory(config, env).sendResponse(servletRequest, servletResponse, proxyResponse);
            }
        } catch (Exception e) {
            if (sendTheHttpResponse(i)) {
                handleRequestException(proxyRequest, proxyResponse, e);
            }
        } finally {
            // make sure the entire entity was consumed, so the connection is released
            if (proxyResponse != null) {
                EntityUtils.consumeQuietly(proxyResponse.getEntity());
            }
            //Note: Don't need to close servlet outputStream:
            // http://stackoverflow.com/questions/1159168/should-one-call-close-on-httpservletresponse-getoutputstream-getwriter
        }
    }

    boolean sendTheHttpResponse(int i) {
        return i == 0;
    }

    protected void handleRequestException(HttpRequest proxyRequest, HttpResponse proxyResonse, Exception e) throws ServletException, IOException {
        //abort request, according to best practice with HttpClient
        if (proxyRequest instanceof AbortableHttpRequest) {
            AbortableHttpRequest abortableHttpRequest = (AbortableHttpRequest) proxyRequest;
            abortableHttpRequest.abort();
        }
        // If the response is a chunked response, it is read to completion when
        // #close is called. If the sending site does not timeout or keeps sending,
        // the connection will be kept open indefinitely. Closing the respone
        // object terminates the stream.
        if (proxyResonse instanceof Closeable) {
            ((Closeable) proxyResonse).close();
        }
        if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
        }
        if (e instanceof ServletException) {
            throw (ServletException) e;
        }
        //noinspection ConstantConditions
        if (e instanceof IOException) {
            throw (IOException) e;
        }
        throw new RuntimeException(Mediator.class.getSimpleName(),e);
    }
}
