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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author pi
 */
@WireMockTest
public class MediatorTest {

    /**
     * Test of service method, of class Mediator.
     *
     * @param wmRuntimeInfo
     * @throws java.lang.Exception
     */
    @Test
    /*default*/ void testServiceWithHttpGet_ok(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
        String baseUrl = wmRuntimeInfo.getHttpBaseUrl();
        String uriASlash = "/aGET";
        String responseADashContent = "a-content";
        String method = "GET";

        Config config = new Config();
        WireMock.stubFor(get(uriASlash)
                .willReturn(ok().withBody(responseADashContent)));

        Env env = new Env.Builder().targetUri(baseUrl + uriASlash).build();
        assertTrue(baseUrl.contains(env.getTargetHost().getHostName()), env.getTargetHost().toURI());
        assertTrue(baseUrl.contains("" + env.getTargetHost().getPort()), env.getTargetHost().toURI());

        //---
        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        when(servletRequest.getMethod()).thenReturn(method);
        when(servletRequest.getRequestURI()).thenReturn(uriASlash);
        when(servletRequest.getMethod()).thenReturn("GET");
        when(servletRequest.getProtocol()).thenReturn("HTTP/1.1");
        HeaderStore hs = new HeaderStore();
        when(servletRequest.getHeaderNames()).thenReturn(hs.getHeaderNames());
        when(servletRequest.getHeader(anyString())).thenReturn(null);

        //---
        HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);

        try (ByteArrayServletOutputStream basos = new ByteArrayServletOutputStream()) {

            when(servletResponse.getOutputStream()).thenReturn(basos);
            //---
            Mediator instance = new Mediator(config, env);
            instance.service(0, servletRequest, servletResponse);

            verify(servletResponse, times(1)).setStatus(200, "OK");
            assertEquals(responseADashContent, basos.baos.toString("UTF-8"));
        }
    }

    @Test
    /*default*/ void testServiceWithHttpGet_500(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {
        String baseUrl = wmRuntimeInfo.getHttpBaseUrl();
        String uriASlash = "/aGET";
        String responseADashContent = "a-content";
        String method = "GET";

        Config config = new Config();
        WireMock.stubFor(get(uriASlash)
                .willReturn(status(500).withBody(responseADashContent)));

        Env env = new Env.Builder().targetUri(baseUrl + uriASlash).build();
        assertTrue(baseUrl.contains(env.getTargetHost().getHostName()), env.getTargetHost().toURI());
        assertTrue(baseUrl.contains("" + env.getTargetHost().getPort()), env.getTargetHost().toURI());

        //---
        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        when(servletRequest.getMethod()).thenReturn(method);
        when(servletRequest.getRequestURI()).thenReturn(uriASlash);
        when(servletRequest.getMethod()).thenReturn("GET");
        when(servletRequest.getProtocol()).thenReturn("HTTP/1.1");
        HeaderStore hs = new HeaderStore();
        when(servletRequest.getHeaderNames()).thenReturn(hs.getHeaderNames());
        when(servletRequest.getHeader(anyString())).thenReturn(null);

        //---
        HttpServletResponse servletResponse = Mockito.mock(HttpServletResponse.class);

        try (ByteArrayServletOutputStream basos = new ByteArrayServletOutputStream()) {

            when(servletResponse.getOutputStream()).thenReturn(basos);
            //---
            Mediator instance = new Mediator(config, env);
            instance.service(0, servletRequest, servletResponse);

            verify(servletResponse, times(1)).setStatus(500, "Server Error");
            assertEquals(responseADashContent, basos.baos.toString("UTF-8"));
        }
    }

    static class ByteArrayServletOutputStream extends ServletOutputStream {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
        }

        @Override
        public void write(int b) throws IOException {
            baos.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            baos.write(b, off, len);
        }

        @Override
        public void write(byte[] b) throws IOException {
            baos.write(b);
        }

        @Override
        public void close() throws IOException {
            baos.close();
        }

        @Override
        public void flush() throws IOException {
            baos.flush();
        }

    };
}
