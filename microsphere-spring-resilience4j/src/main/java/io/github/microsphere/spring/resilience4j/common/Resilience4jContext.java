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
package io.github.microsphere.spring.resilience4j.common;

import io.vavr.CheckedConsumer;
import io.vavr.CheckedFunction0;
import io.vavr.CheckedFunction1;
import io.vavr.CheckedFunction2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * The Resilience4j Context
 *
 * @param <E> The entry type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class Resilience4jContext<E> {

    private static final Logger logger = LoggerFactory.getLogger(Resilience4jContext.class);

    private final String name;

    private final E entry;

    private final Resilience4jModule module;

    private long startTime;

    public Resilience4jContext(String name, E entry, Resilience4jModule module) {
        this.name = name;
        this.entry = entry;
        this.module = module;
    }

    public void start(CheckedConsumer<E> entryStart) throws Throwable {
        start(e -> {
            entryStart.accept(e);
            return null;
        });
    }

    public void start(CheckedConsumer<E> entryStart, Consumer<Throwable> failureHandle) {
        start(e -> {
            entryStart.accept(e);
            return null;
        }, failureHandle);
    }

    public <R> R start(CheckedFunction1<E, R> entryStart) throws Throwable {
        R result = entryStart.apply(this.entry);
        started();
        return result;
    }

    public <R> R start(CheckedFunction1<E, R> entryStart, Consumer<Throwable> handler) {
        R result = handle(() -> entryStart.apply(this.entry), handler);
        started();
        return result;
    }

    private void started() {
        startTime = System.nanoTime();
        logger.debug("Resilience4j {}[name : '{}'] is starting...", module.name(), name);
    }

    public void end(BiConsumer<E, Long> entryEnd) {
        end(entryEnd, f -> {
            logger.warn("It's failed to end Resilience4j {}[name : '{}']", module.name(), name);
        });
    }

    public void end(BiConsumer<E, Long> entryEnd, Consumer<Throwable> failureHandler) {
        end((e, d) -> {
            entryEnd.accept(e, d);
            return null;
        }, (r, t) -> {
            failureHandler.accept(t);
        });
    }

    public <R> void end(CheckedFunction2<E, Long, R> entryEnd, BiConsumer<R, Throwable> handler) {
        long duration = System.nanoTime() - startTime;
        handle(() -> entryEnd.apply(entry, duration), handler);
    }

    public <R> R end(CheckedFunction2<E, Long, R> entryEnd, Consumer<Throwable> handler) {
        long duration = System.nanoTime() - startTime;
        return handle(() -> entryEnd.apply(entry, duration), handler);
    }

    public <R> R handle(CheckedFunction0<R> function, Consumer<Throwable> failureHandler) {
        return handle(function, (r, f) -> {
            failureHandler.accept(f);
            return r;
        });
    }

    public <R, T> void handle(CheckedFunction0<R> function, BiConsumer<R, Throwable> handler) {
        handle(function, (r, t) -> {
            handler.accept(r, t);
            return null;
        });
    }

    public <R, T> T handle(CheckedFunction0<R> function, BiFunction<R, Throwable, T> handler) {
        T target = null;
        try {
            R result = function.apply();
            target = handler.apply(result, null);
        } catch (Throwable failure) {
            target = handler.apply(null, failure);
        }
        return target;
    }

    public String getName() {
        return name;
    }

    public E getEntry() {
        return entry;
    }
}
