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
package io.github.microsphere.spring.convert;

import io.github.microsphere.convert.Converter;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.ConditionalGenericConverter;
import org.springframework.core.convert.support.ConfigurableConversionService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.github.microsphere.util.ServiceLoaderUtils.loadServicesList;

/**
 * Spring {@link org.springframework.core.convert.converter.Converter} Adapter based on Microsphere {@link Converter Converters}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Converter
 * @see org.springframework.core.convert.converter.Converter
 * @see ConfigurableConversionService
 * @since 1.0.0
 */
public class SpringConverterAdapter implements ConditionalGenericConverter {

    /**
     * Singleton {@link SpringConverterAdapter}
     */
    public static final SpringConverterAdapter INSTANCE = new SpringConverterAdapter();

    private final static Map<ConvertiblePair, Converter> convertersMap;

    static {
        convertersMap = loadConvertersMap();
    }

    private static Map<ConvertiblePair, Converter> loadConvertersMap() {
        ClassLoader classLoader = SpringConverterAdapter.class.getClassLoader();
        List<Converter> converters = loadServicesList(classLoader, Converter.class);
        int size = converters.size();
        Map<ConvertiblePair, Converter> convertersMap = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            Converter converter = converters.get(i);
            ConvertiblePair convertiblePair = buildConvertiblePair(converter);
            convertersMap.put(convertiblePair, converter);
        }
        return convertersMap;
    }

    static ConvertiblePair buildConvertiblePair(Converter converter) {
        return buildConvertiblePair(converter.getSourceType(), converter.getTargetType());
    }

    static ConvertiblePair buildConvertiblePair(TypeDescriptor sourceType, TypeDescriptor targetType) {
        return new ConvertiblePair(sourceType.getType(), targetType.getType());
    }

    static ConvertiblePair buildConvertiblePair(Class<?> sourceType, Class<?> targetType) {
        return new ConvertiblePair(sourceType, targetType);
    }

    static Converter getConverter(TypeDescriptor sourceType, TypeDescriptor targetType) {
        ConvertiblePair convertiblePair = buildConvertiblePair(sourceType, targetType);
        return convertersMap.get(convertiblePair);
    }

    @Override
    public boolean matches(TypeDescriptor sourceType, TypeDescriptor targetType) {
        ConvertiblePair convertiblePair = buildConvertiblePair(sourceType, targetType);
        return convertersMap.containsKey(convertiblePair);
    }

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        return convertersMap.keySet();
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        Converter converter = getConverter(sourceType, targetType);
        return converter.convert(source);
    }
}
