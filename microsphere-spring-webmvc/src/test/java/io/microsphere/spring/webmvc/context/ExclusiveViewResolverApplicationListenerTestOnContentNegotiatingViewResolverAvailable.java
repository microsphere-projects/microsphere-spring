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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ExclusiveViewResolverApplicationListener} Test on {@link ContentNegotiatingViewResolver} available
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ExclusiveViewResolverApplicationListener
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {
        ExclusiveViewResolverApplicationListener.class,
        ExclusiveViewResolverApplicationListenerTestOnContentNegotiatingViewResolverAvailable.class,
        ExclusiveViewResolverApplicationListenerTestOnContentNegotiatingViewResolverAvailable.Config.class
})
@TestPropertySource(
        properties = {
                "microsphere.spring.webmvc.view-resolver.exclusive-bean-name=beanNameViewResolver"
        }
)
@EnableWebMvc
class ExclusiveViewResolverApplicationListenerTestOnContentNegotiatingViewResolverAvailable {

    @Autowired
    private ContentNegotiatingViewResolver contentNegotiatingViewResolver;

    @Autowired
    private BeanNameViewResolver beanNameViewResolver;

    static class Config {

        @Bean
        public ContentNegotiatingViewResolver contentNegotiatingViewResolver(
                @Autowired ContentNegotiationManager contentNegotiationManager) {
            ContentNegotiatingViewResolver viewResolver = new ContentNegotiatingViewResolver();
            viewResolver.setContentNegotiationManager(contentNegotiationManager);
            return viewResolver;
        }

        @Bean
        public BeanNameViewResolver beanNameViewResolver() {
            return new BeanNameViewResolver();
        }

    }

    @Test
    void test() {
        List<ViewResolver> viewResolvers = this.contentNegotiatingViewResolver.getViewResolvers();
        assertEquals(1, viewResolvers.size());
        assertTrue(viewResolvers.contains(this.beanNameViewResolver));
    }

}