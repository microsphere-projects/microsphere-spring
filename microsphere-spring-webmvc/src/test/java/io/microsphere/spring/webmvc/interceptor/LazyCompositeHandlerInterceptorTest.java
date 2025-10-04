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

package io.microsphere.spring.webmvc.interceptor;


import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor.BEAN_NAME;
import static io.microsphere.util.ExceptionUtils.create;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * {@link LazyCompositeHandlerInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LazyCompositeHandlerInterceptor
 * @since 1.0.0
 */
public class LazyCompositeHandlerInterceptorTest extends AbstractHandlerInterceptorTest implements HandlerInterceptor {

    private static final String exceptionTypeAttributeName = "_exception_type_";

    @Override
    protected HandlerInterceptor createHandlerInterceptor() {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(LoggingMethodHandlerInterceptor.class);
        context.register(LazyCompositeHandlerInterceptorTest.class);
        registerBeanDefinition(context, BEAN_NAME, LazyCompositeHandlerInterceptor.class, HandlerInterceptor.class);
        context.refresh();
        LazyCompositeHandlerInterceptor interceptor = context.getBean(LazyCompositeHandlerInterceptor.class);
        return interceptor;
    }

    @Test
    public void testPreHandleOnReturnFalse() throws Exception {
        this.testPreHandle();
    }

    @Test
    public void testPreHandleOnException() throws Exception {
        this.request.setAttribute(exceptionTypeAttributeName, Exception.class);
        assertThrows(Exception.class, this::testPreHandle);

        this.request.setAttribute(exceptionTypeAttributeName, Throwable.class);
        assertThrows(RuntimeException.class, this::testPreHandle);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request == null) {
            return false;
        }
        Class<? extends Throwable> exceptionType = (Class<? extends Throwable>) request.getAttribute(exceptionTypeAttributeName);
        if (exceptionType == null) {
            return true;
        }
        if (Exception.class.isAssignableFrom(exceptionType)) { // Exception
            throw create((Class<? extends Exception>) exceptionType, "For testing");
        }

        // StackOverflowError
        return preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

    @Override
    protected void testPreHandle(Object handler) throws Exception {
        assertTrue(this.interceptor.preHandle(this.request, this.response, handler));
        assertFalse(this.interceptor.preHandle(null, this.response, handler));
    }
}