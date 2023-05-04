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
package io.github.microsphere.spring.config.annotation;

import io.github.microsphere.spring.config.zookeeper.annotation.ZookeeperConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.PropertySource;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * A meta-annotation enables Spring Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface EnableConfig {

    /**
     * The name of {@link PropertySource} If absent
     *
     * @return the name of {@link PropertySource}, the default value means the name of {@link PropertySource}
     * will be auto-generated
     */
    String name() default "";

    /**
     * It indicates the property source is auto-refreshed when the configuration is
     * changed.
     *
     * @return default value is <code>false</code>
     */
    boolean autoRefreshed() default false;

    /**
     * Indicates current {@link PropertySource} is first order or not If specified ,
     * {@link #before()} and {@link #after()} will be ignored, or last order.
     *
     * @return default value is <code>false</code>
     */
    boolean first() default false;

    /**
     * The relative order before specified {@link PropertySource}
     * <p>
     * If not specified , current {@link PropertySource} will be added last.
     * <p>
     * If {@link #first()} specified , current attribute will be ignored.
     *
     * @return the name of {@link PropertySource}, default value is the empty string
     */
    String before() default "";

    /**
     * The relative order after specified {@link PropertySource}
     * <p>
     * If not specified , current {@link PropertySource} will be added last.
     * <p>
     * If {@link #first()} specified , current attribute will be ignored.
     *
     * @return the name of {@link PropertySource}, default value is the empty string
     */
    String after() default "";
}
