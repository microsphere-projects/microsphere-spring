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

package io.microsphere.spring.web.util;

import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Map;
import java.util.Set;

import static io.microsphere.spring.web.util.SpringWebType.UNKNOWN;

/**
 * Unknown {@link SpringWebHelper}, all method return {@code null} exception {@link #getType()}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringWebHelper
 * @since 1.0.0
 */
public class UnknownSpringWebHelper implements SpringWebHelper {

    public static final UnknownSpringWebHelper INSTANCE = new UnknownSpringWebHelper();

    public UnknownSpringWebHelper() {
    }

    @Override
    public String getMethod(NativeWebRequest request) {
        return null;
    }

    @Override
    public String getCookieValue(NativeWebRequest request, String cookieName) {
        return null;
    }

    @Override
    public <T> T getRequestBody(NativeWebRequest request, Class<T> requestBodyType) {
        return null;
    }

    @Override
    public Object getBestMatchingHandler(NativeWebRequest request) {
        return null;
    }

    @Override
    public String getPathWithinHandlerMapping(NativeWebRequest request) {
        return null;
    }

    @Override
    public String getBestMatchingPattern(NativeWebRequest request) {
        return null;
    }

    @Override
    public Map<String, String> getUriTemplateVariables(NativeWebRequest request) {
        return null;
    }

    @Override
    public Map<String, MultiValueMap<String, String>> getMatrixVariables(NativeWebRequest request) {
        return null;
    }

    @Override
    public Set<MediaType> getProducibleMediaTypes(NativeWebRequest request) {
        return null;
    }

    @Override
    public SpringWebType getType() {
        return UNKNOWN;
    }
}