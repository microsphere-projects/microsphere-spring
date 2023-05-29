package io.microsphere.spring.test;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * Class {@link ImportBeanDefinitionRegistrar}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class ClassImportBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private final String[] beanClassNames;

    private final BeanNameGenerator beanNameGenerator;

    public ClassImportBeanDefinitionRegistrar(String[] beanClassNames) {
        this.beanClassNames = beanClassNames;
        this.beanNameGenerator = new AnnotationBeanNameGenerator();
    }

    @Override
    public final void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        for (String beanClassName : beanClassNames) {
            registerBeanDefinition(beanClassName, registry);
        }
    }

    private void registerBeanDefinition(String beanClassName, BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanClassName);
        BeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }
}
