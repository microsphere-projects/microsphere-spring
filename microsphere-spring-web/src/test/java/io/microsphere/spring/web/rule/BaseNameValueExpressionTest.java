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

package io.microsphere.spring.web.rule;


import org.junit.Test;

/**
 * Base {@link NameValueExpression} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractNameValueExpression
 * @since 1.0.0
 */
public abstract class BaseNameValueExpressionTest<E extends AbstractNameValueExpression<String>> {

    @Test
    public abstract void testGetName();

    @Test
    public abstract void testGetValue();

    @Test
    public abstract void testIsNegated();

    @Test
    public abstract void testMatch();

    @Test
    public abstract void testIsCaseSensitiveName();

    @Test
    public abstract void testParseValue();

    @Test
    public abstract void testMatchName();

    @Test
    public abstract void testMatchValue();

    @Test
    public abstract void testGetExpression();

    @Test
    public abstract void testEquals();

    @Test
    public abstract void testHashCode();

    @Test
    public abstract void testToString();
}