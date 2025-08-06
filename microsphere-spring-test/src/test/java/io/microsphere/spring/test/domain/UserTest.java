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

package io.microsphere.spring.test.domain;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.function.BiConsumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link User} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see User
 * @since 1.0.0
 */
public class UserTest {

    private User user;

    @Before
    public void setUp() {
        this.user = new User();
    }

    @Test
    public void testGetName() {
        assertNull(this.user.getName());
    }

    @Test
    public void testSetName() {
        this.user.setName("Mercy");
        assertEquals("Mercy", this.user.getName());
    }

    @Test
    public void testGetAge() {
        assertEquals(0, this.user.getAge());
    }

    @Test
    public void testSetAge() {
        this.user.setAge(18);
        assertEquals(18, this.user.getAge());
    }

    @Test
    public void testEquals() {
        assertNotEquals(this.user, null);
        assertEquals(this.user, this.user);
        assertUser(Assert::assertEquals);

        User user = new User();
        assertNotEquals(this.user, user);
        assertNotEquals(user, this.user);

        user.setAge(1);
        assertNotEquals(this.user, user);
        assertNotEquals(user, this.user);

        user.setName("Mercy");
        assertNotEquals(this.user, user);
        assertNotEquals(user, this.user);

        user.setAge(18);
        user.setName(null);
        assertNotEquals(this.user, user);
        assertNotEquals(user, this.user);
    }

    @Test
    public void testHashCode() {
        assertUser((u1, u2) -> assertEquals(u1.hashCode(), u2.hashCode()));
    }

    @Test
    public void testToString() {
        assertUser((u1, u2) -> assertEquals(u1.toString(), u2.toString()));
    }

    void assertUser(BiConsumer<User, User> consumer) {
        User user = new User();
        consumer.accept(this.user, user);

        this.user.setName("Mercy");
        user.setName("Mercy");
        consumer.accept(this.user, user);

        this.user.setAge(18);
        user.setAge(18);
        consumer.accept(this.user, user);
    }
}