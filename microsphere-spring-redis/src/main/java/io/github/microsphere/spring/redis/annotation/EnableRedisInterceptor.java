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
package io.github.microsphere.spring.redis.annotation;

import io.github.microsphere.spring.redis.beans.RedisTemplateWrapper;
import io.github.microsphere.spring.redis.beans.StringRedisTemplateWrapper;
import io.github.microsphere.spring.redis.event.RedisCommandEvent;
import io.github.microsphere.spring.redis.util.RedisConstants;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable Redis Interceptor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisInterceptorBeanDefinitionRegistrar
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableRedisContext
@Import(RedisInterceptorBeanDefinitionRegistrar.class)
public @interface EnableRedisInterceptor {

    /**
     * The bean names of {@link RedisTemplateWrapper} or {@link StringRedisTemplateWrapper} that will be wrapped
     * for interception. The default value is empty.
     * <p>
     * Each attribute value can be :
     * <ul>
     *     <li>the string presenting bean name</li>
     *     <li>the Spring property placeholder, like : {@link RedisConstants#WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME "microsphere.redis.wrapped-redis-templates"}</li>
     *     <li>{@link RedisConstants#ALL_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES "*"} indicates all {@link RedisTemplate} beans</li>
     *  </ul>
     *
     * @return If the value is empty, it indicates no  {@link RedisTemplateWrapper} or {@link StringRedisTemplateWrapper} being wrapped.
     */
    String[] wrapRedisTemplates() default {};

    /**
     * Expose {@link RedisCommandEvent} or not
     *
     * @return If {@link RedisCommandEvent} is required to be exposed, return <code>true</code>, or <code>false</code>
     */
    boolean exposeCommandEvent() default true;

}
