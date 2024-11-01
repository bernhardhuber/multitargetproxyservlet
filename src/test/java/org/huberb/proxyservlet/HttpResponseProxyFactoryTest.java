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

import java.io.IOException;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 *
 * @author pi
 */
public class HttpResponseProxyFactoryTest {

    Env env = new Env.Builder().targetUri("http://localhost/targetUri").build();
    Config config = new Config();
    HttpResponseProxyFactory instance;

    @BeforeEach
    /*default*/ void createInstance() {
        instance = new HttpResponseProxyFactory(config, env);
    }

    @Test
    /*default*/ void testSendResponse_only_headers() throws IOException {
        final int statusOk = 200;
        final String headerNameX = "X-HttpResponseProxyFactoryTest";
        final String headerValueX = "value1";
        final String headerNameTransferEncoding = "Transfer-Encoding";

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        HttpResponse proxyResponse = new DefaultHttpResponseFactory()
                .newHttpResponse(new ProtocolVersion("HTTP", 1, 1),
                        statusOk,
                        new BasicHttpContext());

        proxyResponse.addHeader(headerNameX, headerValueX);

        proxyResponse.addHeader(headerNameTransferEncoding, headerNameTransferEncoding);
        instance.sendResponse(servletRequest, servletResponse, proxyResponse);

        verify(servletResponse, times(1)).setStatus(statusOk, "OK");
        verify(servletResponse, times(1)).addHeader(headerNameX, headerValueX);
        verify(servletResponse, times(0)).addHeader(headerNameTransferEncoding, headerNameTransferEncoding);
    }

    @ParameterizedTest
    @MethodSource
    /*default*/ void testSendResponse_vary_status(int statusCode, String statusMessage) throws IOException {
        final int statusOk = statusCode;
        final String headerNameX = "X-HttpResponseProxyFactoryTest";
        final String headerValueX = "value1";
        final String headerNameTransferEncoding = "Transfer-Encoding";

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        HttpResponse proxyResponse = new DefaultHttpResponseFactory()
                .newHttpResponse(new ProtocolVersion("HTTP", 1, 1),
                        statusOk,
                        new BasicHttpContext());

        proxyResponse.addHeader(headerNameX, headerValueX);

        proxyResponse.addHeader(headerNameTransferEncoding, headerNameTransferEncoding);
        instance.sendResponse(servletRequest, servletResponse, proxyResponse);

        verify(servletResponse, times(1)).setStatus(statusOk, statusMessage);
        verify(servletResponse, times(1)).addHeader(headerNameX, headerValueX);
        verify(servletResponse, times(0)).addHeader(headerNameTransferEncoding, headerNameTransferEncoding);
    }

    static Stream<Arguments> testSendResponse_vary_status() {
        return Stream.of(
                Arguments.of(200, "OK"),
                Arguments.of(404, "Not Found"),
                Arguments.of(500, "Internal Server Error")
        );
    }
}
