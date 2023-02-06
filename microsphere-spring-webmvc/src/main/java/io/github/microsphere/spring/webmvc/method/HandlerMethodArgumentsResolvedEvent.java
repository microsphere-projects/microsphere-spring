package io.github.microsphere.spring.webmvc.method;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;

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
     * Create a new ApplicationEvent.
     *
     * @param method
     * @param arguments
     * @param webRequest
     */
    public HandlerMethodArgumentsResolvedEvent(Method method, Object[] arguments, WebRequest webRequest) {
        super(method);
        this.method = method;
        this.arguments = arguments;
        this.webRequest = webRequest;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public WebRequest getWebRequest() {
        return webRequest;
    }
}
