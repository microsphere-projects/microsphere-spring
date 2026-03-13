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


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import static ch.qos.logback.classic.Level.toLevel;
import static org.junit.runner.Description.createTestDescription;
import static org.slf4j.LoggerFactory.getILoggerFactory;

/**
 * The {@link LoggingLevelsStatement} for logback.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggingLevelsStatement
 * @since 1.0.0
 */
public class LogbackLevelsStatement extends LoggingLevelsStatement {

    private static final LoggerContext loggerContext = (LoggerContext) getILoggerFactory();

    public LogbackLevelsStatement(Statement next, Description description, String... levels) {
        super(next, description, levels);
    }

    @Override
    public void evaluate() throws Throwable {
        Class<?> testClass = this.testClass;
        Logger logger = getTargetLogger(testClass);
        Level orignalLevel = logger.getLevel();
        try {
            for (String levelString : levels) {
                description.addChild(createTestDescription(testClass, description.getMethodName() + " with logging level - " + levelString));
                Level level = toLevel(levelString);
                logger.setLevel(level);
                next.evaluate();
            }
        } finally {
            // Reset original level
            logger.setLevel(orignalLevel);
        }
    }

    protected Logger getTargetLogger(Class<?> testClass) {
        return loggerContext.getLogger(testClass.getPackage().getName());
    }
}