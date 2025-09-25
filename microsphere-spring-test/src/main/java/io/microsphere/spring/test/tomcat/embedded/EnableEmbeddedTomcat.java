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

import org.apache.catalina.startup.Tomcat;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Enable the Embedded Tomcat
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Tomcat
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
public @interface EnableEmbeddedTomcat {

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
     * @see Tomcat#addContext(String, String)
     */
    String contextPath() default "/";


    /**
     * The basedir of Tomcat
     *
     * @return
     * @see Tomcat#setBaseDir(String)
     */
    String basedir() default "${java.io.tmpdir}/tomcat";
}
