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
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.protocol.BasicHttpContext;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 *
 * @author pi
 */
public class HttpResponseProxyFactoryTest {

    @Test
    /*default*/ void testSendResponse() throws IOException {
        Env env = new Env.Builder().targetUri("http://localhost/targetUri").build();
        Config config = new Config();
        HttpResponseProxyFactory instance = new HttpResponseProxyFactory(config, env);

        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        HttpResponse proxyResponse = new DefaultHttpResponseFactory()
                .newHttpResponse(new ProtocolVersion("HTTP", 1, 1),
                        200,
                        new BasicHttpContext());
        instance.sendResponse(servletRequest, servletResponse, proxyResponse);

        verify(servletResponse, times(1)).setStatus(200, "OK");
    }

}
