package io.microsphere.spring.beans.factory.support;

/**
 * generate configuration bean alias
 *
 * @since 1.0.0
 */
public interface ConfigurationBeanAliasGenerator {

    String generateAlias(String prefix, String beanName, Class<?> configClass);

}
