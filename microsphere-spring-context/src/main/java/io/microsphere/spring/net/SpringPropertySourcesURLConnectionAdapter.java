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
package io.microsphere.spring.net;

import io.microsphere.spring.core.convert.SpringConverterAdapter;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.PropertySources;
import org.springframework.util.MimeType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;
import java.util.Properties;

import static io.microsphere.constants.PathConstants.SLASH_CHAR;
import static io.microsphere.net.URLUtils.resolveAuthority;
import static io.microsphere.net.URLUtils.resolvePath;
import static io.microsphere.spring.util.PropertySourcesUtils.getSubProperties;

/**
 * The {@link URLConnection} adapter class is based on the Spring {@link PropertySources}
 * <p>
 * The URL pattern : "spring:env:property-sources://{property-name-prefix}/{media-type}":
 * <ul>
 *  <li>{property-name-prefix} : the prefix of property name, for instance : "microsphere." or "microsphere" </li>
 *  <li>{media-type} : the media type, may be : {@link #DEFAULT_MEDIA_TYPE "text/properties"}(as default if absent) or "text/yaml" </li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SpringPropertySourcesURLConnectionAdapter extends URLConnection {

    public static final MimeType DEFAULT_MEDIA_TYPE = MimeType.valueOf("text/properties");

    private final PropertySources propertySources;

    private final ConfigurableConversionService conversionService;

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url               the specified URL.
     * @param propertySources   {@link PropertySources}
     * @param conversionService {@link ConfigurableConversionService}
     */
    protected SpringPropertySourcesURLConnectionAdapter(URL url, PropertySources propertySources, ConfigurableConversionService conversionService) {
        super(url);
        this.propertySources = propertySources;
        this.conversionService = conversionService;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        URL url = getURL();
        String prefix = getPropertyNamePrefix(url);
        Map<String, Object> properties = getSubProperties(propertySources, prefix);
        MimeType mediaType = getMediaType(url);
        String primaryType = mediaType.getType();
        String subtype = mediaType.getSubtype();
        // Map -> subtype
        Object result = convert(properties, Map.class, subtype);
        // subtype -> primary type
        result = convert(result, subtype, primaryType);
        // primary type -> InputStream
        return convert(result, primaryType, InputStream.class);
    }

    @Override
    public void connect() throws IOException {
    }

    private Object convert(Object source, String sourceMediaType, String targetMediaType) throws UnsupportedOperationException {
        Class<?> sourceType = getJavaType(sourceMediaType);
        Class<?> targetType = getJavaType(targetMediaType);
        return convert(source, sourceType, targetType);
    }

    private Object convert(Object source, Class<?> sourceType, String targetMediaType) throws UnsupportedOperationException {
        Class<?> targetType = getJavaType(targetMediaType);
        return convert(source, sourceType, targetType);
    }

    private <T> T convert(Object source, String fromType, Class<T> targetType) throws UnsupportedOperationException {
        Class<?> sourceType = getJavaType(fromType);
        return convert(source, sourceType, targetType);
    }

    private <S, T> T convert(Object source, Class<S> sourceType, Class<T> targetType) throws UnsupportedOperationException {
        if (!conversionService.canConvert(sourceType, targetType)) {
            throw new UnsupportedOperationException("The source type['" + sourceType.getName() + "] can't be converted to the target type['" + targetType.getName() + "']!");
        }
        return conversionService.convert(source, targetType);
    }

    private String getPropertyNamePrefix(URL url) {
        return resolveAuthority(url.getAuthority());
    }

    private MimeType getMediaType(URL url) {
        String path = resolvePath(url);
        if (path.indexOf(SLASH_CHAR) == 0) {
            path = path.substring(1);
        }
        MimeType mimeType = path == null ? DEFAULT_MEDIA_TYPE : MimeType.valueOf(path);
        return mimeType;
    }

    /**
     * Get the Java {@link Class} object from the specified type
     *
     * @param type the primary type or sub type from {@link MimeType media-type}
     * @return Java {@link Class} object
     */
    private Class<?> getJavaType(String type) {
        if ("text".equals(type)) {
            return String.class;
        } else if ("properties".equals(type)) {
            return Properties.class;
        } else if ("map".equals(type)) {
            return Map.class;
        }
        // TODO support more media type
        throw new UnsupportedOperationException("The 'type'[" + type + "] does not be supported");
    }
}
