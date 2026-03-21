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
package sample;

import java.util.Map;
import java.util.Set;

/**
 * A sample interface for testing documentation generation.
 * <p>
 * This interface demonstrates how the doc generator extracts information from Java source files.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * SampleInterface impl = new SampleInterfaceImpl();
 * Map<String, Set<String>> result = impl.resolve("myBean");
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see java.util.Map
 * @see java.util.Set
 * @since 1.0.0
 */
public interface SampleInterface {

    /**
     * Resolve dependencies for the given bean name
     *
     * @param beanName the name of the bean
     * @return a map of bean names to their dependencies
     * @since 1.0.0
     */
    Map<String, Set<String>> resolve(String beanName);

    /**
     * Check if the given bean has dependencies
     *
     * @param beanName the name of the bean
     * @return true if the bean has dependencies
     */
    boolean hasDependencies(String beanName);
}
