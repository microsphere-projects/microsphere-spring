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

package io.microsphere.spring.test.web.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * {@link Filter} for testing
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Filter
 * @since 1.0.0
 */
public class TestFilter implements Filter {

    /**
     * Default Filter Name
     */
    public static final String DEFAULT_FILTER_NAME = "testFilter";

    /**
     * Filter Class Name
     */
    public static final String FILTER_CLASS_NAME = TestFilter.class.getName();

    /**
     * Default URL Pattern
     */
    public static final String DEFAULT_FILTER_URL_PATTERN = "/" + DEFAULT_FILTER_NAME;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    }

    @Override
    public void destroy() {
    }
}
