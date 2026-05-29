/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in dependencyliance with
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

import io.microsphere.logging.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static java.util.Collections.emptyList;

/**
 * Spring Bean Dependency Graph
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Dependency
 * @since 1.0.0
 */
public class DependencyGraph {

    private final static Logger logger = getLogger(DependencyGraph.class);

    private final Map<String, Dependency> dependencies = newHashMap();

    private final List<String> cyclePath = newLinkedList(); // Stores cycle path when detected

    // Add a component to the graph
    public Dependency addDependencyNode(String name) {
        return dependencies.computeIfAbsent(name, Dependency::new);
    }

    // Add dependency relationship: 'from' depends on 'to'
    public void addDependencyRelation(String from, String to) {
        Dependency fromDep = addDependencyNode(from);
        Dependency toDep = addDependencyNode(to);
        fromDep.addDependency(toDep);
    }

    // Detect cycles using DFS
    private boolean hasCycle(Dependency dep, StringBuilder cycleLog) {
        if (dep.inStack) {
            // Cycle detected
            cyclePath.add(dep.beanName);
            return true;
        }

        if (dep.visited) {
            return false;
        }

        dep.visited = true;
        dep.inStack = true;

        for (Dependency dependency : dep.dependencies) {
            if (hasCycle(dependency, cycleLog)) {
                cyclePath.add(dep.beanName);
                return true;
            }
        }

        dep.inStack = false;
        return false;
    }

    // Check all dependencies for cycles
    private boolean detectCycles(StringBuilder cycleLog) {
        // Reset visit states
        for (Dependency dep : dependencies.values()) {
            dep.visited = false;
            dep.inStack = false;
        }

        cyclePath.clear();

        for (Dependency dep : dependencies.values()) {
            if (!dep.visited) {
                if (hasCycle(dep, cycleLog)) {
                    Collections.reverse(cyclePath);
                    cycleLog.append("Cycle detected! Path: ");
                    for (int i = 0; i < cyclePath.size(); i++) {
                        cycleLog.append(cyclePath.get(i));
                        if (i < cyclePath.size() - 1) {
                            cycleLog.append(" -> ");
                        }
                    }
                    cycleLog.append("\n");
                    return true;
                }
            }
        }
        return false;
    }

    // Calculate level graph in order using topological sort
    public List<List<String>> calculateGraph() {
        StringBuilder cycleLog = new StringBuilder();

        // Check for cycles first
        if (detectCycles(cycleLog)) {
            logger.error(cycleLog.toString());
            return emptyList();
        }

        Map<Dependency, Integer> inDegree = newHashMap();
        Queue<Dependency> queue = newLinkedList();
        List<List<String>> levels = newLinkedList();

        // Initialize in-degrees
        for (Dependency dep : dependencies.values()) {
            inDegree.put(dep, dep.dependencies.size());
            if (dep.dependencies.isEmpty()) {
                queue.offer(dep);
            }
        }

        // Topological sort by levels
        while (!queue.isEmpty()) {
            int levelSize = queue.size();
            List<String> currentLevel = new ArrayList<>();
            for (int i = 0; i < levelSize; i++) {
                Dependency dep = queue.poll();
                currentLevel.add(dep.beanName);
                // Decrease in-degree of dependents
                for (Dependency dependent : dep.dependents) {
                    int newInDegree = inDegree.get(dependent) - 1;
                    inDegree.put(dependent, newInDegree);
                    if (newInDegree == 0) {
                        queue.offer(dependent);
                    }
                }
            }
            levels.add(currentLevel);
        }

        // Double-check for cycles (safety net)
        int totalNodes = dependencies.size();
        int processedNodes = levels.stream().mapToInt(List::size).sum();
        if (processedNodes != totalNodes) {
            logger.warn("Topological sort didn't process all nodes, possible undetected cycle!");
            logger.warn("Processed nodes: " + processedNodes + ", Total nodes: " + totalNodes);

            // Find unprocessed nodes
            Set<String> processedNames = new HashSet<>();
            for (List<String> level : levels) {
                processedNames.addAll(level);
            }

            StringBuilder unprocessed = new StringBuilder("Unprocessed nodes: ");
            for (Dependency dep : dependencies.values()) {
                if (!processedNames.contains(dep.beanName)) {
                    unprocessed.append(dep.beanName).append(" ");
                }
            }
            logger.warn(unprocessed.toString());
            return emptyList();
        }

        return levels;
    }
}