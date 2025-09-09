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

import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;

/**
 * {@link AbstractWebRequestRule} implementation class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractWebRequestRule
 * @since 1.0.0
 */
public class AbstractWebRequestRuleImpl extends AbstractWebRequestRule<String> {

    private final Collection<String> content;

    private final String infix;

    public AbstractWebRequestRuleImpl(Collection<String> content, String infix) {
        this.content = content;
        this.infix = infix;
    }

    @Override
    public boolean matches(NativeWebRequest request) {
        return true;
    }

    @Override
    protected Collection<String> getContent() {
        return content;
    }

    @Override
    protected String getToStringInfix() {
        return infix;
    }

    @Override
    public boolean isEmpty() {
        return super.isEmpty();
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
