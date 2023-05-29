package io.microsphere.spring.test.jdbc.embedded;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

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
 * @since 1.0.0
 */
class EmbeddedDataBaseBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Class<? extends Annotation> ANNOTATION_TYPE = EnableEmbeddedDatabase.class;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = fromMap(metadata.getAnnotationAttributes(ANNOTATION_TYPE.getName()));
        registerBeanDefinitions(attributes, registry);
    }

    void registerBeanDefinitions(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        EmbeddedDatabaseType type = attributes.getEnum("type");
        String beanName = attributes.getString("dataSource");
        boolean primary = attributes.getBoolean("primary");

        switch (type) {
            case SQLITE:
                registerSQLiteBeanDefinitions(beanName, primary, attributes, registry);
                break;
        }
    }

    private void registerSQLiteBeanDefinitions(String beanName, boolean primary, AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        Properties properties = resolveProperties(attributes);
        BeanDefinitionBuilder beanDefinitionBuilder = genericBeanDefinition(DriverManagerDataSource.class);
        beanDefinitionBuilder.addConstructorArgValue("jdbc:sqlite::memory:");
        beanDefinitionBuilder.addConstructorArgValue(properties);
        if (registry.containsBeanDefinition(beanName)) {
            throw new BeanCreationException("The duplicated BeanDefinition with name : " + beanName);
        }
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        beanDefinition.setPrimary(primary);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }

    private Properties resolveProperties(AnnotationAttributes attributes) {
        String[] values = attributes.getStringArray("properties");
        if (values.length == 0) {
            return null;
        }
        StringJoiner stringJoiner = new StringJoiner(System.lineSeparator());
        for (String value : values) {
            stringJoiner.add(value);
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
