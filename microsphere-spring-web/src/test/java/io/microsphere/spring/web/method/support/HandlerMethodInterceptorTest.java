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

package io.microsphere.spring.web.method.support;


import org.junit.Before;
import org.junit.Test;

/**
 * {@link HandlerMethodInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethodInterceptor
 * @since 1.0.0
 */
public class HandlerMethodInterceptorTest {

    private HandlerMethodInterceptor interceptor;

    @Before
    public void setUp() {
        this.interceptor = new HandlerMethodInterceptor() {
        };
    }

    @Test
    public void testBeforeExecute() throws Exception {
        this.interceptor.beforeExecute(null, null, null);
    }

    @Test
    public void testAfterExecute() throws Exception {
        this.interceptor.afterExecute(null, null, null, null, null);
    }

}