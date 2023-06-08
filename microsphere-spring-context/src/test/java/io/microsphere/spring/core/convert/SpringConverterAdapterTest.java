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
package io.microsphere.spring.core.convert;

import org.junit.Test;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import static io.microsphere.spring.core.convert.SpringConverterAdapter.buildConvertiblePair;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.core.convert.TypeDescriptor.forObject;
import static org.springframework.core.convert.TypeDescriptor.valueOf;

/**
 * {@link SpringConverterAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SpringConverterAdapterTest {

    private SpringConverterAdapter converterAdapter = SpringConverterAdapter.INSTANCE;

    @Test
    public void testMatch() {
        assertMatch(String.class, Boolean.class);
        assertMatch(String.class, Character.class);
        assertMatch(String.class, char[].class);
        assertMatch(String.class, Float.class);
        assertMatch(String.class, Double.class);
        assertMatch(String.class, Short.class);
        assertMatch(String.class, Integer.class);
        assertMatch(String.class, Long.class);
        assertMatch(String.class, Optional.class);
        assertMatch(String.class, String.class);
        assertMatch(Map.class, Properties.class);
        assertMatch(Properties.class, String.class);
    }

    @Test
    public void testGetConvertibleTypes() {
        assertGetConvertibleTypes(String.class, Boolean.class);
        assertGetConvertibleTypes(String.class, Character.class);
        assertGetConvertibleTypes(String.class, char[].class);
        assertGetConvertibleTypes(String.class, Float.class);
        assertGetConvertibleTypes(String.class, Double.class);
        assertGetConvertibleTypes(String.class, Short.class);
        assertGetConvertibleTypes(String.class, Integer.class);
        assertGetConvertibleTypes(String.class, Long.class);
        assertGetConvertibleTypes(String.class, Optional.class);
        assertGetConvertibleTypes(String.class, String.class);
        assertGetConvertibleTypes(Map.class, Properties.class);
        assertGetConvertibleTypes(Properties.class, String.class);
    }

    @Test
    public void testConvert() {
        assertConvert("true", Boolean.TRUE);
        assertConvert("c", 'c');
        assertConvert("1", Float.valueOf(1));
        assertConvert("1", Double.valueOf(1));
        assertConvert("1", Short.valueOf((short) 1));
        assertConvert("1", Integer.valueOf(1));
        assertConvert("1", Long.valueOf(1));
        assertConvert("1", Optional.of("1"));
        assertConvert("1", "1");
    }

    private void assertMatch(Class<?> sourceType, Class<?> targetClass) {
        assertTrue(converterAdapter.matches(valueOf(sourceType), valueOf(targetClass)));
    }

    private void assertGetConvertibleTypes(Class<?> sourceType, Class<?> targetClass) {
        Set<GenericConverter.ConvertiblePair> convertibleTypes = converterAdapter.getConvertibleTypes();
        assertTrue(convertibleTypes.contains(buildConvertiblePair(sourceType, targetClass)));
    }

    private <S, T> void assertConvert(S source, T expected) {
        TypeDescriptor sourceType = forObject(source);
        TypeDescriptor targetType = forObject(expected);
        Object target = converterAdapter.convert(source, sourceType, targetType);
        if (targetType.isArray()) {
            assertArrayEquals((Object[]) expected, (Object[]) target);
        } else {
            assertEquals(expected, target);
        }
    }
}
