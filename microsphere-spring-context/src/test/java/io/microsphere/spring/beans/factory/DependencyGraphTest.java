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


import io.microsphere.logging.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * {@link DependencyGraph} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DependencyGraph
 * @since 1.0.0
 */
class DependencyGraphTest {

    private static final Logger logger = getLogger(DependencyGraphTest.class);

    @Test
    void testNormalCase() {
        DependencyGraph graph = new DependencyGraph();

        // Build dependency graph
        graph.addDependencyNode("A");
        graph.addDependencyNode("B");
        graph.addDependencyNode("C");
        graph.addDependencyNode("D");
        graph.addDependencyNode("E");
        graph.addDependencyNode("F");

        // A depends on B
        graph.addDependencyRelation("A", "B");

        // B depends on C, D, F
        graph.addDependencyRelation("B", "C");
        graph.addDependencyRelation("B", "D");
        graph.addDependencyRelation("B", "F");

        // C depends on E, F
        graph.addDependencyRelation("C", "E");
        graph.addDependencyRelation("C", "F");

        try {
            List<List<String>> leveledGraph = graph.calculateGraph();

            logger.info("The leveled graph in order (same line can run concurrently):");
            for (int i = 0; i < leveledGraph.size(); i++) {
                logger.info("Level " + (i + 1) + ": " + leveledGraph.get(i));
            }
        } catch (RuntimeException e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    @Test
    void testCyclicCase() {
        DependencyGraph graph = new DependencyGraph();

        // Create cyclic dependencies
        graph.addDependencyNode("X");
        graph.addDependencyNode("Y");
        graph.addDependencyNode("Z");

        // X -> Y -> Z -> X creates a cycle
        graph.addDependencyRelation("X", "Y");
        graph.addDependencyRelation("Y", "Z");
        graph.addDependencyRelation("Z", "X");

        List<List<String>> leveledGraph = graph.calculateGraph();
        logger.info("Leveled graph: " + leveledGraph);
    }
}