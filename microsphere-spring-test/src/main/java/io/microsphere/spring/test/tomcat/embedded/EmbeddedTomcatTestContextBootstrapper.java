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

import org.springframework.test.context.ContextLoader;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.web.WebMergedContextConfiguration;
import org.springframework.test.context.web.WebTestContextBootstrapper;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;

/**
 * {@link WebTestContextBootstrapper} extension class for {@link EmbeddedTomcatConfiguration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EmbeddedTomcatConfiguration
 * @see EmbeddedTomcatMergedContextConfiguration
 * @since 1.0.0
 */
class EmbeddedTomcatTestContextBootstrapper extends WebTestContextBootstrapper {

    @Override
    protected Class<? extends ContextLoader> getDefaultContextLoaderClass(Class<?> testClass) {
        return EmbeddedTomcatContextLoader.class;
    }

    @Override
    protected MergedContextConfiguration processMergedContextConfiguration(MergedContextConfiguration mergedConfig) {
        EmbeddedTomcatConfiguration annotation = getEmbeddedTomcatConfiguration(mergedConfig.getTestClass());
        WebMergedContextConfiguration webMergedContextConfiguration = (WebMergedContextConfiguration) super.processMergedContextConfiguration(mergedConfig);
        return new EmbeddedTomcatMergedContextConfiguration(webMergedContextConfiguration,
                annotation.port(),
                annotation.contextPath(),
                annotation.basedir());
    }

    private static EmbeddedTomcatConfiguration getEmbeddedTomcatConfiguration(Class<?> testClass) {
        return findMergedAnnotation(testClass, EmbeddedTomcatConfiguration.class);
    }
}
