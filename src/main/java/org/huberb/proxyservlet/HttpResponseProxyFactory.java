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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpCookie;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;

/**
 *
 * @author pi
 */
public class HttpResponseProxyFactory {

    protected boolean doPreserveCookies = false;
    protected boolean doHandleCompression = false;

    private final Env env;

    public HttpResponseProxyFactory(Config config, Env env) {
        this.doPreserveCookies = config.isDoPreserveCookies();
        this.doHandleCompression = config.isDoHandleCompression();
        this.env = env;
    }

    public void sendResponse(HttpServletRequest servletRequest,
            HttpServletResponse servletResponse,
            HttpResponse proxyResponse) throws IOException {
        // Process the response:
        // Pass the response code. This method with the "reason phrase" is deprecated but it's the
        //   only way to pass the reason along too.
        int statusCode = proxyResponse.getStatusLine().getStatusCode();
        //noinspection deprecation
        servletResponse.setStatus(statusCode, proxyResponse.getStatusLine().getReasonPhrase());

        // Copying response headers to make sure SESSIONID or other Cookie which comes from the remote
        // server will be saved in client when the proxied url was redirected to another one.
        // See issue [#51](https://github.com/mitre/HTTP-Proxy-Servlet/issues/51)
        copyResponseHeaders(proxyResponse, servletRequest, servletResponse);

        if (statusCode == HttpServletResponse.SC_NOT_MODIFIED) {
            // 304 needs special handling.  See:
            // http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
            // Don't send body entity/content!
            servletResponse.setIntHeader(HttpHeaders.CONTENT_LENGTH, 0);
        } else {
            // Send the content to the client
            copyResponseEntity(proxyResponse, servletResponse);
        }
    }

    /**
     * Copy proxied response headers back to the servlet client.
     *
     * @param proxyResponse
     * @param servletRequest
     * @param servletResponse
     */
    private void copyResponseHeaders(HttpResponse proxyResponse, HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        for (Header header : proxyResponse.getAllHeaders()) {
            copyResponseHeader(servletRequest, servletResponse, header);
        }
    }

    /**
     * Copy a proxied response header back to the servlet client. This is easily
     * overwritten to filter out certain headers if desired.
     *
     * @param servletRequest
     * @param servletResponse
     * @param header
     */
    private void copyResponseHeader(HttpServletRequest servletRequest,
            HttpServletResponse servletResponse, Header header) {
        String headerName = header.getName();
        if (HobByHopHeaders.containsHeader(headerName)) {
            return;
        }
        String headerValue = header.getValue();
        if (headerName.equalsIgnoreCase(org.apache.http.cookie.SM.SET_COOKIE)
                || headerName.equalsIgnoreCase(org.apache.http.cookie.SM.SET_COOKIE2)) {
            copyProxyCookie(servletRequest, servletResponse, headerValue);
        } else if (headerName.equalsIgnoreCase(HttpHeaders.LOCATION)) {
            // LOCATION Header may have to be rewritten.
            servletResponse.addHeader(headerName, rewriteUrlFromResponse(servletRequest, headerValue));
        } else {
            servletResponse.addHeader(headerName, headerValue);
        }
    }

    /**
     * For a redirect response from the target server, this translates
     * {@code theUrl} to redirect to and translates it to one the original
     * client can use.
     *
     * @param servletRequest
     * @param theUrl
     * @return
     */
    private String rewriteUrlFromResponse(HttpServletRequest servletRequest, String theUrl) {
        //TODO document example paths
        final String targetUri = env.getTargetUri();
        if (theUrl.startsWith(targetUri)) {
            /*
             * The URL points back to the back-end server.
             * Instead of returning it verbatim we replace the target path with our
             * source path in a way that should instruct the original client to
             * request the URL pointed through this Proxy.
             * We do this by taking the current request and rewriting the path part
             * using this servlet's absolute path and the path from the returned URL
             * after the base target URL.
             */
            StringBuffer curUrl = servletRequest.getRequestURL();//no query
            int pos;
            // Skip the protocol part
            if ((pos = curUrl.indexOf("://")) >= 0) {
                // Skip the authority part
                // + 3 to skip the separator between protocol and authority
                if ((pos = curUrl.indexOf("/", pos + 3)) >= 0) {
                    // Trim everything after the authority part.
                    curUrl.setLength(pos);
                }
            }
            // Context path starts with a / if it is not blank
            curUrl.append(servletRequest.getContextPath());
            // Servlet path starts with a / if it is not blank
            curUrl.append(servletRequest.getServletPath());
            curUrl.append(theUrl, targetUri.length(), theUrl.length());
            return curUrl.toString();
        }
        return theUrl;
    }

    /**
     * Copy cookie from the proxy to the servlet client. Replaces cookie path to
     * local path and renames cookie to avoid collisions.
     *
     * @param servletRequest
     * @param servletResponse
     * @param headerValue
     */
    private void copyProxyCookie(HttpServletRequest servletRequest,
            HttpServletResponse servletResponse, String headerValue) {
        for (HttpCookie cookie : HttpCookie.parse(headerValue)) {
            Cookie servletCookie = createProxyCookie(servletRequest, cookie);
            servletResponse.addCookie(servletCookie);
        }
    }

    /**
     * Creates a proxy cookie from the original cookie.
     *
     * @param servletRequest original request
     * @param cookie original cookie
     * @return proxy cookie
     */
    private Cookie createProxyCookie(HttpServletRequest servletRequest, HttpCookie cookie) {
        String proxyCookieName = getProxyCookieName(cookie);
        Cookie servletCookie = new Cookie(proxyCookieName, cookie.getValue());
        servletCookie.setPath(buildProxyCookiePath(servletRequest)); //set to the path of the proxy servlet
        servletCookie.setComment(cookie.getComment());
        servletCookie.setMaxAge((int) cookie.getMaxAge());
        // don't set cookie domain
        servletCookie.setSecure(cookie.getSecure());
        servletCookie.setVersion(cookie.getVersion());
        servletCookie.setHttpOnly(cookie.isHttpOnly());
        return servletCookie;
    }

    /**
     * Set cookie name prefixed with a proxy value so it won't collide with
     * other cookies.
     *
     * @param cookie cookie to get proxy cookie name for
     * @return non-conflicting proxy cookie name
     */
    private String getProxyCookieName(HttpCookie cookie) {
        return doPreserveCookies ? cookie.getName() : env.getCookieNamePrefix() + cookie.getName();
    }

    /**
     * Create path for proxy cookie.
     *
     * @param servletRequest original request
     * @return proxy cookie path
     */
    private String buildProxyCookiePath(HttpServletRequest servletRequest) {
        String path = servletRequest.getContextPath(); // path starts with / or is empty string
        path += servletRequest.getServletPath(); // servlet path starts with / or is empty string
        if (path.isEmpty()) {
            path = "/";
        }
        return path;
    }

    /**
     * Copy response body data (the entity) from the proxy to the servlet
     * client.
     *
     * @param proxyResponse
     * @param servletResponse
     * @throws java.io.IOException
     */
    private void copyResponseEntity(HttpResponse proxyResponse, HttpServletResponse servletResponse)
            throws IOException {
        HttpEntity entity = proxyResponse.getEntity();
        if (entity != null) {
            if (entity.isChunked()) {
                // Flush intermediate results before blocking on input -- needed for SSE
                InputStream is = entity.getContent();
                OutputStream os = servletResponse.getOutputStream();
                byte[] buffer = new byte[10 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                    /*-
                    * Issue in Apache http client/JDK: if the stream from client is
                    * compressed, apache http client will delegate to GzipInputStream.
                    * The #available implementation of InflaterInputStream (parent of
                    * GzipInputStream) return 1 until EOF is reached. This is not
                    * consistent with InputStream#available, which defines:
                    *
                    *   A single read or skip of this many bytes will not block,
                    *   but may read or skip fewer bytes.
                    *
                    *  To work around this, a flush is issued always if compression
                    *  is handled by apache http client
                     */
                    if (doHandleCompression || is.available() == 0 /* next is.read will block */) {
                        os.flush();
                    }
                }
                // Entity closing/cleanup is done in the caller (#service)
            } else {
                OutputStream servletOutputStream = servletResponse.getOutputStream();
                entity.writeTo(servletOutputStream);
            }
        }
    }
}
