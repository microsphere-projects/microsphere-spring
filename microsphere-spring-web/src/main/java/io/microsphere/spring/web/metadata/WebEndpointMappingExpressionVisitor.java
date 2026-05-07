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

package io.microsphere.spring.web.metadata;

/**
 * {@link WebEndpointMappingVisitor} class to present the expression of {@link WebEndpointMapping}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMappingVisitor
 * @see WebEndpointMapping
 * @since 1.0.0
 */
public class WebEndpointMappingExpressionVisitor implements WebEndpointMappingVisitor {

    private final StringBuilder expressionBuilder = new StringBuilder();

    @Override
    public void method(String[] methods) {

    }

    @Override
    public void path(String pattern) {

    }

    @Override
    public void header(String name, String value) {

    }

    @Override
    public void queryParam(String name, String value) {

    }

    @Override
    public void startAnd() {

    }

    @Override
    public void and() {

    }

    @Override
    public void endAnd() {

    }

    @Override
    public void startOr() {

    }

    @Override
    public void or() {

    }

    @Override
    public void endOr() {

    }

    @Override
    public void startNegate() {

    }

    @Override
    public void endNegate() {

    }

    public String toString() {
        return expressionBuilder.toString();
    }
}
