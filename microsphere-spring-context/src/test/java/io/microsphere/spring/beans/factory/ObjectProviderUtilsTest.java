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


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static io.microsphere.lang.function.ThrowableAction.execute;
import static io.microsphere.spring.beans.factory.ObjectProviderUtils.getIfAvailable;
import static io.microsphere.spring.beans.factory.ObjectProviderUtils.getIfUnique;
import static io.microsphere.spring.beans.factory.ObjectProviderUtils.ifAvailable;
import static io.microsphere.spring.beans.factory.ObjectProviderUtils.ifUnique;
import static io.microsphere.spring.beans.factory.ObjectProviderUtils.iterator;
import static io.microsphere.spring.beans.factory.ObjectProviderUtils.orderedStream;
import static io.microsphere.spring.beans.factory.ObjectProviderUtils.stream;
import static io.microsphere.spring.core.SpringVersion.CURRENT;
import static io.microsphere.spring.core.SpringVersion.SPRING_5_1;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link ObjectProviderUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ObjectProviderUtils
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ObjectProviderUtilsTest.class)
public class ObjectProviderUtilsTest {

    private static final boolean isBelowSpring51 = SPRING_5_1.gt(CURRENT);

    @Autowired
    private ObjectProvider<ObjectProviderUtilsTest> objectProvider;

    @Autowired
    private ObjectProvider<ObjectProviderUtils> optionalProvider;

    @Autowired
    private ObjectProviderUtilsTest test;

    @Before
    public void setUp() throws Exception {
        assertSame(test, objectProvider.getIfAvailable());
        assertNull(optionalProvider.getIfAvailable());
    }

    @Test
    public void testGetIfAvailable() {
        assertSame(test, getIfAvailable(objectProvider));
        assertNull(getIfAvailable(optionalProvider));
    }

    @Test
    public void testGetIfAvailableOnNull() {
        assertNull(getIfAvailable(null));
    }

    @Test
    public void testGetIfAvailableWithSupplier() {
        assertSame(test, getIfAvailable(objectProvider, () -> test));
        assertNull(getIfAvailable(optionalProvider, () -> null));
    }

    @Test
    public void testGetIfAvailableWithSupplierOnNull() {
        assertSame(test, getIfAvailable(objectProvider, null));
        assertNull(getIfAvailable(optionalProvider, null));
    }

    @Test
    public void testIfAvailable() {
        ifAvailable(this.objectProvider, instance -> {
            assertSame(test, instance);
        });

        ifAvailable(this.optionalProvider, instance -> {
            throw new RuntimeException("Impossible here!");
        });
    }

    @Test
    public void testIfAvailableOnNull() {
        ifAvailable(null, instance -> {
            throw new RuntimeException("Impossible here!");
        });
        ifAvailable(this.objectProvider, null);
        ifAvailable(this.optionalProvider, null);
    }

    @Test
    public void testGetIfUnique() {
        assertSame(test, getIfUnique(objectProvider));
        assertNull(getIfUnique(optionalProvider));
    }

    @Test
    public void testGetIfUniqueOnNull() {
        assertNull(getIfUnique(null));
    }

    @Test
    public void testGetIfUniqueWithSupplier() {
        assertSame(test, getIfUnique(objectProvider, () -> test));
        assertSame(test, getIfUnique(objectProvider, () -> null));
        assertNull(getIfUnique(optionalProvider, () -> null));
    }

    @Test
    public void testGetIfUniqueWithSupplierOnNull() {
        assertSame(test, getIfUnique(objectProvider, null));
        assertNull(getIfUnique(optionalProvider, null));
    }

    @Test
    public void testIfUnique() {
        ifUnique(this.objectProvider, instance -> {
            assertSame(test, instance);
        });

        ifUnique(this.optionalProvider, instance -> {
            throw new RuntimeException("Impossible here!");
        });
    }

    @Test
    public void testIfUniqueOnNull() {
        ifUnique(null, instance -> {
            throw new RuntimeException("Impossible here!");
        });
        ifUnique(this.objectProvider, null);
        ifUnique(this.optionalProvider, null);
    }


    @Test
    public void testIterator() {
        if (isBelowSpring51) {
            execute(() -> iterator(objectProvider), failure -> {
                assertTrue(failure instanceof UnsupportedOperationException);
            });
        } else {
            Iterator<?> iterator = iterator(objectProvider);
            assertTrue(iterator.hasNext());
            assertSame(test, iterator.next());
            execute(iterator::next, failure -> {
                assertTrue(failure instanceof NoSuchElementException);
            });

            iterator = iterator(optionalProvider);
            assertFalse(iterator.hasNext());
            execute(iterator::next, failure -> {
                assertTrue(failure instanceof NoSuchElementException);
            });
        }
    }

    @Test
    public void testStream() {
        if (isBelowSpring51) {
            execute(() -> stream(objectProvider), failure -> {
                assertTrue(failure instanceof UnsupportedOperationException);
            });
        } else {
            Stream<?> stream = stream(objectProvider);
            assertEquals(1, stream.count());

            stream = orderedStream(optionalProvider);
            assertEquals(0, stream.count());
        }
    }

    @Test
    public void testOrderedStream() {
        if (isBelowSpring51) {
            execute(() -> orderedStream(objectProvider), failure -> {
                assertTrue(failure instanceof UnsupportedOperationException);
            });
        } else {
            Stream<?> stream = orderedStream(objectProvider);
            assertEquals(1, stream.count());

            stream = orderedStream(optionalProvider);
            assertEquals(0, stream.count());
        }
    }
}