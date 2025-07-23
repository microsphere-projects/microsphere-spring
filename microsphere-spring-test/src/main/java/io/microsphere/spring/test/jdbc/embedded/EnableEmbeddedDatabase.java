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

package io.microsphere.spring.test.jdbc.embedded;

import org.springframework.context.annotation.Import;

import javax.sql.DataSource;
import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.microsphere.spring.test.jdbc.embedded.EmbeddedDatabaseType.SQLITE;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Enable an embedded database
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Documented
@Import(EmbeddedDataBaseBeanDefinitionRegistrar.class)
@Repeatable(EnableEmbeddedDatabases.class)
public @interface EnableEmbeddedDatabase {

    /**
     * @return Embedded Database Type, SQLite as default
     */
    EmbeddedDatabaseType type() default SQLITE;

    /**
     * JDBC connection port
     *
     * @return if not specified port, return <code>-1</code>
     */
    int port() default -1;

    /**
     * @return {@link DataSource} Bean Name
     */
    String dataSource();

    /**
     * @return Primary Bean or not
     */
    boolean primary() default false;

    /**
     * @return JDBC Properties Properties, Key-value
     */
    String[] properties() default {};
}
