package io.microsphere.spring.webmvc.method;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

import java.lang.reflect.Method;

/**
 * {@link HandlerMethod} Arguments Resolved Event
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class HandlerMethodArgumentsResolvedEvent extends ApplicationEvent {

    private final Method method;

    private final Object[] arguments;

    private final WebRequest webRequest;

    /**
     * @param resolver   {@link HandlerMethodArgumentResolver}
     * @param method     {@link Method}
     * @param arguments  {@link Object the argument object that was resolved}
     * @param webRequest {@link WebRequest}
     */
    public HandlerMethodArgumentsResolvedEvent(HandlerMethodArgumentResolver resolver, Method method, Object[] arguments, WebRequest webRequest) {
        super(resolver);
        this.method = method;
        this.arguments = arguments;
        this.webRequest = webRequest;
    }

    /**
     * @return {@link HandlerMethodArgumentResolver}, a.k.a {@link #getSource() the event source}
     * @see #getSource()
     */
    public HandlerMethodArgumentResolver getHandlerMethodArgumentResolver() {
        return (HandlerMethodArgumentResolver) getSource();
    }

    /**
     * @return the {@link Method} of {@link HandlerMethod} being invoking
     */
    public Method getMethod() {
        return method;
    }

    /**
     * @return the resolved arguments of {@link HandlerMethod} being invoking
     */
    public Object[] getArguments() {
        return arguments;
    }

    /**
     * @return {@link WebRequest The Web Request for HandlerMethod invocation}
     */
    public WebRequest getWebRequest() {
        return webRequest;
    }
}
