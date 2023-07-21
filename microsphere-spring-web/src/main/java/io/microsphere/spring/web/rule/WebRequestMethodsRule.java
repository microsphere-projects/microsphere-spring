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

import org.springframework.http.HttpMethod;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static io.microsphere.spring.web.util.WebRequestUtils.getMethod;
import static io.microsphere.spring.web.util.WebRequestUtils.isPreFlightRequest;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

/**
 * {@link NativeWebRequest WebRequest} {@link HttpMethod Methods} {@link WebRequestRule}
 * <p>
 * A logical disjunction (' || ') request condition that matches a request
 * against a set of {@link RequestMethod RequestMethods}.
 *
 * @author Rossen Stoyanchev
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition
 * @see org.springframework.web.reactive.result.condition.RequestMethodsRequestCondition
 * @since 1.0.0
 */
public class WebRequestMethodsRule extends AbstractWebRequestRule<RequestMethod> {

    private static final String ERROR_REQUEST_URI = "javax.servlet.error.request_uri";

    private final Set<RequestMethod> methods;

    public WebRequestMethodsRule(RequestMethod... requestMethods) {
        this.methods = (ObjectUtils.isEmpty(requestMethods) ?
                Collections.emptySet() : new LinkedHashSet<>(Arrays.asList(requestMethods)));
    }

    @Override
    protected Collection<RequestMethod> getContent() {
        return methods;
    }

    @Override
    protected String getToStringInfix() {
        return " || ";
    }

    @Override
    public boolean matches(NativeWebRequest request) {
        if (isPreFlightRequest(request)) {
            return false;
        }

        String method = getMethod(request);

        if (isEmpty()) {
            if (RequestMethod.OPTIONS.name().equals(method) && request.getAttribute(ERROR_REQUEST_URI, SCOPE_REQUEST) == null) {
                return false;
            }
            return true;
        }

        return matchRequestMethod(method);
    }

    private boolean matchRequestMethod(String method) {
        RequestMethod requestMethod;
        Collection<RequestMethod> methods = this.methods;
        try {
            requestMethod = RequestMethod.valueOf(method);
            if (methods.contains(requestMethod)) {
                return true;
            }
        } catch (IllegalArgumentException ex) {
            // Custom request method
        }
        return false;
    }
}
