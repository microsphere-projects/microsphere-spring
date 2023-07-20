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

import org.springframework.http.HttpRequest;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.WebUtils;

import java.net.URI;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.net.URLUtils.resolveQueryParameters;
import static java.util.Collections.emptySet;

/**
 * {@link HttpRequest} Parameter {@link NameValueExpression}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NameValueExpression
 * @since 1.0.0
 */
public class HttpRequestParamExpression extends AbstractNameValueExpression {

    private final Set<String> namesToMatch = new HashSet<>(WebUtils.SUBMIT_IMAGE_SUFFIXES.length + 1);

    public HttpRequestParamExpression(String expression) {
        super(expression);
        this.namesToMatch.add(getName());
        for (String suffix : WebUtils.SUBMIT_IMAGE_SUFFIXES) {
            this.namesToMatch.add(getName() + suffix);
        }
    }

    @Override
    protected boolean isCaseSensitiveName() {
        return true;
    }

    @Override
    protected Object parseValue(String valueExpression) {
        return valueExpression;
    }

    @Override
    protected boolean matchName(HttpRequest request) {
        Map<String, List<String>> parametersMap = getParametersMap(request);
        for (String current : this.namesToMatch) {
            if (parametersMap.containsKey(current)) {
                return true;
            }
        }
        return parametersMap.containsKey(this.name);
    }

    @Override
    protected boolean matchValue(HttpRequest request) {
        Map<String, List<String>> parametersMap = getParametersMap(request);
        List<String> parameterValues = parametersMap.get(this.name);
        return parameterValues != null && parameterValues.contains(this.value);
    }

    private Map<String, List<String>> getParametersMap(HttpRequest request) {
        URI requestURI = request.getURI();
        return resolveQueryParameters(requestURI.toString());
    }

    protected static Set<HttpRequestParamExpression> of(String... params) {
        if (ObjectUtils.isEmpty(params)) {
            return emptySet();
        }
        int length = params.length;
        Set<HttpRequestParamExpression> expressions = new LinkedHashSet<>(length);
        for (int i = 0; i < length; i++) {
            String param = params[i];
            expressions.add(new HttpRequestParamExpression(param));
        }
        return expressions;
    }
}
