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

package io.microsphere.spring.core.io.support;

import io.microsphere.annotation.Nullable;
import io.microsphere.util.Utils;
import org.springframework.core.env.PropertyResolver;

import java.io.StringReader;
import java.util.Properties;

import static io.microsphere.constants.SeparatorConstants.LINE_SEPARATOR;
import static io.microsphere.lang.function.ThrowableAction.execute;
import static org.springframework.util.StringUtils.arrayToDelimitedString;

/**
 * The utility class for {@link Properties}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Properties
 * @see Utils
 * @since 1.0.0
 */
public abstract class PropertiesUtils implements Utils {

    /**
     * Load {@link Properties} from the specified {@code propertiesValue}
     *
     * @param propertiesValue the specified {@code propertiesValue}
     * @return non-null
     */
    public static Properties loadProperties(String... propertiesValue) {
        return loadProperties(propertiesValue, null);
    }

    /**
     * Load {@link Properties} from the specified {@code propertiesValue}
     *
     * @param propertiesValue  the specified {@code propertiesValue}
     * @param propertyResolver {@link PropertyResolver}
     * @return non-null
     */
    public static Properties loadProperties(String[] propertiesValue, @Nullable PropertyResolver propertyResolver) {
        Properties properties = new Properties();
        String content = arrayToDelimitedString(propertiesValue, LINE_SEPARATOR);
        final String resolvedText;
        if (propertyResolver != null) {
            resolvedText = propertyResolver.resolvePlaceholders(content);
        } else {
            resolvedText = content;
        }
        execute(() -> properties.load(new StringReader(resolvedText)));
        return properties;
    }

    private PropertiesUtils() {
    }
}