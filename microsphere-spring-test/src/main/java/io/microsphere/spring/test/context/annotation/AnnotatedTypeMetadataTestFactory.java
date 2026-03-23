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

package io.microsphere.spring.test.context.annotation;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.StandardMethodMetadata;

import java.lang.reflect.Method;

import static java.lang.Thread.currentThread;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.ReflectionUtils.findMethod;

/**
 * {@link AnnotatedTypeMetadata} Test Factory
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class AnnotatedTypeMetadataTestFactory implements BeanClassLoaderAware {

    private ClassLoader classLoader;

    public AnnotatedTypeMetadata createMethodAnnotatedTypeMetadata() {
        Method method = findTestMethod();
        return new StandardMethodMetadata(method);
    }

    private Method findTestMethod() {
        // TODO be refactored by StackWalker API
        StackTraceElement[] stackTraces = currentThread().getStackTrace();
        StackTraceElement stackTrace = stackTraces[3];
        String className = stackTrace.getClassName();
        String methodName = stackTrace.getMethodName();
        Class<?> targetClass = resolveClassName(className, classLoader);
        return findMethod(targetClass, methodName);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
