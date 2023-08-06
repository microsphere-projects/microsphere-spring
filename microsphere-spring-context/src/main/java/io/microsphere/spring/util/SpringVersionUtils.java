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
package io.microsphere.spring.util;

import io.microsphere.spring.core.SpringVersion;
import io.microsphere.util.BaseUtils;
import io.microsphere.util.Version;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.function.BiFunction;
import java.util.function.Function;

import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;

/**
 * The Utilities class for Spring Framework Version
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringVersion
 * @see org.springframework.core.SpringVersion
 * @see Version
 * @since 1.0.0
 */
public abstract class SpringVersionUtils extends BaseUtils {

    private static final ClassLoader classLoader = getDefaultClassLoader();

    public static final String SPRING_BASE_PACKAGE_NAME = "org.springframework";

    /**
     * The version of "spring-core" module
     */
    @NonNull
    public static final Version SPRING_CORE_VERSION = getSpringVersion(StringUtils.class);

    /**
     * The version of "spring-aop" module
     */
    @Nullable
    public static final Version SPRING_AOP_VERSION = getSpringVersion("org.springframework.aop.Advisor");

    /**
     * The version of "spring-beans" module
     */
    @NonNull
    public static final Version SPRING_BEANS_VERSION = getSpringVersion(BeanFactory.class);

    /**
     * The version of "spring-context" module
     */
    @NonNull
    public static final Version SPRING_CONTEXT_VERSION = getSpringVersion(ApplicationContext.class);

    /**
     * The version of "spring-context" module
     */
    @Nullable
    public static final Version SPRING_CONTEXT_SUPPORT_VERSION = getSpringVersion("org.springframework.mail.MailSender");

    /**
     * Get the {@link Version} from the Spring class
     *
     * @param springClassName the Spring class name
     * @return <code>null</code> of the specified class can't be found by its' name
     * the "Implementation-Version" attribute is absent in the "META-INF/MANIFEST.MF" resource
     * @throws NullPointerException     if <code>springClass</code> is <code>null</code>
     * @throws IllegalArgumentException if the package name of <code>springClass</code> does not start with "org.springframework"
     * @throws IllegalStateException    if the "Implementation-Version" attribute is absent in the "META-INF/MANIFEST.MF" resource
     */
    @Nullable
    public static Version getSpringVersion(String springClassName) throws NullPointerException, IllegalArgumentException {
        Class<?> springClass = resolveClass(springClassName, classLoader);
        return springClass == null ? null : getSpringVersion(springClass);
    }

    /**
     * Get the {@link Version} from the Spring class
     *
     * @param springClass the Spring class
     * @return non-null
     * @throws NullPointerException     if <code>springClass</code> is <code>null</code>
     * @throws IllegalArgumentException if the package name of <code>springClass</code> does not start with "org.springframework"
     * @throws IllegalStateException    if the "Implementation-Version" attribute is absent in the "META-INF/MANIFEST.MF" resource
     */
    @NonNull
    public static Version getSpringVersion(Class<?> springClass) throws NullPointerException, IllegalArgumentException {
        Package classPackage = springClass.getPackage();
        String classPackageName = classPackage.getName();
        if (!classPackageName.startsWith(SPRING_BASE_PACKAGE_NAME)) {
            String errorMessage = format("The class[name : '{}'] is not packaged into the Spring package['{}']", springClass.getName(), SPRING_BASE_PACKAGE_NAME);
            throw new IllegalArgumentException(errorMessage);
        }
        String implementationVersion = classPackage.getImplementationVersion();
        if (implementationVersion == null) {

        }
        return implementationVersion == null ? null : Version.of(implementationVersion);
    }
}
