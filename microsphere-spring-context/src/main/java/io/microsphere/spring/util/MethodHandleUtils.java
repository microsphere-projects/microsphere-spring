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
package io.microsphere.spring.util;

import io.microsphere.annotation.Experimental;
import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;
import io.microsphere.util.Utils;

import java.lang.invoke.MethodHandle;

import static io.microsphere.util.ArrayUtils.arrayToString;

/**
 * The utilities class for {@link MethodHandle}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MethodHandle
 * @since 1.0.0
 */
@Experimental(description = "Current class will be merged into microsphere-java-core")
public abstract class MethodHandleUtils implements Utils {

    private static final Logger logger = LoggerFactory.getLogger(MethodHandleUtils.class);

    public static void handleInvokeExactFailure(Throwable e, MethodHandle methodHandle, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn("Failed to invokeExact on the {} with arguments : {}", methodHandle, arrayToString(args), e);
        }
    }

    private MethodHandleUtils() {
    }
}
