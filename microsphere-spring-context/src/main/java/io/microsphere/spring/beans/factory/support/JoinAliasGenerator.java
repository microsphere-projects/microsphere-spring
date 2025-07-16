package io.microsphere.spring.beans.factory.support;

import static io.microsphere.util.StringUtils.split;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.springframework.util.StringUtils.capitalize;

/**
 * A {@link ConfigurationBeanAliasGenerator} implementation that generates aliases by joining
 * the parts of the prefix with the bean name in a cohesive format.
 *
 * <p>
 * For example, given a prefix like "user.config" and a bean name "dataSource", it would generate an alias like:
 * {@code userConfig + delimiter() + dataSource}, where the delimiter is defined by the implementing subclass.
 * </p>
 *
 * @author <a href="mailto:15868175516@163.com">qi.li</a>
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class JoinAliasGenerator implements ConfigurationBeanAliasGenerator {

    public String generateAlias(String prefix, String beanName, Class<?> configClass) {
        String[] prefixArray = split(prefix, ".");
        String first = prefixArray[0];
        String others = stream(prefixArray).skip(1)
                .map(item -> capitalize(item))
                .collect(joining());
        return first + others + delimiter() + beanName;
    }

    protected abstract String delimiter();
}
