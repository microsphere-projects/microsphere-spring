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
package io.microsphere.spring.config.env.support;

import io.microsphere.spring.config.context.annotation.ResourcePropertySource;
import io.microsphere.util.CharSequenceComparator;
import org.springframework.core.io.Resource;

import java.util.Comparator;

/**
 * The default {@link Comparator} for {@link Resource} comparing {@link Resource#getFilename()}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySource#resourceComparator()
 * @since 1.0.0
 */
public class DefaultResourceComparator implements Comparator<Resource> {

    public static final DefaultResourceComparator INSTANCE = new DefaultResourceComparator();

    @Override
    public int compare(Resource r1, Resource r2) {
        String fileName1 = r1.getFilename();
        String fileName2 = r2.getFilename();
        return CharSequenceComparator.INSTANCE.compare(fileName1, fileName2);
    }
}
