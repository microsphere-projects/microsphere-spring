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

package io.microsphere.spring.test.tomcat.embedded;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.web.WebMergedContextConfiguration;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import java.io.IOException;

import static io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatContextLoader.setParent;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link EmbeddedTomcatContextLoader} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see {@link EmbeddedTomcatContextLoader}
 * @since 1.0.0
 */
class EmbeddedTomcatContextLoaderTest {

    private EmbeddedTomcatContextLoader loader;

    @BeforeEach
    void setUp() {
        this.loader = new EmbeddedTomcatContextLoader();
    }

    @Test
    void testSetParent() {
        GenericApplicationContext context = new GenericApplicationContext();
        setParent(context, null);

        ConfigurableWebApplicationContext webApplicationContext = new GenericWebApplicationContext();
        setParent(context, webApplicationContext);

        setParent(context, null);
    }

    @Test
    void testDeployContextOnDocBaseMissing() {
        testDeployContextOnFailed(null);
        testDeployContextOnFailed("");
    }

    @Test
    void testDeployContextOnDocBaseNotFound() {
        testDeployContextOnFailed("not-found-docBase");
    }

    void testDeployContextOnFailed(String resourceBasePath) {
        MergedContextConfiguration mergedConfig = new MergedContextConfiguration(this.getClass(),
                EMPTY_STRING_ARRAY,
                EMPTY_CLASS_ARRAY,
                EMPTY_STRING_ARRAY,
                loader
        );
        WebMergedContextConfiguration webMergedContextConfiguration = new WebMergedContextConfiguration(mergedConfig, resourceBasePath);
        EmbeddedTomcatMergedContextConfiguration config = new EmbeddedTomcatMergedContextConfiguration(
                webMergedContextConfiguration, 0, "", "", "");
        GenericApplicationContext context = new GenericApplicationContext();
        assertThrows(IOException.class, () -> this.loader.deployContext(context, config));
    }

}