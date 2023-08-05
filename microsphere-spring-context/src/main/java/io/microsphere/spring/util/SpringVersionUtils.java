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

import io.microsphere.util.BaseUtils;
import io.microsphere.util.Version;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import static io.microsphere.text.FormatUtils.format;

/**
 * The Utilities class for Spring Framework Version
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class SpringVersionUtils extends BaseUtils {

    public static final String SPRING_BASE_PACKAGE_NAME = "org.springframework";

    /**
     * Get the {@link Version} from the Spring class
     *
     * @param springClass the Spring class
     * @return non-null
     * @throws NullPointerException     if <code>springClass</code> is <code>null</code>
     * @throws IllegalArgumentException if the package name of <code>springClass</code> does not start with "org.springframework"
     */
    @NonNull
    public static Version getSpringVersion(Class<?> springClass) throws NullPointerException, IllegalArgumentException {
        Package classPackage = springClass.getPackage();
        String classPackageName = classPackage.getName();
        Assert.isTrue(classPackageName.startsWith(SPRING_BASE_PACKAGE_NAME),
                () -> format("The class[name : '{}'] is not packaged into the Spring package['{}']",
                        springClass.getName(), SPRING_BASE_PACKAGE_NAME));
        String implementationVersion = classPackage.getImplementationVersion();
        return Version.of(implementationVersion);
    }
}
