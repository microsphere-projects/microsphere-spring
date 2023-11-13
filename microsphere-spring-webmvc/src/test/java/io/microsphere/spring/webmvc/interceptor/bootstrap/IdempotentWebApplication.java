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
package io.microsphere.spring.webmvc.interceptor.bootstrap;

import io.microsphere.spring.web.event.HandlerMethodArgumentsResolvedEvent;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension;
import io.microsphere.spring.webmvc.annotation.Idempotent;
import io.microsphere.spring.webmvc.interceptor.IdempotentAnnotatedMethodHandlerInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Idempotent Web Application
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@EnableAutoConfiguration
@EnableWebMvcExtension(registerHandlerInterceptors = {
        IdempotentAnnotatedMethodHandlerInterceptor.class
})
@Import(value = {
        DemoController.class
})
public class IdempotentWebApplication {

    private static final Logger logger = LoggerFactory.getLogger(IdempotentWebApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(IdempotentWebApplication.class, args);
    }

    @EventListener(WebEndpointMappingsReadyEvent.class)
    public void onEvent(WebEndpointMappingsReadyEvent event) {
        logger.info(event.toString());
    }

    @EventListener(HandlerMethodArgumentsResolvedEvent.class)
    public void onEvent(HandlerMethodArgumentsResolvedEvent event) {
        logger.info(event.toString());
    }

}

@RestController
class DemoController {

    @GetMapping("/echo/{message}")
    @Idempotent
    public String echo(@PathVariable String message) {
        return "[ECHO] : " + message;
    }
}
