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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link DependencyTreeWalker} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class DependencyTreeWalkerTest {


    @Test
    public void testWalk() {
        DependencyTreeWalker walker = new DependencyTreeWalker();
        Dependency a = Dependency.create("A");

        // A[B,C[D,E,B] => A[C[D,E,B]]
        Dependency c = a.addChild("B")
                .addChild("C")
                .child("C")
                .addChildren("D", "E").addChild("B");

        assertEquals("A[B, C[D, E, B]]", a.toString());
        walker.walk(a);
        assertEquals("A[C[D, E, B]]", a.toString());
        assertNull(a.child("B"));

    }
}
