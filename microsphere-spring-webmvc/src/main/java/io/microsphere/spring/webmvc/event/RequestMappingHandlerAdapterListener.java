package io.microsphere.spring.webmvc.event;

import io.microsphere.spring.context.OnceApplicationContextEventListener;
import io.microsphere.spring.webmvc.method.support.HandlerMethodArgumentResolverWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;

/**
 * {@link RequestMappingHandlerAdapter} {@link ApplicationListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class RequestMappingHandlerAdapterListener extends OnceApplicationContextEventListener<ContextRefreshedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RequestMappingHandlerAdapterListener.class);

    private static final Class<RequestMappingHandlerAdapter> ADAPTER_CLASS = RequestMappingHandlerAdapter.class;

    public RequestMappingHandlerAdapterListener(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected void onApplicationContextEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        Map<String, RequestMappingHandlerAdapter> adaptersMap = beansOfTypeIncludingAncestors(context, ADAPTER_CLASS);
        adaptersMap.forEach(this::wrapHandlerMethodArgumentResolvers);
    }

    private void wrapHandlerMethodArgumentResolvers(String beanName, RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        List<HandlerMethodArgumentResolver> argumentResolvers = requestMappingHandlerAdapter.getArgumentResolvers();

        int size = argumentResolvers.size();

        if (logger.isDebugEnabled()) {
            logger.debug("RequestMappingHandlerAdapter[bean name : '{}' , class : '{}'] associates {} HandlerMethodArgumentResolver!",
                    beanName, requestMappingHandlerAdapter.getClass().getName(), size);
        }

        if (size < 1) {
            return;
        }

        ApplicationContext context = requestMappingHandlerAdapter.getApplicationContext();

        List<HandlerMethodArgumentResolver> argumentResolverWrappers = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            HandlerMethodArgumentResolver argumentResolver = argumentResolvers.get(i);
            if (!(argumentResolver instanceof HandlerMethodArgumentResolverWrapper)) {
                argumentResolverWrappers.add(new HandlerMethodArgumentResolverWrapper(argumentResolver, context));
            }
        }

        requestMappingHandlerAdapter.setArgumentResolvers(argumentResolverWrappers);
    }

}
