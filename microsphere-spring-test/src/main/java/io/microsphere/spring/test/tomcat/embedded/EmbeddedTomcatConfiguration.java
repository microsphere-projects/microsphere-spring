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
import org.apache.catalina.startup.ContextConfig;
import org.apache.catalina.startup.Tomcat;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AliasFor;
import org.springframework.test.context.BootstrapWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
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
 * @see Context
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
@RunWith(SpringRunner.class)
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
     * @return the default value of context path : ""
     * @see Context#setPath(String)
     */
    String contextPath() default "";

    /**
     * The resource location of the Tomcat's {@link Tomcat#setBaseDir(String) base directory}.
     * <p>
     * The value supports two types of locations:
     * <ul>
     *     <li>File System, e.g: /tmp/catalina </li>
     *     <li>Spring Resource location, e.g: classpath:/catalina, file:/temp/catalina</li>
     * </ul>
     *
     * @return the default value of basedir : The "java.io.tmpdir" system property (the directory where Java temporary
     * directory) where a directory named tomcat.$PORT will be created. $PORT is the value configured via
     * {@link Tomcat#setPort(int)} which defaults to 8080 if not set
     * @see Tomcat#setBaseDir(String)
     */
    String basedir() default "${java.io.tmpdir}";

    /**
     * The resource location of the Tomcat's {@link Context#getDocBase() the root directory of context document}.
     * <p>
     * The value supports two types of locations:
     * <ul>
     *     <li>File System, e.g: /tmp/webapp </li>
     *     <li>Spring Resource location, e.g: classpath:/WEB-INF/webapp, file:/temp/webapp</li>
     * </ul>
     * <p>
     * Alias for {@link WebAppConfiguration#value()}.
     * <p>
     * Note : {@link #docBase()} could be ignored if {@link #alternativeWebXml()} is specified.
     *
     * @return the default value : "classpath:/webapp"
     * @see Context#setDocBase(String)
     */
    @AliasFor(annotation = WebAppConfiguration.class, attribute = "value")
    String docBase() default "classpath:/webapp";

    /**
     * The resource location of an alternative deployment descriptor for the web application.
     * <p>
     * The value supports two types of locations:
     * <ul>
     *     <li>File System, e.g: /tmp/web.xml </li>
     *     <li>Spring Resource location, e.g: classpath:/WEB-INF/web.xml, file:/temp/web.xml</li>
     * </ul>
     * <p>
     * Note : Whether the location is based on File System or Spring Resource, Tomcat only uses the deployment descriptor
     * whose underlying layer resource must be the file, please reference to {@link ContextConfig#getContextWebXmlSource()
     * the Tomcat's source code}.
     * <p>
     * If {@link #alternativeWebXml()} is specified, {@link #docBase()} will be ignored, and the context's document base
     * will be the parent directory of the alternative deployment descriptor.
     *
     * @return the default value is "", which means no alternative deployment descriptor required.
     * @see Context#setAltDDName(String)
     */
    String alternativeWebXml() default "";

    /**
     * Enable the Tomcat features
     *
     * @return the empty array as default
     */
    Feature[] features() default {};

    /**
     * The <em>component classes</em> to use for loading an {@link ApplicationContext ApplicationContext}.
     * <p>
     * Alias for {@link ContextConfiguration#classes}.
     */
    @AliasFor(annotation = ContextConfiguration.class)
    Class<?>[] classes() default {};

    /**
     * The resource locations to use for loading an {@link ApplicationContext ApplicationContext}.
     * <p>
     * Alias for {@link ContextConfiguration#locations}.
     */
    @AliasFor(annotation = ContextConfiguration.class)
    String[] locations() default {};

    /**
     * The application context <em>initializer classes</em> to use for initializing a {@link ConfigurableApplicationContext}.
     * <p>
     * Alias for {@link ContextConfiguration#initializers}.
     */
    @AliasFor(annotation = ContextConfiguration.class)
    Class<? extends ApplicationContextInitializer<?>>[] initializers() default {};

    /**
     * Whether {@linkplain #locations resource locations} or {@linkplain #classes <em>component classes</em>} from
     * test superclasses and enclosing classes should be <em>inherited</em>.
     * <p>
     * Alias for {@link ContextConfiguration#inheritLocations}.
     */
    @AliasFor(annotation = ContextConfiguration.class)
    boolean inheritLocations() default true;

    /**
     * Whether {@linkplain #initializers context initializers} from test superclasses and enclosing classes should be
     * <em>inherited</em>.
     * <p>
     * Alias for {@link ContextConfiguration#inheritInitializers}.
     */
    @AliasFor(annotation = ContextConfiguration.class)
    boolean inheritInitializers() default true;

    /**
     * The Tomcat features
     */
    enum Feature {

        /**
         * Enables the JNDI feature
         *
         * @see Tomcat#enableNaming()
         */
        NAMING,

        /**
         * Add the default web.xml file to the context
         *
         * @see Tomcat#setAddDefaultWebXmlToWebapp(boolean)
         */
        DEFAULT_WEB_XML,

        /**
         * Provide default configuration for a context. This is broadly the programmatic equivalent of the default web.xml
         * and provides the following features:
         * <ul>
         * <li>Default servlet mapped to "/"</li>
         * <li>JSP servlet mapped to "*.jsp" and ""*.jspx"</li>
         * <li>Session timeout of 30 minutes</li>
         * <li>MIME mappings (subset of those in conf/web.xml)</li>
         * <li>Welcome files</li>
         * </ul>
         *
         * @see Tomcat#initWebappDefaults(Context)
         */
        WEB_APP_DEFAULTS,

        /**
         * Uses test dependencies rather than only runtime
         *
         * @see Context#setParentClassLoader(ClassLoader)
         */
        USE_TEST_CLASSPATH,

        /**
         * Controls the loggers will be silenced.
         *
         * @see Tomcat#setSilent(boolean)
         */
        SILENT;
    }
}