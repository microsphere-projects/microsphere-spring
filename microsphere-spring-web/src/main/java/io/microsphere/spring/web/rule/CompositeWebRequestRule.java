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

import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Objects;

import static io.microsphere.collection.Lists.ofList;
import static java.util.Collections.emptyList;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Composite {@link WebRequestRule}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see org.springframework.web.servlet.mvc.condition.CompositeRequestCondition
 * @see org.springframework.web.reactive.result.condition.CompositeRequestCondition
 * @since 1.0.0
 */
public class CompositeWebRequestRule implements WebRequestRule {

    private final List<WebRequestRule> webRequestRules;

    public CompositeWebRequestRule(WebRequestRule... requestRules) {
        this.webRequestRules = isEmpty(requestRules) ? emptyList() : ofList(requestRules);
    }

    @Override
    public boolean matches(NativeWebRequest request) {
        for (WebRequestRule webRequestRule : webRequestRules) {
            if (!webRequestRule.matches(request)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompositeWebRequestRule that = (CompositeWebRequestRule) o;
        return Objects.equals(webRequestRules, that.webRequestRules);
    }

    @Override
    public int hashCode() {
        return webRequestRules.hashCode();
    }

    @Override
    public String toString() {
        return webRequestRules.toString();
    }
}
