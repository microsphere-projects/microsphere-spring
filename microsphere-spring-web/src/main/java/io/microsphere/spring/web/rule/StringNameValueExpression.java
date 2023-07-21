/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.microsphere.spring.web.rule;

import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

/**
 * String {@link NameValueExpression}
 *
 * @author Rossen Stoyanchev
 * @author Arjen Poutsma
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class StringNameValueExpression implements NameValueExpression<String> {

    protected final String expression;

    protected final boolean caseSensitiveName;

    protected final String name;

    @Nullable
    protected final String value;

    protected final boolean isNegated;

    public StringNameValueExpression(String expression) {
        this(expression, true);
    }

    public StringNameValueExpression(String expression, boolean caseSensitiveName) {
        this.expression = expression;
        this.caseSensitiveName = caseSensitiveName;
        int separator = expression.indexOf('=');
        if (separator == -1) {
            this.isNegated = expression.startsWith("!");
            this.name = (this.isNegated ? expression.substring(1) : expression);
            this.value = null;
        } else {
            this.isNegated = (separator > 0) && (expression.charAt(separator - 1) == '!');
            this.name = (this.isNegated ? expression.substring(0, separator - 1) : expression.substring(0, separator));
            this.value = expression.substring(separator + 1);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    @Nullable
    public String getValue() {
        return this.value;
    }

    @Override
    public boolean isNegated() {
        return this.isNegated;
    }

    protected boolean isCaseSensitiveName() {
        return caseSensitiveName;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || !NameValueExpression.class.isAssignableFrom(other.getClass())) {
            return false;
        }
        NameValueExpression that = (NameValueExpression) other;
        return ((isCaseSensitiveName() ? this.name.equals(that.getName()) : this.name.equalsIgnoreCase(that.getName())) &&
                ObjectUtils.nullSafeEquals(this.value, that.getValue()) && this.isNegated == that.isNegated());
    }

    @Override
    public int hashCode() {
        int result = (isCaseSensitiveName() ? this.name.hashCode() : this.name.toLowerCase().hashCode());
        result = 31 * result + (this.value != null ? this.value.hashCode() : 0);
        result = 31 * result + (this.isNegated ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return expression;
    }

    public static StringNameValueExpression of(String expression) {
        return new StringNameValueExpression(expression);
    }
}
