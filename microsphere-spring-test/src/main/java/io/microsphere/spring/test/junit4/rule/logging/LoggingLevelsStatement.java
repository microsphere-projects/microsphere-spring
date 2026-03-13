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

package io.microsphere.spring.test.junit4.rule.logging;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Set;

import static io.microsphere.collection.Sets.ofSet;

/**
 * The {@link Statement} repeats the execution with the specified logging levels.
 * <p>
 * The implementation class must have and only have a constructor with types in order:
 * <ol>
 *     <li>{@link Statement}</li>
 *     <li>{@link Description}</li>
 *     <li>{@link String}[]</li>
 * </ol>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Statement
 * @since 1.0.0
 */
public abstract class LoggingLevelsStatement extends Statement {

    protected final Statement next;

    protected final Description description;

    protected final Class<?> testClass;

    protected final Set<String> levels;

    protected LoggingLevelsStatement(Statement next, Description description, String... levels) {
        this.next = next;
        this.description = description;
        this.testClass = description.getTestClass();
        this.levels = ofSet(levels);
    }
}