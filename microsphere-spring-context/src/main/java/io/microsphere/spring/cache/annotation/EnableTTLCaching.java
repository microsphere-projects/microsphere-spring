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
package io.microsphere.spring.cache.annotation;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.springframework.context.annotation.AdviceMode.PROXY;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

/**
 * Enables Spring's caching functionality with TTL (Time-To-Live) support.
 * <p>
 * This annotation is an extension of Spring's {@link EnableCaching} and provides additional
 * Time-To-Live capabilities for cached data. It allows developers to configure the duration
 * for which cache entries should remain valid, helping manage cache expiration more effectively.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * @Configuration
 * @EnableTTLCaching
 * public class CachingConfig {
 *     // Cacheable beans and configurations go here
 * }
 * }</pre>
 *
 * <h3>Customizing Behavior</h3>
 * You can customize the proxying behavior and order of execution like in standard Spring caching:
 *
 * <ul>
 *   <li>{@link #proxyTargetClass()}</li>
 *   <li>{@link #mode()}</li>
 *   <li>{@link #order()}</li>
 * </ul>
 *
 * <p>
 * These settings affect how caching advice is applied across your application. For example,
 * using the AspectJ mode enables more comprehensive interception compared to the default proxy-based approach.
 * </p>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@EnableCaching
@Import(TTLCachingConfiguration.class)
public @interface EnableTTLCaching {

    /**
     * Indicate whether subclass-based (CGLIB) proxies are to be created as opposed
     * to standard Java interface-based proxies. The default is {@code false}. <strong>
     * Applicable only if {@link #mode()} is set to {@link AdviceMode#PROXY}</strong>.
     * <p>Note that setting this attribute to {@code true} will affect <em>all</em>
     * Spring-managed beans requiring proxying, not just those marked with {@code @Cacheable}.
     * For example, other beans marked with Spring's {@code @Transactional} annotation will
     * be upgraded to subclass proxying at the same time. This approach has no negative
     * impact in practice unless one is explicitly expecting one type of proxy vs another,
     * e.g. in tests.
     */
    @AliasFor(annotation = EnableCaching.class)
    boolean proxyTargetClass() default false;

    /**
     * Indicate how caching advice should be applied.
     * <p><b>The default is {@link AdviceMode#PROXY}.</b>
     * Please note that proxy mode allows for interception of calls through the proxy
     * only. Local calls within the same class cannot get intercepted that way;
     * a caching annotation on such a method within a local call will be ignored
     * since Spring's interceptor does not even kick in for such a runtime scenario.
     * For a more advanced mode of interception, consider switching this to
     * {@link AdviceMode#ASPECTJ}.
     */
    @AliasFor(annotation = EnableCaching.class)
    AdviceMode mode() default PROXY;

    /**
     * Indicate the ordering of the execution of the caching advisor
     * when multiple advices are applied at a specific joinpoint.
     * <p>The default is {@link Ordered#LOWEST_PRECEDENCE}.
     */
    @AliasFor(annotation = EnableCaching.class)
    int order() default LOWEST_PRECEDENCE;
}
