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
package io.microsphere.spring.beans.factory.config;

import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.Comparator;

/**
 * A comparator for {@link NamedBeanHolder} instances, typically used to sort
 * bean holders based on the order of their contained bean instances.
 *
 * <p>This comparator delegates the actual comparison logic to
 * {@link AnnotationAwareOrderComparator}, which takes into account
 * the {@link org.springframework.core.Ordered} interface as well as
 * the {@link java.util.Collections#reverseOrder()} if applicable.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * List<NamedBeanHolder<MyBean>> beanHolders = getBeanHolders();
 * beanHolders.sort(NamedBeanHolderComparator.INSTANCE);
 * }</pre>
 *
 * @param <T> the type of the bean instance held by the {@link NamedBeanHolder}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotationAwareOrderComparator
 * @see NamedBeanHolder
 * @since 1.0.0
 */
public class NamedBeanHolderComparator<T> implements Comparator<NamedBeanHolder<T>> {

    public static final NamedBeanHolderComparator INSTANCE = new NamedBeanHolderComparator();

    @Override
    public int compare(NamedBeanHolder<T> o1, NamedBeanHolder<T> o2) {
        return AnnotationAwareOrderComparator.INSTANCE.compare(o1.getBeanInstance(), o2.getBeanInstance());
    }
}
