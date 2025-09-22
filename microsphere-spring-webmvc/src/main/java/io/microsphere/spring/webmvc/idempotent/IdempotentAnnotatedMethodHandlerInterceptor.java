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
package io.microsphere.spring.webmvc.idempotent;

import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import io.microsphere.spring.web.idempotent.DefaultIdempotentService;
import io.microsphere.spring.web.idempotent.Idempotent;
import io.microsphere.spring.web.idempotent.IdempotentAttributes;
import io.microsphere.spring.web.idempotent.IdempotentService;
import io.microsphere.spring.webmvc.interceptor.AnnotatedMethodHandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.HandlerMethod;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.BeanUtils.getOptionalBean;
import static io.microsphere.spring.web.idempotent.IdempotentAttributes.of;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * {@link AnnotatedMethodHandlerInterceptor} for {@link Idempotent} annotation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class IdempotentAnnotatedMethodHandlerInterceptor extends AnnotatedMethodHandlerInterceptor<Idempotent> implements
        EnvironmentAware, ApplicationListener<ContextRefreshedEvent>, DisposableBean {

    private static final Logger logger = getLogger(IdempotentAnnotatedMethodHandlerInterceptor.class);

    @Nonnull
    private IdempotentService idempotentService;

    private Environment environment;

    @Override
    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod,
                                Idempotent idempotent) throws Exception {
        NativeWebRequest nativeWebRequest = new ServletWebRequest(request);
        IdempotentAttributes attributes = of(idempotent, environment);
        idempotentService.validateToken(nativeWebRequest, attributes);
        return super.preHandle(request, response, handlerMethod, idempotent);
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        initIdempotentService(context);
    }

    @Override
    public void destroy() {
        this.idempotentService.destroy();
    }

    @Override
    protected Idempotent getAnnotation(HandlerMethod handlerMethod) {
        Idempotent idempotent = super.getAnnotation(handlerMethod);
        if (idempotent == null) {
            Class<?> beanType = handlerMethod.getBeanType();
            idempotent = findAnnotation(beanType, Idempotent.class);
        }
        return idempotent;
    }

    protected void initIdempotentService(ApplicationContext context) {
        IdempotentService idempotentService = getOptionalBean(context, IdempotentService.class);
        if (idempotentService == null) {
            logger.trace("The IdempotentService bean can't be found, DefaultIdempotentService will be used!");
            idempotentService = new DefaultIdempotentService();
        }
        this.idempotentService = idempotentService;
    }
}
