/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.spring.web.servlet;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Test {@link ServletRegistration.Dynamic}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class TestServletRegistration implements ServletRegistration.Dynamic {

    private final String servletName;

    private final String servletClassName;

    private final Set<String> urlPatterns = new LinkedHashSet<>();

    private final Map<String, String> initParameters = new HashMap<>();

    private int loadOnStartup;

    private String roleName;

    private boolean asyncSupported;

    public TestServletRegistration(String servletName, String servletClassName) {
        this.servletName = servletName;
        this.servletClassName = servletClassName;
    }

    @Override
    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        // TODO
        return null;
    }

    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {
        // TODO
    }

    @Override
    public void setRunAsRole(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {
        this.asyncSupported = isAsyncSupported;
    }

    @Override
    public Set<String> addMapping(String... urlPatterns) {
        for (String urlPattern : urlPatterns) {
            this.urlPatterns.add(urlPattern);
        }
        return this.urlPatterns;
    }

    @Override
    public Collection<String> getMappings() {
        return urlPatterns;
    }

    @Override
    public String getRunAsRole() {
        return roleName;
    }

    @Override
    public String getName() {
        return servletName;
    }

    @Override
    public String getClassName() {
        return servletClassName;
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return initParameters.put(name, value) == null;
    }

    @Override
    public String getInitParameter(String name) {
        return this.initParameters.get(name);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        this.initParameters.putAll(initParameters);
        return this.initParameters.keySet();
    }

    @Override
    public Map<String, String> getInitParameters() {
        return this.initParameters;
    }
}
