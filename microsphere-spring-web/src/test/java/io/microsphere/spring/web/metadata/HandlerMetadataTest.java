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

import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * {@link HandlerMetadataTest} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMetadataTest
 * @since 1.0.0
 */
public class HandlerMetadataTest {

    private HandlerMetadata<String, String> metadata;

    @Before
    public void setUp() {
        metadata = new HandlerMetadata<>("handler", "metadata");
    }

    @Test
    public void testGetHandler() {
        assertEquals("handler", metadata.getHandler());
    }

    @Test
    public void testGetMetadata() {
        assertEquals("metadata", metadata.getMetadata());
    }

    @Test
    public void testEquals() {
        assertNotEquals(this.metadata, null);
        assertNotEquals(this.metadata, "test");
        assertNotEquals(this.metadata, new HandlerMetadata<>("handler", ""));
        assertNotEquals(this.metadata, new HandlerMetadata<>("", "metadata"));
        assertEquals(this.metadata, new HandlerMetadata<>("handler", "metadata"));
        assertEquals(this.metadata, this.metadata);
    }

    @Test
    public void testHashCode() {
        assertEquals(this.metadata.hashCode(), Objects.hash("handler", "metadata"));
    }

    @Test
    public void testToString() {
        assertEquals("HandlerMetadata{handler=handler, metadata=metadata}", metadata.toString());
    }
}