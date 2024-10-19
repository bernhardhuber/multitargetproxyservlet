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

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.message.BasicRequestLine;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 *
 * @author pi
 */
@WireMockTest
public class HttpClientExecutorTest {

    @Test
    public void testHttpGetWithConten(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        String baseUrl = wmRuntimeInfo.getHttpBaseUrl();
        String uriASlash = "/aGET";
        String responseADashContent = "a-content";
        String method = "GET";

        WireMock.stubFor(get(uriASlash)
                .willReturn(ok().withBody(responseADashContent)));

        Env env = new Env.Builder().targetUri(baseUrl + uriASlash).build();
        assertTrue(baseUrl.contains(env.getTargetHost().getHostName()), env.getTargetHost().toURI());
        assertTrue(baseUrl.contains("" + env.getTargetHost().getPort()), env.getTargetHost().toURI());

        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        when(servletRequest.getMethod()).thenReturn(method);
        when(servletRequest.getRequestURI()).thenReturn(uriASlash);
        HttpRequest proxyRequest = new BasicHttpRequest(
                new BasicRequestLine(method, uriASlash,
                        new ProtocolVersion("HTTP", 1, 1)
                )
        );
        HttpClientExecutor instance = new HttpClientExecutor(new Config(), env);
        HttpResponse httpResponse = instance.doExecute(servletRequest, proxyRequest);
        assertNotNull(httpResponse);
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());

        String content = EntityUtils.toString(httpResponse.getEntity());
        assertEquals(responseADashContent, content);
    }

    @Test
    public void testHttpPostWithConten(WireMockRuntimeInfo wmRuntimeInfo) throws IOException {
        String baseUrl = wmRuntimeInfo.getHttpBaseUrl();
        String uriASlash = "/aPOST";
        String responseADashContent = "a-content";
        String method = "POST";

        WireMock.stubFor(post(uriASlash)
                .willReturn(ok().withBody(responseADashContent)));

        Env env = new Env.Builder().targetUri(baseUrl + uriASlash).build();
        assertTrue(baseUrl.contains(env.getTargetHost().getHostName()), env.getTargetHost().toURI());
        assertTrue(baseUrl.contains("" + env.getTargetHost().getPort()), env.getTargetHost().toURI());

        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        when(servletRequest.getMethod()).thenReturn(method);
        when(servletRequest.getRequestURI()).thenReturn(uriASlash);
        HttpRequest proxyRequest = new BasicHttpRequest(
                new BasicRequestLine(method, uriASlash,
                        new ProtocolVersion("HTTP", 1, 1)
                )
        );
        HttpClientExecutor instance = new HttpClientExecutor(new Config(), env);
        HttpResponse httpResponse = instance.doExecute(servletRequest, proxyRequest);
        assertNotNull(httpResponse);
        assertEquals(200, httpResponse.getStatusLine().getStatusCode());

        String content = EntityUtils.toString(httpResponse.getEntity());
        assertEquals(responseADashContent, content);
    }

}
