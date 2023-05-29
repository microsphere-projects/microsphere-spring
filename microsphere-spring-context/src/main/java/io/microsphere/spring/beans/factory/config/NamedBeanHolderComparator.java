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
 * {@link NamedBeanHolder} {@link Comparator}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class NamedBeanHolderComparator<T> implements Comparator<NamedBeanHolder<T>> {

    public static final NamedBeanHolderComparator INSTANCE = new NamedBeanHolderComparator();

    @Override
    public int compare(NamedBeanHolder<T> o1, NamedBeanHolder<T> o2) {
        return AnnotationAwareOrderComparator.INSTANCE.compare(o1.getBeanInstance(), o2.getBeanInstance());
    }
}
