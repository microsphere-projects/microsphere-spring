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

package io.microsphere.spring.core.convert;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.util.Utils;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import java.lang.invoke.MethodHandle;

import static io.microsphere.invoke.MethodHandlesLookupUtils.findPublicStatic;
import static io.microsphere.spring.util.MethodHandleUtils.handleInvokeExactFailure;

/**
 * The utilities class for {@link ConversionService}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConversionService
 * @since 1.0.0
 */
public abstract class ConversionServiceUtils implements Utils {

    @Nullable
    private static volatile DefaultConversionService sharedInstance;

    /**
     * The {@link MethodHandle} for {@link DefaultConversionService#getSharedInstance()}
     *
     * @see DefaultConversionService#getSharedInstance()
     * @since Spring Framework 4.3.5
     */
    private static final MethodHandle getSharedInstanceMethodHandle = findPublicStatic(DefaultConversionService.class, "getSharedInstance");

    /**
     * Get the shared {@link ConversionService}
     *
     * @return the shard {@link ConversionService}
     * @see DefaultConversionService#getSharedInstance()
     */
    @Nonnull
    public static ConversionService getSharedInstance() {
        MethodHandle methodHandle = getSharedInstanceMethodHandle;
        if (methodHandle != null) {
            try {
                return (ConversionService) methodHandle.invoke();
            } catch (Throwable e) {
                handleInvokeExactFailure(e, methodHandle);
            }
        }
        return doGetSharedInstance();
    }

    /**
     * Fork the source code from {@link DefaultConversionService#getSharedInstance()}
     *
     * @return the instance of {@link DefaultConversionService}
     */
    @Nonnull
    protected static ConversionService doGetSharedInstance() {
        DefaultConversionService cs = sharedInstance;
        if (cs == null) {
            synchronized (ConversionServiceUtils.class) {
                cs = sharedInstance;
                if (cs == null) {
                    cs = new DefaultConversionService();
                    sharedInstance = cs;
                }
            }
        }
        return cs;
    }

    private ConversionServiceUtils() {
    }
}
