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

package io.microsphere.spring.web.idempotent;

import io.microsphere.spring.web.util.RequestValueSource;
import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.spring.web.util.WebScope.SESSION;

/**
 * The default {@link IdempotentService} implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see IdempotentService
 * @since 1.0.0
 */
public class DefaultIdempotentRepository implements IdempotentService {

    @Override
    public String getToken(NativeWebRequest request, Idempotent idempotent) {
        String tokenName = idempotent.tokenName();
        RequestValueSource source = idempotent.source();
        return source.getValue(request, tokenName);
    }

    @Override
    public String loadToken(NativeWebRequest request, Idempotent idempotent) {
        String tokenName = idempotent.tokenName();
        return SESSION.getAttribute(request, tokenName);
    }

    @Override
    public boolean storeToken(NativeWebRequest request, Idempotent idempotent) {
        return false;
    }
}
