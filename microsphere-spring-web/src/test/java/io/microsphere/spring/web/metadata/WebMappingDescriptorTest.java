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
package io.microsphere.spring.web.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.StreamUtils;


import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.util.StreamUtils.copyToByteArray;
import static org.springframework.util.StreamUtils.copyToString;

/**
 * {@link WebMappingDescriptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = WebMappingDescriptorTest.class)
public class WebMappingDescriptorTest {

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

    @Value("classpath:META-INF/web-mapping-descriptor.json")
    private Resource fullJsonResource;

    private String fullJson;

    @PostConstruct
    public void init() throws Throwable {
        this.fullJson = copyToString(this.fullJsonResource.getInputStream(), UTF_8);
    }

    @Test
    public void testToJSON() throws IOException {
        WebMappingDescriptor descriptor = WebMappingDescriptor.source(this)
                .patterns("/a", "/b", "/c")
                .build();
        assertEquals(P_JSON, descriptor.toJSON());

        descriptor = WebMappingDescriptor.source(this)
                .patterns("/a", "/b", "/c")
                .methods("GET", "POST")
                .build();
        assertEquals(PM_JSON, descriptor.toJSON());

        descriptor = WebMappingDescriptor.source(this)
                .patterns("/a", "/b", "/c")
                .methods("GET", "POST")
                .params("a=1", "b=2")
                .build();

        assertEquals(PMP_JSON, descriptor.toJSON());


        descriptor = WebMappingDescriptor.source(this)
                .patterns("/a", "/b", "/c")
                .methods("GET", "POST")
                .params("a=1", "b=2")
                .consumes("application/json", "application/xml")
                .produces("text/html", "text/xml")
                .build();

        assertEquals(fullJson, descriptor.toJSON());
    }
}
