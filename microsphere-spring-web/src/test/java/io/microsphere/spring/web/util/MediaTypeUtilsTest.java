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


import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static io.microsphere.spring.web.util.MediaTypeUtils.SPECIFICITY_COMPARATOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.parseMediaType;

/**
 * {@link MediaTypeUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MediaTypeUtils
 * @since 1.0.0
 */
class MediaTypeUtilsTest {

    @Test
    void testSPECIFICITY_COMPARATOR() {
        MediaType audioType07 = parseMediaType("audio/*;q=0.7");
        MediaType audioType03 = parseMediaType("audio/*;q=0.3");
        MediaType audioType07A = parseMediaType("audio/*;q=0.7;a=b");
        assertEquals(-1, SPECIFICITY_COMPARATOR.compare(audioType07, audioType03));
        assertEquals(1, SPECIFICITY_COMPARATOR.compare(audioType07, audioType07A));
    }
}