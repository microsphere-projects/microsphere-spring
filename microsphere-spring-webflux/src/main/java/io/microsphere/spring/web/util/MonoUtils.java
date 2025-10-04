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

package io.microsphere.spring.web.util;

import io.microsphere.annotation.Nullable;
import reactor.core.publisher.Mono;

import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static reactor.core.scheduler.Schedulers.isInNonBlockingThread;

/**
 * The utility class for {@link Mono}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Mono
 * @since 1.0.0
 */
public abstract class MonoUtils {

    /**
     * Get the emitted value from {@link Mono}
     *
     * @param mono {@link Mono}
     * @param <T>  the type of value
     * @return the emitted value
     */
    @Nullable
    public static <T> T getValue(Mono<T> mono) {
        if (isInNonBlockingThread()) {
            return execute(() -> mono.toFuture().get());
        } else {
            return mono.block();
        }
    }

    private MonoUtils() {
    }
}
