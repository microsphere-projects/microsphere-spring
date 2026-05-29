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

import java.util.List;

import static io.microsphere.collection.ListUtils.newLinkedList;

/**
 * Spring Bean Dependency
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class Dependency {

    final String beanName;

    final List<Dependency> dependencies; // Components this one depends on (incoming edges)

    final List<Dependency> dependents;   // Components that depend on this one (outgoing edges)

    boolean visited;               // DFS traversal marker

    boolean inStack;               // DFS recursion stack marker

    public Dependency(String beanName) {
        this.beanName = beanName;
        this.dependencies = newLinkedList();
        this.dependents = newLinkedList();
    }

    void addDependency(Dependency dependency) {
        dependencies.add(dependency);
        dependency.dependents.add(this);
    }
}
