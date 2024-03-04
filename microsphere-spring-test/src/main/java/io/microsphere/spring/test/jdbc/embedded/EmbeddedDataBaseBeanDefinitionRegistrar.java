package io.microsphere.spring.test.jdbc.embedded;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.util.Properties;
import java.util.StringJoiner;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * Embedded database {@link ImportBeanDefinitionRegistrar}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableEmbeddedDatabase
 * @since 1.0.0
 */
class EmbeddedDataBaseBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Class<? extends Annotation> ANNOTATION_TYPE = EnableEmbeddedDatabase.class;

    private static final String INTERNAL_USE_PROPERTY_NAME_PREFIX = "_";

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = fromMap(metadata.getAnnotationAttributes(ANNOTATION_TYPE.getName()));
        registerBeanDefinitions(attributes, registry);
    }

    void registerBeanDefinitions(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        EmbeddedDatabaseType type = attributes.getEnum("type");

        switch (type) {
            case SQLITE:
                processSQLite(attributes, registry);
                break;
            case MARIADB:
                processMariaDB(attributes, registry);
                break;
        }
    }

    private void processSQLite(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        registerSQLiteDataSourceBeanDefinition(attributes, registry);
    }

    private void processMariaDB(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        int port = attributes.getNumber("port");
        Properties properties = resolveProperties(attributes, registry);
        startEmbeddedMariaDB4j(port, properties);
        registerMariaDBDataSourceBeanDefinition(port, attributes, registry);
    }

    private void registerSQLiteDataSourceBeanDefinition(AnnotationAttributes attributes,
                                                        BeanDefinitionRegistry registry) {
        registerDataSourceBeanDefinition("jdbc:sqlite::memory:", attributes, registry);
    }

    private void startEmbeddedMariaDB4j(int port, Properties properties) {
        DBConfigurationBuilder configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(port);
        String sqlSource = properties.getProperty("_sql.source");
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        DB db = null;
        try {
            db = DB.newEmbeddedDB(configBuilder.build());
            db.start();
            if (StringUtils.hasText(sqlSource)) {
                Resource resource = resourceLoader.getResource(sqlSource);
                db.source(resource.getInputStream());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void registerMariaDBDataSourceBeanDefinition(int port,
                                                         AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        String jdbcURL = "jdbc:mariadb://127.0.0.1:" + port;
        registerDataSourceBeanDefinition(jdbcURL, attributes, registry);
    }

    private void registerDataSourceBeanDefinition(String jdbcURL,
                                                  AnnotationAttributes attributes,
                                                  BeanDefinitionRegistry registry) {
        String beanName = attributes.getString("dataSource");
        boolean primary = attributes.getBoolean("primary");
        Properties properties = resolveProperties(attributes, registry);

        BeanDefinitionBuilder beanDefinitionBuilder = genericBeanDefinition(DriverManagerDataSource.class);
        beanDefinitionBuilder.addConstructorArgValue(jdbcURL);
        beanDefinitionBuilder.addConstructorArgValue(properties);
        if (registry.containsBeanDefinition(beanName)) {
            throw new BeanCreationException("The duplicated BeanDefinition with name : " + beanName);
        }
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setPrimary(primary);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    private Properties resolveProperties(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        ConfigurableBeanFactory beanFactory = null;
        if (registry instanceof ConfigurableBeanFactory) {
            beanFactory = (ConfigurableBeanFactory) registry;
        }
        String[] values = attributes.getStringArray("properties");
        if (values.length == 0) {
            return null;
        }
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
        for (String value : values) {
            // TODO
            // Resolve the placeholders
            // If value starts with "_", ignored
//            if (value.startsWith(INTERNAL_USE_PROPERTY_NAME_PREFIX)) {
//                continue;
//            }
            String resolvedValue = beanFactory == null ? value :
                    beanFactory.resolveEmbeddedValue(value);
            stringJoiner.add(resolvedValue);
        }
        Properties properties = new Properties();
        try {
            properties.load(new StringReader(stringJoiner.toString()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }
}
