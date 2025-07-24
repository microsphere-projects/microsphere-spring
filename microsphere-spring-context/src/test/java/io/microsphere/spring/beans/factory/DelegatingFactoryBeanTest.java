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

package io.microsphere.spring.beans.factory;

import io.microsphere.logging.Logger;
import io.microsphere.spring.test.domain.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.DisposableBean;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link DelegatingFactoryBean} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DelegatingFactoryBean
 * @since 1.0.0
 */
public class DelegatingFactoryBeanTest implements DisposableBean {

    private static final Logger logger = getLogger(DelegatingFactoryBeanTest.class);

    private User user;

    private DelegatingFactoryBean factoryBean;

    private DelegatingFactoryBean factoryBean2;

    @Before
    public void before() {
        this.user = new User();
        this.factoryBean = new DelegatingFactoryBean(this);
        this.factoryBean2 = new DelegatingFactoryBean(this.user, false);
    }

    @Test
    public void testIsSingleton() {
        assertTrue(factoryBean.isSingleton());
        assertFalse(factoryBean2.isSingleton());
    }

    @Test
    public void testGetObject() throws Exception {
        assertSame(this, factoryBean.getObject());
        assertSame(this.user, factoryBean2.getObject());
    }

    @Test
    public void testGetObjectType() {
        assertSame(this.getClass(), factoryBean.getObjectType());
        assertSame(User.class, factoryBean2.getObjectType());
    }

    @Test
    public void testAfterPropertiesSet() throws Exception {
        factoryBean.afterPropertiesSet();
        factoryBean2.afterPropertiesSet();
    }

    @Test
    public void testSetApplicationContext() {
        factoryBean.setApplicationContext(null);
        factoryBean2.setApplicationContext(null);
    }

    @Test
    public void testSetBeanName() {
        factoryBean.setBeanName("factoryBean");
        factoryBean2.setBeanName("factoryBean2");
    }

    @Test
    public void testDestroy() throws Exception {
        factoryBean.destroy();
        factoryBean2.destroy();
    }

    @Override
    public void destroy() throws Exception {
        logger.debug("destroy...");
    }
}
