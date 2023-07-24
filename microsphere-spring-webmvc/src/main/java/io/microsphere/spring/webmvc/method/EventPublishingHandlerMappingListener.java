package io.microsphere.spring.webmvc.method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;

/**
 * Event Publishing Listener for Spring WebMVC's {@link HandlerMapping}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see HandlerMethodsInitializedEvent
 * @see RequestMappingInfoHandlerMethodMetadataReadyEvent
 * @since 1.0.0
 */
public class EventPublishingHandlerMappingListener implements ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(EventPublishingHandlerMappingListener.class);

    private ApplicationContext context;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        if (!Objects.equals(context, applicationContext)) {
            return;
        }
        publishHandlerMethodsEvent(applicationContext);
    }

    private void publishHandlerMethodsEvent(ApplicationContext applicationContext) {
        Map<String, HandlerMapping> handlerMappingsMap = beansOfTypeIncludingAncestors(applicationContext, HandlerMapping.class);
        Set<HandlerMethod> handlerMethods = new HashSet<>();

        Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethods = new HashMap<>();

        for (HandlerMapping handlerMapping : handlerMappingsMap.values()) {
            if (handlerMapping instanceof RequestMappingInfoHandlerMapping) {
                RequestMappingInfoHandlerMapping requestMappingInfoHandlerMapping = (RequestMappingInfoHandlerMapping) handlerMapping;
                handlerMethods.addAll(requestMappingInfoHandlerMapping.getHandlerMethods().values());
                requestMappingInfoHandlerMethods.putAll(requestMappingInfoHandlerMapping.getHandlerMethods());
            }
        }

        applicationContext.publishEvent(new HandlerMethodsInitializedEvent(applicationContext, handlerMethods));
        applicationContext.publishEvent(new RequestMappingInfoHandlerMethodMetadataReadyEvent(applicationContext, requestMappingInfoHandlerMethods));
        logger.info("The current application context [id: '{}'] has sent the HandlerMethod events");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
