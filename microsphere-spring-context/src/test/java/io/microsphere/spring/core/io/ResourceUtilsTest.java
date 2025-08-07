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
package io.microsphere.spring.core.io;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.IOException;
import java.net.URL;
import java.util.function.Function;

import static io.microsphere.spring.core.io.ResourceLoaderUtils.getResourceLoader;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ResourceUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ResourceUtils
 * @since 1.0.0
 */
class ResourceUtilsTest {

    private static final ResourceLoader resourceLoader = getResourceLoader();

    private Resource classPathResource;

    private Resource urlResource;

    @BeforeEach
    public void setUp() throws IOException {
        this.classPathResource = resourceLoader.getResource("classpath:/META-INF/spring.factories");
        URL url = this.classPathResource.getURL();
        this.urlResource = resourceLoader.getResource(url.toString());
    }

    @Test
    public void testIsFileUrlResource() {
        assertResource(this.classPathResource, ResourceUtils::isFileUrlResource, false);
        assertResource(this.urlResource, ResourceUtils::isFileUrlResource, true);
    }

    @Test
    public void testIsFileBasedResource() throws IOException {
        assertResource(this.classPathResource, ResourceUtils::isFileBasedResource, false);
        assertResource(this.urlResource, ResourceUtils::isFileBasedResource, true);
        assertResource(new FileSystemResource(this.urlResource.getFile()), ResourceUtils::isFileBasedResource, true);
    }

    private void assertResource(Resource resource, Function<Resource, Boolean> isFunction, boolean expected) {
        assertEquals(expected, isFunction.apply(resource));
    }
}
