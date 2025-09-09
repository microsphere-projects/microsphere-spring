/*
 * Copyright 2002-2020 the original author or authors.
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

import io.microsphere.annotation.Nullable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

import static org.springframework.http.MediaType.SPECIFICITY_COMPARATOR;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.util.StringUtils.hasText;

/**
 * A {@link MediaTypeExpression} implementation that supports matching
 * against a specific media type, with optional negation.
 *
 * <p>This class is used to represent media type expressions as described
 * in Spring's {@link RequestMapping} annotations, such as those used in
 * {@link RequestMapping#consumes()} and {@link RequestMapping#produces()}.
 *
 * <p>Examples of media type expressions include:
 * <ul>
 *     <li>{@code "application/json"} - Matches JSON content.</li>
 *     <li>{@code "!text/plain"} - Matches anything <em>except</em> plain text.</li>
 *     <li>{@code "application/*+xml"} - Matches XML-based content types such as
 *         {@code "application/xml"} or {@code "application/atom+xml"}.</li>
 * </ul>
 *
 * <p>Instances of this class are immutable and can be safely shared.
 *
 * @author Arjen Poutsma
 * @author Rossen Stoyanchev
 * @since 3.1
 */
public class GenericMediaTypeExpression implements MediaTypeExpression, Comparable<GenericMediaTypeExpression> {

    private final MediaType mediaType;

    private final boolean isNegated;

    public GenericMediaTypeExpression(String expression) {
        if (expression.startsWith("!")) {
            this.isNegated = true;
            expression = expression.substring(1);
        } else {
            this.isNegated = false;
        }
        this.mediaType = parseMediaType(expression);
    }

    GenericMediaTypeExpression(MediaType mediaType, boolean negated) {
        this.mediaType = mediaType;
        this.isNegated = negated;
    }

    @Override
    public MediaType getMediaType() {
        return this.mediaType;
    }

    @Override
    public boolean isNegated() {
        return this.isNegated;
    }

    protected boolean matchParameters(MediaType mediaType) {
        for (Map.Entry<String, String> entry : getMediaType().getParameters().entrySet()) {
            if (hasText(entry.getValue())) {
                String value = mediaType.getParameter(entry.getKey());
                if (hasText(value) && !entry.getValue().equalsIgnoreCase(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int compareTo(GenericMediaTypeExpression other) {
        return SPECIFICITY_COMPARATOR.compare(this.getMediaType(), other.getMediaType());
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        GenericMediaTypeExpression otherExpr = (GenericMediaTypeExpression) other;
        return (this.mediaType.equals(otherExpr.mediaType) && this.isNegated == otherExpr.isNegated);
    }

    @Override
    public int hashCode() {
        return this.mediaType.hashCode();
    }

    @Override
    public String toString() {
        if (this.isNegated) {
            return '!' + this.mediaType.toString();
        }
        return this.mediaType.toString();
    }

    public static GenericMediaTypeExpression of(String expression) {
        return new GenericMediaTypeExpression(expression);
    }
}
