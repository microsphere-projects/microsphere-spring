package io.microsphere.spring.webmvc.method;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;

/**
 * {@link HandlerMethod} Argument Resolved Event
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class HandlerMethodArgumentResolvedEvent extends ApplicationEvent {

    private final MethodParameter methodParameter;

    private final Object argument;

    private final WebRequest webRequest;

    /**
     * @param resolver        {@link HandlerMethodArgumentResolver}
     * @param methodParameter {@link MethodParameter}
     * @param argument        {@link Object the argument object that was resolved}
     * @param webRequest      {@link WebRequest}
     */
    public HandlerMethodArgumentResolvedEvent(HandlerMethodArgumentResolver resolver, MethodParameter methodParameter, Object argument, WebRequest webRequest) {
        super(resolver);
        this.methodParameter = methodParameter;
        this.argument = argument;
        this.webRequest = webRequest;
    }

    /**
     * @return {@link HandlerMethodArgumentResolver}
     * @see #getSource()
     */
    public HandlerMethodArgumentResolver getHandlerMethodArgumentResolver() {
        return (HandlerMethodArgumentResolver) getSource();
    }

    /**
     * @return {@link MethodParameter}
     */
    public MethodParameter getMethodParameter() {
        return methodParameter;
    }

    /**
     * @return the argument object that was resolved
     */
    public Object getArgument() {
        return argument;
    }

    /**
     * @return {@link WebRequest The Web Request for HandlerMethod invocation}
     */
    public WebRequest getWebRequest() {
        return webRequest;
    }
}
