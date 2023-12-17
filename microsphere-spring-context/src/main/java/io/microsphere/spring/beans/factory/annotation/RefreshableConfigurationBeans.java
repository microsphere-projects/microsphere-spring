package io.microsphere.spring.beans.factory.annotation;

import io.microsphere.spring.context.config.ConfigurationBeanBinder;
import io.microsphere.spring.context.config.DefaultConfigurationBeanBinder;
import io.microsphere.spring.context.event.ConfigurationBeanRefreshedEvent;
import io.microsphere.spring.core.convert.support.ConversionServiceResolver;
import io.microsphere.spring.util.PropertySourcesUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.*;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.microsphere.spring.context.config.ConfigurationBeanBinder.*;

/**
 * collect Configuration Beans in current context and its parent.<br/>
 * also provider {@link RefreshableConfigurationBeans#refreshByName(String, boolean)} operation for refresh special bean
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @since 1.0.0
 */
@ManagedResource
public class RefreshableConfigurationBeans implements ApplicationContextAware, EnvironmentAware, ApplicationEventPublisherAware {

    private final Log log = LogFactory.getLog(getClass());

    public static final String BEAN_NAME = "refreshableConfigurationBeans";

    private final Map<String, Object> beans = new ConcurrentHashMap<>(64);
    private final Map<String, String> prefixToBeanNames = new ConcurrentHashMap<>(64);
    private ApplicationContext applicationContext;
    private ConfigurableListableBeanFactory beanFactory;
    private ConfigurableEnvironment environment;
    private ApplicationEventPublisher eventPublisher;
    private volatile ConfigurationBeanBinder configurationBeanBinder;
    private final Collection<RefreshableConfigurationBeanPredicate> filters = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        Assert.isInstanceOf(ConfigurableListableBeanFactory.class, applicationContext.getAutowireCapableBeanFactory());
        this.beanFactory = (ConfigurableListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
        this.filters.addAll(BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, RefreshableConfigurationBeanPredicate.class).values());
    }

    public void registerConfigurationBean(String beanName, Object instance, BeanDefinition beanDefinition) {
        EnableConfigurationBeanBinding.ConfigurationBeanRefreshStrategy refreshStrategy = (EnableConfigurationBeanBinding.ConfigurationBeanRefreshStrategy) beanDefinition.getAttribute(ConfigurationBeanBinder.CONFIGURATION_BEAN_REFRESH_STRATEGY);
        if (refreshStrategy == EnableConfigurationBeanBinding.ConfigurationBeanRefreshStrategy.DISABLED) {
            log.debug("bean [name=" + beanName + "] disable refresh");
            return;
        }

        synchronized (this.filters) {
            for (RefreshableConfigurationBeanPredicate filter : this.filters) {
                if (!filter.support(beanName, instance, beanDefinition)) {
                    log.debug("bean [name=" + beanName + "] is not supported");
                    return;
                }
            }
        }

        this.beans.put(beanName, instance);
        String prefix = (String) beanDefinition.getAttribute(CONFIGURATION_PREFIX_ATTRIBUTE_NAME);
        boolean usingMultiple = (Boolean) beanDefinition.getAttribute(USING_MULTIPLE_CONFIGURATION_ATTRIBUTE_NAME);
        if (usingMultiple) {
            prefix = prefix + '.' + beanName;
        }
        this.prefixToBeanNames.put(prefix, beanName);
    }


    public void addFilter(RefreshableConfigurationBeanPredicate filter) {
        synchronized (this.filters) {
            if (filter != null)
                this.filters.add(filter);
        }
    }



    /**
     * @return all bean names
     */
    @ManagedAttribute
    public Set<String> managedBeanNames() {
        return Collections.unmodifiableSet(this.beans.keySet());
    }

    /**
     *
     * @return all prefixes
     */
    @ManagedAttribute
    public Set<String> managedPrefixes() {
        return Collections.unmodifiableSet(this.prefixToBeanNames.keySet());
    }

    public ConfigurationBeanBinder getConfigurationBeanBinder() {
        if (this.configurationBeanBinder == null)
            initConfigurationBeanBinder();
        return this.configurationBeanBinder;
    }

    private void initConfigurationBeanBinder() {
        ConfigurationBeanBinder configurationBeanBinder = this.configurationBeanBinder;
        if (configurationBeanBinder == null) {
            try {
                configurationBeanBinder = beanFactory.getBean(ConfigurationBeanBinder.class);
            } catch (BeansException ignored) {
                if (log.isInfoEnabled()) {
                    log.info("configurationBeanBinder Bean can't be found in ApplicationContext.");
                }
                // Use Default implementation
                configurationBeanBinder = new DefaultConfigurationBeanBinder();
            }
        }

        ConversionService conversionService = new ConversionServiceResolver(beanFactory).resolve();
        configurationBeanBinder.setConversionService(conversionService);

        this.configurationBeanBinder = configurationBeanBinder;
    }

    /**
     * refresh the special configuration bean
     * @param name the special configuration bean name
     * @param notifyEvent should send {@link ConfigurationBeanRefreshedEvent}
     */
    @ManagedOperation
    public void refreshByName(String name, boolean notifyEvent) {
        //check exists
        Object instance = this.beans.get(name);
        if (instance == null) {
            log.debug("ConfigurationBean[name= " + name +"] not registered, please check refreshStrategy is enabled?");
            return;
        }
        BeanDefinition beanDefinition = this.beanFactory.getBeanDefinition(name);
        EnableConfigurationBeanBinding.ConfigurationBeanRefreshStrategy strategy = (EnableConfigurationBeanBinding.ConfigurationBeanRefreshStrategy)beanDefinition.getAttribute(CONFIGURATION_BEAN_REFRESH_STRATEGY);
        Map<String, Object> previousConfigurationProperties = (Map<String, Object>) beanDefinition.getAttribute(CONFIGURATION_PROPERTIES_ATTRIBUTE_NAME);




        //override configuration properties
        String prefix = (String) beanDefinition.getAttribute(CONFIGURATION_PREFIX_ATTRIBUTE_NAME);
        boolean multiple = (Boolean) beanDefinition.getAttribute(USING_MULTIPLE_CONFIGURATION_ATTRIBUTE_NAME);
        if (multiple) {
            prefix = prefix + '.' + name;
        }
        Map<String, Object> configurationProperties = PropertySourcesUtils.getSubProperties(environment.getPropertySources(), environment, prefix);
        beanDefinition.setAttribute(CONFIGURATION_PROPERTIES_ATTRIBUTE_NAME, configurationProperties);
        switch (strategy) {
            case DISABLED:
                log.info("bean [" + name + "] has disabled refresh");
                return;
            case REINITIALIZE:
                refreshAndReinitialized(name, instance, beanDefinition);
                if (notifyEvent)
                    notifyConfigurationBeanRefreshed(name, instance, previousConfigurationProperties);
                return;
            case REBIND:
                //resolve sub properties
                refreshProperties(name, instance, configurationProperties, beanDefinition);
                if (notifyEvent)
                    notifyConfigurationBeanRefreshed(name, instance, previousConfigurationProperties);
                return;
            default:
                //todo throws new exception?
                log.error("Unknown refresh strategy : " + strategy + " with " + name);
        }
    }

    /**
     * refresh the special configuration bean
     * @param changedProperty the special configuration property
     * @param notifyEvent should send {@link ConfigurationBeanRefreshedEvent}
     */
    @ManagedOperation
    public void refreshByProperty(String changedProperty, boolean notifyEvent) {
        String selectedPrefix = null;
        Set<String> prefixSet = managedPrefixes();
        for (String prefix : prefixSet)
            if (changedProperty.startsWith(prefix)) {
                selectedPrefix = prefix;
                break;
            }

        if (selectedPrefix == null) {
            log.debug("changedProperty : [" + changedProperty + "] map to prefix : " + selectedPrefix);
            return;
        }


        final String beanName = this.prefixToBeanNames.get(selectedPrefix);
        refreshByName(beanName, notifyEvent);

    }

    protected void refreshProperties(String beanName, Object instance, Map<String, Object> configurationProperties, BeanDefinition beanDefinition) {
        getConfigurationBeanBinder().bind(beanDefinition, instance);
    }

    protected void refreshAndReinitialized(String beanName, Object instance, BeanDefinition beanDefinition) {
        try {
            this.applicationContext.getAutowireCapableBeanFactory().destroyBean(instance);
            this.applicationContext.getAutowireCapableBeanFactory().initializeBean(instance, beanName);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot rebind to " + beanName, e);
        }

    }

    @Override
    public void setEnvironment(Environment environment) {

        Assert.isInstanceOf(ConfigurableEnvironment.class, environment);

        this.environment = (ConfigurableEnvironment) environment;

    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    private void notifyConfigurationBeanRefreshed(String beanName, Object instance, Map<String, Object> previousConfigurationProperties) {
        eventPublisher.publishEvent(new ConfigurationBeanRefreshedEvent(beanName, instance, previousConfigurationProperties));
    }
}
