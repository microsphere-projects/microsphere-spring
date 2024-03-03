package io.microsphere.spring.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationStartupAware;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.metrics.ApplicationStartup;
import org.springframework.stereotype.Component;
import org.springframework.util.StringValueResolver;

import java.util.Objects;

/**
 * Test Bean
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2017.01.13
 */
@Component("testBean")
@Order(1)
public class TestBean implements Bean, BeanFactoryAware, BeanClassLoaderAware, EnvironmentAware,
        EmbeddedValueResolverAware, ResourceLoaderAware, ApplicationEventPublisherAware, MessageSourceAware,
        ApplicationStartupAware, ApplicationContextAware, BeanNameAware {

    private ClassLoader classLoader;

    private BeanFactory beanFactory;

    private ApplicationContext applicationContext;

    private ApplicationEventPublisher applicationEventPublisher;

    private ApplicationStartup applicationStartup;

    private StringValueResolver resolver;

    private Environment environment;

    private MessageSource messageSource;

    private ResourceLoader resourceLoader;

    private String beanName;

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void setApplicationStartup(ApplicationStartup applicationStartup) {
        this.applicationStartup = applicationStartup;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public BeanFactory getBeanFactory() {
        return beanFactory;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public ApplicationEventPublisher getApplicationEventPublisher() {
        return applicationEventPublisher;
    }

    public ApplicationStartup getApplicationStartup() {
        return applicationStartup;
    }

    public StringValueResolver getResolver() {
        return resolver;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public MessageSource getMessageSource() {
        return messageSource;
    }

    public ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    public String getBeanName() {
        return beanName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestBean testBean = (TestBean) o;
        return Objects.equals(classLoader, testBean.classLoader) &&
                Objects.equals(beanFactory, testBean.beanFactory) &&
                Objects.equals(applicationContext, testBean.applicationContext) &&
                Objects.equals(applicationEventPublisher, testBean.applicationEventPublisher) &&
                Objects.equals(applicationStartup, testBean.applicationStartup) &&
                Objects.equals(resolver, testBean.resolver) &&
                Objects.equals(environment, testBean.environment) &&
                Objects.equals(messageSource, testBean.messageSource) &&
                Objects.equals(resourceLoader, testBean.resourceLoader);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classLoader, beanFactory, applicationContext, applicationEventPublisher, applicationStartup, resolver, environment, messageSource, resourceLoader);
    }

}
