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

import org.junit.Test;

import static io.microsphere.spring.beans.factory.Dependency.create;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * {@link Dependency} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class DependencyTest {

    /**
     * <ul>
     *     <li>A</li>
     *     <ul>
     *         <li>B</li>
     *         <li>C
     *          <ul>
     *              <li>D</li>
     *              <li>E</li>
     *          </ul>
     *         </li>
     *     </ul>
     * </ul>
     */
    @Test
    public void test() {
        Dependency a = create("A");

        Dependency c = a.addChild("B")
                .addChild("C")
                .child("C")
                .addChildren("D", "E");

        Dependency root = c.root();

        assertSame(root, a);
        assertSame(c, a.child("C"));
        assertEquals(a, a.child("C").parent());
    }

    @Test
    public void testHashCode() {
        assertEquals(create("A").hashCode(), create("A").hashCode());
    }
}
