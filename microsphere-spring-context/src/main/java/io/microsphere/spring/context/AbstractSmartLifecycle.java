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
package io.microsphere.spring.context;

import org.springframework.context.SmartLifecycle;

/**
 * The abstract class for {@link SmartLifecycle}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class AbstractSmartLifecycle implements SmartLifecycle {

    private volatile boolean started = false;

    private int phase = DEFAULT_PHASE;

    @Override
    public final void start() {
        doStart();
        started = true;
    }

    protected void doStart() {
    }

    @Override
    public final void stop() {
        doStop();
        started = false;
    }

    protected void doStop() {
    }

    @Override
    public final boolean isRunning() {
        return started;
    }

    @Override
    public boolean isAutoStartup() {
        return true;
    }

    @Override
    public void stop(Runnable callback) {
        stop();
        callback.run();
    }

    @Override
    public int getPhase() {
        return phase;
    }

    public boolean isStarted() {
        return started;
    }

    public void setPhase(int phase) {
        this.phase = phase;
    }

    protected void setStarted(boolean started) {
        this.started = started;
    }
}
