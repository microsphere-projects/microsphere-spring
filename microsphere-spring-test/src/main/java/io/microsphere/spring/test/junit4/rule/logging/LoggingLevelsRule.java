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


import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.List;

import static io.microsphere.util.ClassLoaderUtils.getClassLoader;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.newInstance;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames;

/**
 * The {@link TestRule} to iterate Logging levels
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggingLevelsStatement
 * @see TestRule
 * @since 1.0.0
 */
public class LoggingLevelsRule implements TestRule {

    private String[] levels;

    protected LoggingLevelsRule(String... levels) {
        this.levels = levels;
    }

    @Override
    public Statement apply(Statement base, Description description) {
        ClassLoader classLoader = getClassLoader(description.getTestClass());
        List<String> loggingLevelsClassNames = loadFactoryNames(LoggingLevelsStatement.class, classLoader);
        Statement targetStatement = base;
        for (String loggingLevelsClassName : loggingLevelsClassNames) {
            Class<?> loggingLevelsClass = resolveClass(loggingLevelsClassName, classLoader, true);
            if (loggingLevelsClass != null) {
                targetStatement = (Statement) newInstance(loggingLevelsClass, targetStatement, description, levels);
            }
        }
        return targetStatement;
    }

    public static LoggingLevelsRule levels(String... levels) {
        return new LoggingLevelsRule(levels);
    }
}