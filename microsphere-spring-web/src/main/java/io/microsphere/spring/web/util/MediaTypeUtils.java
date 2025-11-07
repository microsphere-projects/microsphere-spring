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

package io.microsphere.spring.web.util;

import io.microsphere.spring.util.MimeTypeUtils.SpecificityComparator;
import org.springframework.http.MediaType;

import java.util.Comparator;

/**
 * The utility class for {@link MediaType}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MediaType
 * @since 1.0.0
 */
public abstract class MediaTypeUtils {

    /**
     * The {@link Comparator} for {@link MediaType}
     */
    public static final Comparator<MediaType> SPECIFICITY_COMPARATOR = new SpecificityComparator<>() {

        @Override
        protected int compareParameters(MediaType mediaType1, MediaType mediaType2) {
            double quality1 = mediaType1.getQualityValue();
            double quality2 = mediaType2.getQualityValue();
            int qualityComparison = Double.compare(quality2, quality1);
            if (qualityComparison != 0) {
                return qualityComparison;  // audio/*;q=0.7 < audio/*;q=0.3
            }
            return super.compareParameters(mediaType1, mediaType2);
        }
    };
}
