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
package io.github.microsphere.spring.core.annotation;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.github.microsphere.spring.util.AnnotationUtils.getAnnotationAttributes;

/**
 * Generic {@link AnnotationAttributes}
 *
 * @param <A> The type of {@link Annotation}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotationAttributes
 * @since 1.0.0
 */
public class GenericAnnotationAttributes<A extends Annotation> extends AnnotationAttributes {

    public GenericAnnotationAttributes(A annotation) {
        this(getAnnotationAttributes(annotation, false));
    }

    public GenericAnnotationAttributes(AnnotationAttributes another) {
        super(another);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AnnotationAttributes)) {
            return false;
        }

        AnnotationAttributes that = (AnnotationAttributes) o;

        if (this.size() == that.size()) {
            for (Map.Entry<String, Object> entry : this.entrySet()) {
                String attributeName = entry.getKey();
                Object attributeValue = entry.getValue();
                Object thatAttributeValue = that.get(attributeName);
                if (!Objects.deepEquals(attributeValue, thatAttributeValue)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (Map.Entry<String, Object> entry : this.entrySet()) {
            String attributeName = entry.getKey();
            h += 31 * attributeName.hashCode();
            Object attributeValue = entry.getValue();
            if (attributeValue != null) {
                Class<?> attributeValueType = attributeValue.getClass();
                if (attributeValueType.isArray()) {
                    h += 31 * Arrays.deepHashCode((Object[]) attributeValue);
                } else {
                    h += 31 * attributeValue.hashCode();
                }

            }
        }
        return h;
    }
}
