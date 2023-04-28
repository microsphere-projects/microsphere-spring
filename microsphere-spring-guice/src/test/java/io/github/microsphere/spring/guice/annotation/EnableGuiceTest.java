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
package io.github.microsphere.spring.guice.annotation;

import com.google.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * {@link EnableGuice} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        EnableGuiceTest.class
})
@EnableGuice
public class EnableGuiceTest {

    @Inject
    private ApplicationContext context;

    @Inject
    private GuiceConfiguration guiceConfiguration;

    @Inject(optional = true)
    private GuiceInjectAnnotationBeanPostProcessor postProcessor;

    @Inject(optional = true)
    private String notExists;

    @Test
    public void testInject() {
        assertNotNull(context);
        assertNotNull(guiceConfiguration);
        assertNotNull(postProcessor);
        assertNull(notExists);
    }
}
