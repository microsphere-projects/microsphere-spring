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

import io.microsphere.util.ClassLoaderUtils;
import org.junit.Test;

import static io.microsphere.spring.util.ResourceLoaderUtils.getResourceLoader;
import static io.microsphere.spring.util.ResourceLoaderUtils.getResourcePatternResolver;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

/**
 * {@link ResourceLoaderUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see 1.0.0
 * @since 1.0.0
 */
public class ResourceLoaderUtilsTest {

    @Test
    public void testGetResourceLoader() {
        assertNotNull(getResourceLoader());
        assertSame(getResourceLoader(), getResourceLoader(null));
        assertSame(getResourceLoader(), getResourceLoader(ClassLoaderUtils.getDefaultClassLoader()));
        assertSame(getResourceLoader(), getResourceLoader(Thread.currentThread().getContextClassLoader()));
    }

    @Test
    public void testGetResourcePatternResolver() {
        assertNotNull(getResourcePatternResolver());
        assertNotNull(getResourcePatternResolver(null));
        assertSame(getResourcePatternResolver(), getResourcePatternResolver(null));
        assertSame(getResourcePatternResolver(), getResourcePatternResolver(getResourcePatternResolver()));
    }
}
