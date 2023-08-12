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
package io.microsphere.spring.web.rule;

import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.util.UrlPathHelper;
import org.springframework.web.util.pattern.PathPattern;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static io.microsphere.spring.web.util.WebRequestUtils.getResolvedLookupPath;
import static io.microsphere.spring.web.util.WebRequestUtils.isPreFlightRequest;

/**
 * {@link NativeWebRequest WebRequest} Patterns {@link WebRequestRule}
 * <p>
 * A logical disjunction (' || ') request condition that matches a request
 * against a set of URL path patterns.
 *
 * <p>In contrast to {@link PathPatternsRequestCondition} which uses parsed
 * {@link PathPattern}s, this condition does String pattern matching via
 * {@link org.springframework.util.AntPathMatcher AntPathMatcher}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see org.springframework.web.servlet.mvc.condition.PatternsRequestCondition
 * @see org.springframework.web.reactive.result.condition.PatternsRequestCondition
 * @since 1.0.0
 */
public class WebRequestPattensRule extends AbstractWebRequestRule<String> {

    private final static Set<String> EMPTY_PATH_PATTERN = Collections.singleton("");

    private final Set<String> patterns;

    private final PathMatcher pathMatcher;

    private final boolean useSuffixPatternMatch;

    private final boolean useTrailingSlashMatch;

    private final List<String> fileExtensions = new ArrayList<>();

    /**
     * Constructor with URL patterns which are prepended with "/" if necessary.
     *
     * @param patterns 0 or more URL patterns; no patterns results in an empty
     *                 path {@code ""} mapping which matches all requests.
     */
    public WebRequestPattensRule(String... patterns) {
        this(patterns, true, null);
    }

    /**
     * Variant of {@link #WebRequestPattensRule(String...)} with a
     * {@link PathMatcher} and flag for matching trailing slashes.
     */
    public WebRequestPattensRule(String[] patterns, boolean useTrailingSlashMatch,
                                 @Nullable PathMatcher pathMatcher) {

        this(patterns, null, pathMatcher, useTrailingSlashMatch);
    }

    /**
     * Variant of {@link #WebRequestPattensRule(String...)} with a
     * {@link UrlPathHelper} and a {@link PathMatcher}, and whether to match
     * trailing slashes.
     * <p>As of 5.3 the path is obtained through the static method
     * {@link UrlPathHelper#getResolvedLookupPath} and a {@code UrlPathHelper}
     * does not need to be passed in.
     * {@link #WebRequestPattensRule(String[], boolean, PathMatcher)}.
     */
    @Deprecated
    public WebRequestPattensRule(String[] patterns, @Nullable UrlPathHelper urlPathHelper,
                                 @Nullable PathMatcher pathMatcher, boolean useTrailingSlashMatch) {
        this(patterns, urlPathHelper, pathMatcher, false, useTrailingSlashMatch);
    }

    /**
     * Variant of {@link #PatternsRequestCondition(String...)} with a
     * {@link UrlPathHelper} and a {@link PathMatcher}, and flags for matching
     * with suffixes and trailing slashes.
     * <p>As of 5.3 the path is obtained through the static method
     * {@link UrlPathHelper#getResolvedLookupPath} and a {@code UrlPathHelper}
     * does not need to be passed in.
     */
    public WebRequestPattensRule(String[] patterns, @Nullable UrlPathHelper urlPathHelper,
                                 @Nullable PathMatcher pathMatcher, boolean useSuffixPatternMatch, boolean useTrailingSlashMatch) {
        this(patterns, urlPathHelper, pathMatcher, useSuffixPatternMatch, useTrailingSlashMatch, null);
    }

    /**
     * Variant of {@link #WebRequestPattensRule(String...)} with a
     * {@link UrlPathHelper} and a {@link PathMatcher}, and flags for matching
     * with suffixes and trailing slashes, along with specific extensions.
     *
     * @param patterns
     * @param urlPathHelper
     * @param pathMatcher
     * @param useSuffixPatternMatch
     * @param useTrailingSlashMatch
     * @param fileExtensions
     */
    public WebRequestPattensRule(String[] patterns, @Nullable UrlPathHelper urlPathHelper,
                                 @Nullable PathMatcher pathMatcher, boolean useSuffixPatternMatch,
                                 boolean useTrailingSlashMatch, @Nullable List<String> fileExtensions) {
        this.patterns = initPatterns(patterns);
        this.pathMatcher = pathMatcher != null ? pathMatcher : new AntPathMatcher();
        this.useSuffixPatternMatch = useSuffixPatternMatch;
        this.useTrailingSlashMatch = useTrailingSlashMatch;

        if (fileExtensions != null) {
            for (String fileExtension : fileExtensions) {
                if (fileExtension.charAt(0) != '.') {
                    fileExtension = "." + fileExtension;
                }
                this.fileExtensions.add(fileExtension);
            }
        }
    }

    @Override
    protected Collection<String> getContent() {
        return patterns;
    }

    @Override
    protected String getToStringInfix() {
        return " || ";
    }

    @Override
    public boolean matches(NativeWebRequest request) {
        if (isPreFlightRequest(request)) {
            return false;
        }
        // FIXME : WebFlux
        String lookupPath = getResolvedLookupPath(request);
        return matches(lookupPath);

    }

    public boolean matches(String lookupPath) {
        List<String> matches = getMatchingPatterns(lookupPath);
        return !matches.isEmpty();
    }

    public List<String> getMatchingPatterns(String lookupPath) {
        List<String> matches = null;
        for (String pattern : this.patterns) {
            String match = getMatchingPattern(pattern, lookupPath);
            if (match != null) {
                matches = (matches != null ? matches : new ArrayList<>());
                matches.add(match);
            }
        }
        if (matches == null) {
            return Collections.emptyList();
        }
        if (matches.size() > 1) {
            matches.sort(this.pathMatcher.getPatternComparator(lookupPath));
        }
        return matches;
    }

    @Nullable
    protected String getMatchingPattern(String pattern, String lookupPath) {
        if (pattern.equals(lookupPath)) {
            return pattern;
        }
        if (this.useSuffixPatternMatch) {
            if (!this.fileExtensions.isEmpty() && lookupPath.indexOf('.') != -1) {
                for (String extension : this.fileExtensions) {
                    if (this.pathMatcher.match(pattern + extension, lookupPath)) {
                        return pattern + extension;
                    }
                }
            } else {
                boolean hasSuffix = pattern.indexOf('.') != -1;
                if (!hasSuffix && this.pathMatcher.match(pattern + ".*", lookupPath)) {
                    return pattern + ".*";
                }
            }
        }
        if (this.pathMatcher.match(pattern, lookupPath)) {
            return pattern;
        }
        if (this.useTrailingSlashMatch) {
            if (!pattern.endsWith("/") && this.pathMatcher.match(pattern + "/", lookupPath)) {
                return pattern + "/";
            }
        }
        return null;
    }

    private static Set<String> initPatterns(String[] patterns) {
        if (!hasPattern(patterns)) {
            return EMPTY_PATH_PATTERN;
        }
        Set<String> result = new LinkedHashSet<>(patterns.length);
        for (String pattern : patterns) {
            if (StringUtils.hasLength(pattern) && !pattern.startsWith("/")) {
                pattern = "/" + pattern;
            }
            result.add(pattern);
        }
        return result;
    }

    private static boolean hasPattern(String[] patterns) {
        if (!ObjectUtils.isEmpty(patterns)) {
            for (String pattern : patterns) {
                if (StringUtils.hasText(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }
}
