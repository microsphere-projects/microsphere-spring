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

import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.util.WebUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.springframework.util.ObjectUtils.containsElement;

/**
 * {@link NativeWebRequest WebRequest} Parameter {@link NameValueExpression}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NameValueExpression
 * @since 1.0.0
 */
public class WebRequestParamExpression extends AbstractNameValueExpression<String> {

    private final Set<String> namesToMatch = new HashSet<>(WebUtils.SUBMIT_IMAGE_SUFFIXES.length + 1);

    public WebRequestParamExpression(String expression) {
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
    protected String parseValue(String valueExpression) {
        return valueExpression;
    }

    @Override
    protected boolean matchName(NativeWebRequest request) {
        for (String current : this.namesToMatch) {
            if (request.getParameter(current) != null) {
                return true;
            }
        }
        return request.getParameter(this.name) != null;
    }

    @Override
    protected boolean matchValue(NativeWebRequest request) {
        String[] parameterValues = request.getParameterValues(this.name);
        return containsElement(parameterValues, this.value);
    }

    protected static List<WebRequestParamExpression> parseExpressions(String... params) {
        if (ObjectUtils.isEmpty(params)) {
            return emptyList();
        }
        int length = params.length;
        List<WebRequestParamExpression> expressions = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            String param = params[i];
            expressions.add(new WebRequestParamExpression(param));
        }
        return expressions;
    }
}
