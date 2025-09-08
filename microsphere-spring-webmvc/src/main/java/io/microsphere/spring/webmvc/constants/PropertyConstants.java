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
package io.microsphere.spring.webmvc.constants;

import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;

/**
 * The Property constants for Microsphere Spring WebMVC
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface PropertyConstants {

    /**
     * The property name prefix of Microsphere Spring WebMVC: "microsphere.spring.webmvc."
     */
    String MICROSPHERE_SPRING_WEBMVC_PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "webmvc.";

    /**
     * The property name prefix of Microsphere Spring WebMVC View Resolver: "microsphere.spring.webmvc.view-resolver."
     */
    String MICROSPHERE_SPRING_WEBMVC_VIEW_RESOLVER_PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_WEBMVC_PROPERTY_NAME_PREFIX + "view-resolver.";
}
