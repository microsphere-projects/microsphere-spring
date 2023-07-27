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

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Test {@link FilterRegistration.Dynamic}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class TestFilterRegistration implements FilterRegistration.Dynamic {

    private final String filterName;

    private final String filterClassName;

    private final Set<String> servletNames = new LinkedHashSet<>();

    private final Set<String> urlPatterns = new LinkedHashSet<>();

    private final Map<String, String> initParameters = new HashMap<>();

    private boolean asyncSupported;

    public TestFilterRegistration(String filterName, String filterClassName) {
        this.filterName = filterName;
        this.filterClassName = filterClassName;
    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {
        this.asyncSupported = isAsyncSupported;
    }

    @Override
    public String getName() {
        return filterName;
    }

    @Override
    public String getClassName() {
        return filterClassName;
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

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
        for (String servletName : servletNames) {
            this.servletNames.add(servletName);
        }
    }

    @Override
    public Collection<String> getServletNameMappings() {
        return this.servletNames;
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
        for (String urlPattern : urlPatterns) {
            this.urlPatterns.add(urlPattern);
        }
    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        return this.urlPatterns;
    }
}
