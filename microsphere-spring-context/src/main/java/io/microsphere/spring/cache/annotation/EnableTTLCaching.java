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

/**
 * {@link @EnableCaching} Plus TTL features
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
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
    AdviceMode mode() default AdviceMode.PROXY;

    /**
     * Indicate the ordering of the execution of the caching advisor
     * when multiple advices are applied at a specific joinpoint.
     * <p>The default is {@link Ordered#LOWEST_PRECEDENCE}.
     */
    @AliasFor(annotation = EnableCaching.class)
    int order() default Ordered.LOWEST_PRECEDENCE;
}
