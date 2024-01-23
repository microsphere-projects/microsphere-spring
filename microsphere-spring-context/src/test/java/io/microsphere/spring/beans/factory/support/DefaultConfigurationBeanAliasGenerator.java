package io.microsphere.spring.beans.factory.support;

/**
 * generate configuration bean alias by bean name  and class name
 *
 * @since 1.0.0
 */
public class DefaultConfigurationBeanAliasGenerator implements ConfigurationBeanAliasGenerator {

    public String generateAlias(String prefix, String beanName, Class<?> configClass) {

        return configClass.getSimpleName() + beanName.substring(0, 1).toUpperCase() + beanName.substring(1);

    }
}
