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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Set;

/**
 * {@link AnnotatedInjectionPointDependencyResolver} for {@link Autowired}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class AutowiredInjectionPointDependencyResolver extends AnnotatedInjectionPointDependencyResolver<Autowired> {

    @Override
    public Autowired getAnnotation(Parameter parameter) {
        // Find @Autowired annotation in the parameter
        Autowired autowired = super.getAnnotation(parameter);
        if (autowired == null) {
            // try to find @Autowired annotation in the method or constructor
            Executable executable = parameter.getDeclaringExecutable();
            autowired = super.getAnnotation(executable);
        }
        return autowired;
    }

    @Override
    public void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Autowired autowired = getAnnotation(field);
        if (autowired == null) {
            // @Autowired annotation can't be found in the field
            return;
        }
        super.resolve(field, beanFactory, dependentBeanNames);
    }

    @Override
    public void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Autowired autowired = getAnnotation(parameter);
        if (autowired == null) {
            // @Autowired annotation can't be found in the method parameter
            return;
        }
        super.resolve(parameter, beanFactory, dependentBeanNames);
    }
}
