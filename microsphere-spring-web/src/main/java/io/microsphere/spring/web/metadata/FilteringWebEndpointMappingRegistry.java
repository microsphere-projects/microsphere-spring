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
import io.microsphere.filter.FilterOperator;

import java.util.List;

import static io.microsphere.filter.FilterOperator.OR;
import static io.microsphere.lang.function.Streams.filterList;
import static io.microsphere.util.ArrayUtils.asArray;

/**
 * Abstract {@link WebEndpointMappingRegistry} with filtering
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMappingRegistry
 * @see WebEndpointMappingFilter
 * @since 1.0.0
 */
public abstract class FilteringWebEndpointMappingRegistry implements WebEndpointMappingRegistry {

    protected static final Filter<WebEndpointMapping> DEFAULT_FILTER = e -> true;

    protected static final FilterOperator DEFAULT_FILTER_OPERATOR = OR;

    private Filter<WebEndpointMapping> filter;

    private FilterOperator filterOperator;

    @Override
    public final int register(Iterable<WebEndpointMapping> webEndpointMappings) {
        List<WebEndpointMapping> filteredWebEndpointMappings = filterWebEndpointMappings(webEndpointMappings);
        int size = filteredWebEndpointMappings.size();
        int count = size;
        for (int i = 0; i < size; i++) {
            WebEndpointMapping filteredWebEndpointMapping = filteredWebEndpointMappings.get(i);
            if (!register(filteredWebEndpointMapping)) {
                count--;
            }
        }
        return count;
    }

    protected List<WebEndpointMapping> filterWebEndpointMappings(Iterable<WebEndpointMapping> webEndpointMappings) {
        return filterList(webEndpointMappings, getFilter()::accept);
    }

    public void setFilterOperator(FilterOperator filterOperator) {
        this.filterOperator = filterOperator;
    }

    public void setWebEndpointMappingFilters(Iterable<WebEndpointMappingFilter> filters) {
        this.setWebEndpointMappingFilters(asArray(filters, WebEndpointMappingFilter.class));
    }

    public void setWebEndpointMappingFilters(WebEndpointMappingFilter... filters) {
        filter = getFilterOperator().createFilter(filters);
    }

    public Filter<WebEndpointMapping> getFilter() {
        if (filter == null) {
            return DEFAULT_FILTER;
        }
        return filter;
    }

    public FilterOperator getFilterOperator() {
        if (filterOperator == null) {
            return DEFAULT_FILTER_OPERATOR;
        }
        return filterOperator;
    }
}
