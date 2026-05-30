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

import io.microsphere.annotation.Nonnull;
import io.microsphere.spring.beans.factory.DelegatingFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.GenericBeanDefinition;

import java.util.function.Supplier;

import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.setInstanceSupplier;
import static org.springframework.aop.support.AopUtils.getTargetClass;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * The utilities class of {@link BeanDefinitionBuilder}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanDefinitionBuilder
 * @since 1.0.0
 */
public abstract class BeanDefinitionBuilderUtils {

    /**
     * Create a new {@code BeanDefinitionBuilder} used to construct a {@link GenericBeanDefinition}.
     *
     * @param instanceSupplier a callback for creating an instance of the bean
     * @param <T>              the type of instance
     * @return non-null
     */
    @Nonnull
    public static <T> BeanDefinitionBuilder genericBeanDefinitionBuilder(Supplier<T> instanceSupplier) {
        T bean = instanceSupplier.get();
        Class beanClass = getTargetClass(bean);
        return genericBeanDefinitionBuilder(beanClass, instanceSupplier);
    }

    /**
     * Create a new {@code BeanDefinitionBuilder} used to construct a {@link GenericBeanDefinition}.
     *
     * @param beanClass        the {@code Class} of the bean that the definition is being created for
     * @param instanceSupplier a callback for creating an instance of the bean
     * @param <T>              the type of instance
     * @return non-null
     * @see BeanDefinitionBuilder#genericBeanDefinition(Class, Supplier)
     */
    @Nonnull
    public static <T> BeanDefinitionBuilder genericBeanDefinitionBuilder(Class<T> beanClass, Supplier<T> instanceSupplier) {
        BeanDefinitionBuilder beanDefinitionBuilder = genericBeanDefinition(beanClass);
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        boolean spring5AndAbove = setInstanceSupplier(beanDefinition, instanceSupplier);
        initBeanDefinitionBuilder(spring5AndAbove, beanDefinitionBuilder, instanceSupplier);
        return beanDefinitionBuilder;
    }

    static <T> void initBeanDefinitionBuilder(boolean spring5AndAbove, BeanDefinitionBuilder beanDefinitionBuilder, Supplier<T> instanceSupplier) {
        if (spring5AndAbove) { // Spring 5.x and above
            return;
        }
        T bean = instanceSupplier.get();
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getRawBeanDefinition();
        beanDefinition.setBeanClass(DelegatingFactoryBean.class);
        beanDefinition.setSource(bean);
        beanDefinitionBuilder.addConstructorArgValue(bean);
    }

    private BeanDefinitionBuilderUtils() {
    }
}