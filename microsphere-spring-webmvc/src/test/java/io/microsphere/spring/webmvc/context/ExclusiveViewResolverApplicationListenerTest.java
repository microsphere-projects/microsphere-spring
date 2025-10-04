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

package io.microsphere.spring.webmvc.context;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.view.BeanNameViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.ViewResolverComposite;

import java.util.List;

import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static io.microsphere.spring.webmvc.context.ExclusiveViewResolverApplicationListener.EXCLUSIVE_VIEW_RESOLVER_BEAN_NAME_PROPERTY_NAME;
import static io.microsphere.spring.webmvc.util.ViewResolverUtils.BEAN_NAME_VIEW_RESOLVER_BEAN_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ExclusiveViewResolverApplicationListener} Test on defaults
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ExclusiveViewResolverApplicationListener
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        ExclusiveViewResolverApplicationListener.class,
        ExclusiveViewResolverApplicationListenerTest.class,
        ExclusiveViewResolverApplicationListenerTest.ViewResolverConfig.class,
        ExclusiveViewResolverApplicationListenerTest.ContentNegotiatingViewResolverConfig.class
})
@TestPropertySource(
        properties = {
                "microsphere.spring.webmvc.view-resolver.exclusive-bean-name=beanNameViewResolver"
        }
)
@EnableWebMvc
class ExclusiveViewResolverApplicationListenerTest {

    @Autowired
    private ConfigurableApplicationContext context;

    @Autowired
    private ContentNegotiatingViewResolver contentNegotiatingViewResolver;

    @Autowired
    private BeanNameViewResolver beanNameViewResolver;

    static class ViewResolverConfig {

        @Bean(name = BEAN_NAME_VIEW_RESOLVER_BEAN_NAME)
        public BeanNameViewResolver beanNameViewResolver() {
            return new BeanNameViewResolver();
        }
    }

    static class ContentNegotiatingViewResolverConfig {
        @Bean
        public ContentNegotiatingViewResolver contentNegotiatingViewResolver(
                @Autowired ContentNegotiationManager contentNegotiationManager) {
            ContentNegotiatingViewResolver viewResolver = new ContentNegotiatingViewResolver();
            viewResolver.setContentNegotiationManager(contentNegotiationManager);
            return viewResolver;
        }
    }

    static class ViewResolverCompositeConfig {
        @Bean
        public ViewResolverComposite mvcViewResolver(List<ViewResolver> viewResolvers) {
            ViewResolverComposite viewResolverComposite = new ViewResolverComposite();
            viewResolverComposite.setViewResolvers(viewResolvers);
            return viewResolverComposite;
        }
    }

    @Test
    void testOnDefaults() {
        testInSpringContainer(this::assertListener, ExclusiveViewResolverApplicationListener.class);
    }

    @Test
    void testOnContentNegotiatingViewResolverAvailable() {
        List<ViewResolver> viewResolvers = this.contentNegotiatingViewResolver.getViewResolvers();
        assertEquals(1, viewResolvers.size());
        assertTrue(viewResolvers.contains(this.beanNameViewResolver));
    }

    @Test
    void testOnViewResolverCompositeAvailable() {
        System.setProperty(EXCLUSIVE_VIEW_RESOLVER_BEAN_NAME_PROPERTY_NAME, BEAN_NAME_VIEW_RESOLVER_BEAN_NAME);
        testInSpringContainer((context, env) -> {
            assertListener(context);
            assertNotNull(context.getBean(BEAN_NAME_VIEW_RESOLVER_BEAN_NAME));
        }, ViewResolverConfig.class, ViewResolverCompositeConfig.class, ExclusiveViewResolverApplicationListener.class);
    }

    @Test
    void testOnViewResolverCompositeNotAvailable() {
        System.setProperty(EXCLUSIVE_VIEW_RESOLVER_BEAN_NAME_PROPERTY_NAME, BEAN_NAME_VIEW_RESOLVER_BEAN_NAME);
        testInSpringContainer((context, env) -> {
            assertListener(context);
            assertNotNull(context.getBean(BEAN_NAME_VIEW_RESOLVER_BEAN_NAME));
        }, ViewResolverConfig.class, ExclusiveViewResolverApplicationListener.class);
    }

    @Test
    void testOnViewResolverNotFound() {
        System.setProperty(EXCLUSIVE_VIEW_RESOLVER_BEAN_NAME_PROPERTY_NAME, "Not-Found");
        testInSpringContainer((context, env) -> {
            assertListener(context);
        }, ExclusiveViewResolverApplicationListener.class);
    }

    void assertListener(ConfigurableApplicationContext context) {
        assertNotNull(context.getBean(ExclusiveViewResolverApplicationListener.class));
    }

}