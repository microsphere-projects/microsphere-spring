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

package io.microsphere.spring.web.metadata;


import org.junit.Before;
import org.junit.Test;

import static java.util.Optional.empty;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link WebEndpointMappingFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMappingFactory
 * @since 1.0.0
 */
public class WebEndpointMappingFactoryTest {

    private WebEndpointMappingFactory factory;

    @Before
    public void setUp() throws Exception {
        this.factory = endpoint -> empty();
    }

    @Test
    public void testSupports() {
        assertTrue(factory.supports(null));
    }

    @Test
    public void testCreate() {
        assertFalse(factory.create(null).isPresent());
    }

    @Test
    public void testGetSourceType() {
        assertNull(factory.getSourceType());
    }
}