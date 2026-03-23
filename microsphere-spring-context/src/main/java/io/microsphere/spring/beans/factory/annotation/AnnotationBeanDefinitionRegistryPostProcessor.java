/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.spring.beans.factory.annotation;

import io.microsphere.logging.Logger;
import io.microsphere.spring.context.annotation.ExposingClassPathBeanDefinitionScanner;
import io.microsphere.util.ArrayUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableListableBeanFactory;
import static io.microsphere.spring.context.annotation.AnnotatedBeanDefinitionRegistryUtils.resolveAnnotatedBeanNameGenerator;
import static io.microsphere.spring.core.annotation.AnnotationUtils.tryGetMergedAnnotation;
import static io.microsphere.spring.core.env.EnvironmentUtils.asConfigurableEnvironment;
import static io.microsphere.util.ArrayUtils.arrayToString;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.addAll;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.Assert.noNullElements;
import static org.springframework.util.Assert.notEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * An abstract class for the extension to {@link BeanDefinitionRegistryPostProcessor}, which will execute two main registration
 * methods orderly:
 * <ol>
 *     <li>{@link #registerMainBeanDefinitions(ExposingClassPathBeanDefinitionScanner, String[])} : Scan and register
 *     the main {@link BeanDefinition BeanDefinitions} that were annotated by
 *     {@link #getSupportedAnnotationTypes() the supported annotation types}, and then return the {@link Map} with bean name plus
 *     aliases if present and main {@link AnnotatedBeanDefinition AnnotatedBeanDefinitions},
 *     it's allowed to be override
 *     </li>
 *     <li>{@link #registerExtendedBeanDefinitions(ExposingClassPathBeanDefinitionScanner, Map, String[])} :
 *      it's mandatory to be override by the sub-class to register secondary {@link BeanDefinition BeanDefinitions}
 *      if required
 *     </li>
 * </ol>
 *
 * <h3>Example Usage</h3>
 *
 * <pre>{@code
 * public class MyAnnotationBeanDefinitionRegistryPostProcessor extends AnnotationBeanDefinitionRegistryPostProcessor {
 *
 *     public MyAnnotationBeanDefinitionRegistryPostProcessor() {
 *         super(MyAnnotation.class, "com.example.package");
 *     }
 *
 *     protected Map<String, AnnotatedBeanDefinition> registerMainBeanDefinitions(ExposingClassPathBeanDefinitionScanner scanner,
 *                                                                                   String[] basePackages) {
 *         // Custom logic to register main bean definitions
 *         return super.registerMainBeanDefinitions(scanner, basePackages);
 *     }
 *
 *     @Override
 *     protected void registerExtendedBeanDefinitions(ExposingClassPathBeanDefinitionScanner scanner,
 *                                                    Map<String, AnnotatedBeanDefinition> mainBeanDefinitions,
 *                                                    String[] basePackages) {
 *         // Logic to register extended bean definitions based on main ones
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public abstract class AnnotationBeanDefinitionRegistryPostProcessor implements BeanDefinitionRegistryPostProcessor,
        BeanFactoryAware, EnvironmentAware, ResourceLoaderAware, BeanClassLoaderAware {

    protected final Logger logger = getLogger(getClass());

    private final Set<Class<? extends Annotation>> supportedAnnotationTypes;

    private final Set<String> packagesToScan;

    private ConfigurableListableBeanFactory beanFactory;

    private ConfigurableEnvironment environment;

    private ResourceLoader resourceLoader;

    private ClassLoader classLoader;

    public AnnotationBeanDefinitionRegistryPostProcessor(Class<? extends Annotation> annotationType,
                                                         Class<?>... basePackageClasses) {
        this(annotationType, resolveBasePackages(basePackageClasses));
    }

    public AnnotationBeanDefinitionRegistryPostProcessor(Class<? extends Annotation> annotationType,
                                                         String... packagesToScan) {
        this(annotationType, asList(packagesToScan));
    }

    public AnnotationBeanDefinitionRegistryPostProcessor(Class<? extends Annotation> annotationType,
                                                         Iterable<String> packagesToScan) {
        this.supportedAnnotationTypes = newLinkedHashSet();
        this.packagesToScan = newLinkedHashSet(packagesToScan);
        addSupportedAnnotationType(annotationType);
    }

    public void addSupportedAnnotationType(Class<? extends Annotation>... annotationTypes) {
        notEmpty(annotationTypes, "The argument of annotation types can't be empty");
        noNullElements(annotationTypes, "Any element of annotation types can't be null");
        addAll(this.supportedAnnotationTypes, annotationTypes);
    }

    static String[] resolveBasePackages(Class<?>... basePackageClasses) {
        int size = basePackageClasses.length;
        String[] basePackages = new String[size];
        for (int i = 0; i < size; i++) {
            basePackages[i] = basePackageClasses[i].getPackage().getName();
        }
        return basePackages;
    }

    protected static Annotation getAnnotation(AnnotatedElement annotatedElement, Class<? extends Annotation> annotationType) {
        return tryGetMergedAnnotation(annotatedElement, annotationType);
    }

    @Override
    public final void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        String[] basePackages = resolveBasePackages(getPackagesToScan());
        registerBeanDefinitions(registry, basePackages);
    }

    protected void registerBeanDefinitions(BeanDefinitionRegistry registry, String... basePackages) {
        if (ArrayUtils.isEmpty(basePackages)) {
            if (logger.isWarnEnabled()) {
                logger.warn("The 'packagesToScan' is empty , The BeanDefinition's registry will be ignored!");
            }
            return;
        }

        ExposingClassPathBeanDefinitionScanner scanner = new ExposingClassPathBeanDefinitionScanner(registry, false,
                getEnvironment(), getResourceLoader());

        BeanNameGenerator beanNameGenerator = resolveAnnotatedBeanNameGenerator(registry);
        // Set the BeanNameGenerator
        scanner.setBeanNameGenerator(beanNameGenerator);
        // Add the AnnotationTypeFilter for annotationTypes
        for (Class<? extends Annotation> supportedAnnotationType : getSupportedAnnotationTypes()) {
            scanner.addIncludeFilter(new AnnotationTypeFilter(supportedAnnotationType));
        }
        // Register the main BeanDefinitions
        Map<String, AnnotatedBeanDefinition> mainBeanDefinitions = registerMainBeanDefinitions(scanner, basePackages);
        // Register the extended BeanDefinitions
        registerExtendedBeanDefinitions(scanner, mainBeanDefinitions, basePackages);
    }

    /**
     * Scan and register the main {@link BeanDefinition BeanDefinitions} that were annotated by
     * {@link #getSupportedAnnotationTypes() the supported annotation types}, and then return the {@link Map} with bean name plus
     * aliases if present and main {@link AnnotatedBeanDefinition AnnotatedBeanDefinitions}.
     * <p>
     * Current method is allowed to be override by the sub-class to change the registration logic
     *
     * @param scanner      {@link ExposingClassPathBeanDefinitionScanner}
     * @param basePackages the base packages to scan
     * @return the {@link Map} with bean name plus aliases if present and main
     * {@link AnnotatedBeanDefinition AnnotatedBeanDefinitions}
     */
    protected Map<String, AnnotatedBeanDefinition> registerMainBeanDefinitions(ExposingClassPathBeanDefinitionScanner scanner,
                                                                               String[] basePackages) {
        // Scan and register
        Set<BeanDefinitionHolder> mainBeanDefinitionHolders = scanner.doScan(basePackages);
        // Log the main BeanDefinitions
        logBeanDefinitions(mainBeanDefinitionHolders, basePackages);

        Map<String, AnnotatedBeanDefinition> mainBeanDefinitions = newLinkedHashMap();

        for (BeanDefinitionHolder beanDefinitionHolder : mainBeanDefinitionHolders) {
            putBeanDefinitions(mainBeanDefinitions, beanDefinitionHolder);
        }

        // return
        return mainBeanDefinitions;
    }

    void putBeanDefinitions(Map<String, AnnotatedBeanDefinition> mainBeanDefinitions,
                            BeanDefinitionHolder beanDefinitionHolder) {
        BeanDefinition beanDefinition = beanDefinitionHolder.getBeanDefinition();

        if (beanDefinition instanceof AnnotatedBeanDefinition) {
            AnnotatedBeanDefinition annotatedBeanDefinition = (AnnotatedBeanDefinition) beanDefinition;
            putBeanDefinition(mainBeanDefinitions, annotatedBeanDefinition, beanDefinitionHolder.getBeanName());
            putBeanDefinition(mainBeanDefinitions, annotatedBeanDefinition, beanDefinitionHolder.getAliases());
        } else {
            if (logger.isErrorEnabled()) {
                logger.error("What's the problem? Please investigate " + beanDefinitionHolder);
            }
        }
    }

    private void putBeanDefinition(Map<String, AnnotatedBeanDefinition> mainBeanDefinitions,
                                   AnnotatedBeanDefinition annotatedBeanDefinition, String... keys) {
        if (isNotEmpty(keys)) {
            for (String key : keys) {
                mainBeanDefinitions.put(key, annotatedBeanDefinition);
            }
        }
    }

    /**
     * Register the extended {@link BeanDefinition BeanDefinitions}
     * <p>
     * Current method is allowed to be override by the sub-class to change the registration logic
     *
     * @param scanner             the {@link ExposingClassPathBeanDefinitionScanner} instance
     * @param mainBeanDefinitions the {@link Map} with bean name plus aliases if present and main
     *                            {@link AnnotatedBeanDefinition AnnotatedBeanDefinitions}, which may be empty
     * @param basePackages        the base packages to scan
     */
    protected abstract void registerExtendedBeanDefinitions(ExposingClassPathBeanDefinitionScanner scanner,
                                                            Map<String, AnnotatedBeanDefinition> mainBeanDefinitions,
                                                            String[] basePackages);

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // DO NOTHING
    }

    void logBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitionHolders, String... basePackages) {
        if (isEmpty(beanDefinitionHolders)) {
            if (logger.isWarnEnabled()) {
                logger.warn("No Spring Bean annotation @{} was found under base packages : {}",
                        getSupportedAnnotationTypeNames(), arrayToString(basePackages));
            }
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("{} annotations {} components { {} } were scanned under packages : {}", beanDefinitionHolders.size(),
                        getSupportedAnnotationTypeNames(), beanDefinitionHolders, arrayToString(basePackages));
            }
        }
    }

    /**
     * Resolve the placeholders for the raw scanned packages to scan
     *
     * @param packagesToScan the raw scanned packages to scan
     * @return non-null
     */
    protected String[] resolveBasePackages(Set<String> packagesToScan) {
        return packagesToScan.stream()
                .map(getEnvironment()::resolvePlaceholders)
                .filter(StringUtils::hasText)
                .toArray(String[]::new);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Set<Class<? extends Annotation>> getSupportedAnnotationTypes() {
        return unmodifiableSet(this.supportedAnnotationTypes);
    }

    protected Set<String> getSupportedAnnotationTypeNames() {
        Set<String> supportedAnnotationTypeNames = getSupportedAnnotationTypes()
                .stream()
                .map(Class::getName)
                .collect(toSet());
        return unmodifiableSet(supportedAnnotationTypeNames);
    }

    public Set<String> getPackagesToScan() {
        return this.packagesToScan;
    }

    public ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = asConfigurableListableBeanFactory(beanFactory);
    }

    public ConfigurableEnvironment getEnvironment() {
        return this.environment;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = asConfigurableEnvironment(environment);
    }

    public ResourceLoader getResourceLoader() {
        return this.resourceLoader;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }
}