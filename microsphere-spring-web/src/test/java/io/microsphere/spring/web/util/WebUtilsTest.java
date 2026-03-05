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

package io.microsphere.spring.web.util;


import io.microsphere.spring.test.web.controller.TestController;
import org.junit.jupiter.api.Test;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static io.microsphere.spring.web.util.WebUtils.isHandlerMethod;
import static io.microsphere.spring.web.util.WebUtils.isNoArgumentHandlerMethod;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.beans.BeanUtils.findMethod;

/**
 * {@link WebUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebUtils
 * @since 1.0.0
 */
class WebUtilsTest {

    private static final Method helloWorldMethod = findMethod(TestController.class, "helloWorld");

    private static final Method greetingMethod = findMethod(TestController.class, "greeting", String.class);

    @Test
    void testIsHandlerMethod() {
        assertFalse(isHandlerMethod(null));
        assertFalse(isHandlerMethod(new Object()));
        assertTrue(isHandlerMethod(new HandlerMethod(this, helloWorldMethod)));
    }

    @Test
    void testIsNoArgumentHandlerMethod() {
        assertTrue(isNoArgumentHandlerMethod(new HandlerMethod(this, helloWorldMethod)));
        assertFalse(isNoArgumentHandlerMethod(new HandlerMethod(this, greetingMethod)));
    }
}