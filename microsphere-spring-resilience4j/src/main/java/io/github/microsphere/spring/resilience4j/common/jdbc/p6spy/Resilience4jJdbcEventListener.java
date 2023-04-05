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
package io.github.microsphere.spring.resilience4j.common.jdbc.p6spy;

import com.p6spy.engine.common.StatementInformation;
import com.p6spy.engine.event.JdbcEventListener;
import com.p6spy.engine.event.SimpleJdbcEventListener;
import io.github.microsphere.spring.resilience4j.common.Resilience4jContext;
import io.github.microsphere.spring.resilience4j.common.Resilience4jModule;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static io.github.microsphere.spring.resilience4j.common.Resilience4jModule.valueOf;
import static org.springframework.core.ResolvableType.forType;

/**
 * The abstract class based on P6Spy {@link JdbcEventListener} class for Resilience4j features.
 *
 * @param <E> the type of Resilience4j's entity, e.g., {@link CircuitBreaker}
 * @param <C> the type of Resilience4j's configuration, e.g., {@link CircuitBreakerConfig}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class Resilience4jJdbcEventListener<E, C> extends SimpleJdbcEventListener {

    protected final static int ENTRY_CLASS_GENERIC_INDEX = 0;

    protected final static int CONFIGURATION_CLASS_GENERIC_INDEX = 1;

    protected final static ThreadLocal<Map<Class<?>, Resilience4jContext<?>>> contextHolder = ThreadLocal.withInitial(HashMap::new);

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final Registry<E, C> registry;

    /**
     * Local Cache using {@link HashMap} with better performance
     */
    protected final Map<String, E> entryCaches;

    private final Class<E> entryClass;

    private final Class<C> configurationClass;

    private final Resilience4jModule module;

    public Resilience4jJdbcEventListener(Registry<E, C> registry) {
        Assert.notNull(registry, "The 'registry' argument can't be null");
        this.registry = registry;
        this.entryCaches = new HashMap<>();
        ResolvableType currentType = forType(getClass());
        ResolvableType superType = currentType.as(getClass());
        this.entryClass = (Class<E>) superType.getGeneric(ENTRY_CLASS_GENERIC_INDEX).resolve();
        this.configurationClass = (Class<C>) superType.getGeneric(CONFIGURATION_CLASS_GENERIC_INDEX).resolve();
        this.module = valueOf(this.entryClass);
    }

    @Override
    public void onBeforeAnyExecute(StatementInformation statementInformation) {
        Resilience4jContext<E> context = getContext(statementInformation);
    }

    @Override
    public void onAfterAnyExecute(StatementInformation statementInformation, long timeElapsedNanos, SQLException e) {
        Resilience4jContext<E> context = getContext(statementInformation);
    }

    protected final Resilience4jContext<E> getContext(StatementInformation statementInformation) {
        Class<E> entryClass = this.entryClass;
        Map<Class<?>, Resilience4jContext<?>> contextMap = contextHolder.get();
        Resilience4jContext<E> context = (Resilience4jContext<E>) contextMap.get(entryClass);
        if (context == null) {
            String name = getEntryName(statementInformation);
            E entry = getEntry(name);
            context = new Resilience4jContext<>(name, entry, module);
            contextMap.put(entryClass, context);
        }
        return context;
    }

    protected abstract E getEntry(String name);

    protected String getEntryName(StatementInformation statementInformation) {
        String sql = statementInformation.getSql();
        return "jdbc:p6spy:" + module.name() + getEntryNameSuffix(sql);
    }

    private String getEntryNameSuffix(String sql) {
        // SELECT ... FROM {table_name}
        // INSERT ... INTO {table_name}
        // UPDATE {table_name} SET {column_name} = ?
        // DELETE FROM {table_name}
        return null;
    }

    public Registry<E, C> getRegistry() {
        return registry;
    }

    public Map<String, E> getEntryCaches() {
        return entryCaches;
    }

    public Class<E> getEntryClass() {
        return entryClass;
    }

    public Class<C> getConfigurationClass() {
        return configurationClass;
    }

    public Resilience4jModule getModule() {
        return module;
    }
}
