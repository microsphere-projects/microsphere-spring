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
package io.github.microsphere.spring.webmvc.util;

import org.springframework.web.servlet.ViewResolver;

/**
 * {@link ViewResolver} Utilities
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ViewResolver
 * @since 2017.03.22
 */
public class ViewResolverUtils {

    /**
     * The bean name of InternalResourceViewResolver
     */
    public static final String INTERNAL_RESOURCE_VIEW_RESOLVER_BEAN_NAME = "defaultViewResolver";

    /**
     * The bean name of org.springframework.web.servlet.view.velocity.VelocityViewResolver
     */
    public static final String VELOCITY_VIEW_RESOLVER_BEAN_NAME = "velocityViewResolver";

    /**
     * The bean name of org.thymeleaf.spring5.view.ThymeleafViewResolver
     */
    public static final String THYMELEAF_VIEW_RESOLVER_BEAN_NAME = "thymeleafViewResolver";

    /**
     * The bean name of org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver
     */
    public static final String FREEMARKER_VIEW_RESOLVER_BEAN_NAME = "freeMarkerViewResolver";

    /**
     * The bean name of org.springframework.web.servlet.view.groovy.GroovyMarkupViewResolver
     */
    public static final String GROOVY_MARKUP_VIEW_RESOLVER_BEAN_NAME = "groovyMarkupViewResolver";

    /**
     * The bean name of org.springframework.boot.web.servlet.view.MustacheViewResolver
     */
    public static final String MUSTACHE_VIEW_RESOLVER_BEAN_NAME = "mustacheViewResolver";


}