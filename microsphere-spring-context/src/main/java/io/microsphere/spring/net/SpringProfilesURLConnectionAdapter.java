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
package io.microsphere.spring.net;

import io.microsphere.io.FastByteArrayInputStream;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.microsphere.collection.ListUtils.first;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.Maps.ofMap;
import static io.microsphere.net.URLUtils.DEFAULT_ENCODING;
import static io.microsphere.net.URLUtils.resolveQueryParameters;
import static io.microsphere.util.StringUtils.isBlank;
import static io.microsphere.util.StringUtils.substringBefore;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;

/**
 * The {@link URLConnection} adapter class is based on the Spring {@link Profile Profiles}
 * <p>
 * The URL pattern : "spring:env:profiles://{type}"
 * {type} :
 * <ul>
 *     <li>"default" : {@link Environment#getDefaultProfiles()}</li>
 *     <li>"active" : {@link Environment#getActiveProfiles()}</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Environment
 * @see Environment#getActiveProfiles()
 * @see Environment#getDefaultProfiles()
 * @see Profile
 * @since 1.0.0
 */
class SpringProfilesURLConnectionAdapter extends URLConnection {

    public static final String DEFAULT_TYPE = "default";

    public static final String ACTIVE_TYPE = "active";

    /**
     * The encoding parameter name for the {@link URL} query string
     */
    public static final String ENCODING_PARAM_NAME = "encoding";

    private final Environment environment;

    private static final Function<Environment, String[]> DEFAULT_PROFILES_FUNCTION = Environment::getDefaultProfiles;

    private static final Function<Environment, String[]> FALLBACK_PROFILES_FUNCTION = DEFAULT_PROFILES_FUNCTION;

    private static final Map<String, Function<Environment, String[]>> profilesFunctionsMap = ofMap(
            DEFAULT_TYPE, DEFAULT_PROFILES_FUNCTION,
            ACTIVE_TYPE, Environment::getActiveProfiles
    );

    /**
     * Constructs a URL connection to the specified URL. A connection to
     * the object referenced by the URL is not created.
     *
     * @param url         the specified URL.
     * @param environment {@link Environment}
     */
    protected SpringProfilesURLConnectionAdapter(URL url, Environment environment) {
        super(url);
        this.environment = environment;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        URL url = getURL();
        String type = getType(url);
        String encoding = getEncoding(url);
        Function<Environment, String[]> profilesFunction = profilesFunctionsMap.getOrDefault(type, FALLBACK_PROFILES_FUNCTION);
        String[] profiles = profilesFunction.apply(environment);
        String profilesString = arrayToCommaDelimitedString(profiles);
        byte[] bytes = profilesString.getBytes(encoding);
        return new FastByteArrayInputStream(bytes);
    }

    @Override
    public void connect() {
    }

    static String getType(URL url) {
        String type = substringBefore(url.getHost(), ";");
        return isBlank(type) ? DEFAULT_TYPE : type;
    }

    static String getEncoding(URL url) {
        Map<String, List<String>> queryParameters = resolveQueryParameters(url.toString());
        List<String> encodings = queryParameters.getOrDefault(ENCODING_PARAM_NAME, ofList(DEFAULT_ENCODING));
        return first(encodings);
    }
}