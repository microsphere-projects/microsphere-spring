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

package io.microsphere.spring.test.tomcat.embedded;

import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The annotation to startup the Embedded Tomcat server for Spring integration testing.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Tomcat
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
@ExtendWith(SpringExtension.class)
@BootstrapWith(EmbeddedTomcatTestContextBootstrapper.class)
@ContextConfiguration
@WebAppConfiguration
public @interface EmbeddedTomcatConfiguration {

    /**
     * The port of Tomcat
     *
     * @return the default value of port : 8080
     */
    int port() default 8080;

    /**
     * The context path of Tomcat
     *
     * @return the default value of context path : "/"
     * @see Context#getPath()
     */
    String contextPath() default "";

    /**
     * Alias for {@link WebAppConfiguration#value()}.
     * The resource location of the Tomcat's {@link Context#getDocBase() document root}
     *
     * @return An absolute pathname or a relative (to the Host's appBase) pathname.
     * The default value : "classpath:/webapp"
     * @see Context#setDocBase(String)
     */
    @AliasFor(annotation = WebAppConfiguration.class, attribute = "value")
    String docBase() default "classpath:/webapp";

    /**
     * Alias for {@link ContextConfiguration#classes}.
     */
    @AliasFor(annotation = ContextConfiguration.class)
    Class<?>[] classes() default {};

    /**
     * Alias for {@link ContextConfiguration#locations}.
     */
    @AliasFor(annotation = ContextConfiguration.class)
    String[] locations() default {};

    /**
     * Alias for {@link ContextConfiguration#initializers}.
     */
    @AliasFor(annotation = ContextConfiguration.class)
    Class<? extends ApplicationContextInitializer<?>>[] initializers() default {};

    /**
     * Alias for {@link ContextConfiguration#inheritLocations}.
     */
    @AliasFor(annotation = ContextConfiguration.class)
    boolean inheritLocations() default true;

    /**
     * Alias for {@link ContextConfiguration#inheritInitializers}.
     */
    @AliasFor(annotation = ContextConfiguration.class)
    boolean inheritInitializers() default true;

    /**
     * Alias for {@link ContextConfiguration#name}.
     */
    @AliasFor(annotation = ContextConfiguration.class)
    String name() default "";
}
