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

import io.microsphere.util.BaseUtils;
import org.springframework.core.MethodParameter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * The utility class for {@link MethodParameter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MethodParameter
 * @since 1.0.0
 */
public abstract class MethodParameterUtils extends BaseUtils {

    /**
     * Create a new MethodParameter for the given parameter descriptor.
     * <p>This is a convenience factory method for scenarios where a
     * Java 8 {@link Parameter} descriptor is already available.
     *
     * @param parameter the parameter descriptor
     * @return the corresponding MethodParameter instance
     * @see MethodParameter#forParameter(Parameter)
     * @since Spring Framework 5.0
     */
    public static MethodParameter forParameter(Parameter parameter) {
        return forExecutable(parameter.getDeclaringExecutable(), findParameterIndex(parameter));
    }

    /**
     * Create a new MethodParameter for the given method or constructor.
     * <p>This is a convenience factory method for scenarios where a
     * Method or Constructor reference is treated in a generic fashion.
     *
     * @param executable     the Method or Constructor to specify a parameter for
     * @param parameterIndex the index of the parameter
     * @return the corresponding MethodParameter instance
     * @since Spring Framework 5.0
     */
    public static MethodParameter forExecutable(Executable executable, int parameterIndex) {
        if (executable instanceof Method) {
            return new MethodParameter((Method) executable, parameterIndex);
        } else if (executable instanceof Constructor) {
            return new MethodParameter((Constructor<?>) executable, parameterIndex);
        } else {
            throw new IllegalArgumentException("Not a Method/Constructor: " + executable);
        }
    }


    protected static int findParameterIndex(Parameter parameter) {
        Executable executable = parameter.getDeclaringExecutable();
        Parameter[] allParams = executable.getParameters();
        // Try first with identity checks for greater performance.
        for (int i = 0; i < allParams.length; i++) {
            if (parameter == allParams[i]) {
                return i;
            }
        }
        // Potentially try again with object equality checks in order to avoid race
        // conditions while invoking java.lang.reflect.Executable.getParameters().
        for (int i = 0; i < allParams.length; i++) {
            if (parameter.equals(allParams[i])) {
                return i;
            }
        }
        throw new IllegalArgumentException("Given parameter [" + parameter +
                "] does not match any parameter in the declaring executable");
    }
}
