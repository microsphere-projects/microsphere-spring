package io.github.microsphere.spring.webmvc.method;

import io.github.microsphere.spring.webmvc.method.support.HandlerMethodArgumentResolverWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.CollectionUtils;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * {@link RequestMappingHandlerAdapter} {@link ApplicationListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RequestMappingHandlerAdapterListener implements ApplicationListener<ContextRefreshedEvent> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final ApplicationContext applicationContext;

    public RequestMappingHandlerAdapterListener(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();

        if (!matches(applicationContext)) {
            logger.info("The current event ApplicationContext[id: {}] does not match the associated ApplicationContext[id: {}], ignore processing!",
                    applicationContext.getId(), this.applicationContext.getId());
            return;
        }

        Map<String, RequestMappingHandlerAdapter> requestMappingHandlerAdapters =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, RequestMappingHandlerAdapter.class);

        requestMappingHandlerAdapters.values().forEach(this::wrapHandlerMethodArgumentResolvers);
    }

    private boolean matches(ApplicationContext applicationContext) {
        return Objects.equals(this.applicationContext, applicationContext);
    }

    private void wrapHandlerMethodArgumentResolvers(RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();

        if (CollectionUtils.isEmpty(argumentResolvers)) {
            logger.warn("RequestMappingHandlerAdapter [class: {}] not associated HandlerMethodArgumentResolver!",
                    requestMappingHandlerAdapter.getClass().getName());
            return;
        }

        wrapHandlerMethodArgumentResolvers(requestMappingHandlerAdapter, argumentResolvers);
    }

    private void wrapHandlerMethodArgumentResolvers(RequestMappingHandlerAdapter requestMappingHandlerAdapter,
                                                    List<HandlerMethodArgumentResolver> argumentResolvers) {
        int size = argumentResolvers.size();

        logger.info("RequestMappingHandlerAdapter associated/class: {} {} a HandlerMethodArgumentResolver!",
                requestMappingHandlerAdapter.getClass().getName(), size);

        List<HandlerMethodArgumentResolver> wrappedArgumentResolvers = new ArrayList<>(argumentResolvers.size());

        for (int i = 0; i < size; i++) {
            HandlerMethodArgumentResolver argumentResolver = argumentResolvers.get(i);
            if (!(argumentResolver instanceof HandlerMethodArgumentResolverWrapper)) {
                wrappedArgumentResolvers.add(new HandlerMethodArgumentResolverWrapper(argumentResolver, applicationContext));
            }
        }

        requestMappingHandlerAdapter.setArgumentResolvers(wrappedArgumentResolvers);
    }
}
