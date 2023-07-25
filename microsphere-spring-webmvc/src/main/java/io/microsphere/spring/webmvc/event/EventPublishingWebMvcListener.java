package io.microsphere.spring.webmvc.event;

import io.microsphere.spring.context.OnceApplicationContextEventListener;
import io.microsphere.spring.webmvc.metadata.RequestMappingMetadataReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;

/**
 * Event Publishing Listener for Spring WebMVC
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see HandlerMethodsInitializedEvent
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
            publishEvents((WebApplicationContext) context);
        }

    }

    private void publishEvents(WebApplicationContext context) {

        Map<String, HandlerMapping> handlerMappingsMap = beansOfTypeIncludingAncestors(context, HandlerMapping.class);

        Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethods = new HashMap<>();

        for (HandlerMapping handlerMapping : handlerMappingsMap.values()) {
            collectRequestMappingInfoHandlerMethods(handlerMapping, requestMappingInfoHandlerMethods);

        }

        context.publishEvent(new RequestMappingMetadataReadyEvent(context, requestMappingInfoHandlerMethods));
        logger.info("The current application context [id: '{}'] has published the events");
    }

    private void collectRequestMappingInfoHandlerMethods(HandlerMapping handlerMapping, Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethods) {
        if (handlerMapping instanceof RequestMappingInfoHandlerMapping) {
            RequestMappingInfoHandlerMapping requestMappingInfoHandlerMapping = (RequestMappingInfoHandlerMapping) handlerMapping;
            Map<RequestMappingInfo, HandlerMethod> handlerMethodsMap = requestMappingInfoHandlerMapping.getHandlerMethods();
            requestMappingInfoHandlerMethods.putAll(handlerMethodsMap);
        }
    }
}
