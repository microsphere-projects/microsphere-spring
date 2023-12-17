package io.microsphere.spring.beans.factory.annotation;

import org.springframework.beans.factory.config.BeanDefinition;

/**
 * the filter for {@link RefreshableConfigurationBeans}
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @since 1.0
 */
@FunctionalInterface
public interface RefreshableConfigurationBeanPredicate {

    boolean support(String beanName, Object instance, BeanDefinition beanDefinition);

}
