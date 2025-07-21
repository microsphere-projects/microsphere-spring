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

import io.microsphere.filter.Filter;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Set;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.filter.FilterOperator.AND;
import static io.microsphere.filter.FilterOperator.OR;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.of;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link FilteringWebEndpointMappingRegistry} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see FilteringWebEndpointMappingRegistry
 * @since 1.0.0
 */
public class FilteringWebEndpointMappingRegistryTest {

    private Set<WebEndpointMapping> webEndpointMappings;

    private FilteringWebEndpointMappingRegistry registry;

    static class FilteringWebEndpointMappingRegistryImpl extends FilteringWebEndpointMappingRegistry {

        private final boolean result;

        FilteringWebEndpointMappingRegistryImpl() {
            this(true);
        }

        FilteringWebEndpointMappingRegistryImpl(boolean result) {
            this.result = result;
        }

        @Override
        public boolean register(WebEndpointMapping webEndpointMapping) {
            return result;
        }

        @Override
        public Collection<WebEndpointMapping> getWebEndpointMappings() {
            return emptyList();
        }
    }

    @Before
    public void setUp() throws Exception {
        this.webEndpointMappings = ofSet(
                of("/a").build(),
                of("/b").build(),
                of("/c").build()
        );
        this.registry = new FilteringWebEndpointMappingRegistryImpl();
    }

    @Test
    public void testRegister() {
        assertEquals(3, this.registry.register(this.webEndpointMappings));
        assertEquals(0, new FilteringWebEndpointMappingRegistryImpl(false).register(this.webEndpointMappings));
    }

    @Test
    public void testSetFilterOperator() {
        this.registry.setFilterOperator(AND);
        assertEquals(3, this.registry.register(this.webEndpointMappings));
    }

    @Test
    public void testSetWebEndpointMappingFilters() {
        this.registry.setWebEndpointMappingFilters(asList(e -> true));
        assertEquals(3, this.registry.register(this.webEndpointMappings));
        assertNotNull(this.registry.getFilter());
    }

    @Test
    public void testGetFilter() {
        Filter<WebEndpointMapping> filter = this.registry.getFilter();
        assertNotNull(filter);
        this.webEndpointMappings.forEach(mapping -> assertTrue(filter.accept(mapping)));
    }

    @Test
    public void testGetFilterOperator() {
        assertSame(OR, this.registry.getFilterOperator());

        this.registry.setFilterOperator(AND);
        assertSame(AND, this.registry.getFilterOperator());
    }

}