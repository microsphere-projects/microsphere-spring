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
 * The visitor interface of {@link WebEndpointMapping}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMapping
 * @since 1.0.0
 */
public interface WebEndpointMappingVisitor {

    /**
     * Receive notification of an HTTP method.
     *
     * @param methods the HTTP methods
     */
    void method(String[] methods);

    /**
     * Receive notification of a path.
     *
     * @param pattern the path pattern
     */
    void path(String pattern);

    /**
     * Receive notification of an HTTP header.
     *
     * @param name  the name of the HTTP header to check
     * @param value the desired value of the HTTP header
     */
    void header(String name, String value);

    /**
     * Receive notification of a query parameter.
     *
     * @param name  the name of the query parameter
     * @param value the desired value of the parameter
     */
    void queryParam(String name, String value);

    /**
     * Receive first notification of a logical AND.
     * The first subsequent notification will contain the left-hand side of the AND-predicate;
     * followed by {@link #and()}, followed by the right-hand side, followed by {@link #endAnd()}.
     *
     */
    void startAnd();

    /**
     * Receive "middle" notification of a logical AND.
     * The following notification contains the right-hand side, followed by {@link #endAnd()}.
     *
     */
    void and();

    /**
     * Receive last notification of a logical AND.
     *
     */
    void endAnd();

    /**
     * Receive first notification of a logical OR.
     * The first subsequent notification will contain the left-hand side of the OR-predicate;
     * the second notification contains the right-hand side, followed by {@link #endOr()}.
     *
     */
    void startOr();

    /**
     * Receive "middle" notification of a logical OR.
     * The following notification contains the right-hand side, followed by {@link #endOr()}.
     *
     */
    void or();

    /**
     * Receive last notification of a logical OR.
     *
     */
    void endOr();

    /**
     * Receive first notification of a negated.
     * The first subsequent notification will contain the negated predicated, followed
     * by {@link #endNegate()}.
     *
     */
    void startNegate();

    /**
     * Receive last notification of a negated.
     *
     */
    void endNegate();
}