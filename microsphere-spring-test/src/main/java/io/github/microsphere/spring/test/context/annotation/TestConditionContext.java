package io.github.microsphere.spring.test.context.annotation;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;

/**
 * {@link ConditionContext} test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class TestConditionContext implements ConditionContext, ApplicationContextAware {

    private ConfigurableApplicationContext applicationContext;

    @Override
    public BeanDefinitionRegistry getRegistry() {
        return (BeanDefinitionRegistry) applicationContext.getBeanFactory();
    }

    @Override
    public ConfigurableListableBeanFactory getBeanFactory() {
        return applicationContext.getBeanFactory();
    }

    @Override
    public Environment getEnvironment() {
        return applicationContext.getEnvironment();
    }

    @Override
    public ResourceLoader getResourceLoader() {
        return applicationContext;
    }

    @Override
    public ClassLoader getClassLoader() {
        return applicationContext.getClassLoader();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }
}
