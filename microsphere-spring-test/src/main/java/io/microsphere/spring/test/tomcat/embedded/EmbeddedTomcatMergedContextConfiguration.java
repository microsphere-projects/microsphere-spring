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

import io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration.Feature;
import org.springframework.core.style.DefaultToStringStyler;
import org.springframework.core.style.DefaultValueStyler;
import org.springframework.core.style.ToStringCreator;
import org.springframework.test.context.web.WebMergedContextConfiguration;

import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ArrayUtils.ofArray;

/**
 * The {@link WebMergedContextConfiguration} extension class for {@link EmbeddedTomcatConfiguration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EmbeddedTomcatConfiguration
 * @see WebMergedContextConfiguration
 * @see EmbeddedTomcatTestContextBootstrapper
 * @since 1.0.0
 */
class EmbeddedTomcatMergedContextConfiguration extends WebMergedContextConfiguration {

    private final int port;

    private final String contextPath;

    private final String basedir;

    private final String alternativeWebXml;

    private final Feature[] features;

    public EmbeddedTomcatMergedContextConfiguration(WebMergedContextConfiguration webMergedContextConfiguration,
                                                    int port, String contextPath, String basedir, String alternativeWebXml, Feature... features) {
        super(webMergedContextConfiguration, webMergedContextConfiguration.getResourceBasePath());
        this.port = port;
        this.contextPath = contextPath;
        this.basedir = basedir;
        this.alternativeWebXml = alternativeWebXml;
        this.features = features;
    }

    /**
     * Get the Tomcat port
     *
     * @return {@link EmbeddedTomcatConfiguration#port()}
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the Tomcat context path
     *
     * @return {@link EmbeddedTomcatConfiguration#contextPath()}
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Get the Tomcat basedir
     *
     * @return {@link EmbeddedTomcatConfiguration#basedir()}
     */
    public String getBasedir() {
        return basedir;
    }

    /**
     * Get the Tomcat web.xml
     *
     * @return {@link EmbeddedTomcatConfiguration#alternativeWebXml()}
     */
    public String getAlternativeWebXml() {
        return alternativeWebXml;
    }

    /**
     * Get the Tomcat features
     *
     * @return {@link EmbeddedTomcatConfiguration#features()}
     */
    public Feature[] getFeatures() {
        return features;
    }

    @Override
    public Class<?>[] getClasses() {
        Class<?>[] classes = super.getClasses();
        if (isEmpty(classes)) {
            return ofArray(getTestClass());
        }
        return classes;
    }

    @Override
    public String toString() {
        return new ToStringCreator(this, new DefaultToStringStyler(new DefaultValueStyler()))
                .append(super.toString())
                .append("port", getPort())
                .append("contextPath", getContextPath())
                .append("basedir", getBasedir())
                .append("alternativeWebXml", getAlternativeWebXml())
                .append("features", getFeatures())
                .toString();
    }
}