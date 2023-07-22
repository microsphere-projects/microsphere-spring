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
package io.microsphere.spring.web.bind.annotation;

import org.junit.jupiter.api.Test;

import static io.microsphere.util.ArrayUtils.of;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link RawRequestMappingMetadata} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class RawRequestMappingMetadataTest {

    private static final String P_JSON = "{\n" +
            "\"patterns\":[\"/a\",\"/b\",\"/c\"]\n" +
            "}";

    private static final String PM_JSON = "{\n" +
            "\"patterns\":[\"/a\",\"/b\",\"/c\"],\n" +
            "\"methods\":[\"GET\",\"POST\"]\n" +
            "}";

    private static final String PMP_JSON = "{\n" +
            "\"patterns\":[\"/a\",\"/b\",\"/c\"],\n" +
            "\"methods\":[\"GET\",\"POST\"],\n" +
            "\"params\":[\"a=1\",\"b=2\"]\n" +
            "}";

    @Test
    public void testToJSON() {
        RawRequestMappingMetadata metadata = new RawRequestMappingMetadata("/a", "/b", "/c");
        assertEquals(P_JSON, metadata.toJSON());

        metadata = new RawRequestMappingMetadata(of("/a", "/b", "/c"), "GET", "POST");
        assertEquals(PM_JSON, metadata.toJSON());

        metadata = new RawRequestMappingMetadata(of("/a", "/b", "/c"), of("GET", "POST"), "a=1", "b=2");
        assertEquals(PMP_JSON, metadata.toJSON());
    }
}
