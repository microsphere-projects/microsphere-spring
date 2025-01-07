package io.microsphere.spring.context.annotation;

import io.microsphere.logging.Logger;
import io.microsphere.util.BaseUtils;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static java.util.Arrays.asList;
import static org.springframework.context.annotation.AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.ObjectUtils.nullSafeEquals;

/**
 * Annotated {@link BeanDefinition} Utilities
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanDefinition
 * @since 2017.01.09
 */
public abstract class AnnotatedBeanDefinitionRegistryUtils extends BaseUtils {

    private static final Logger logger = getLogger(AnnotatedBeanDefinitionRegistryUtils.class);

    /**
     * Is present bean that was registered by the specified {@link Annotation annotated} {@link Class class}
     *
     * @param registry       {@link BeanDefinitionRegistry}
     * @param annotatedClass the {@link Annotation annotated} {@link Class class}
     * @return if present, return <code>true</code>, or <code>false</code>
     */
    public static boolean isPresentBean(BeanDefinitionRegistry registry, Class<?> annotatedClass) {

        boolean present = false;

        String[] beanNames = registry.getBeanDefinitionNames();

        ClassLoader classLoader = annotatedClass.getClassLoader();

        for (String beanName : beanNames) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            if (beanDefinition instanceof AnnotatedBeanDefinition) {
                AnnotationMetadata annotationMetadata = ((AnnotatedBeanDefinition) beanDefinition).getMetadata();
                String className = annotationMetadata.getClassName();
                Class<?> targetClass = resolveClassName(className, classLoader);
                present = nullSafeEquals(targetClass, annotatedClass);
                if (present) {
                    if (logger.isTraceEnabled()) {
                        logger.trace("The annotatedClass[class : '{}' , bean name : '{}'] was present in registry : {}",
                                className, beanName, registry);
                    }
                    break;
                }
            }
        }

        return present;
    }

    /**
     * Register Beans if not present in {@link BeanDefinitionRegistry registry}
     *
     * @param registry         {@link BeanDefinitionRegistry}
     * @param annotatedClasses {@link Annotation annotation} class
     */
    public static void registerBeans(BeanDefinitionRegistry registry, Class<?>... annotatedClasses) {

        if (isEmpty(annotatedClasses)) {
            return;
        }

        Set<Class<?>> classesToRegister = new LinkedHashSet<Class<?>>(asList(annotatedClasses));

        // Remove all annotated-classes that have been registered
        Iterator<Class<?>> iterator = classesToRegister.iterator();

        while (iterator.hasNext()) {
            Class<?> annotatedClass = iterator.next();
            if (isPresentBean(registry, annotatedClass)) {
                iterator.remove();
            }
        }

        AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(registry);

        if (logger.isTraceEnabled()) {
            logger.trace(registry.getClass().getSimpleName() + " will register annotated classes : " + asList(annotatedClasses) + " .");
        }

        reader.register(classesToRegister.toArray(EMPTY_CLASS_ARRAY));

    }

    /**
     * Scan base packages for register {@link Component @Component}s
     *
     * @param registry     {@link BeanDefinitionRegistry}
     * @param basePackages base packages
     * @return the count of registered components.
     */
    public static int scanBasePackages(BeanDefinitionRegistry registry, String... basePackages) {

        int count = 0;

        if (isNotEmpty(basePackages)) {

            boolean traceEnabled = logger.isTraceEnabled();

            if (traceEnabled) {
                logger.trace(registry.getClass().getSimpleName() + " will scan base packages " + Arrays.asList(basePackages) + ".");
            }

            List<String> registeredBeanNames = Arrays.asList(registry.getBeanDefinitionNames());

            ClassPathBeanDefinitionScanner classPathBeanDefinitionScanner = new ClassPathBeanDefinitionScanner(registry);
            count = classPathBeanDefinitionScanner.scan(basePackages);

            List<String> scannedBeanNames = new ArrayList<String>(count);
            scannedBeanNames.addAll(Arrays.asList(registry.getBeanDefinitionNames()));
            scannedBeanNames.removeAll(registeredBeanNames);

            if (traceEnabled) {
                logger.trace("The Scanned Components[ count : " + count + "] under base packages " + Arrays.asList(basePackages) + " : ");
            }

            for (String scannedBeanName : scannedBeanNames) {
                BeanDefinition scannedBeanDefinition = registry.getBeanDefinition(scannedBeanName);
                if (traceEnabled) {
                    logger.trace("Component [ name : " + scannedBeanName + " , class : " + scannedBeanDefinition.getBeanClassName() + " ]");
                }
            }
        }

        return count;

    }

    /**
     * It'd better to use BeanNameGenerator instance that should reference
     * {@link ConfigurationClassPostProcessor#componentScanBeanNameGenerator},
     * thus it maybe a potential problem on bean name generation.
     *
     * @param registry {@link BeanDefinitionRegistry}
     * @return try to find the {@link BeanNameGenerator} bean named {@link AnnotationConfigUtils#CONFIGURATION_BEAN_NAME_GENERATOR},
     * if it can't be found, return an instance of {@link AnnotationBeanNameGenerator}
     * @see SingletonBeanRegistry
     * @see AnnotationConfigUtils#CONFIGURATION_BEAN_NAME_GENERATOR
     * @see ConfigurationClassPostProcessor#processConfigBeanDefinitions
     */
    public static BeanNameGenerator resolveAnnotatedBeanNameGenerator(BeanDefinitionRegistry registry) {
        BeanNameGenerator beanNameGenerator = null;

        if (registry instanceof SingletonBeanRegistry) {
            SingletonBeanRegistry singletonBeanRegistry = SingletonBeanRegistry.class.cast(registry);
            beanNameGenerator = (BeanNameGenerator) singletonBeanRegistry.getSingleton(CONFIGURATION_BEAN_NAME_GENERATOR);
        }

        if (beanNameGenerator == null) {

            if (logger.isInfoEnabled()) {

                logger.info("BeanNameGenerator bean can't be found in BeanFactory with name ["
                        + CONFIGURATION_BEAN_NAME_GENERATOR + "]");
                logger.info("BeanNameGenerator will be a instance of " +
                        AnnotationBeanNameGenerator.class.getName() +
                        " , it maybe a potential problem on bean name generation.");
            }

            beanNameGenerator = new AnnotationBeanNameGenerator();

        }

        return beanNameGenerator;
    }

    /**
     * Finds a {@link Set} of {@link BeanDefinitionHolder BeanDefinitionHolders}
     *
     * @param scanner           {@link ClassPathBeanDefinitionScanner}
     * @param packageToScan     package to scan
     * @param registry          {@link BeanDefinitionRegistry}
     * @param beanNameGenerator {@link BeanNameGenerator}
     * @return non-null
     */
    public static Set<BeanDefinitionHolder> findBeanDefinitionHolders(ClassPathBeanDefinitionScanner scanner,
                                                                      String packageToScan,
                                                                      BeanDefinitionRegistry registry,
                                                                      BeanNameGenerator beanNameGenerator) {

        Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(packageToScan);

        Set<BeanDefinitionHolder> beanDefinitionHolders = new LinkedHashSet<BeanDefinitionHolder>(beanDefinitions.size());

        for (BeanDefinition beanDefinition : beanDefinitions) {

            String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
            beanDefinitionHolders.add(beanDefinitionHolder);

        }

        return beanDefinitionHolders;

    }
}
