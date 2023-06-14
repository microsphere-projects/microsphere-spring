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
package io.microsphere.spring.beans.factory.annotation;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.beans.Introspector;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Set;

/**
 * {@link AnnotatedDependencyInjectionResolver} for {@link Resource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ResourceDependencyInjectionResolver extends AnnotatedDependencyInjectionResolver<Resource> {

    @Override
    public Resource getAnnotation(Parameter parameter) {
        // Find @Resource annotation in the method
        Executable executable = parameter.getDeclaringExecutable();
        Resource resource = super.getAnnotation(executable);
        return resource;
    }

    @Override
    public void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Resource resource = getAnnotation(field);
        if (resource == null) {
            // @Resource annotation can't be found in the field
            return;
        }
        Class<?> beanType = resource.type();
        if (Object.class.equals(beanType)) { // Default value
            String beanName = resolveBeanName(field, resource, beanFactory);
            dependentBeanNames.add(beanName);
        } else {
            String[] beanNames = beanFactory.getBeanNamesForType(beanType, false, false);
            for (String name : beanNames) {
                dependentBeanNames.add(name);
            }
        }
    }


    private String resolveBeanName(Field field, Resource resource, ConfigurableListableBeanFactory beanFactory) {
        String name = resource.name();
        if (StringUtils.hasText(name)) {
            return name;
        } else {
            return field.getName();
        }
    }

    private String resolveBeanName(Parameter parameter, Resource resource, ConfigurableListableBeanFactory beanFactory) {
        String name = resource.name();
        if (!StringUtils.hasText(name)) {
            Executable executable = parameter.getDeclaringExecutable();
            String methodName = executable.getName();
            if (methodName.startsWith("set")) {
                name = Introspector.decapitalize(methodName.substring(3));
            }
        }
        return name;
    }

    @Override
    public void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Resource resource = getAnnotation(parameter);
        if (resource == null) {
            // @Resource annotation can't be found in the method parameter
            return;
        }
        Class<?> beanType = resource.type();
        if (Object.class.equals(beanType)) { // Default value
            String beanName = resolveBeanName(parameter, resource, beanFactory);
            dependentBeanNames.add(beanName);
        } else {
            String[] beanNames = beanFactory.getBeanNamesForType(beanType, false, false);
            for (String name : beanNames) {
                dependentBeanNames.add(name);
            }
        }
    }
}
