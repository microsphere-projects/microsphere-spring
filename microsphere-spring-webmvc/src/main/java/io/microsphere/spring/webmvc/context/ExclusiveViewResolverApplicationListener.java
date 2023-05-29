package io.microsphere.spring.webmvc.context;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;

import java.util.Arrays;
import java.util.Map;

/**
 * Exclusive {@link ViewResolver} {@link ApplicationListener} on {@link ContextRefreshedEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ViewResolver
 * @see ApplicationListener
 * @see ContextRefreshedEvent
 * @since 2017.03.23
 */
public class ExclusiveViewResolverApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    private static final Class<ViewResolver> VIEW_RESOLVER_CLASS = ViewResolver.class;

    private final String exclusiveViewResolverBeanName;

    public ExclusiveViewResolverApplicationListener(String exclusiveViewResolverBeanName) {
        this.exclusiveViewResolverBeanName = exclusiveViewResolverBeanName;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        ApplicationContext applicationContext = event.getApplicationContext();

        configureExclusiveViewResolver(applicationContext);


    }

    /**
     * Configure exclusive {@link ViewResolver}
     *
     * @param applicationContext {@link ApplicationContext}
     */
    private void configureExclusiveViewResolver(ApplicationContext applicationContext) {

        Map<String, ViewResolver> viewResolversMap =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, VIEW_RESOLVER_CLASS);

        int size = viewResolversMap.size();

        if (size < 2) {
            return;
        }

        ViewResolver exclusiveViewResolver = viewResolversMap.get(exclusiveViewResolverBeanName);

        if (exclusiveViewResolver == null) {

            throw new NoSuchBeanDefinitionException(VIEW_RESOLVER_CLASS, exclusiveViewResolverBeanName);

        }

        ContentNegotiatingViewResolver contentNegotiatingViewResolver =
                applicationContext.getBean(ContentNegotiatingViewResolver.class);

        contentNegotiatingViewResolver.setViewResolvers(Arrays.asList(exclusiveViewResolver));

    }

}
