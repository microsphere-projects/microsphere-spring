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
package io.microsphere.spring.util;

import io.microsphere.annotation.Nullable;
import io.microsphere.util.Utils;
import org.springframework.util.MimeType;

import java.util.Collection;
import java.util.Comparator;

import static io.microsphere.collection.CollectionUtils.isEmpty;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static io.microsphere.util.StringUtils.substringAfter;

/**
 * The utility class for MIME Type
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MimeType
 * @see org.springframework.util.MimeTypeUtils
 * @since 1.0.0
 */
public abstract class MimeTypeUtils implements Utils {

    /**
     * Public constant mime type for {@code application/graphql+json}.
     *
     * @see <a href="https://github.com/graphql/graphql-over-http">GraphQL over HTTP spec</a>
     * @since Spring Framework 5.3.19
     */
    public static final MimeType APPLICATION_GRAPHQL = new MimeType("application", "graphql+json");

    /**
     * A String equivalent of {@link org.springframework.util.MimeTypeUtils#APPLICATION_GRAPHQL}.
     *
     * @since Spring Framework 5.3.19
     */
    public static final String APPLICATION_GRAPHQL_VALUE = "application/graphql+json";

    /**
     * Unlike {@link Collection#contains(Object)} which relies on
     * {@link MimeType#equals(Object)}, this method only checks the type and the
     * subtype, but otherwise ignores parameters.
     *
     * @param targetMimeType the mime type to check
     * @param mimeTypes      the list of mime types to perform the check against
     * @return whether the list contains the given mime type
     * @since Spring Framework 5.1.4
     */
    public static boolean isPresentIn(MimeType targetMimeType, Collection<? extends MimeType> mimeTypes) {
        if (isEmpty(mimeTypes)) {
            return false;
        }
        for (MimeType mimeType : mimeTypes) {
            if (equalsTypeAndSubtype(mimeType, targetMimeType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Similar to {@link MimeType#equals(Object)} but based on the type and subtype
     * only, i.e. ignoring parameters.
     *
     * @param one   the mime type to compare
     * @param other the other mime type to compare to
     * @return whether the two mime types have the same type and subtype
     * @since Spring Framework 5.1.4
     */
    public static boolean equalsTypeAndSubtype(MimeType one, MimeType other) {
        if (one == other) {
            return true;
        }
        if (one == null || other == null) {
            return false;
        }
        return one.getType().equalsIgnoreCase(other.getType()) && one.getSubtype().equalsIgnoreCase(other.getSubtype());
    }

    /**
     * Return the subtype suffix as defined in RFC 6839.
     *
     * @since Spring Framework 5.3
     */
    @Nullable
    public static String getSubtypeSuffix(MimeType one) {
        if (one == null) {
            return null;
        }
        String subtype = one.getSubtype();
        String suffix = substringAfter(subtype, "+");
        return suffix == EMPTY_STRING ? null : suffix;
    }

    /**
     * A {@link Comparator} for {@link MimeType} that orders by specificity.
     */
    public static class SpecificityComparator<T extends MimeType> implements Comparator<T> {

        @Override
        public int compare(T mimeType1, T mimeType2) {
            if (mimeType1.isWildcardType() && !mimeType2.isWildcardType()) {  // */* < audio/*
                return 1;
            } else if (mimeType2.isWildcardType() && !mimeType1.isWildcardType()) {  // audio/* > */*
                return -1;
            } else if (!mimeType1.getType().equals(mimeType2.getType())) {  // audio/basic == text/html
                return 0;
            } else {  // mediaType1.getType().equals(mediaType2.getType())
                if (mimeType1.isWildcardSubtype() && !mimeType2.isWildcardSubtype()) {  // audio/* < audio/basic
                    return 1;
                } else if (mimeType2.isWildcardSubtype() && !mimeType1.isWildcardSubtype()) {  // audio/basic > audio/*
                    return -1;
                } else if (!mimeType1.getSubtype().equals(mimeType2.getSubtype())) {  // audio/basic == audio/wave
                    return 0;
                } else {  // mediaType2.getSubtype().equals(mediaType2.getSubtype())
                    return compareParameters(mimeType1, mimeType2);
                }
            }
        }

        protected int compareParameters(T mimeType1, T mimeType2) {
            int paramsSize1 = mimeType1.getParameters().size();
            int paramsSize2 = mimeType2.getParameters().size();
            return Integer.compare(paramsSize2, paramsSize1);  // audio/basic;level=1 < audio/basic
        }
    }

    private MimeTypeUtils() {
    }
}
