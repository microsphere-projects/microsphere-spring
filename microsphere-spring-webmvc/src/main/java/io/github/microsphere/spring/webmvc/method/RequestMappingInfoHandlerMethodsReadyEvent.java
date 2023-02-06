package io.github.microsphere.spring.webmvc.method;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * {@link RequestMappingInfo} {@link HandlerMethod} Ready Event
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RequestMappingInfoHandlerMethodsReadyEvent extends ApplicationContextEvent {

    private final Map<RequestMappingInfo, HandlerMethod> handlerMethods;

    /**
     * Create a new ContextStartedEvent.
     *
     * @param source         the {@code ApplicationContext} that the event is raised for
     *                       (must not be {@code null})
     * @param handlerMethods
     */
    public RequestMappingInfoHandlerMethodsReadyEvent(ApplicationContext source, Map<RequestMappingInfo, HandlerMethod> handlerMethods) {
        super(source);
        this.handlerMethods = unmodifiableMap(handlerMethods);
    }

    public Map<RequestMappingInfo, HandlerMethod> getHandlerMethods() {
        return handlerMethods;
    }
}
