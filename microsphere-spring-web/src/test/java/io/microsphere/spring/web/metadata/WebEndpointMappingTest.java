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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.CUSTOMIZED;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;
import static org.springframework.util.StreamUtils.copyToString;

/**
 * {@link WebEndpointMapping} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = WebEndpointMappingTest.class)
public class WebEndpointMappingTest {

    @Value("classpath:META-INF/web-mapping-descriptor.json")
    private Resource fullJsonResource;

    private String fullJson;

    @Before
    public void init() throws Throwable {
        this.fullJson = copyToString(this.fullJsonResource.getInputStream(), UTF_8);
    }

    @Test
    public void testToJSON() throws IOException {
        WebEndpointMapping descriptor = WebEndpointMapping
                .of(CUSTOMIZED, 1, "/a", "/b", "/c")
                .methods("GET", "POST")
                .params("a=1", "b=2")
                .headers("c=3", "d!=4")
                .consumes("application/json", "application/xml")
                .produces("text/html", "text/xml")
                .build();

        assertEquals(fullJson, descriptor.toJSON());
    }
}
