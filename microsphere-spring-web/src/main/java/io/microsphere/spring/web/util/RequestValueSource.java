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

import io.microsphere.annotation.Nullable;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import static io.microsphere.collection.ListUtils.first;
import static io.microsphere.spring.web.util.WebRequestUtils.getCookieValue;
import static io.microsphere.spring.web.util.WebRequestUtils.getMatrixVariables;
import static io.microsphere.spring.web.util.WebRequestUtils.getRequestBody;
import static io.microsphere.spring.web.util.WebRequestUtils.getUriTemplateVariables;
import static io.microsphere.spring.web.util.WebScope.REQUEST;
import static io.microsphere.spring.web.util.WebScope.SESSION;

/**
 * The source of the request value.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestAttribute
 * @see SessionAttribute
 * @see RequestParam
 * @see RequestHeader
 * @see CookieValue
 * @see RequestBody
 * @see PathVariable
 * @see MatrixVariable
 * @since 1.0.0
 */
public enum RequestValueSource {

    /**
     * The scope of the request attribute
     *
     * @see RequestAttribute
     */
    ATTRIBUTE {
        @Override
        public String getValue(NativeWebRequest request, String name) {
            return REQUEST.getAttribute(request, name);
        }
    },

    /**
     * The scope of the session attribute
     *
     * @see SessionAttribute
     */
    SESSION_ATTRIBUTE {
        @Override
        public String getValue(NativeWebRequest request, String name) {
            return SESSION.getAttribute(request, name);
        }
    },

    /**
     * The scope of the request parameter
     *
     * @see RequestParam
     */
    PARAMETER {
        @Override
        public String getValue(NativeWebRequest request, String name) {
            return request.getParameter(name);
        }
    },

    /**
     * The scope of the request header
     *
     * @see RequestHeader
     */
    HEADER {
        @Override
        public String getValue(NativeWebRequest request, String name) {
            return request.getHeader(name);
        }
    },

    /**
     * The scope of the request cookie
     *
     * @see CookieValue
     */
    COOKIE {
        @Override
        public String getValue(NativeWebRequest request, String name) {
            return getCookieValue(request, name);
        }
    },

    /**
     * The scope of the request body
     *
     * @see RequestBody
     */
    BODY {
        @Override
        public String getValue(NativeWebRequest request, String name) {
            Map<String, Object> requestBody = getRequestBody(request, Map.class);
            return requestBody == null ? null : (String) requestBody.get(name);
        }
    },

    /**
     * The scope of the request path variable
     *
     * @see PathVariable
     */
    PATH_VARIABLE {
        @Override
        public String getValue(NativeWebRequest request, String name) {
            Map<String, String> variables = getUriTemplateVariables(request);
            return variables == null ? null : variables.get(name);
        }
    },

    /**
     * The scope of the request matrix variable
     *
     * @see MatrixVariable
     */
    MATRIX_VARIABLE {
        @Override
        public String getValue(NativeWebRequest request, String name) {
            Map<String, MultiValueMap<String, String>> variables = getMatrixVariables(request);
            for (MultiValueMap<String, String> parameters : variables.values()) {
                for (Entry<String, List<String>> entry : parameters.entrySet()) {
                    if (Objects.equals(name, entry.getKey())) {
                        List<String> values = entry.getValue();
                        return first(values);
                    }
                }
            }
            return null;
        }
    },
    ;

    /**
     * Get the value of the request by the specified name
     *
     * @param request {@link NativeWebRequest}
     * @param name    the name of request value
     * @return <code>null</code> if not found
     */
    @Nullable
    public abstract String getValue(NativeWebRequest request, String name);
}