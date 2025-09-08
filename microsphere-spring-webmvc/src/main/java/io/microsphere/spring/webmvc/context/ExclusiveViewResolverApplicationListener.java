package io.microsphere.spring.webmvc.context;

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.logging.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.ViewResolverComposite;

import java.util.List;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.BeanUtils.getBeanIfAvailable;
import static io.microsphere.spring.beans.BeanUtils.getOptionalBean;
import static io.microsphere.spring.webmvc.constants.PropertyConstants.MICROSPHERE_SPRING_WEBMVC_VIEW_RESOLVER_PROPERTY_NAME_PREFIX;
import static io.microsphere.util.StringUtils.isBlank;

/**
 * Exclusive {@link ViewResolver} {@link ApplicationListener} on {@link ContextRefreshedEvent}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ViewResolver
 * @see ApplicationListener
 * @see ContextRefreshedEvent
 * @since 1.0.0
 */
public class ExclusiveViewResolverApplicationListener implements ApplicationListener<ContextRefreshedEvent>, EnvironmentAware {

    private static final Logger logger = getLogger(ExclusiveViewResolverApplicationListener.class);

    /**
     * The property name of the exclusive {@link ViewResolver} bean name
     */
    @ConfigurationProperty
    public static final String EXCLUSIVE_VIEW_RESOLVER_BEAN_NAME_PROPERTY_NAME =
            MICROSPHERE_SPRING_WEBMVC_VIEW_RESOLVER_PROPERTY_NAME_PREFIX + "exclusive-bean-name";

    private String exclusiveViewResolverBeanName;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        ApplicationContext applicationContext = event.getApplicationContext();
        configureExclusiveViewResolver(applicationContext);
    }

    @Override
    public void setEnvironment(Environment environment) {
        String exclusiveViewResolverBeanName = environment.getProperty(EXCLUSIVE_VIEW_RESOLVER_BEAN_NAME_PROPERTY_NAME);
        setExclusiveViewResolverBeanName(exclusiveViewResolverBeanName);
    }

    public void setExclusiveViewResolverBeanName(String exclusiveViewResolverBeanName) {
        this.exclusiveViewResolverBeanName = exclusiveViewResolverBeanName;
    }

    /**
     * Configure exclusive {@link ViewResolver}
     *
     * @param context {@link ApplicationContext}
     */
    void configureExclusiveViewResolver(ApplicationContext context) {
        String beanName = this.exclusiveViewResolverBeanName;
        if (isBlank(beanName)) {
            logger.trace("The 'exclusiveViewResolverBeanName' is blank, the configuration will be ignored!");
            return;
        }

        ViewResolver exclusiveViewResolver = getBeanIfAvailable(context, beanName, ViewResolver.class);
        if (exclusiveViewResolver == null) {
            logger.trace("No ViewResolver was found by the bean name : '{}'", beanName);
            return;
        }

        configureContentNegotiatingViewResolver(exclusiveViewResolver, context);
    }

    private void configureContentNegotiatingViewResolver(ViewResolver exclusiveViewResolver, ApplicationContext context) {

        ContentNegotiatingViewResolver contentNegotiatingViewResolver = getOptionalBean(context, ContentNegotiatingViewResolver.class);

        if (contentNegotiatingViewResolver == null) {
            logger.trace("No ContentNegotiatingViewResolver was found in the application context : {}", context);
            configureViewResolverComposite(exclusiveViewResolver, context);
            return;
        }

        List<ViewResolver> viewResolvers = contentNegotiatingViewResolver.getViewResolvers();

        contentNegotiatingViewResolver.setViewResolvers(ofList(exclusiveViewResolver));

        logger.trace("The view resolvers of ContentNegotiatingViewResolver has been reset , before : {} , after : {}",
                viewResolvers, exclusiveViewResolver);
    }

    private void configureViewResolverComposite(ViewResolver exclusiveViewResolver, ApplicationContext context) {
        ViewResolverComposite viewResolverComposite = getOptionalBean(context, ViewResolverComposite.class);

        if (viewResolverComposite == null) {
            logger.trace("No ViewResolverComposite was found in the application context : {}", context);
            return;
        }

        List<ViewResolver> viewResolvers = viewResolverComposite.getViewResolvers();

        viewResolverComposite.setViewResolvers(ofList(exclusiveViewResolver));

        logger.trace("The view resolvers of ViewResolverComposite has been reset , before : {} , after : {}",
                viewResolvers, exclusiveViewResolver);
    }
}
