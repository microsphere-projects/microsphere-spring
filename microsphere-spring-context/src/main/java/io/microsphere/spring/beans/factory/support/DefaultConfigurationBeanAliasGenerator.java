package io.microsphere.spring.beans.factory.support;

import static io.microsphere.util.ClassUtils.getSimpleName;
import static org.springframework.util.StringUtils.capitalize;

/**
 * A {@link ConfigurationBeanAliasGenerator} implementation that generates configuration bean aliases
 * by combining the simple name of the configuration class and the capitalized bean name.
 *
 * <p>
 * For example, if the configuration class is {@code com.example.config.AppConfig} and the bean name is
 * {@code dataSource}, the generated alias will be: {@code AppConfigDataSource}.
 * </p>
 *
 * @author <a href="mailto:15868175516@163.com">qi.li</a>
 * @since 1.0.0
 */
public class DefaultConfigurationBeanAliasGenerator implements ConfigurationBeanAliasGenerator {

    public String generateAlias(String prefix, String beanName, Class<?> configClass) {
        return getSimpleName(configClass) + capitalize(beanName);
    }
}
