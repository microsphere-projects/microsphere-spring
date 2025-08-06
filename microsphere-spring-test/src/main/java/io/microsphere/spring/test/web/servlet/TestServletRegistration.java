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
package io.microsphere.spring.test.web.servlet;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletSecurityElement;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.util.Assert.assertNotEmpty;
import static io.microsphere.util.Assert.assertNotNull;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Test {@link ServletRegistration.Dynamic}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class TestServletRegistration implements ServletRegistration.Dynamic {

    private final String servletName;

    private final String servletClassName;

    private final Servlet servlet;

    private ServletSecurityElement servletSecurityElement;

    private MultipartConfigElement multipartConfig;

    private final Set<String> urlPatterns = newLinkedHashSet();

    private final Map<String, String> initParameters = newLinkedHashMap();

    private int loadOnStartup;

    private String roleName;

    private boolean asyncSupported;

    public TestServletRegistration(String servletName, String servletClassName, Servlet servlet) {
        assertNotEmpty(servletName, () -> "The 'servletName' argument must not be empty!");
        assertNotEmpty(servletClassName, () -> "The 'servletClassName' argument must not be empty!");
        assertNotNull(servlet, () -> "The 'servlet' argument must not be null!");
        this.servletName = servletName;
        this.servletClassName = servletClassName;
        this.servlet = servlet;
    }

    @Override
    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        this.servletSecurityElement = constraint;
        return emptySet();
    }

    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {
        this.multipartConfig = multipartConfig;
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
        return unmodifiableSet(this.urlPatterns);
    }

    @Override
    public Collection<String> getMappings() {
        return unmodifiableCollection(urlPatterns);
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
        return unmodifiableMap(this.initParameters);
    }

    /**
     * Get Servlet Security Element
     *
     * @return Servlet Security Element
     */
    @Nullable
    public ServletSecurityElement getServletSecurityElement() {
        return servletSecurityElement;
    }

    /**
     * Get Multipart Config
     *
     * @return Multipart Config
     */
    @Nullable
    public MultipartConfigElement getMultipartConfig() {
        return multipartConfig;
    }

    @Nonnull
    public Servlet getServlet() {
        return servlet;
    }

    /**
     * Get URL Patterns
     *
     * @return URL Patterns
     */
    @Nonnull
    public Set<String> getUrlPatterns() {
        return unmodifiableSet(urlPatterns);
    }

    /**
     * Get Load On Startup
     *
     * @return Load On Startup
     */
    public int getLoadOnStartup() {
        return loadOnStartup;
    }

    /**
     * Get Role Name
     *
     * @return Role Name
     */
    @Nullable
    public String getRoleName() {
        return roleName;
    }

    /**
     * Get Async Supported
     *
     * @return Async Supported
     */
    public boolean isAsyncSupported() {
        return asyncSupported;
    }
}
