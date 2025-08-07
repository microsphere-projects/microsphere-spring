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

package io.microsphere.spring.core;

import io.microsphere.spring.test.domain.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static io.microsphere.reflect.AccessibleObjectUtils.trySetAccessible;
import static io.microsphere.reflect.ConstructorUtils.findConstructor;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.core.MethodParameterUtils.findParameterIndex;
import static io.microsphere.spring.core.MethodParameterUtils.forExecutable;
import static io.microsphere.spring.core.MethodParameterUtils.forParameter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * {@link MethodParameterUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MethodParameterUtils
 * @since 1.0.0
 */
public class MethodParameterUtilsTest {

    private Constructor constructor;

    private Method method;

    private Parameter parameter;

    @Before
    public void before() {
        this.constructor = findConstructor(User.class);
        this.method = findMethod(User.class, "setName", String.class);
        Method method = findMethod(User.class, "setName", String.class);
        Parameter[] parameters = method.getParameters();
        assertEquals(1, parameters.length);
        this.parameter = parameters[0];
    }

    @Test
    public void testForParameter() {
        MethodParameter methodParameter = forParameter(parameter);
        assertMethodParameter(methodParameter);
    }

    @Test
    public void testForExecutable() {
        MethodParameter methodParameter = forExecutable(constructor, -1);
        assertEquals(-1, methodParameter.getParameterIndex());

        methodParameter = forExecutable(method, 0);
        assertMethodParameter(methodParameter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testForNullExecutable() {
        forExecutable(null, -1);
    }

    void assertMethodParameter(MethodParameter methodParameter) {
        assertSame(String.class, methodParameter.getParameterType());
        assertEquals(0, methodParameter.getParameterIndex());
    }

    @Test
    public void testFindParameterIndex() throws Throwable {
        assertParameters(User.class);
    }

    @Test
    public void testFindParameterIndexWithClonedParameters() throws Throwable {
        for (Method method : User.class.getMethods()) {
            for (int i = 0; i < method.getParameterCount(); i++) {
                Parameter parameter = method.getParameters()[i];
                assertParameter(i, parameter);
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindParameterIndexOnIllegalArgumentException() throws Throwable {
        Method waitMethod = findMethod(Object.class, "wait", long.class, int.class);
        Parameter clonedParameter = clone(waitMethod.getParameters()[1], findMethod(User.class, "getName"), 0);
        findParameterIndex(clonedParameter);
    }

    void assertParameters(Class<?> klass) throws Throwable {
        for (Constructor constructor : klass.getConstructors()) {
            Parameter[] parameters = constructor.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                assertParameter(i, clone(parameter, i));
            }
        }
    }

    void assertParameter(int index, Parameter parameter) throws Throwable {
        assertEquals(index, findParameterIndex(parameter));
        Parameter clonedParameter = clone(parameter, index);
        assertEquals(index, findParameterIndex(clonedParameter));
    }

    Parameter clone(Parameter parameter, int index) throws Throwable {
        return clone(parameter, parameter.getDeclaringExecutable(), index);
    }

    Parameter clone(Parameter parameter, Executable executable, int index) throws Throwable {
        Constructor<Parameter> constructor = findConstructor(Parameter.class, String.class, int.class, Executable.class, int.class);
        trySetAccessible(constructor);
        return constructor.newInstance(parameter.getName(), parameter.getModifiers(), executable, index);
    }
}
