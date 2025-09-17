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
package io.microsphere.spring.webmvc.util;

import io.microsphere.annotation.Nullable;
import io.microsphere.util.Utils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.servlet.View;

import java.util.Map;

import static io.microsphere.spring.web.util.WebScope.REQUEST;
import static org.springframework.web.servlet.View.PATH_VARIABLES;
import static org.springframework.web.servlet.View.RESPONSE_STATUS_ATTRIBUTE;
import static org.springframework.web.servlet.View.SELECTED_CONTENT_TYPE;

/**
 * {@link View} Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see View
 * @since 1.0.0
 */
public abstract class ViewUtils implements Utils {

    /**
     * Get the {@link HttpStatus} for the {@link View}
     *
     * @param requestAttributes the {@link RequestAttributes}
     * @return the {@link HttpStatus} if set, otherwise {@code null}
     * @see View#RESPONSE_STATUS_ATTRIBUTE
     */
    @Nullable
    public static HttpStatus getResponseStatus(RequestAttributes requestAttributes) {
        return REQUEST.getAttribute(requestAttributes, RESPONSE_STATUS_ATTRIBUTE);
    }

    /**
     * Get a Map with path variables. The map consists of String-based URI template variable names as keys and their
     * corresponding Object-based values -- extracted from segments of the URL and type converted.
     *
     * @param requestAttributes the {@link RequestAttributes}
     * @return The map consists of String-based URI template variable names as keys and their
     * corresponding Object-based values -- extracted from segments of the URL and type converted.
     * @see View#PATH_VARIABLES
     */
    @Nullable
    public static Map<String, Object> getPathVariables(RequestAttributes requestAttributes) {
        return REQUEST.getAttribute(requestAttributes, PATH_VARIABLES);
    }

    /**
     * Get the {@link MediaType} selected during content negotiation, which may be more specific than the one the View
     * is configured with. For example: "application/vnd.example-v1+xml" vs "application/*+xml".
     *
     * @param requestAttributes the {@link RequestAttributes}
     * @return the {@link MediaType} if selected, otherwise {@code null}
     * @see View#SELECTED_CONTENT_TYPE
     */
    @Nullable
    public static MediaType getSelectedContentType(RequestAttributes requestAttributes) {
        return REQUEST.getAttribute(requestAttributes, SELECTED_CONTENT_TYPE);
    }

    private ViewUtils() {
    }
}