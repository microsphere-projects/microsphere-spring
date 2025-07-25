package io.microsphere.spring.context.annotation;

import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static io.microsphere.collection.SetUtils.newLinkedHashSet;
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
 * @since 1.0.0
 */
public abstract class AnnotatedBeanDefinitionRegistryUtils implements Utils {

    private static final Logger logger = getLogger(AnnotatedBeanDefinitionRegistryUtils.class);

    /**
     * Checks whether a bean defined by the specified annotated class is already present in the registry.
     *
     * <p>This method iterates over all bean definitions in the registry and compares the class of the
     * annotation metadata with the provided class. If a match is found, it returns true, indicating
     * that the bean is already registered.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean isBeanPresent = AnnotatedBeanDefinitionRegistryUtils.isPresentBean(registry, MyService.class);
     * if (isBeanPresent) {
     *     System.out.println("MyService is already registered.");
     * } else {
     *     System.out.println("MyService is not registered yet.");
     * }
     * }</pre>
     *
     * @param registry       the {@link BeanDefinitionRegistry} to check for the presence of the bean
     * @param annotatedClass the annotated class to check in the registry
     * @return true if the bean defined by the annotated class is present, false otherwise
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
     * Registers the specified annotated classes as beans in the given {@link BeanDefinitionRegistry},
     * if they are not already present.
     *
     * <p>This method ensures idempotent registration by first checking whether each class is already registered
     * using the {@link #isPresentBean(BeanDefinitionRegistry, Class)} method. Only those classes that are not
     * yet registered will be processed for bean registration.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Register MyService and MyRepository if not already registered
     * AnnotatedBeanDefinitionRegistryUtils.registerBeans(registry, MyService.class, MyRepository.class);
     * }</pre>
     *
     * <p>If the provided array of classes is empty or null, this method will return immediately without performing
     * any operations.
     *
     * @param registry         the {@link BeanDefinitionRegistry} where beans will be registered
     * @param annotatedClasses one or more annotated classes to register as beans if not already present
     */
    /**
     * Registers the specified annotated classes as beans in the given {@link BeanDefinitionRegistry},
     * if they are not already present.
     *
     * <p>This method ensures idempotent registration by first checking whether each class is already registered
     * using the {@link #isPresentBean(BeanDefinitionRegistry, Class)} method. Only those classes that are not
     * yet registered will be processed for bean registration.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Register MyService and MyRepository if not already registered
     * AnnotatedBeanDefinitionRegistryUtils.registerBeans(registry, MyService.class, MyRepository.class);
     * }</pre>
     *
     * <p>If the provided array of classes is empty or null, this method will return immediately without performing
     * any operations.
     *
     * @param registry         the {@link BeanDefinitionRegistry} where beans will be registered
     * @param annotatedClasses one or more annotated classes to register as beans if not already present
     */
    public static void registerBeans(BeanDefinitionRegistry registry, Class<?>... annotatedClasses) {

        if (isEmpty(annotatedClasses)) {
            return;
        }

        Set<Class<?>> classesToRegister = newLinkedHashSet(annotatedClasses);

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
     * Scans the specified base packages for Spring components annotated with stereotypes such as
     * {@link Component @Component}, and registers them as beans in the provided registry.
     *
     * <p>This method returns the number of beans that were registered during the scan. It ensures idempotent
     * behavior by logging the scanned components at TRACE level if enabled.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int componentCount = AnnotatedBeanDefinitionRegistryUtils.scanBasePackages(registry, "com.example.app");
     * System.out.println("Registered " + componentCount + " components.");
     * }</pre>
     *
     * <p>If the provided array of package names is empty or null, this method will return 0 without performing
     * any operations.
     *
     * @param registry     the {@link BeanDefinitionRegistry} where beans will be registered
     * @param basePackages one or more package names to scan for components
     * @return the number of beans registered from the scanned packages
     */
    public static int scanBasePackages(BeanDefinitionRegistry registry, String... basePackages) {

        int count = 0;

        if (isNotEmpty(basePackages)) {

            boolean traceEnabled = logger.isTraceEnabled();

            if (traceEnabled) {
                logger.trace(registry.getClass().getSimpleName() + " will scan base packages " + asList(basePackages) + ".");
            }

            List<String> registeredBeanNames = asList(registry.getBeanDefinitionNames());

            ClassPathBeanDefinitionScanner classPathBeanDefinitionScanner = new ClassPathBeanDefinitionScanner(registry);
            count = classPathBeanDefinitionScanner.scan(basePackages);

            List<String> scannedBeanNames = new ArrayList<String>(count);
            scannedBeanNames.addAll(asList(registry.getBeanDefinitionNames()));
            scannedBeanNames.removeAll(registeredBeanNames);

            if (traceEnabled) {
                logger.trace("The Scanned Components[ count : " + count + "] under base packages " + asList(basePackages) + " : ");
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
     * Resolves the appropriate {@link BeanNameGenerator} instance for generating bean names during annotation-based configuration.
     * <p>
     * It'd better to use BeanNameGenerator instance that should reference
     * {@link ConfigurationClassPostProcessor#componentScanBeanNameGenerator},
     * thus it maybe a potential problem on bean name generation.
     *
     * <p>This method attempts to retrieve an existing {@link BeanNameGenerator} from the registry if it implements
     * the {@link SingletonBeanRegistry} interface. The bean name generator is typically named
     * {@link AnnotationConfigUtils#CONFIGURATION_BEAN_NAME_GENERATOR}. If it cannot be found, a new instance of
     * {@link AnnotationBeanNameGenerator} is created and returned as a fallback.</p>
     *
     * <p><strong>Note:</strong> It is preferable to use the shared instance from the registry (if available),
     * such as the one used by Spring's {@link ConfigurationClassPostProcessor}, to ensure consistent bean naming.
     * Failing to do so may lead to discrepancies in bean name generation.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * BeanNameGenerator beanNameGenerator = AnnotatedBeanDefinitionRegistryUtils.resolveAnnotatedBeanNameGenerator(registry);
     * }</pre>
     *
     * @param registry the {@link BeanDefinitionRegistry} used to look up or create a bean name generator
     * @return a non-null instance of {@link BeanNameGenerator}
     * @see AnnotationConfigUtils#CONFIGURATION_BEAN_NAME_GENERATOR
     * @see AnnotationBeanNameGenerator
     * @see ConfigurationClassPostProcessor#processConfigBeanDefinitions
     * @see SingletonBeanRegistry
     */
    @Nonnull
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
     * Scans the specified package for candidate components (beans) using the provided scanner,
     * generates bean names using the given bean name generator, and returns a set of
     * {@link BeanDefinitionHolder} objects encapsulating the found bean definitions.
     *
     * <p>This method is typically used during component scanning to locate beans annotated with Spring stereotypes
     * such as {@link Component @Component}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry);
     * BeanNameGenerator beanNameGenerator = AnnotatedBeanDefinitionRegistryUtils.resolveAnnotatedBeanNameGenerator(registry);
     * Set<BeanDefinitionHolder> holders = AnnotatedBeanDefinitionRegistryUtils.findBeanDefinitionHolders(scanner, "com.example.app", registry, beanNameGenerator);
     * }</pre>
     *
     * @param scanner           the {@link ClassPathBeanDefinitionScanner} used to scan for components
     * @param packageToScan     the package to scan for Spring components
     * @param registry          the {@link BeanDefinitionRegistry} used to generate and register bean names
     * @param beanNameGenerator the {@link BeanNameGenerator} used to generate bean names for discovered components
     * @return a non-null set of {@link BeanDefinitionHolder} instances representing the discovered bean definitions
     */
    @Nonnull
    public static Set<BeanDefinitionHolder> findBeanDefinitionHolders(ClassPathBeanDefinitionScanner scanner,
                                                                      String packageToScan,
                                                                      BeanDefinitionRegistry registry,
                                                                      BeanNameGenerator beanNameGenerator) {
        Set<BeanDefinition> beanDefinitions = scanner.findCandidateComponents(packageToScan);
        Set<BeanDefinitionHolder> beanDefinitionHolders = newLinkedHashSet(beanDefinitions.size());
        for (BeanDefinition beanDefinition : beanDefinitions) {
            String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
            BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, beanName);
            beanDefinitionHolders.add(beanDefinitionHolder);
        }
        return beanDefinitionHolders;
    }

    private AnnotatedBeanDefinitionRegistryUtils() {
    }
}
