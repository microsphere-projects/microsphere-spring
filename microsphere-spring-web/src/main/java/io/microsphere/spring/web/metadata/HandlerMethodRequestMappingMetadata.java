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
package io.microsphere.spring.web.metadata;

import io.microsphere.spring.web.rule.MediaTypeExpression;
import io.microsphere.spring.web.rule.NameValueExpression;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;

/**
 * The {@link HandlerMethod HandlerMethods'} {@link RequestMappingMetadata} sources from the annotated
 * {@link RequestMapping @RequestMapping}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethod
 * @see RequestMappingMetadata
 * @see RequestMapping
 * @since 1.0.0
 */
public class HandlerMethodRequestMappingMetadata extends RequestMappingMetadata {

    private final HandlerMethod handlerMethod;

    public HandlerMethodRequestMappingMetadata(HandlerMethod handlerMethod,
                                               String[] patterns,
                                               RequestMethod[] methods,
                                               NameValueExpression<String>[] params,
                                               NameValueExpression<String>[] headers,
                                               MediaTypeExpression[] consumes,
                                               MediaTypeExpression[] produces) {
        super(patterns, methods, params, headers, consumes, produces);
        this.handlerMethod = handlerMethod;
    }

    public HandlerMethod getHandlerMethod() {
        return handlerMethod;
    }
}
