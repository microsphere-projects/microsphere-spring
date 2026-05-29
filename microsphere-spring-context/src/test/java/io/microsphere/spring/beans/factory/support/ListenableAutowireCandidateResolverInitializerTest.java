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
package io.microsphere.spring.beans.factory.support;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link ListenableAutowireCandidateResolverInitializer} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ListenableAutowireCandidateResolverInitializer
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = ListenableAutowireCandidateResolverInitializerTest.class,
        initializers = ListenableAutowireCandidateResolverInitializer.class
)
class ListenableAutowireCandidateResolverInitializerTest {

    @Autowired
    private ObjectProvider<ListenableAutowireCandidateResolver> resolverProvider;

    @Test
    void testInitializerRegistersResolver() {
        // The initializer registers a ListenableAutowireCandidateResolver as an infrastructure bean.
        // After context refresh the resolver should be available.
        ListenableAutowireCandidateResolver resolver = resolverProvider.getIfAvailable();
        assertNotNull(resolver, "ListenableAutowireCandidateResolver should have been registered by the initializer");
    }
}
