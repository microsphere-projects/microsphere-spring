package io.microsphere.spring.webmvc.util;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.spring.web.util.RequestAttributesUtils;
import io.microsphere.util.Utils;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.spring.web.servlet.util.WebUtils.findServletRegistrations;
import static io.microsphere.util.Assert.assertNotNull;
import static java.util.stream.Stream.of;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.web.context.ContextLoader.CONTEXT_INITIALIZER_CLASSES_PARAM;
import static org.springframework.web.context.ContextLoader.GLOBAL_INITIALIZER_CLASSES_PARAM;
import static org.springframework.web.context.request.RequestContextHolder.getRequestAttributes;
import static org.springframework.web.servlet.support.RequestContextUtils.findWebApplicationContext;

/**
 * Spring Web MVC Utilities Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class WebMvcUtils implements Utils {

    /**
     * The supported {@link HttpMessageConverter} types
     */
    public static final Set<Class<? extends HttpMessageConverter<?>>> SUPPORTED_CONVERTER_TYPES =
            ofSet(MappingJackson2HttpMessageConverter.class, StringHttpMessageConverter.class);

    /**
     * Any number of these characters are considered delimiters between
     * multiple values in a single init-param String value.
     *
     * @see ContextLoader#INIT_PARAM_DELIMITERS
     */
    public static final String INIT_PARAM_DELIMITERS = ",; \t\n";

    /**
     * Gets the current {@link HttpServletRequest} object
     * <p>
     * By default, {@link HttpServletRequest} is initialized in {@link RequestContextFilter}, {@link HttpServletRequest}
     * from the Servlet HTTP request thread {@link ThreadLocal} is obtained from {@link InheritableThreadLocal} and
     * can be obtained in the child thread.
     *
     * @return <code>null<code> returns the current {@link HttpServletRequest} object.
     */
    @Nullable
    public static HttpServletRequest getHttpServletRequest() throws IllegalStateException {
        RequestAttributes requestAttributes = getRequestAttributes();
        return getHttpServletRequest(requestAttributes);
    }

    @Nullable
    public static HttpServletRequest getHttpServletRequest(RequestAttributes requestAttributes) {
        HttpServletRequest request = null;
        if (requestAttributes instanceof ServletWebRequest) {
            request = ((ServletWebRequest) requestAttributes).getRequest();
        }
        return request;
    }

    /**
     * Gets the {@link WebApplicationContext} associated with the current Servlet Request request
     *
     * @return Current Servlet Request associated with {@link WebApplicationContext}
     * @throws IllegalStateException In a non-Web scenario, an exception is thrown
     */
    @Nonnull
    public static WebApplicationContext getWebApplicationContext() throws IllegalStateException {
        HttpServletRequest request = getHttpServletRequest();
        if (request == null) {
            throw new IllegalStateException("Use it in your Servlet Web application!");
        }
        ServletContext servletContext = request.getServletContext();
        return getWebApplicationContext(request, servletContext);
    }

    /**
     * Get the {@link WebApplicationContext} from {@link HttpServletRequest}
     *
     * @param request        {@link HttpServletRequest}
     * @param servletContext {@link ServletContext}
     * @return {@link WebApplicationContext}
     * @throws IllegalStateException if no servlet-specific context has been found
     * @see RequestContextUtils#getWebApplicationContext(HttpServletRequest)
     * @see RequestContextUtils#findWebApplicationContext(HttpServletRequest, ServletContext)
     * @see DispatcherServlet#WEB_APPLICATION_CONTEXT_ATTRIBUTE
     */
    @Nullable
    public static WebApplicationContext getWebApplicationContext(HttpServletRequest request, @Nullable ServletContext servletContext) {
        return findWebApplicationContext(request, servletContext);
    }

    /**
     * Set the {@link RequestBody @RequestBody} method parameter in {@link HandlerMethod} to the {@link HttpServletRequest} context
     *
     * @param method              Handler {@link Method}
     * @param requestBodyArgument {@link RequestBody @RequestBody} The method parameters
     */
    public static void setHandlerMethodRequestBodyArgument(Method method, Object requestBodyArgument) {
        setHandlerMethodRequestBodyArgument(getHttpServletRequest(), method, requestBodyArgument);
    }

    /**
     * Set the {@link RequestBody @RequestBody} method parameter in {@link HandlerMethod} to the {@link HttpServletRequest} context
     *
     * @param request             {@link HttpServletRequest}
     * @param method              Handler {@link Method}
     * @param requestBodyArgument {@link RequestBody @RequestBody} The method parameters
     */
    public static void setHandlerMethodRequestBodyArgument(HttpServletRequest request, Method method, Object requestBodyArgument) {
        RequestAttributesUtils.setHandlerMethodRequestBodyArgument(new ServletWebRequest(request), method, requestBodyArgument);
    }

    /**
     * Set the return value of the {@link HandlerMethod} to the {@link HttpServletRequest} context
     *
     * @param method      Handler {@link Method}
     * @param returnValue The return value
     */
    public static void setHandlerMethodReturnValue(Method method, Object returnValue) {
        setHandlerMethodReturnValue(getHttpServletRequest(), method, returnValue);
    }

    /**
     * Set the return value of the {@link HandlerMethod} to the {@link HttpServletRequest} context
     *
     * @param request     {@link HttpServletRequest}
     * @param method      Handler {@link Method}
     * @param returnValue The return value
     */
    public static void setHandlerMethodReturnValue(HttpServletRequest request, Method method, Object returnValue) {
        RequestAttributesUtils.setHandlerMethodReturnValue(new ServletWebRequest(request), method, returnValue);
    }

    /**
     * Gets the {@link RequestBody @RequestBody} method parameter from the {@link HttpServletRequest} context
     *
     * @param handlerMethod {@link HandlerMethod}
     * @param <T>           {@link RequestBody @RequestBody} Method parameter Types
     * @return {@link RequestBody @RequestBody} Method parameters if present, otherwise，<code>null</code>
     */
    @Nullable
    public static <T> T getHandlerMethodRequestBodyArgument(HandlerMethod handlerMethod) {
        return getHandlerMethodRequestBodyArgument(getHttpServletRequest(), handlerMethod);
    }

    /**
     * Gets the {@link RequestBody @RequestBody} method parameter from the {@link HttpServletRequest} context
     *
     * @param request       {@link HttpServletRequest}
     * @param handlerMethod {@link HandlerMethod}
     * @param <T>           {@link RequestBody @RequestBody} Method parameter Types
     * @return {@link RequestBody @RequestBody} Method parameters if present, otherwise，<code>null</code>
     */
    @Nullable
    public static <T> T getHandlerMethodRequestBodyArgument(HttpServletRequest request, HandlerMethod handlerMethod) {
        return getHandlerMethodRequestBodyArgument(request, handlerMethod.getMethod());
    }

    /**
     * Gets the {@link RequestBody @RequestBody} method parameter from the {@link HttpServletRequest} context
     *
     * @param method Handler {@link Method}
     * @param <T>    {@link RequestBody @RequestBody} Method parameter Types
     * @return {@link RequestBody @RequestBody} method parameter if present, otherwise <code>null<code>
     */
    @Nonnull
    public static <T> T getHandlerMethodRequestBodyArgument(Method method) {
        return getHandlerMethodRequestBodyArgument(getHttpServletRequest(), method);
    }

    /**
     * Gets the {@link RequestBody @RequestBody} method parameter from the {@link HttpServletRequest} context
     *
     * @param request {@link HttpServletRequest}
     * @param method  Handler {@link Method}
     * @param <T>     {@link RequestBody @RequestBody} Method parameter Types
     * @return {@link RequestBody @RequestBody} method parameter if present, otherwise <code>null<code>
     */
    @Nonnull
    public static <T> T getHandlerMethodRequestBodyArgument(HttpServletRequest request, Method method) {
        return RequestAttributesUtils.getHandlerMethodRequestBodyArgument(new ServletWebRequest(request), method);
    }

    /**
     * Gets the {@link HandlerMethod} method parameter
     *
     * @param handlerMethod {@link HandlerMethod}
     * @return non-null
     */
    @Nonnull
    public static Object[] getHandlerMethodArguments(HandlerMethod handlerMethod) {
        return getHandlerMethodArguments(getHttpServletRequest(), handlerMethod);
    }

    /**
     * Gets the {@link HandlerMethod} method parameter
     *
     * @param request       {@link HttpServletRequest}
     * @param handlerMethod {@link HandlerMethod}
     * @return non-null
     */
    @Nonnull
    public static Object[] getHandlerMethodArguments(HttpServletRequest request, HandlerMethod handlerMethod) {
        return getHandlerMethodArguments(request, handlerMethod.getMethod());
    }

    /**
     * Gets the {@link HandlerMethod} method parameter
     *
     * @param method {@link HandlerMethod}
     * @return non-null
     */
    @Nonnull
    public static Object[] getHandlerMethodArguments(Method method) {
        return getHandlerMethodArguments(getHttpServletRequest(), method);
    }

    /**
     * Gets the {@link HandlerMethod} method parameter
     *
     * @param request {@link HttpServletRequest}
     * @param method  {@link Method}
     * @return non-null
     */
    @Nonnull
    public static Object[] getHandlerMethodArguments(HttpServletRequest request, Method method) {
        return RequestAttributesUtils.getHandlerMethodArguments(new ServletWebRequest(request), method);
    }

    @Nullable
    public static <T> T getHandlerMethodReturnValue(HandlerMethod handlerMethod) {
        return getHandlerMethodReturnValue(getHttpServletRequest(), handlerMethod);
    }

    /**
     * Gets the value returned by the {@link HandlerMethod} method
     *
     * @param request       {@link HttpServletRequest}
     * @param handlerMethod {@link HandlerMethod}
     * @param <T>           Method return value type
     * @return {@link HandlerMethod} Method return value
     */
    @Nullable
    public static <T> T getHandlerMethodReturnValue(HttpServletRequest request, HandlerMethod handlerMethod) {
        return getHandlerMethodReturnValue(request, handlerMethod.getMethod());
    }

    /**
     * Gets the value returned by the {@link HandlerMethod} method
     *
     * @param method {@link Method}
     * @param <T>    Method return value type
     * @return {@link HandlerMethod} Method return value
     */
    @Nullable
    public static <T> T getHandlerMethodReturnValue(Method method) {
        return getHandlerMethodReturnValue(getHttpServletRequest(), method);
    }

    /**
     * Gets the value returned by the {@link HandlerMethod} method
     *
     * @param request {@link HttpServletRequest}
     * @param method  {@link Method}
     * @param <T>     Method return value type
     * @return {@link HandlerMethod} Method return value
     */
    @Nullable
    public static <T> T getHandlerMethodReturnValue(HttpServletRequest request, Method method) {
        return RequestAttributesUtils.getHandlerMethodReturnValue(new ServletWebRequest(request), method);
    }

    /**
     * Determine whether the Bean Type is present annotated by {@link ControllerAdvice}
     *
     * @param beanType Bean Type
     * @return If {@link ControllerAdvice} bean type is present , return <code>true</code> , or <code>false</code>.
     */
    public static boolean isControllerAdviceBeanType(Class<?> beanType) {
        return findAnnotation(beanType, ControllerAdvice.class) != null;
    }

    /**
     * Sets {@link ServletContext#setInitParameter(String, String) ServletContext Intialized Parameters}
     *
     * @param servletContext  {@link ServletContext}
     * @param parameterName   the name of init parameter
     * @param parameterValues the values of init parameters
     */
    public static void setInitParameters(ServletContext servletContext, String parameterName, String... parameterValues) {
        assertNotNull(servletContext, () -> "The argument 'servletContext' must not be null!");
        assertNotNull(parameterValues, () -> "The argument 'parameterValues' must not be null!");

        String newParameterValue = arrayToCommaDelimitedString(parameterValues);
        servletContext.setInitParameter(parameterName, newParameterValue);
    }

    /**
     * Sets the initialized parameter for {@link ApplicationContextInitializer Global Initializer Class}
     *
     * @param servletContext            {@link ServletContext}
     * @param contextInitializerClasses the classes of {@link ApplicationContextInitializer}
     * @see ContextLoader#GLOBAL_INITIALIZER_CLASSES_PARAM
     */
    public static void setGlobalInitializerClassInitParameter(ServletContext servletContext,
                                                              Class<? extends ApplicationContextInitializer>... contextInitializerClasses) {
        setInitParameters(servletContext, GLOBAL_INITIALIZER_CLASSES_PARAM, getClassNames(contextInitializerClasses));
    }

    /**
     * Sets the initialized parameter for {@link ApplicationContextInitializer Context Initializer Class}
     *
     * @param servletContext            {@link ServletContext}
     * @param contextInitializerClasses the classes of {@link ApplicationContextInitializer}
     * @see ContextLoader#CONTEXT_INITIALIZER_CLASSES_PARAM
     */
    public static void setContextInitializerClassInitParameter(ServletContext servletContext,
                                                               Class<? extends ApplicationContextInitializer>... contextInitializerClasses) {
        setInitParameters(servletContext, CONTEXT_INITIALIZER_CLASSES_PARAM, getClassNames(contextInitializerClasses));
    }

    /**
     * Sets initialized parameter for {@link ApplicationContextInitializer Context Initializer Class} into {@link
     * FrameworkServlet}
     *
     * @param servletContext            {@link ServletContext}
     * @param contextInitializerClasses the classes of {@link ApplicationContextInitializer}
     * @see FrameworkServlet#applyInitializers(ConfigurableApplicationContext)
     */
    public static void setFrameworkServletContextInitializerClassInitParameter(
            ServletContext servletContext,
            Class<? extends ApplicationContextInitializer>... contextInitializerClasses) {

        Collection<? extends ServletRegistration> servletRegistrations =
                findServletRegistrations(servletContext, FrameworkServlet.class).values();

        for (ServletRegistration servletRegistration : servletRegistrations) {
            String classNames = arrayToCommaDelimitedString(getClassNames(contextInitializerClasses));
            servletRegistration.setInitParameter(CONTEXT_INITIALIZER_CLASSES_PARAM, classNames);
        }
    }

    /**
     * Is page render request
     *
     * @param modelAndView {@link ModelAndView}
     * @return If current request is for page render , return <code>true</code> , or <code>false</code>.
     */
    public static boolean isPageRenderRequest(ModelAndView modelAndView) {
        if (modelAndView != null) {
            String viewName = modelAndView.getViewName();
            return hasText(viewName);
        }
        return false;
    }

    protected static String[] getClassNames(Class<?>... classes) {
        return of(classes).map(Class::getName).toArray(String[]::new);
    }

    private WebMvcUtils() {
    }
}
