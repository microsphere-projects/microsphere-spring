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

package io.microsphere.spring.webmvc.util;


import io.microsphere.spring.beans.factory.support.ListenableAutowireCandidateResolverInitializer;
import io.microsphere.spring.context.event.EventPublishingBeanInitializer;
import io.microsphere.spring.core.env.ListenableConfigurableEnvironmentInitializer;
import io.microsphere.spring.test.domain.User;
import io.microsphere.spring.test.web.controller.TestRestController;
import io.microsphere.spring.test.web.servlet.TestServlet;
import io.microsphere.spring.test.web.servlet.TestServletContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.INIT_PARAM_DELIMITERS;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.SUPPORTED_CONVERTER_TYPES;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.getClassNames;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.getHandlerMethodArguments;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.getHandlerMethodRequestBodyArgument;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.getHandlerMethodReturnValue;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.getHttpServletRequest;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.getWebApplicationContext;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.isControllerAdviceBeanType;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.isPageRenderRequest;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.setContextInitializerClassInitParameter;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.setFrameworkServletContextInitializerClassInitParameter;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.setGlobalInitializerClassInitParameter;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.setHandlerMethodRequestBodyArgument;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.setHandlerMethodReturnValue;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.setInitParameters;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.springframework.core.MethodParameter.forExecutable;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;
import static org.springframework.web.context.ContextLoader.CONTEXT_INITIALIZER_CLASSES_PARAM;
import static org.springframework.web.context.ContextLoader.GLOBAL_INITIALIZER_CLASSES_PARAM;
import static org.springframework.web.context.WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE;
import static org.springframework.web.context.request.RequestContextHolder.resetRequestAttributes;
import static org.springframework.web.context.request.RequestContextHolder.setRequestAttributes;

/**
 * {@link WebMvcUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebMvcUtils
 * @since 1.0.0
 */
@ControllerAdvice
public class WebMvcUtilsTest {

    private static final Class<? extends ApplicationContextInitializer>[] INITIALIZER_CLASSES = ofArray(
            ListenableConfigurableEnvironmentInitializer.class,
            ListenableAutowireCandidateResolverInitializer.class,
            EventPublishingBeanInitializer.class
    );

    private MockHttpServletRequest servletRequest;

    private RequestAttributes requestAttributes;

    private HandlerMethod handlerMethod;

    private Method method;

    private MethodParameter methodParameter;

    private User user;

    @Before
    public void setUp() throws NoSuchMethodException {
        this.servletRequest = new MockHttpServletRequest();
        this.requestAttributes = new ServletWebRequest(this.servletRequest);
        setRequestAttributes(this.requestAttributes);
        this.handlerMethod = new HandlerMethod(new TestRestController(), "user", User.class);
        this.method = this.handlerMethod.getMethod();
        this.methodParameter = forExecutable(this.method, 0);
        this.user = new User();
        this.user.setName("Mercy");
        this.user.setAge(18);
    }

    @After
    public void tearDown() {
        resetRequestAttributes();
    }

    @Test
    public void testConstants() {
        assertEquals(ofSet(MappingJackson2HttpMessageConverter.class, StringHttpMessageConverter.class), SUPPORTED_CONVERTER_TYPES);
        assertEquals(",; \t\n", INIT_PARAM_DELIMITERS);
    }

    @Test
    public void testGetHttpServletRequest() {
        assertSame(this.servletRequest, getHttpServletRequest());
    }

    @Test
    public void testGetHttpServletRequestWithNullRequestAttributes() {
        assertNull(getHttpServletRequest(null));
    }

    @Test
    public void testGetWebApplicationContext() {
        HttpServletRequest request = getHttpServletRequest();
        ServletContext servletContext = request.getServletContext();
        WebApplicationContext context = new GenericWebApplicationContext();
        servletContext.setAttribute(ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, context);
        assertSame(context, getWebApplicationContext());
    }

    @Test
    public void testGetWebApplicationContextWithoutHttpServletRequest() {
        resetRequestAttributes();
        assertThrows(IllegalStateException.class, WebMvcUtils::getWebApplicationContext);
    }

    @Test
    public void testGetWebApplicationContextWithoutWebApplicationContext() {
        assertNull(getWebApplicationContext());
    }

    @Test
    public void testSetHandlerMethodRequestBodyArgument() {
        setHandlerMethodRequestBodyArgument(this.method, this.user);
        assertEquals(this.user, getHandlerMethodRequestBodyArgument(this.method));
    }

    @Test
    public void testSetHandlerMethodRequestBodyArgumentWithHandlerMethod() throws NoSuchMethodException {
        setHandlerMethodRequestBodyArgument(this.handlerMethod.getMethod(), this.user);
        assertEquals(this.user, getHandlerMethodRequestBodyArgument(this.handlerMethod));
    }

    @Test
    public void testSetHandlerMethodRequestBodyArgumentWithNull() {
        setHandlerMethodRequestBodyArgument(null, this.user);
        setHandlerMethodRequestBodyArgument(this.method, null);
    }

    @Test
    public void testGetHandlerMethodArgumentsWithHandlerMethod() {
        assertHandlerMethodArguments(getHandlerMethodArguments(this.handlerMethod));
    }

    @Test
    public void testGetHandlerMethodArgumentsWithMethod() {
        assertHandlerMethodArguments(getHandlerMethodArguments(this.method));
    }

    @Test
    public void testSetHandlerMethodReturnValue() {
        setHandlerMethodReturnValue(this.method, this.user);
        assertEquals(this.user, getHandlerMethodReturnValue(this.method));
    }

    @Test
    public void testGetHandlerMethodReturnValueWithHandlerMethod() {
        setHandlerMethodReturnValue(getHttpServletRequest(), this.method, this.user);
        assertEquals(this.user, getHandlerMethodReturnValue(this.handlerMethod));
    }

    @Test
    public void testSetHandlerMethodReturnValueWithNull() {
        setHandlerMethodReturnValue(null, this.user);
        setHandlerMethodReturnValue(this.method, null);
    }

    @Test
    public void testIsControllerAdviceBeanType() {
        assertTrue(isControllerAdviceBeanType(WebMvcUtilsTest.class));
        assertFalse(isControllerAdviceBeanType(TestRestController.class));
    }

    @Test
    public void testSetInitParameters() {
        MockServletContext servletContext = new MockServletContext();
        String parameterName = "names";
        setInitParameters(servletContext, parameterName, "a", "b", "c");
        assertEquals("a,b,c", servletContext.getInitParameter(parameterName));

        setInitParameters(servletContext, parameterName, "");
        assertEquals("a,b,c", servletContext.getInitParameter(parameterName));
    }

    @Test
    public void testSetGlobalInitializerClassInitParameter() {
        MockServletContext servletContext = new MockServletContext();
        setGlobalInitializerClassInitParameter(servletContext, INITIALIZER_CLASSES);
        assertEquals(arrayToCommaDelimitedString(getClassNames(INITIALIZER_CLASSES)), servletContext.getInitParameter(GLOBAL_INITIALIZER_CLASSES_PARAM));
    }

    @Test
    public void testSetContextInitializerClassInitParameter() {
        MockServletContext servletContext = new MockServletContext();
        setContextInitializerClassInitParameter(servletContext, INITIALIZER_CLASSES);
        assertEquals(arrayToCommaDelimitedString(getClassNames(INITIALIZER_CLASSES)), servletContext.getInitParameter(CONTEXT_INITIALIZER_CLASSES_PARAM));
    }

    @Test
    public void testSetFrameworkServletContextInitializerClassInitParameter() {
        TestServletContext testServletContext = new TestServletContext();
        ServletRegistration servletRegistration = testServletContext.addServlet("dispatcherServlet", DispatcherServlet.class);

        setFrameworkServletContextInitializerClassInitParameter(testServletContext, INITIALIZER_CLASSES);
        assertEquals(arrayToCommaDelimitedString(getClassNames(INITIALIZER_CLASSES)), servletRegistration.getInitParameter(CONTEXT_INITIALIZER_CLASSES_PARAM));
    }

    @Test
    public void testSetFrameworkServletContextInitializerClassInitParameterWithoutServlet() {
        setFrameworkServletContextInitializerClassInitParameter(new MockServletContext(), INITIALIZER_CLASSES);
    }

    @Test
    public void testSetFrameworkServletContextInitializerClassInitParameterWithoutFrameworkServlet() {
        TestServletContext testServletContext = new TestServletContext();
        ServletRegistration servletRegistration = testServletContext.addServlet("testServlet", TestServlet.class);
        setFrameworkServletContextInitializerClassInitParameter(testServletContext, INITIALIZER_CLASSES);
        assertNull(servletRegistration.getInitParameter(CONTEXT_INITIALIZER_CLASSES_PARAM));
    }

    @Test
    public void testIsPageRenderRequest() {
        ModelAndView modelAndView = new ModelAndView();
        assertFalse(isPageRenderRequest(modelAndView));

        modelAndView.setViewName("test-view");
        assertTrue(isPageRenderRequest(modelAndView));
    }

    @Test
    public void testIsPageRenderRequestWithNull() {
        assertFalse(isPageRenderRequest(null));
    }
    
    void assertHandlerMethodArguments(Object[] arguments) {
        assertEquals(1, arguments.length);
    }
}