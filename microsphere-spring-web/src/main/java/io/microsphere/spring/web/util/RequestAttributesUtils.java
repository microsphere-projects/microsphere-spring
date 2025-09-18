package io.microsphere.spring.web.util;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;

import static io.microsphere.spring.web.util.WebScope.REQUEST;
import static org.springframework.web.context.request.RequestContextHolder.getRequestAttributes;

/**
 * {@link RequestAttributes} Utilities Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestAttributesUtils
 * @since 1.0.0
 */
public abstract class RequestAttributesUtils {

    /**
     * The prefix of the attribute name of the {@link HandlerMethod} method parameter
     */
    public static final String HANDLER_METHOD_ARGUMENTS_ATTRIBUTE_NAME_PREFIX = "HM.ARGS:";

    /**
     * The prefix of the attribute name of the {@link HandlerMethod} method parameter annotated {@link ResponseBody}
     */
    public static final String HANDLER_METHOD_REQUEST_BODY_ARGUMENT_ATTRIBUTE_NAME_PREFIX = "HM.RB.ARG:";

    /**
     * The prefix of the attribute name of the {@link HandlerMethod} method return value
     */
    public static final String HANDLER_METHOD_RETURN_VALUE_ATTRIBUTE_NAME_PREFIX = "HM.RV:";

    /**
     * Set an attribute to the {@link RequestAttributes} with the specified name and value.
     *
     * @param requestAttributes the {@link RequestAttributes} to set the attribute
     * @param attributeName     the name of the attribute to set
     * @param attributeValue    the value of the attribute to set
     */
    public static void setRequestAttribute(@Nonnull RequestAttributes requestAttributes, @Nullable String attributeName,
                                           @Nullable Object attributeValue) {
        REQUEST.setAttribute(requestAttributes, attributeName, attributeValue);
    }

    /**
     * Get an attribute from the {@link RequestAttributes} with the specified name.
     *
     * @param requestAttributes the {@link RequestAttributes} to get the attribute from
     * @param attributeName     the name of the attribute to get
     * @param <T>               the type of the attribute
     * @return the attribute value, or {@code null} if not found
     * @throws IllegalArgumentException if the {@link RequestAttributes} or attributeName is null
     */
    @Nullable
    public static <T> T getRequestAttribute(@Nonnull RequestAttributes requestAttributes, String attributeName) {
        return REQUEST.getAttribute(requestAttributes, attributeName);
    }

    /**
     * Set the {@link RequestBody @RequestBody} method parameter in {@link HandlerMethod} to the {@link RequestAttributes}
     *
     * @param method              Handler {@link Method}
     * @param requestBodyArgument {@link RequestBody @RequestBody} The method parameters
     */
    public static void setHandlerMethodRequestBodyArgument(Method method, Object requestBodyArgument) {
        setHandlerMethodRequestBodyArgument(getRequestAttributes(), method, requestBodyArgument);
    }

    /**
     * Set the {@link RequestBody @RequestBody} method parameter in {@link HandlerMethod} to the {@link RequestAttributes}
     *
     * @param requestAttributes   {@link RequestAttributes}
     * @param method              Handler {@link Method}
     * @param requestBodyArgument {@link RequestBody @RequestBody} The method parameters
     */
    public static void setHandlerMethodRequestBodyArgument(RequestAttributes requestAttributes, Method method, Object requestBodyArgument) {
        if (requestAttributes != null && method != null && requestBodyArgument != null) {
            String attributeName = getHandlerMethodRequestBodyArgumentAttributeName(method);
            setRequestAttribute(requestAttributes, attributeName, requestBodyArgument);
        }
    }

    /**
     * Gets the {@link RequestBody @RequestBody} method parameter from the {@link RequestContextHolder#getRequestAttributes()}
     *
     * @param handlerMethod {@link HandlerMethod}
     * @param <T>           {@link RequestBody @RequestBody} Method parameter Types
     * @return {@link RequestBody @RequestBody} Method parameters if present, otherwise，<code>null</code>
     */
    @Nullable
    public static <T> T getHandlerMethodRequestBodyArgument(HandlerMethod handlerMethod) {
        return getHandlerMethodRequestBodyArgument(getRequestAttributes(), handlerMethod);
    }

    /**
     * Gets the {@link RequestBody @RequestBody} method parameter from the {@link RequestAttributes}
     *
     * @param requestAttributes {@link RequestAttributes}
     * @param handlerMethod     {@link HandlerMethod}
     * @param <T>               {@link RequestBody @RequestBody} Method parameter Types
     * @return {@link RequestBody @RequestBody} Method parameters if present, otherwise，<code>null</code>
     */
    @Nullable
    public static <T> T getHandlerMethodRequestBodyArgument(RequestAttributes requestAttributes, HandlerMethod handlerMethod) {
        return getHandlerMethodRequestBodyArgument(requestAttributes, handlerMethod.getMethod());
    }

    /**
     * Gets the {@link RequestBody @RequestBody} method parameter from the {@link RequestContextHolder#getRequestAttributes()}
     *
     * @param method Handler {@link Method}
     * @param <T>    {@link RequestBody @RequestBody} Method parameter Types
     * @return {@link RequestBody @RequestBody} method parameter if present, otherwise <code>null<code>
     */
    @Nullable
    public static <T> T getHandlerMethodRequestBodyArgument(Method method) {
        return getHandlerMethodRequestBodyArgument(getRequestAttributes(), method);
    }

    /**
     * Gets the {@link RequestBody @RequestBody} method parameter from the {@link RequestAttributes}
     *
     * @param requestAttributes {@link RequestAttributes}
     * @param method            Handler {@link Method}
     * @param <T>               {@link RequestBody @RequestBody} Method parameter Types
     * @return {@link RequestBody @RequestBody} method parameter if present, otherwise <code>null<code>
     */
    @Nullable
    public static <T> T getHandlerMethodRequestBodyArgument(RequestAttributes requestAttributes, Method method) {
        String attributeName = getHandlerMethodRequestBodyArgumentAttributeName(method);
        return getRequestAttribute(requestAttributes, attributeName);
    }

    /**
     * Gets the {@link HandlerMethod} method parameter from the {@link RequestContextHolder#getRequestAttributes()}
     *
     * @param handlerMethod {@link HandlerMethod}
     * @return non-null
     */
    @Nonnull
    public static Object[] getHandlerMethodArguments(HandlerMethod handlerMethod) {
        return getHandlerMethodArguments(getRequestAttributes(), handlerMethod);
    }

    /**
     * Gets the {@link HandlerMethod} method parameter
     *
     * @param requestAttributes {@link RequestAttributes}
     * @param handlerMethod     {@link HandlerMethod}
     * @return non-null
     */
    @Nonnull
    public static Object[] getHandlerMethodArguments(RequestAttributes requestAttributes, HandlerMethod handlerMethod) {
        return getHandlerMethodArguments(requestAttributes, handlerMethod.getMethod());
    }

    /**
     * Gets the {@link HandlerMethod} method parameter from the {@link RequestContextHolder#getRequestAttributes()}
     *
     * @param parameter {@link MethodParameter}
     * @return non-null
     */
    @Nonnull
    public static Object[] getHandlerMethodArguments(MethodParameter parameter) {
        return getHandlerMethodArguments(getRequestAttributes(), parameter);
    }

    /**
     * Gets the {@link HandlerMethod} method parameter from the {@link RequestContextHolder#getRequestAttributes()}
     *
     * @param requestAttributes {@link RequestAttributes}
     * @param parameter         {@link MethodParameter}
     * @return non-null
     */
    @Nonnull
    public static Object[] getHandlerMethodArguments(RequestAttributes requestAttributes, MethodParameter parameter) {
        return getHandlerMethodArguments(requestAttributes, parameter.getMethod());
    }

    /**
     * Gets the {@link HandlerMethod} method parameter from the {@link RequestContextHolder#getRequestAttributes()}
     *
     * @param method {@link Method}
     * @return non-null
     */
    @Nonnull
    public static Object[] getHandlerMethodArguments(Method method) {
        return getHandlerMethodArguments(getRequestAttributes(), method);
    }

    /**
     * Gets the {@link HandlerMethod} method parameter
     *
     * @param requestAttributes {@link RequestAttributes}
     * @param method            {@link Method}
     * @return non-null
     */
    @Nonnull
    public static Object[] getHandlerMethodArguments(RequestAttributes requestAttributes, Method method) {
        String attributeName = getHandlerMethodArgumentsAttributeName(method);
        Object[] arguments = getRequestAttribute(requestAttributes, attributeName);
        if (arguments == null) {
            arguments = new Object[method.getParameterCount()];
            setRequestAttribute(requestAttributes, attributeName, arguments);
        }
        return arguments;
    }


    /**
     * Set the {@link HandlerMethod} method return value to the {@link RequestContextHolder#getRequestAttributes()}
     *
     * @param method      {@link Method}
     * @param returnValue Method return value
     */
    public static void setHandlerMethodReturnValue(Method method, Object returnValue) {
        setHandlerMethodReturnValue(getRequestAttributes(), method, returnValue);
    }

    /**
     * Set the {@link HandlerMethod} method return value to the {@link RequestAttributes}
     *
     * @param requestAttributes {@link RequestAttributes}
     * @param method            {@link Method}
     * @param returnValue       Method return value
     */
    public static void setHandlerMethodReturnValue(RequestAttributes requestAttributes, Method method, Object returnValue) {
        if (requestAttributes != null && method != null && returnValue != null) {
            String attributeName = getHandlerMethodReturnValueAttributeName(method);
            setRequestAttribute(requestAttributes, attributeName, returnValue);
        }
    }

    /**
     * Gets the value returned by the {@link HandlerMethod} method from the {@link RequestContextHolder#getRequestAttributes()}
     *
     * @param handlerMethod {@link HandlerMethod}
     * @param <T>           Method return value type
     * @return {@link HandlerMethod} Method return value
     */
    @Nullable
    public static <T> T getHandlerMethodReturnValue(HandlerMethod handlerMethod) {
        return getHandlerMethodReturnValue(getRequestAttributes(), handlerMethod);
    }

    /**
     * Gets the value returned by the {@link HandlerMethod} method
     *
     * @param requestAttributes {@link RequestAttributes}
     * @param handlerMethod     {@link HandlerMethod}
     * @param <T>               Method return value type
     * @return {@link HandlerMethod} Method return value
     */
    @Nullable
    public static <T> T getHandlerMethodReturnValue(RequestAttributes requestAttributes, HandlerMethod handlerMethod) {
        return getHandlerMethodReturnValue(requestAttributes, handlerMethod.getMethod());
    }

    /**
     * Gets the value returned by the {@link HandlerMethod} method from the {@link RequestContextHolder#getRequestAttributes()}
     *
     * @param method {@link Method}
     * @param <T>    Method return value type
     * @return {@link HandlerMethod} Method return value
     */
    @Nullable
    public static <T> T getHandlerMethodReturnValue(Method method) {
        return getHandlerMethodReturnValue(getRequestAttributes(), method);
    }

    /**
     * Gets the value returned by the {@link HandlerMethod} method
     *
     * @param requestAttributes {@link RequestAttributes}
     * @param method            {@link Method}
     * @param <T>               Method return value type
     * @return {@link HandlerMethod} Method return value
     */
    @Nullable
    public static <T> T getHandlerMethodReturnValue(RequestAttributes requestAttributes, Method method) {
        String attributeName = getHandlerMethodReturnValueAttributeName(method);
        return getRequestAttribute(requestAttributes, attributeName);
    }

    static String getHandlerMethodRequestBodyArgumentAttributeName(Method method) {
        return HANDLER_METHOD_REQUEST_BODY_ARGUMENT_ATTRIBUTE_NAME_PREFIX + getMethodInfo(method);
    }

    static String getHandlerMethodReturnValueAttributeName(Method method) {
        return HANDLER_METHOD_RETURN_VALUE_ATTRIBUTE_NAME_PREFIX + getMethodInfo(method);
    }

    static String getHandlerMethodArgumentsAttributeName(Method method) {
        return HANDLER_METHOD_ARGUMENTS_ATTRIBUTE_NAME_PREFIX + getMethodInfo(method);
    }

    static String getMethodInfo(Method method) {
        return String.valueOf(method);
    }

    private RequestAttributesUtils() {
    }
}