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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author pi
 */
public class MultiProxyingServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(MultiProxyingServlet.class.getName());
    protected boolean doLog;
    private Config config;

    @Override
    public void init() throws ServletException {
        this.config = new Config.ConfigFromServletConfigBuilder()
                .servletConfig(this.getServletConfig())
                .build();
        LOG.info(this.config::formatConfigValues);
        this.doLog = this.config.isDoLog();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    void process(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String uriPrefix = String.format("%s://%s:%d%s",
                req.getScheme(),
                req.getLocalName(), req.getLocalPort(),
                req.getContextPath()
        );
        List<String> targetUriList = createTargetUriList(uriPrefix);
        for (int i = 0; i < targetUriList.size(); i += 1) {
            String targetUri = targetUriList.get(i);
            if (doLog) {
                String msg = String.format("Processing %d: targetUri %s", i, targetUri);
                LOG.info(msg);
            }
            Env env = new Env.Builder().targetUri(targetUri).build();
            new Mediator(config, env).service(i, req, resp);
        }
    }

    List<String> createTargetUriList(String uriPrefix) {
        List<String> l = new ArrayList<>();
        l.addAll(new ParameterValueFactory()
                .setupTargetUrisFromServletInitParameter(this.getServletConfig()));
        if (l.isEmpty()) {
            l.addAll(createDefaultTargetUriList(uriPrefix));
        }

        return l.stream()
                .map(s -> s.startsWith("/") ? uriPrefix + s : s)
                .collect(Collectors.toList());
    }

    List<String> createDefaultTargetUriList(String uriPrefix) {
        return Arrays.asList(
                "/multiproxyingservlet/a.html",
                "/multiproxyingservlet/b.html",
                "/multiproxyingservlet/c.html",
                "/multiproxyingservlet/d.html",
                "/multiproxyingservlet/e.html",
                "/multiproxyingservlet/f.html"
        ).stream()
                .map(s -> uriPrefix + s)
                .collect(Collectors.toList());
    }
}
