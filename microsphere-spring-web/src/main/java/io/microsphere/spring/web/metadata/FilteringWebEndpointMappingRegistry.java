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

import static io.microsphere.lang.function.Streams.filterList;
import static io.microsphere.util.ArrayUtils.asArray;

/**
 * Abstract {@link WebEndpointMappingRegistry} with filtering
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMappingRegistry
 * @see WebEndpointMappingFilter
 * @since 1.0.07`
 */
public abstract class FilteringWebEndpointMappingRegistry implements WebEndpointMappingRegistry {

    protected static final Filter<WebEndpointMapping> DEFAULT_FILTER = e -> true;

    protected static final FilterOperator DEFAULT_FILTER_OPERATOR = FilterOperator.OR;

    private Filter<WebEndpointMapping> compositeFilter = DEFAULT_FILTER;

    private FilterOperator filterOperator = DEFAULT_FILTER_OPERATOR;

    @Override
    public final boolean register(Iterable<WebEndpointMapping> webEndpointMappings) {
        return doRegister(filterList(webEndpointMappings, getCompositeFilter()::accept));
    }

    public void setFilterOperator(FilterOperator filterOperator) {
        this.filterOperator = filterOperator;
    }

    public void setWebEndpointMappingFilters(Iterable<WebEndpointMappingFilter> filters) {
        this.setWebEndpointMappingFilters(asArray(filters, WebEndpointMappingFilter.class));
    }

    public void setWebEndpointMappingFilters(WebEndpointMappingFilter... filters) {
        compositeFilter = getFilterOperator().createFilter(filters);
    }

    public Filter<WebEndpointMapping> getCompositeFilter() {
        if (compositeFilter == null) {
            return DEFAULT_FILTER;
        }
        return compositeFilter;
    }

    public FilterOperator getFilterOperator() {
        if (filterOperator == null) {
            return DEFAULT_FILTER_OPERATOR;
        }
        return filterOperator;
    }

    /**
     * Registers the instances of {@link WebEndpointMapping} after be filtered
     *
     * @param webEndpointMappings the instances of {@link WebEndpointMapping} after be filtered
     * @return <code>true</code> if success, <code>false</code> otherwise
     */
    protected abstract boolean doRegister(Iterable<WebEndpointMapping> webEndpointMappings);
}
