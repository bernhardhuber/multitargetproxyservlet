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

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.status;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 *
 * @author pi
 */
public class MediatorTest {

    @RegisterExtension
    static WireMockExtension wmExtension = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort().dynamicHttpsPort())
            .build();

    /**
     * Test of service method, of class Mediator.
     *
     * @param wmRuntimeInfo
     * @throws java.lang.Exception
     */
    @ParameterizedTest
    @MethodSource
    /*default*/ void testServiceWithHttpGet(int statusCode, String statusMessage) throws Exception {
        WireMockRuntimeInfo wmRuntimeInfo = wmExtension.getRuntimeInfo();

        String baseUrl = wmRuntimeInfo.getHttpBaseUrl();
        String uriASlash = "/aGET";
        String responseADashContent = "a-content";
        String method = "GET";

        Config config = new Config();
        wmExtension.stubFor(get(uriASlash)
                .willReturn(status(statusCode).withBody(responseADashContent)));

        Env env = new Env.Builder().targetUri(baseUrl + uriASlash).build();
        assertTrue(baseUrl.contains(env.getTargetHost().getHostName()), env.getTargetHost().toURI());
        assertTrue(baseUrl.contains("" + env.getTargetHost().getPort()), env.getTargetHost().toURI());

        //---
        HttpServletRequest servletRequest = Mockito.mock(HttpServletRequest.class);
        when(servletRequest.getMethod()).thenReturn(method);
        when(servletRequest.getRequestURI()).thenReturn(uriASlash);
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

            verify(servletResponse, times(1)).setStatus(statusCode, statusMessage);
            assertEquals(responseADashContent, basos.baos.toString("UTF-8"));
        }
    }

    static Stream<Arguments> testServiceWithHttpGet() {
        return Stream.of(
                Arguments.of(200, "OK"),
                Arguments.of(400, "Bad Request"),
                Arguments.of(401, "Unauthorized"),
                Arguments.of(402, "Payment Required"),
                Arguments.of(403, "Forbidden"),
                Arguments.of(404, "Not Found"),
                Arguments.of(500, "Server Error")
        );
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
