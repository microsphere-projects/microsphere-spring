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

import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.spring.web.util.WebRequestUtils.addCookie;
import static io.microsphere.spring.web.util.WebRequestUtils.setHeader;

/**
 * The target of response.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResponseBody
 * @see org.springframework.http.ResponseCookie
 * @since 1.0.0
 */
public enum ResponseTarget {

    /**
     * The body of response
     */
    BODY,

    /*
     * The header of response
     */
    HEADER {
        @Override
        public void writeValue(NativeWebRequest request, String name, String value) {
            setHeader(request, name, value);
        }
    },

    /**
     * The cookie of response Value
     */
    COOKIE {
        @Override
        public void writeValue(NativeWebRequest request, String name, String value) {
            addCookie(request, name, value);
        }
    };

    public void writeValue(NativeWebRequest request, String name, String value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
