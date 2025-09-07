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


import org.junit.jupiter.api.Test;

import static io.microsphere.spring.webmvc.util.ViewResolverUtils.FREEMARKER_VIEW_RESOLVER_BEAN_NAME;
import static io.microsphere.spring.webmvc.util.ViewResolverUtils.GROOVY_MARKUP_VIEW_RESOLVER_BEAN_NAME;
import static io.microsphere.spring.webmvc.util.ViewResolverUtils.INTERNAL_RESOURCE_VIEW_RESOLVER_BEAN_NAME;
import static io.microsphere.spring.webmvc.util.ViewResolverUtils.MUSTACHE_VIEW_RESOLVER_BEAN_NAME;
import static io.microsphere.spring.webmvc.util.ViewResolverUtils.THYMELEAF_VIEW_RESOLVER_BEAN_NAME;
import static io.microsphere.spring.webmvc.util.ViewResolverUtils.VELOCITY_VIEW_RESOLVER_BEAN_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ViewResolverUtils}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ViewResolverUtils
 * @since 1.0.0
 */
class ViewResolverUtilsTest {

    @Test
    void testConstants() {
        assertEquals("defaultViewResolver", INTERNAL_RESOURCE_VIEW_RESOLVER_BEAN_NAME);
        assertEquals("velocityViewResolver", VELOCITY_VIEW_RESOLVER_BEAN_NAME);
        assertEquals("thymeleafViewResolver", THYMELEAF_VIEW_RESOLVER_BEAN_NAME);
        assertEquals("freeMarkerViewResolver", FREEMARKER_VIEW_RESOLVER_BEAN_NAME);
        assertEquals("groovyMarkupViewResolver", GROOVY_MARKUP_VIEW_RESOLVER_BEAN_NAME);
        assertEquals("mustacheViewResolver", MUSTACHE_VIEW_RESOLVER_BEAN_NAME);
    }

}