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
package io.microsphere.spring.redis.metadata;

import org.apache.commons.lang3.ClassUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.redis.connection.RedisCommands;
import org.yaml.snakeyaml.Yaml;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;

/**
 * {@link MethodMetadataRepository} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class MethodRedisMetadataRepositoryTest {

    @Test
    public void testInit() {
        MethodMetadataRepository.init();
    }

    @Test
    public void testTypes() {
        Set<Type> types = new TreeSet<>(Comparator.comparing(Type::getTypeName));

        for (Method method : MethodMetadataRepository.getWriteCommandMethods()) {
            Type returnType = method.getGenericReturnType();
            types.addAll(findTypes(returnType));
            for (Type parameterType : method.getGenericParameterTypes()) {
                types.addAll(findTypes(parameterType));
            }
        }

        types.forEach(type -> {
            if (type instanceof Class) {
                System.out.println(((Class) type).getName());
            }
        });
    }

    @Test
    public void testWriteMethods() throws Throwable {
        Resource resource = new ClassPathResource("/META-INF/redis-metadata.yaml");

        Yaml yaml = new Yaml();

        RedisMetadata redisMetadata = yaml.loadAs(resource.getInputStream(), RedisMetadata.class);

        List<MethodMetadata> methods = redisMetadata.getMethods();
        int size = methods.size();
        for (int i = 0; i < size; i++) {
            MethodMetadata methodMetadata = methods.get(i);
            assertEquals(methodMetadata.getIndex(), i + 1);
        }
    }

    @Test
    public void testMethods() {
        for (Class interfaceClass : ClassUtils.getAllInterfaces(RedisCommands.class)) {
            for (Method method : interfaceClass.getMethods()) {
                String interfaceName = interfaceClass.getName();
                String methodName = method.getName();
                String parameterTypes = getParameterTypes(method);
                System.out.println("- interfaceName: " + interfaceName);
                System.out.println("  methodName: " + methodName);
                System.out.println("  parameterTypes: " + parameterTypes);
            }
        }
    }

    private String getParameterTypes(Method method) {
        StringJoiner parameterTypesBuilder = new StringJoiner(",", "[", "]");
        int parameterCount = method.getParameterCount();
        Class[] parameterClasses = method.getParameterTypes();
        for (int i = 0; i < parameterCount; i++) {
            parameterTypesBuilder.add("'" + parameterClasses[i].getName() + "'");
        }
        return parameterTypesBuilder.toString();
    }

    private Set<Type> findTypes(Type type) {
        Set<Type> types = new HashSet<>();
        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            types.add(pType.getRawType());
            for (Type t : pType.getActualTypeArguments()) {
                types.addAll(findTypes(t));
            }
        } else if (type instanceof Class) {
            types.add(type);
        }
        return types;
    }
}
