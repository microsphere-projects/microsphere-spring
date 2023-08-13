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
import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.constants.SymbolConstants.EQUAL_CHAR;
import static io.microsphere.constants.SymbolConstants.EXCLAMATION;
import static io.microsphere.constants.SymbolConstants.EXCLAMATION_CHAR;

/**
 * Supports "name=value" style expressions as described in:
 * {@link org.springframework.web.bind.annotation.RequestMapping#params()} and
 * {@link org.springframework.web.bind.annotation.RequestMapping#headers()}.
 *
 * @param <T> the value type
 * @author Rossen Stoyanchev
 * @author Arjen Poutsma
 * @since 3.1
 */
public abstract class AbstractNameValueExpression<T> implements NameValueExpression<T> {

    protected final String name;

    @Nullable
    protected final T value;

    protected final boolean isNegated;

    protected AbstractNameValueExpression(String expression) {
        int separator = expression.indexOf(EQUAL_CHAR);
        if (separator == -1) {
            this.isNegated = expression.startsWith(EXCLAMATION);
            this.name = (this.isNegated ? expression.substring(1) : expression);
            this.value = null;
        } else {
            this.isNegated = (separator > 0) && (expression.charAt(separator - 1) == EXCLAMATION_CHAR);
            this.name = (this.isNegated ? expression.substring(0, separator - 1) : expression.substring(0, separator));
            this.value = parseValue(expression.substring(separator + 1));
        }
    }


    @Override
    public String getName() {
        return this.name;
    }

    @Override
    @Nullable
    public T getValue() {
        return this.value;
    }

    @Override
    public boolean isNegated() {
        return this.isNegated;
    }

    public final boolean match(NativeWebRequest request) {
        boolean isMatch;
        if (this.value != null) {
            isMatch = matchValue(request);
        } else {
            isMatch = matchName(request);
        }
        return this.isNegated != isMatch;
    }

    protected abstract boolean isCaseSensitiveName();

    protected abstract T parseValue(String valueExpression);

    protected abstract boolean matchName(NativeWebRequest request);

    protected abstract boolean matchValue(NativeWebRequest request);


    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        AbstractNameValueExpression<?> that = (AbstractNameValueExpression<?>) other;
        return ((isCaseSensitiveName() ? this.name.equals(that.name) : this.name.equalsIgnoreCase(that.name)) &&
                ObjectUtils.nullSafeEquals(this.value, that.value) && this.isNegated == that.isNegated);
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
        StringBuilder builder = new StringBuilder();
        if (this.value != null) {
            builder.append(this.name);
            if (this.isNegated) {
                builder.append(EXCLAMATION_CHAR);
            }
            builder.append('=');
            builder.append(this.value);
        } else {
            if (this.isNegated) {
                builder.append(EXCLAMATION_CHAR);
            }
            builder.append(this.name);
        }
        return builder.toString();
    }

}
