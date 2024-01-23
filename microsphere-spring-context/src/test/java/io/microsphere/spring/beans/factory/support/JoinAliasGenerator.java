package io.microsphere.spring.beans.factory.support;

import io.microsphere.spring.beans.factory.annotation.EnableConfigurationBeanBinding;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * generate configuration bean alias by bean name and {@link EnableConfigurationBeanBinding} prefix
 *
 * @since 1.0.0
 *
 */
public abstract class JoinAliasGenerator implements ConfigurationBeanAliasGenerator {

    public String generateAlias(String prefix, String beanName, Class<?> configClass) {

        String[] prefixArray = prefix.split("\\.");
        String first = prefixArray[0];
        String others = Arrays.stream(prefixArray).skip(1)
                .map(item -> item.substring(0, 1).toUpperCase() + item.substring(1))
                .collect(Collectors.joining());

        return first + others + jointSymbol() + beanName;

    }

    protected abstract String jointSymbol();
}
