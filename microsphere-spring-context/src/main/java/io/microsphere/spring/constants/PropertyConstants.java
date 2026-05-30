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
package io.microsphere.spring.constants;

import static io.microsphere.constants.PropertyConstants.MICROSPHERE_PROPERTY_NAME_PREFIX;
import static java.lang.Boolean.parseBoolean;

/**
 * The Property constants for Microsphere Spring
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface PropertyConstants {

    /**
     * The property name prefix of Microsphere Spring : "microsphere.spring."
     */
    String MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX = MICROSPHERE_PROPERTY_NAME_PREFIX + "spring.";

    /**
     * The property name prefix of beans : "microsphere.spring.beans."
     */
    String BEANS_PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "beans.";

    /**
     * The property name suffix of auto registered : "auto-registered"
     */
    String AUTO_REGISTERED_PROPERTY_NAME_SUFFIX = "auto-registered";

    /**
     * The default value of property of auto registered : "true"
     */
    String DEFAULT_AUTO_REGISTERED_PROPERTY_VALUE = "true";

    /**
     * The default value of auto registered : true
     */
    boolean DEFAULT_AUTO_REGISTERED_VALUE = parseBoolean(DEFAULT_AUTO_REGISTERED_PROPERTY_VALUE);
}