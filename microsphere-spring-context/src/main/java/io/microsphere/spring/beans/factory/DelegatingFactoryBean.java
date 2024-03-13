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
package io.microsphere.spring.beans.factory;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static io.microsphere.spring.util.BeanUtils.invokeAwareInterfaces;
import static io.microsphere.spring.util.BeanUtils.invokeBeanNameAware;
import static io.microsphere.spring.util.BeanUtils.invokeInitializingBean;

/**
 * {@link FactoryBean} implementation based on delegate object that was instantiated
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see FactoryBean
 * @since 1.0.0
 */
public class DelegatingFactoryBean implements FactoryBean<Object>, InitializingBean, DisposableBean,
        ApplicationContextAware, BeanNameAware {

    private final Object delegate;

    private final Class<?> objectType;

    public DelegatingFactoryBean(Object delegate) {
        this.delegate = delegate;
        this.objectType = delegate.getClass();
    }

    @Override
    public Object getObject() throws Exception {
        return delegate;
    }

    @Override
    public Class<?> getObjectType() {
        return objectType;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        invokeInitializingBean(delegate);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        invokeAwareInterfaces(delegate, context);
    }

    @Override
    public void setBeanName(String name) {
        invokeBeanNameAware(delegate, name);
    }

    @Override
    public void destroy() throws Exception {
        if (delegate instanceof DisposableBean) {
            ((DisposableBean) delegate).destroy();
        }
    }
}
