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

package io.microsphere.spring.web.servlet.function;

import org.springframework.core.io.Resource;
import org.springframework.web.servlet.function.HandlerFunction;
import org.springframework.web.servlet.function.RequestPredicate;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.RouterFunctions.Visitor;
import org.springframework.web.servlet.function.ServerRequest;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * The adapter interface for {@link Visitor RouterFunctions.Visitor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Visitor
 * @see RouterFunction
 * @see RouterFunctions
 * @since 1.0.0
 */
public interface RouterFunctionVisitorAdapter extends Visitor {

    @Override
    default void startNested(RequestPredicate predicate) {
    }

    @Override
    default void endNested(RequestPredicate predicate) {
    }

    @Override
    default void route(RequestPredicate predicate, HandlerFunction<?> handlerFunction) {
    }

    @Override
    default void resources(Function<ServerRequest, Optional<Resource>> lookupFunction) {
    }

    /**
     * {@inheritDoc}
     * <p>
     * Compatible with Spring Framework 5.3.x and earlier versions.
     */
    default void attributes(Map<String, Object> attributes) {
    }

    @Override
    default void unknown(RouterFunction<?> routerFunction) {
    }
}
