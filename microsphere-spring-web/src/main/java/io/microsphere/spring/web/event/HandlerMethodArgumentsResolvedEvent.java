package io.microsphere.spring.web.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * {@link HandlerMethod} Arguments Resolved Event
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class HandlerMethodArgumentsResolvedEvent extends ApplicationEvent {

    private transient final HandlerMethod handlerMethod;

    private transient final Object[] arguments;

    /**
     * @param webRequest    {@link WebRequest}
     * @param handlerMethod {@link HandlerMethod}
     * @param arguments     {@link Object the argument object that was resolved}
     */
    public HandlerMethodArgumentsResolvedEvent(WebRequest webRequest, HandlerMethod handlerMethod, Object[] arguments) {
        super(webRequest);
        this.handlerMethod = handlerMethod;
        this.arguments = arguments;
    }

    /**
     * @return the {@link HandlerMethod} being invoking
     */
    public HandlerMethod getHandlerMethod() {
        return handlerMethod;
    }

    /**
     * @return the {@link Method} of {@link HandlerMethod} being invoking
     */
    public Method getMethod() {
        return handlerMethod.getMethod();
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
        return (WebRequest) getSource();
    }

    @Override
    public String toString() {
        return "HandlerMethodArgumentsResolvedEvent{" +
                "handlerMethod=" + handlerMethod +
                ", arguments=" + Arrays.toString(arguments) +
                '}';
    }
}
