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

import io.microsphere.spring.web.util.WebSource;
import io.microsphere.spring.web.util.WebTarget;
import org.springframework.util.ConcurrentReferenceHashMap;
import org.springframework.util.ConcurrentReferenceHashMap.ReferenceType;
import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.util.StringUtils.isBlank;
import static org.springframework.util.ConcurrentReferenceHashMap.ReferenceType.WEAK;

/**
 * The default {@link IdempotentService} implementation based on {@link ConcurrentReferenceHashMap} with weak entity.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see IdempotentService
 * @see ConcurrentReferenceHashMap
 * @see ReferenceType#WEAK
 * @since 1.0.0
 */
public class DefaultIdempotentService implements IdempotentService {

    private final ConcurrentReferenceHashMap cache = new ConcurrentReferenceHashMap<>(256, 0.75f, 16, WEAK);

    @Override
    public String getToken(NativeWebRequest request, IdempotentAttributes attributes) {
        String tokenName = attributes.getTokenName();
        WebSource source = attributes.getSource();
        return source.getValue(request, tokenName);
    }

    @Override
    public boolean checkToken(NativeWebRequest request, IdempotentAttributes attributes, String token) {
        String key = generateTokenKey(attributes, token);
        return cache.containsKey(key);
    }

    @Override
    public boolean invalidate(NativeWebRequest request, IdempotentAttributes attributes, String token) {
        String key = generateTokenKey(attributes, token);
        return cache.remove(key, token);
    }

    @Override
    public boolean storeToken(NativeWebRequest request, IdempotentAttributes attributes, String newToken) {
        if (isBlank(newToken)) {
            return false;
        }
        String tokenName = attributes.getTokenName();
        WebTarget target = attributes.getTarget();
        String key = generateTokenKey(attributes, newToken);
        cache.put(key, newToken);
        target.writeValue(request, tokenName, newToken);
        return true;
    }

    @Override
    public void destroy() {
        this.cache.clear();
    }

    String generateTokenKey(IdempotentAttributes attributes, String token) {
        String tokenName = attributes.getTokenName();
        return generateTokenKey(tokenName, token);
    }

    String generateTokenKey(String tokenName, String token) {
        return tokenName + ":" + token;
    }
}
