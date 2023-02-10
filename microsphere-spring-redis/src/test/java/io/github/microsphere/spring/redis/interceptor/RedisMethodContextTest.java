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
package io.github.microsphere.spring.redis.interceptor;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link RedisMethodContext} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class RedisMethodContextTest {

    private RedisMethodContext context;

    @Before
    public void init() {
        context = createRedisMethodContext();
    }

    private RedisMethodContext createRedisMethodContext() {
        RedisMethodContext context = new RedisMethodContext(this, null, null, null);
        return context;
    }

    @Test
    public void testAttributes() {
        assertTrue(context.getAttributes().isEmpty());
        assertFalse(context.hasAttribute("a"));

        context.setAttribute("a", 1);
        assertTrue(context.hasAttribute("a"));
        assertEquals(1, context.getAttribute("a"));

        context.removeAttribute("a");
        assertFalse(context.hasAttribute("a"));
    }
}
