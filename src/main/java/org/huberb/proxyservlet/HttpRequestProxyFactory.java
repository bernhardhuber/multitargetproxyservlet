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
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.apache.http.message.BasicHttpRequest;

/**
 *
 * @author pi
 */
public class HttpRequestProxyFactory {

    //-------------------------------------------------------------------------
    protected boolean doHandleCompression = false;
    protected boolean doPreserveHost = false;
    protected boolean doPreserveCookies = false;
    protected boolean doForwardIP = false;
    protected boolean doSendUrlFragment = false;
    private final Env env;

    public HttpRequestProxyFactory(Config config, Env env) {
        this.doHandleCompression = config.isDoHandleCompression();
        this.doPreserveHost = config.isDoPreserveHost();
        this.doPreserveCookies = config.isDoPreserveCookies();
        this.doForwardIP = config.isDoForwardIP();
        this.doSendUrlFragment = config.isDoSendUrlFragment();
        this.env = env;
    }

    public HttpRequest createHttpRequest(HttpServletRequest servletRequest) throws IOException {
        String method = servletRequest.getMethod();
        String proxyRequestUri = rewriteUrlFromRequest(servletRequest);
        HttpRequest proxyRequest;
        //spec: RFC 2616, sec 4.3: either of these two headers signal that there is a message body.
        if (servletRequest.getHeader(HttpHeaders.CONTENT_LENGTH) != null
                || servletRequest.getHeader(HttpHeaders.TRANSFER_ENCODING) != null) {
            proxyRequest = newProxyRequestWithEntity(method, proxyRequestUri, servletRequest);
        } else {
            proxyRequest = new BasicHttpRequest(method, proxyRequestUri);
        }
        copyRequestHeaders(servletRequest, proxyRequest);
        setXForwardedForHeader(servletRequest, proxyRequest);

        return proxyRequest;
    }

    //------------------------------------------------------------------------
    /**
     * Reads the request URI from {@code servletRequest} and rewrites it,
     * considering targetUri. It's used to make the new request.
     *
     * @param servletRequest
     * @return
     */
    private String rewriteUrlFromRequest(HttpServletRequest servletRequest) {
        StringBuilder uri = new StringBuilder(500);
        uri.append(env.getTargetUri());
        // Handle the path given to the servlet
        String pathInfo = rewritePathInfoFromRequest(servletRequest);
        if (pathInfo != null) {//ex: /my/path.html
            // getPathInfo() returns decoded string, so we need encodeUriQuery to encode "%" characters
            uri.append(Encoding.encodeUriQuery(pathInfo, true));
        }
        // Handle the query string & fragment
        String queryString = servletRequest.getQueryString();//ex:(following '?'): name=value&foo=bar#fragment
        String fragment = null;
        //split off fragment from queryString, updating queryString if found
        if (queryString != null) {
            int fragIdx = queryString.indexOf('#');
            if (fragIdx >= 0) {
                fragment = queryString.substring(fragIdx + 1);
                queryString = queryString.substring(0, fragIdx);
            }
        }

        queryString = rewriteQueryStringFromRequest(servletRequest, queryString);
        if (queryString != null && queryString.length() > 0) {
            uri.append('?');
            // queryString is not decoded, so we need encodeUriQuery not to encode "%" characters, to avoid double-encoding
            uri.append(Encoding.encodeUriQuery(queryString, false));
        }

        if (doSendUrlFragment && fragment != null) {
            uri.append('#');
            // fragment is not decoded, so we need encodeUriQuery not to encode "%" characters, to avoid double-encoding
            uri.append(Encoding.encodeUriQuery(fragment, false));
        }
        return uri.toString();
    }

    private String rewriteQueryStringFromRequest(HttpServletRequest servletRequest, String queryString) {
        return queryString;
    }

    /**
     * Allow overrides of
     * {@link javax.servlet.http.HttpServletRequest#getPathInfo()}. Useful when
     * url-pattern of servlet-mapping (web.xml) requires manipulation.
     *
     * @param servletRequest
     * @return
     */
    private String rewritePathInfoFromRequest(HttpServletRequest servletRequest) {
        return servletRequest.getPathInfo();
    }

    private HttpRequest newProxyRequestWithEntity(String method, String proxyRequestUri, HttpServletRequest servletRequest) throws IOException {
        HttpEntityEnclosingRequest eProxyRequest = new BasicHttpEntityEnclosingRequest(method, proxyRequestUri);
        // Add the input entity (streamed)
        //  note: we don't bother ensuring we close the servletInputStream since the container handles it
        eProxyRequest.setEntity(new InputStreamEntity(servletRequest.getInputStream(), getContentLength(servletRequest)));
        return eProxyRequest;
    }
    // Get the header value as a long in order to more correctly proxy very large requests

    private long getContentLength(HttpServletRequest request) {
        String contentLengthHeader = request.getHeader("Content-Length");
        if (contentLengthHeader != null) {
            return Long.parseLong(contentLengthHeader);
        }
        return -1L;
    }

    /**
     * Copy request headers from the servlet client to the proxy request. This
     * is easily overridden to add your own.
     *
     * @param servletRequest
     * @param proxyRequest
     */
    private void copyRequestHeaders(HttpServletRequest servletRequest, HttpRequest proxyRequest) {
        // Get an Enumeration of all of the header names sent by the client
        @SuppressWarnings("unchecked")
        Enumeration<String> enumerationOfHeaderNames = servletRequest.getHeaderNames();
        while (enumerationOfHeaderNames.hasMoreElements()) {
            String headerName = enumerationOfHeaderNames.nextElement();
            copyRequestHeader(servletRequest, proxyRequest, headerName);
        }
    }

    /**
     * Copy a request header from the servlet client to the proxy request. This
     * is easily overridden to filter out certain headers if desired.
     *
     * @param servletRequest
     * @param proxyRequest
     * @param headerName
     */
    private void copyRequestHeader(HttpServletRequest servletRequest, HttpRequest proxyRequest,
            String headerName) {
        //Instead the content-length is effectively set via InputStreamEntity
        if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
            return;
        }
        if (HobByHopHeaders.containsHeader(headerName)) {
            return;
        }
        // If compression is handled in the servlet, apache http client needs to
        // control the Accept-Encoding header, not the client
        if (doHandleCompression && headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_ENCODING)) {
            return;
        }

        @SuppressWarnings("unchecked")
        Enumeration<String> headers = servletRequest.getHeaders(headerName);
        while (headers.hasMoreElements()) {//sometimes more than one value
            String headerValue = headers.nextElement();
            // In case the proxy host is running multiple virtual servers,
            // rewrite the Host header to ensure that we get content from
            // the correct virtual server
            if (!doPreserveHost && headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
                HttpHost host = env.getTargetHost();
                headerValue = host.getHostName();
                if (host.getPort() != -1) {
                    headerValue += ":" + host.getPort();
                }
            } else if (!doPreserveCookies && headerName.equalsIgnoreCase(org.apache.http.cookie.SM.COOKIE)) {
                headerValue = getRealCookie(headerValue);
            }
            proxyRequest.addHeader(headerName, headerValue);
        }
    }

    /**
     * Take any client cookies that were originally from the proxy and prepare
     * them to send to the proxy. This relies on cookie headers being set
     * correctly according to RFC 6265 Sec 5.4. This also blocks any local
     * cookies from being sent to the proxy.
     *
     * @param cookieValue
     * @return
     */
    private String getRealCookie(String cookieValue) {
        StringBuilder escapedCookie = new StringBuilder();
        String[] cookies = cookieValue.split("[;,]");
        for (String cookie : cookies) {
            String[] cookieSplit = cookie.split("=");
            if (cookieSplit.length == 2) {
                String cookieName = cookieSplit[0].trim();
                if (cookieName.startsWith(env.getCookieNamePrefix())) {
                    cookieName = cookieName.substring(env.getCookieNamePrefix().length());
                    if (escapedCookie.length() > 0) {
                        escapedCookie.append("; ");
                    }
                    escapedCookie.append(cookieName).append("=").append(cookieSplit[1].trim());
                }
            }
        }
        return escapedCookie.toString();
    }

    private void setXForwardedForHeader(HttpServletRequest servletRequest,
            HttpRequest proxyRequest) {
        if (doForwardIP) {
            String forHeaderName = "X-Forwarded-For";
            String forHeader = servletRequest.getRemoteAddr();
            String existingForHeader = servletRequest.getHeader(forHeaderName);
            if (existingForHeader != null) {
                forHeader = existingForHeader + ", " + forHeader;
            }
            proxyRequest.setHeader(forHeaderName, forHeader);

            String protoHeaderName = "X-Forwarded-Proto";
            String protoHeader = servletRequest.getScheme();
            proxyRequest.setHeader(protoHeaderName, protoHeader);
        }
    }

}
