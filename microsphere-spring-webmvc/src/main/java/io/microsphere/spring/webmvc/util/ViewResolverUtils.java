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
package io.microsphere.spring.webmvc.util;

import io.microsphere.util.Utils;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.ViewResolverComposite;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.springframework.web.servlet.view.groovy.GroovyMarkupViewResolver;

/**
 * {@link ViewResolver} Utilities
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ViewResolver
 * @since 1.0.0
 */
public abstract class ViewResolverUtils implements Utils {

    /**
     * The bean name of {@link BeanNameViewResolver}
     *
     * @see BeanNameViewResolver
     */
    public static final String BEAN_NAME_VIEW_RESOLVER_BEAN_NAME = "beanNameViewResolver";

    /**
     * The bean name of {@link InternalResourceViewResolver}
     *
     * @see InternalResourceViewResolver
     */
    public static final String INTERNAL_RESOURCE_VIEW_RESOLVER_BEAN_NAME = "defaultViewResolver";

    /**
     * The bean name of {@link org.springframework.web.servlet.view.velocity.VelocityViewResolver}
     *
     * @see org.springframework.web.servlet.view.velocity.VelocityViewResolver
     */
    public static final String VELOCITY_VIEW_RESOLVER_BEAN_NAME = "velocityViewResolver";

    /**
     * The bean name of {@link org.thymeleaf.spring5.view.ThymeleafViewResolver}
     *
     * @see org.thymeleaf.spring5.view.ThymeleafViewResolver
     */
    public static final String THYMELEAF_VIEW_RESOLVER_BEAN_NAME = "thymeleafViewResolver";

    /**
     * The bean name of {@link FreeMarkerViewResolver}
     *
     * @see FreeMarkerViewResolver
     */
    public static final String FREEMARKER_VIEW_RESOLVER_BEAN_NAME = "freeMarkerViewResolver";

    /**
     * The bean name of {@link GroovyMarkupViewResolver}
     *
     * @see GroovyMarkupViewResolver
     */
    public static final String GROOVY_MARKUP_VIEW_RESOLVER_BEAN_NAME = "groovyMarkupViewResolver";

    /**
     * The bean name of {@link org.springframework.boot.web.servlet.view.MustacheViewResolver}
     *
     * @see org.springframework.boot.web.servlet.view.MustacheViewResolver
     */
    public static final String MUSTACHE_VIEW_RESOLVER_BEAN_NAME = "mustacheViewResolver";

    /**
     * The bean name of {@link ViewResolverComposite}
     *
     * @see ViewResolverComposite
     * @see WebMvcConfigurationSupport#mvcViewResolver(ContentNegotiationManager)
     * @see ViewResolverRegistry
     */
    public static final String VIEW_RESOLVER_COMPOSITE_BEAN_NAME = "mvcViewResolver";

    /**
     * The bean name of {@link ContentNegotiatingViewResolver}
     *
     * @see ContentNegotiatingViewResolver
     */
    public static final String CONTENT_NEGOTIATING_VIEW_RESOLVER_BEAN_NAME = "viewResolver";

    private ViewResolverUtils() {
    }
}