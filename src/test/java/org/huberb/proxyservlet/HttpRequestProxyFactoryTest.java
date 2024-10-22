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
import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 * @author pi
 */
public class HttpRequestProxyFactoryTest {

    @Test
    /*default*/ void test_createHttpRequest_GET() throws IOException {
        Env env = new Env.Builder().targetUri("http://localhost/targetUri").build();
        Config config = new Config();
        HttpRequestProxyFactory instance = new HttpRequestProxyFactory(config, env);

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getMethod()).thenReturn("GET");
        HeaderStore hs = new HeaderStore();
        when(servletRequest.getHeaderNames()).thenReturn(hs.getHeaderNames());
        when(servletRequest.getHeader(anyString())).thenReturn(null);

        HttpRequest httpRequest = instance.createHttpRequest(servletRequest);
        assertNotNull(httpRequest);
        assertEquals("GET http://localhost/targetUri HTTP/1.1", httpRequest.getRequestLine().toString());
        assertEquals("GET", httpRequest.getRequestLine().getMethod());
        assertEquals("http://localhost/targetUri", httpRequest.getRequestLine().getUri());
    }

    @Test
    /*default*/ void test_createHttpRequest_POST() throws IOException {
        Env env = new Env.Builder().targetUri("http://localhost/targetUri").build();
        Config config = new Config();
        HttpRequestProxyFactory instance = new HttpRequestProxyFactory(config, env);

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getMethod()).thenReturn("POST");
        HeaderStore hs = new HeaderStore()
                .put("CONTENT_LENGTH", "-1");
        when(servletRequest.getHeaderNames()).thenReturn(hs.getHeaderNames());
        when(servletRequest.getHeader("CONTENT_LENGTH")).thenReturn(hs.getHeader("CONTENT_LENGTH"));
        when(servletRequest.getHeaders("CONTENT_LENGTH")).thenReturn(hs.getHeaderNames("CONTENT_LENGTH"));

        HttpRequest httpRequest = instance.createHttpRequest(servletRequest);
        assertNotNull(httpRequest);
        assertEquals("POST http://localhost/targetUri HTTP/1.1", httpRequest.getRequestLine().toString());
        assertEquals("POST", httpRequest.getRequestLine().getMethod());
        assertEquals("http://localhost/targetUri", httpRequest.getRequestLine().getUri());
    }

}
