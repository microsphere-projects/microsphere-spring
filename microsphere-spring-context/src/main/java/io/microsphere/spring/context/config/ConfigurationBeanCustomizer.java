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
package io.microsphere.spring.context.config;

import io.microsphere.spring.beans.factory.annotation.ConfigurationBeanBindingPostProcessor;
import org.springframework.core.Ordered;

/**
 * A callback interface that allows for customizing a configuration bean after it has been bound
 * but before it is registered in the Spring application context.
 *
 * <p>Implementations of this interface can perform additional processing or modifications
 * on the configuration bean. If multiple {@code ConfigurationBeanCustomizer} beans are present,
 * they will be executed in the order determined by the {@link Ordered} interface.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * public class MyConfigurationBeanCustomizer implements ConfigurationBeanCustomizer {
 *
 *     private final int order;
 *
 *     public MyConfigurationBeanCustomizer(int order) {
 *         this.order = order;
 *     }
 *
 *     @Override
 *     public int getOrder() {
 *         return order;
 *     }
 *
 *     @Override
 *     public void customize(String beanName, Object configurationBean) {
 *         // Customization logic here
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConfigurationBeanBinder
 * @see ConfigurationBeanBindingPostProcessor
 * @since 1.0.0
 */
public interface ConfigurationBeanCustomizer extends Ordered {

    /**
     * Customize the configuration bean
     *
     * @param beanName          the name of the configuration bean
     * @param configurationBean the configuration bean
     */
    void customize(String beanName, Object configurationBean);

}
