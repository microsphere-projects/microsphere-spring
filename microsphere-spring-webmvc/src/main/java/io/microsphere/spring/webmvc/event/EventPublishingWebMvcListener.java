package io.microsphere.spring.webmvc.event;

import io.microsphere.spring.context.OnceApplicationContextEventListener;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.FilterRegistrationWebEndpointMappingFactory;
import io.microsphere.spring.web.metadata.ServletRegistrationWebEndpointMappingFactory;
import io.microsphere.spring.webmvc.metadata.HandlerMetadata;
import io.microsphere.spring.webmvc.metadata.HandlerMetadataWebEndpointMappingFactory;
import io.microsphere.spring.webmvc.metadata.RequestMappingMetadata;
import io.microsphere.spring.webmvc.metadata.RequestMappingMetadataReadyEvent;
import io.microsphere.spring.webmvc.metadata.RequestMappingMetadataWebEndpointMappingFactory;
import io.microsphere.spring.webmvc.metadata.WebEndpointMappingsReadyEvent;
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

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.microsphere.enterprise.servlet.enumeration.ServletVersion.SERVLET_3_0;
import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;

/**
 * Event Publishing Listener for Spring WebMVC
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RequestMappingMetadataReadyEvent
 * @see WebMvcEventPublisher
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

    protected void publishWebEvents(WebApplicationContext context) {

        Map<String, HandlerMapping> handlerMappingsMap = beansOfTypeIncludingAncestors(context, HandlerMapping.class);

        Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethods = new HashMap<>();
        List<WebEndpointMapping> webEndpointMappings = new LinkedList<>();

        ServletContext servletContext = context.getServletContext();

        if (SERVLET_3_0.le(servletContext)) { // Servlet 3.0+
            collectFromServletContext(servletContext, context, webEndpointMappings);
        }

        for (HandlerMapping handlerMapping : handlerMappingsMap.values()) {
            collectFromAbstractUrlHandlerMapping(handlerMapping, webEndpointMappings);
            collectFromRequestMappingInfoHandlerMapping(handlerMapping, requestMappingInfoHandlerMethods, webEndpointMappings);
        }

        context.publishEvent(new RequestMappingMetadataReadyEvent(context, requestMappingInfoHandlerMethods));
        context.publishEvent(new WebEndpointMappingsReadyEvent(context, webEndpointMappings));
        logger.info("The current application context [id: '{}'] has published the events");
    }

    private void collectFromServletContext(ServletContext servletContext, WebApplicationContext context,
                                           List<WebEndpointMapping> webEndpointMappings) {
        collectFromFilters(servletContext, context, webEndpointMappings);
        collectFromServlets(servletContext, context, webEndpointMappings);
    }

    private void collectFromFilters(ServletContext servletContext, WebApplicationContext context,
                                    List<WebEndpointMapping> webEndpointMappings) {
        Map<String, ? extends FilterRegistration> filterRegistrations = servletContext.getFilterRegistrations();
        if (filterRegistrations.isEmpty()) {
            return;
        }

        FilterRegistrationWebEndpointMappingFactory factory = new FilterRegistrationWebEndpointMappingFactory(servletContext);
        for (Map.Entry<String, ? extends FilterRegistration> entry : filterRegistrations.entrySet()) {
            String filterName = entry.getKey();
            Optional<WebEndpointMapping<String>> webEndpointMapping = factory.create(filterName);
            webEndpointMapping.ifPresent(webEndpointMappings::add);
        }
    }

    private void collectFromServlets(ServletContext servletContext, WebApplicationContext context,
                                     List<WebEndpointMapping> webEndpointMappings) {
        Map<String, ? extends ServletRegistration> servletRegistrations = servletContext.getServletRegistrations();
        if (servletRegistrations.isEmpty()) {
            return;
        }

        ServletRegistrationWebEndpointMappingFactory factory = new ServletRegistrationWebEndpointMappingFactory(servletContext);
        for (Map.Entry<String, ? extends ServletRegistration> entry : servletRegistrations.entrySet()) {
            String servletName = entry.getKey();
            Optional<WebEndpointMapping<String>> webEndpointMapping = factory.create(servletName);
            webEndpointMapping.ifPresent(webEndpointMappings::add);
        }
    }

    private void collectFromAbstractUrlHandlerMapping(HandlerMapping handlerMapping, List<WebEndpointMapping> webEndpointMappings) {
        if (handlerMapping instanceof AbstractUrlHandlerMapping) {
            AbstractUrlHandlerMapping urlHandlerMapping = (AbstractUrlHandlerMapping) handlerMapping;
            Map<String, Object> handlerMap = urlHandlerMapping.getHandlerMap();
            if (handlerMap.isEmpty()) {
                return;
            }

            HandlerMetadataWebEndpointMappingFactory factory = new HandlerMetadataWebEndpointMappingFactory(urlHandlerMapping);
            for (Map.Entry<String, Object> entry : handlerMap.entrySet()) {
                HandlerMetadata<Object, String> metadata = new HandlerMetadata<>(entry.getValue(), entry.getKey());
                Optional<WebEndpointMapping<HandlerMetadata<Object, String>>> webEndpointMapping = factory.create(metadata);
                webEndpointMapping.ifPresent(webEndpointMappings::add);
            }
        }
    }

    private void collectFromRequestMappingInfoHandlerMapping(HandlerMapping handlerMapping,
                                                             Map<RequestMappingInfo, HandlerMethod> requestMappingInfoHandlerMethods,
                                                             List<WebEndpointMapping> webEndpointMappings) {
        if (handlerMapping instanceof RequestMappingInfoHandlerMapping) {
            RequestMappingInfoHandlerMapping requestMappingInfoHandlerMapping = (RequestMappingInfoHandlerMapping) handlerMapping;
            Map<RequestMappingInfo, HandlerMethod> handlerMethodsMap = requestMappingInfoHandlerMapping.getHandlerMethods();
            if (handlerMethodsMap.isEmpty()) {
                return;
            }

            RequestMappingMetadataWebEndpointMappingFactory factory = new RequestMappingMetadataWebEndpointMappingFactory(requestMappingInfoHandlerMapping);
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethodsMap.entrySet()) {
                RequestMappingMetadata metadata = new RequestMappingMetadata(entry.getKey(), entry.getValue());
                Optional<WebEndpointMapping<HandlerMetadata<HandlerMethod, RequestMappingInfo>>> webEndpointMapping = factory.create(metadata);
                webEndpointMapping.ifPresent(webEndpointMappings::add);
            }
            requestMappingInfoHandlerMethods.putAll(handlerMethodsMap);
        }
    }
}
