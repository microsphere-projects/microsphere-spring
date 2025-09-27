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
import org.springframework.test.context.web.WebMergedContextConfiguration;

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

    private final Feature[] features;

    public EmbeddedTomcatMergedContextConfiguration(WebMergedContextConfiguration webMergedContextConfiguration,
                                                    int port, String contextPath, String basedir, Feature... features) {
        super(webMergedContextConfiguration, webMergedContextConfiguration.getResourceBasePath());
        this.port = port;
        this.contextPath = contextPath;
        this.basedir = basedir;
        this.features = features;
    }

    /**
     * Get the Tomcat port
     *
     * @return Tomcat port
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the Tomcat context path
     *
     * @return Tomcat context path
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Get the Tomcat basedir
     *
     * @return Tomcat basedir
     */
    public String getBasedir() {
        return basedir;
    }

    /**
     * Get the Tomcat features
     *
     * @return Tomcat features
     */
    public Feature[] getFeatures() {
        return features;
    }
}