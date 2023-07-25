package io.microsphere.spring.webmvc.metadata;

import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;

/**
 * {@link RequestMappingInfoHandlerMethodMetadata} Ready Event
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RequestMappingInfoHandlerMethodMetadataReadyEvent extends ApplicationContextEvent {

    private final List<RequestMappingInfoHandlerMethodMetadata> metadata;

    /**
     * Create a new ContextStartedEvent.
     *
     * @param source         the {@code ApplicationContext} that the event is raised for
     *                       (must not be {@code null})
     * @param handlerMethods Map<RequestMappingInfo, HandlerMethod>
     */
    public RequestMappingInfoHandlerMethodMetadataReadyEvent(ApplicationContext source,
                                                             Map<RequestMappingInfo, HandlerMethod> handlerMethods) {
        super(source);
        this.metadata = buildMetadata(handlerMethods);
    }

    private List<RequestMappingInfoHandlerMethodMetadata> buildMetadata(Map<RequestMappingInfo, HandlerMethod> handlerMethods) {
        List<RequestMappingInfoHandlerMethodMetadata> metadata = new ArrayList<>(handlerMethods.size());
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            metadata.add(new RequestMappingInfoHandlerMethodMetadata(entry.getValue(), entry.getKey()));
        }
        return unmodifiableList(metadata);
    }

    public List<RequestMappingInfoHandlerMethodMetadata> getMetadata() {
        return metadata;
    }
}
