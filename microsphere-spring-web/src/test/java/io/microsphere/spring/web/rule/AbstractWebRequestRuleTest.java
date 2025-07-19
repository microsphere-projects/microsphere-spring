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

package io.microsphere.spring.web.rule;

import io.microsphere.collection.MapUtils;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.Map;

/**
 * Abstract {@link WebRequestRule} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestRule
 * @since 1.0.0
 */
public class AbstractWebRequestRuleTest {

    protected NativeWebRequest createWebRequest(Object... params) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Map<String, Object> parameters = MapUtils.of(params);
        request.setParameters(parameters);
        return new ServletWebRequest(request);
    }
}
