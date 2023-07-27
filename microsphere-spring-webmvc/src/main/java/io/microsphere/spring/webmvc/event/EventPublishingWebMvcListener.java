package io.microsphere.spring.webmvc.event;

import io.microsphere.spring.context.OnceApplicationContextEventListener;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.webmvc.metadata.HandlerMetadata;
import io.microsphere.spring.webmvc.metadata.HandlerMetadataWebEndpointMappingFactory;
import io.microsphere.spring.webmvc.metadata.RequestMappingMetadata;
import io.microsphere.spring.webmvc.metadata.RequestMappingMetadataReadyEvent;
import io.microsphere.spring.webmvc.metadata.RequestMappingMetadataWebEndpointMappingFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.AbstractUrlHandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;

/**
 * Event Publishing Listener for Spring WebMVC
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RequestMappingMetadataReadyEvent
 * @since 1.0.0
 */
public class EventPublishingWebMvcListener extends OnceApplicationContextEventListener<ContextRefreshedEvent> implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(EventPublishingWebMvcListener.class);

    public EventPublishingWebMvcListener(ApplicationContext context) {
        super(context);
    }

    @Override
    public void onApplicationContextEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        if (context instanceof WebApplicationContext) {
            publishWebEvents((WebApplicationContext) context);
        }
    }

    private void publishWebEvents(WebApplicationContext context) {

        Map<String, HandlerMapping> handlerMappingsMap = beansOfTypeIncludingAncestors(context, HandlerMapping.class);

        Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethods = new HashMap<>();
        List<WebEndpointMapping> webEndpointMappings = new LinkedList<>();

        for (HandlerMapping handlerMapping : handlerMappingsMap.values()) {
            collectFromAbstractUrlHandlerMapping(handlerMapping, webEndpointMappings);
            collectFromRequestMappingInfoHandlerMapping(handlerMapping, requestMappingInfoHandlerMethods, webEndpointMappings);

        }

        context.publishEvent(new RequestMappingMetadataReadyEvent(context, requestMappingInfoHandlerMethods));
        logger.info("The current application context [id: '{}'] has published the events");
    }

    private void collectFromAbstractUrlHandlerMapping(HandlerMapping handlerMapping, List<WebEndpointMapping> webEndpointMappings) {
        if (handlerMapping instanceof AbstractUrlHandlerMapping) {
            HandlerMetadataWebEndpointMappingFactory factory = HandlerMetadataWebEndpointMappingFactory.INSTANCE;
            AbstractUrlHandlerMapping urlHandlerMapping = (AbstractUrlHandlerMapping) handlerMapping;
            Map<String, Object> handlerMap = urlHandlerMapping.getHandlerMap();
            for (Map.Entry<String, Object> entry : handlerMap.entrySet()) {
                HandlerMetadata<Object, String> metadata = new HandlerMetadata<>(entry.getValue(), entry.getKey());
                Optional<WebEndpointMapping<?>> webEndpointMapping = factory.create(metadata);
                webEndpointMapping.ifPresent(webEndpointMappings::add);
            }
        }
    }

    private void collectFromRequestMappingInfoHandlerMapping(HandlerMapping handlerMapping,
                                                             Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethods,
                                                             List<WebEndpointMapping> webEndpointMappings) {
        RequestMappingMetadataWebEndpointMappingFactory factory = RequestMappingMetadataWebEndpointMappingFactory.INSTANCE;
        if (handlerMapping instanceof RequestMappingInfoHandlerMapping) {
            RequestMappingInfoHandlerMapping requestMappingInfoHandlerMapping = (RequestMappingInfoHandlerMapping) handlerMapping;
            Map<RequestMappingInfo, HandlerMethod> handlerMethodsMap = requestMappingInfoHandlerMapping.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethodsMap.entrySet()) {
                RequestMappingMetadata metadata = new RequestMappingMetadata(entry.getKey(), entry.getValue());
                Optional<WebEndpointMapping<?>> webEndpointMapping = factory.create(metadata);
                webEndpointMapping.ifPresent(webEndpointMappings::add);
            }
            requestMappingInfoHandlerMethods.putAll(handlerMethodsMap);
        }

    }
}
