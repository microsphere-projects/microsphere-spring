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
package io.microsphere.spring.beans.factory.support;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import static io.microsphere.spring.beans.factory.support.ListenableAutowireCandidateResolver.register;

/**
 * An {@link ApplicationContextInitializer} implementation that registers a
 * {@link ListenableAutowireCandidateResolver} to provide extensible autowiring
 * capabilities within the Spring application context.
 *
 * <p><strong>Example usage:</strong>
 * <pre>{@code
 * public class MyConfig implements WebMvcConfigurer {
 *     // Configuration code...
 * }
 * }</pre>
 *
 * <p>This initializer should be registered with a {@link org.springframework.context.ApplicationContext}
 * to enable custom autowiring logic during the application context initialization phase.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ListenableAutowireCandidateResolver
 * @see ApplicationContextInitializer
 * @since 1.0.0
 */
public class ListenableAutowireCandidateResolverInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        register(applicationContext);
    }
}
