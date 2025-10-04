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


/**
 * Base {@link NameValueExpression} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractNameValueExpression
 * @since 1.0.0
 */
public abstract class BaseNameValueExpressionTest<E extends AbstractNameValueExpression<String>> {

    public abstract void testGetName();

    public abstract void testGetValue();

    public abstract void testIsNegated();

    public abstract void testMatch();

    public abstract void testIsCaseSensitiveName();

    public abstract void testParseValue();

    public abstract void testMatchName();

    public abstract void testMatchValue();

    public abstract void testGetExpression();

    public abstract void testEquals();

    public abstract void testHashCode();

    public abstract void testToString();
}