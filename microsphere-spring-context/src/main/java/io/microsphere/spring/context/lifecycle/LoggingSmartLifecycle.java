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
package io.microsphere.spring.context.lifecycle;

import io.microsphere.logging.Logger;
import org.springframework.context.SmartLifecycle;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * {@link SmartLifecycle} for Logging
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractSmartLifecycle
 * @see SmartLifecycle
 * @since 1.0.0
 */
public class LoggingSmartLifecycle extends AbstractSmartLifecycle {

    private static final Logger logger = getLogger(LoggingSmartLifecycle.class);

    @Override
    protected void doStart() {
        logger.info("doStart()...");
    }

    @Override
    protected void doStop() {
        logger.info("doStop()...");
    }
}
