package io.github.microsphere.spring.webmvc.method;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Publishing {@link HandlerMethodsInitializedEvent} Event Listener
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class PublishingHandlerMethodsInitializedEventListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        publishHandlerMethodsInitializedEvent(applicationContext);
    }

    private void publishHandlerMethodsInitializedEvent(ApplicationContext applicationContext) {
        Map<String, AbstractHandlerMethodMapping> handlerMappingsMap =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, AbstractHandlerMethodMapping.class);
        Set<HandlerMethod> handlerMethods = new HashSet<>();
        for (AbstractHandlerMethodMapping handlerMapping : handlerMappingsMap.values()) {
            handlerMethods.addAll(handlerMapping.getHandlerMethods().values());
        }
        applicationContext.publishEvent(new HandlerMethodsInitializedEvent(applicationContext, handlerMethods));
    }
}
