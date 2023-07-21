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

import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.StringJoiner;

/**
 * Abstract {@link WebRequestRule}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class AbstractWebRequestRule<T> implements WebRequestRule {

    /**
     * Indicates whether this condition is empty, i.e. whether it
     * contains any discrete items.
     * @return {@code true} if empty; {@code false} otherwise
     */
    public boolean isEmpty() {
        return getContent().isEmpty();
    }

    /**
     * Return the discrete items a request condition is composed of.
     * <p>For example URL patterns, HTTP request methods, param expressions, etc.
     * @return a collection of objects (never {@code null})
     */
    protected abstract Collection<T> getContent();

    /**
     * The notation to use when printing discrete items of content.
     * <p>For example {@code " || "} for URL patterns or {@code " && "}
     * for param expressions.
     */
    protected abstract String getToStringInfix();

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        return getContent().equals(((AbstractWebRequestRule<?>) other).getContent());
    }

    @Override
    public int hashCode() {
        return getContent().hashCode();
    }

    @Override
    public String toString() {
        String infix = getToStringInfix();
        StringJoiner joiner = new StringJoiner(infix, "[", "]");
        for (Object expression : getContent()) {
            joiner.add(expression.toString());
        }
        return joiner.toString();
    }
}
