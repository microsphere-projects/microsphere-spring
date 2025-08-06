package io.microsphere.spring.beans.factory.support;

/**
 * A strategy interface for generating aliases for configuration beans.
 * <p>
 * Implementations of this interface should provide a consistent and unique alias generation
 * mechanism based on the provided prefix, bean name, and configuration class.
 * </p>
 *
 * @author <a href="mailto:15868175516@163.com">qi.li<a/>
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DefaultConfigurationBeanAliasGenerator
 * @see HyphenAliasGenerator
 * @see UnderScoreJoinAliasGenerator
 * @since 1.0.0
 */
public interface ConfigurationBeanAliasGenerator {

    /**
     * Generates an alias for a configuration bean based on the provided prefix, bean name, and configuration class.
     *
     * <p>
     * This method should create a unique and consistent alias by combining the given prefix, bean name,
     * and the simple name of the configuration class. Implementations are expected to handle cases where
     * any of the inputs may be {@code null} or empty, ensuring a valid alias is returned.
     * </p>
     *
     * <p><b>Example:</b> Given a prefix of "app", a bean name of "dataSource", and a configuration class
     * named {@code DatabaseConfig}, this method might return: <code>"appDataSourceDatabaseConfig"</code>.</p>
     *
     * @param prefix     the prefix to be used in the alias; may be {@code null} or empty
     * @param beanName   the name of the bean for which the alias is being generated;
     *                   must not be {@code null} or empty
     * @param configClass the configuration class associated with the bean;
     *                    may be {@code null} if no specific configuration class is associated
     * @return a generated alias as a {@link String}; never {@code null}
     */
    String generateAlias(String prefix, String beanName, Class<?> configClass);

}
