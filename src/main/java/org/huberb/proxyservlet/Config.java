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

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.servlet.ServletConfig;

import org.huberb.proxyservlet.Config.Accessor;

/**
 *
 * @author pi
 */
public class Config {

    //---
    protected boolean doLog = true;
    protected boolean doHandleCompression = false;
    protected boolean doPreserveHost = false;
    protected boolean doPreserveCookies = false;
    protected boolean doForwardIP;
    protected boolean doSendUrlFragment = false;
    protected boolean doHandleRedirects = false;
    //---
    protected boolean useSystemProperties = false;
    protected int connectTimeout = -1;
    protected int readTimeout = -1;
    protected int connectionRequestTimeout = -1;
    protected int maxConnections = -1;

    //------------------------------------------------------------------------
    public boolean isDoLog() {
        return doLog;
    }

    public void setDoLog(boolean doLog) {
        this.doLog = doLog;
    }

    public boolean isDoHandleCompression() {
        return doHandleCompression;
    }

    public void setDoHandleCompression(boolean doHandleCompression) {
        this.doHandleCompression = doHandleCompression;
    }

    public boolean isDoPreserveHost() {
        return doPreserveHost;
    }

    public void setDoPreserveHost(boolean doPreserveHost) {
        this.doPreserveHost = doPreserveHost;
    }

    public boolean isDoPreserveCookies() {
        return doPreserveCookies;
    }

    public void setDoPreserveCookies(boolean doPreserveCookies) {
        this.doPreserveCookies = doPreserveCookies;
    }

    public boolean isDoForwardIP() {
        return doForwardIP;
    }

    public void setDoForwardIP(boolean doForwardIP) {
        this.doForwardIP = doForwardIP;
    }

    public boolean isDoSendUrlFragment() {
        return doSendUrlFragment;
    }

    public void setDoSendUrlFragment(boolean doSendUrlFragment) {
        this.doSendUrlFragment = doSendUrlFragment;
    }

    public boolean isDoHandleRedirects() {
        return doHandleRedirects;
    }

    public void setDoHandleRedirects(boolean doHandleRedirects) {
        this.doHandleRedirects = doHandleRedirects;
    }

    public boolean isUseSystemProperties() {
        return useSystemProperties;
    }

    public void setUseSystemProperties(boolean useSystemProperties) {
        this.useSystemProperties = useSystemProperties;
    }

    //------------------------------------------------------------------------
    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getConnectionRequestTimeout() {
        return connectionRequestTimeout;
    }

    public void setConnectionRequestTimeout(int connectionRequestTimeout) {
        this.connectionRequestTimeout = connectionRequestTimeout;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public String formatConfigValues() {
        return String.format("ConfigValues%n"
                //---
                + "doLog: %s, "
                + "doHandleCompression: %s, "
                + "doPreserveHost: %s, "
                + "doPreserveCookies: %s, "
                + "doForwardIP: %s, "
                + "doSendUrlFragment: %s, "
                + "doHandleRedirects: %s%n"
                //---
                + "useSystemProperties: %s, "
                + "connectTimeout: %d, "
                + "readTimeout: %d, "
                + "connectionRequestTimeout: %d, "
                + "maxConnections: %d",
                //---
                doLog,
                doHandleCompression,
                doPreserveHost,
                doPreserveCookies,
                doForwardIP,
                doSendUrlFragment,
                doHandleRedirects,
                //---
                useSystemProperties,
                connectTimeout,
                readTimeout,
                connectionRequestTimeout,
                maxConnections
        );
    }

    static class Accessor<T> {

        final String name;
        final Class<T> clazz;
        final Consumer<T> consumer;

        public Accessor(String name, Class<T> clazz, Consumer<T> consumer) {
            this.name = name;
            this.clazz = clazz;
            this.consumer = consumer;
        }
    }

    static class AccessorFactory {

        private final List<Accessor<?>> configNameList;

        public AccessorFactory(Config config) {
            this.configNameList = Arrays.asList(
                    //---
                    new Accessor<>("doLog", Boolean.class, config::setDoLog),
                    new Accessor<>("doHandleCompression", Boolean.class, config::setDoHandleCompression),
                    new Accessor<>("doPreserveHost", Boolean.class, config::setDoPreserveHost),
                    new Accessor<>("doPreserveCookies", Boolean.class, config::setDoPreserveCookies),
                    new Accessor<>("doForwardIP", Boolean.class, config::setDoForwardIP),
                    new Accessor<>("doSendUrlFragment", Boolean.class, config::setDoSendUrlFragment),
                    new Accessor<>("doHandleRedirects", Boolean.class, config::setDoHandleRedirects),
                    //---
                    new Accessor<>("useSystemProperties", Boolean.class, config::setUseSystemProperties),
                    new Accessor<>("connectTimeout", Integer.class, config::setConnectTimeout),
                    new Accessor<>("readTimeout", Integer.class, config::setReadTimeout),
                    new Accessor<>("connectionRequestTimeout", Integer.class, config::setConnectionRequestTimeout),
                    new Accessor<>("maxConnections", Integer.class, config::setMaxConnections)
            );
        }

        List<String> getNames() {
            return configNameList.stream()
                    .map(acc -> acc.name)
                    .collect(Collectors.toList());
        }

        void setKeyValue(String key, String value) {
            configNameList.stream()
                    .filter(acc -> acc.name.equals(key))
                    .findFirst()
                    .ifPresent(acc -> {
                        if (acc.clazz.equals(Boolean.class)) {
                            boolean v = Boolean.parseBoolean(value);
                            ((Accessor<Boolean>) acc).consumer.accept(v);
                        } else if (acc.clazz.equals(Integer.class)) {
                            try {
                                int v = Integer.parseInt(value);
                                ((Accessor<Integer>) acc).consumer.accept(v);
                            } catch (NumberFormatException nfex) {
                                // ignore non number value
                            }
                        } else if (acc.clazz.equals(String.class)) {
                            String v = value;
                            ((Accessor<String>) acc).consumer.accept(v);
                        }
                    });
        }
    }

    /**
     * Build {@link Config} instance from a {@link Properties} instance.
     */
    static class ConfigFromPropertiesBuilder {

        Properties props;

        ConfigFromPropertiesBuilder props(Properties props) {
            this.props = props;
            return this;
        }

        Config build() {
            Config config = new Config();
            AccessorFactory accessorFactory = new AccessorFactory(config);
            accessorFactory.getNames().forEach(key -> {
                String value = props.getProperty(key);
                if (value != null) {
                    accessorFactory.setKeyValue(key, value);
                }
            });
            return config;
        }
    }

    /**
     * Build {@link Config} instance from servlet init-parameters.
     */
    static class ConfigFromServletConfigBuilder {

        ServletConfig servletConfig;

        ConfigFromServletConfigBuilder servletConfig(ServletConfig servletConfig) {
            this.servletConfig = servletConfig;
            return this;
        }

        Config build() {
            Config config = new Config();
            AccessorFactory accessorFactory = new AccessorFactory(config);
            accessorFactory.getNames().forEach(key -> {
                String value = servletConfig.getInitParameter(key);
                if (value != null) {
                    accessorFactory.setKeyValue(key, value);
                }
            });
            return config;
        }
    }
}
