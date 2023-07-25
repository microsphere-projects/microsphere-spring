package io.microsphere.spring.webmvc.event;

import io.microsphere.spring.context.OnceApplicationContextEventListener;
import io.microsphere.spring.webmvc.metadata.RequestMappingInfoHandlerMethodMetadataReadyEvent;
import io.microsphere.spring.webmvc.method.HandlerMethodsInitializedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;

/**
 * Event Publishing Listener for Spring WebMVC
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see HandlerMethodsInitializedEvent
 * @see RequestMappingInfoHandlerMethodMetadataReadyEvent
 * @since 1.0.0
 */
public class EventPublishingWebMvcListener extends OnceApplicationContextEventListener<ContextRefreshedEvent> implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(EventPublishingWebMvcListener.class);

    public EventPublishingWebMvcListener(ApplicationContext context) {
        super(context);
    }

    @Override
    public void onApplicationContextEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        publishEvents(applicationContext);
    }

    private void publishEvents(ApplicationContext applicationContext) {
        Map<String, HandlerMapping> handlerMappingsMap = beansOfTypeIncludingAncestors(applicationContext, HandlerMapping.class);

        Set<HandlerMethod> handlerMethods = new HashSet<>();
        Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethods = new HashMap<>();

        for (HandlerMapping handlerMapping : handlerMappingsMap.values()) {
            if (handlerMapping instanceof RequestMappingInfoHandlerMapping) {
                RequestMappingInfoHandlerMapping requestMappingInfoHandlerMapping = (RequestMappingInfoHandlerMapping) handlerMapping;
                Map<RequestMappingInfo, HandlerMethod> handlerMethodsMap = requestMappingInfoHandlerMapping.getHandlerMethods();
                handlerMethods.addAll(handlerMethodsMap.values());
                requestMappingInfoHandlerMethods.putAll(handlerMethodsMap);
            }
        }

        applicationContext.publishEvent(new HandlerMethodsInitializedEvent(applicationContext, handlerMethods));
        applicationContext.publishEvent(new RequestMappingInfoHandlerMethodMetadataReadyEvent(applicationContext, requestMappingInfoHandlerMethods));
        logger.info("The current application context [id: '{}'] has published the events");
    }
}
