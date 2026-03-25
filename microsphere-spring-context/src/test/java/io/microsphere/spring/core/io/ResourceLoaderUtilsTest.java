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

import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import static io.microsphere.spring.core.io.ResourceLoaderUtils.clearResourceLoadersCache;
import static io.microsphere.spring.core.io.ResourceLoaderUtils.getResourceLoader;
import static io.microsphere.spring.core.io.ResourceLoaderUtils.getResourcePatternResolver;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static java.lang.Thread.currentThread;
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

    @Before
    public void setUp() {
        clearResourceLoadersCache();
    }

    @Test
    public void testGetResourceLoader() {
        assertNotNull(getResourceLoader());
    }

    @Test
    public void testGetResourceLoaderWithDefaultClassLoader() {
        assertSame(getResourceLoader(), getResourceLoader(getDefaultClassLoader()));
    }

    @Test
    public void testGetResourceLoaderWithContextClassLoader() {
        assertSame(getResourceLoader(), getResourceLoader(currentThread().getContextClassLoader()));
    }

    @Test
    public void testGetResourceLoaderWithNullClassLoader() {
        assertSame(getResourceLoader(), getResourceLoader(null));
    }

    @Test
    public void testGetResourcePatternResolver() {
        assertNotNull(getResourcePatternResolver());
        assertNotNull(getResourcePatternResolver());
    }

    @Test
    public void testGetResourcePatternResolverWithResourceLoader() {
        assertSame(getResourcePatternResolver(new PathMatchingResourcePatternResolver()), getResourcePatternResolver());
    }

    @Test
    public void testGetResourcePatternResolverWithNullResourceLoader() {
        assertNotNull(getResourcePatternResolver(null));
        assertSame(getResourcePatternResolver(), getResourcePatternResolver(null));
    }
}
