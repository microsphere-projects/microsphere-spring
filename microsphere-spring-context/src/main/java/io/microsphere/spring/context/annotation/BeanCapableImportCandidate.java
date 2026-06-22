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
package io.microsphere.spring.context.annotation;

import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Map.Entry;

import static io.microsphere.constants.SymbolConstants.AT_CHAR;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asDefaultListableBeanFactory;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerGenericBean;
import static io.microsphere.spring.core.annotation.AnnotationUtils.ofAnnotationAttributes;
import static io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes.of;
import static io.microsphere.spring.core.env.EnvironmentUtils.asConfigurableEnvironment;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.StringUtils.EMPTY_STRING_ARRAY;
import static java.lang.Integer.toHexString;

/**
 * An abstract base class for {@link Import @Import} candidates that supports the full Spring bean lifecycle,
 * including population, initialization, and destruction.
 * <p>
 * Unlike standard {@link ImportSelector} or {@link ImportBeanDefinitionRegistrar} implementations, which are
 * typically instantiated and invoked without full bean lifecycle management, this class ensures that the
 * candidate instance is treated as a Spring-managed bean. It achieves this by implementing key awareness
 * interfaces ({@link BeanClassLoaderAware}, {@link BeanFactoryAware}, {@link EnvironmentAware},
 * {@link ApplicationContextAware}, and {@link ResourceLoaderAware}) and triggering self-registration
 * and initialization upon setting the {@link ResourceLoader}.
 * <p>
 * Subclasses must implement either {@link ImportSelector} or {@link ImportBeanDefinitionRegistrar} to define
 * their import logic. They can then leverage injected dependencies and lifecycle callbacks provided by the
 * Spring container, and can't override those methods:
 * <ul>
 *     <li>{@link #setBeanClassLoader(ClassLoader)}</li>
 *     <li>{@link #setBeanFactory(BeanFactory)}</li>
 *     <li>{@link #setEnvironment(Environment)}</li>
 *     <li>{@link #setResourceLoader(ResourceLoader)}</li>
 *     <li>{@link #setApplicationContext(ApplicationContext)}</li>
 * </ul>
 *
 * <h3>Key Features</h3>
 * <ul>
 *     <li>Supports dependency injection via {@link AbstractAutowireCapableBeanFactory#populateBean(String, RootBeanDefinition, BeanWrapper)}.</li>
 *     <li>Supports initialization callbacks via {@link AutowireCapableBeanFactory#initializeBean}.</li>
 *     <li>Provides access to {@link ConfigurableListableBeanFactory}, {@link ConfigurableEnvironment},
 *         {@link ConfigurableApplicationContext}, and {@link ResourceLoader}.</li>
 *     <li>Ensures the candidate is registered as a singleton bean in the context.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 *
 * <pre>{@code
 * // 1. Define a custom ImportSelector extending BeanCapableImportCandidate
 * public class MyImportSelector extends BeanCapableImportCandidate implements ImportSelector {
 *
 *     // You can inject dependencies here if needed, though typically done via getter methods
 *     // provided by the base class after initialization.
 *
 *     @Override
 *     public String[] selectImports(AnnotationMetadata importingClassMetadata) {
 *         // Access the environment to resolve placeholders or make decisions
 *         String profile = getEnvironment().getActiveProfiles().length > 0 ?
 *             getEnvironment().getActiveProfiles()[0] : "default";
 *
 *         logger.info("Selecting imports for profile: {}", profile);
 *
 *         // Return classes to import based on logic
 *         if ("prod".equals(profile)) {
 *             return new String[]{ProdConfig.class.getName()};
 *         } else {
 *             return new String[]{DevConfig.class.getName()};
 *         }
 *     }
 * }
 *
 * // 2. Use the selector in a configuration class
 * @Import(MyImportSelector.class)
 * @Configuration
 * public class AppConfig {
 *     // ...
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ImportSelector
 * @see ImportBeanDefinitionRegistrar
 * @see BeanClassLoaderAware
 * @see BeanFactoryAware
 * @see EnvironmentAware
 * @see ApplicationContextAware
 * @see ResourceLoaderAware
 * @see AbstractAutowireCapableBeanFactory#populateBean(String, RootBeanDefinition, BeanWrapper)
 * @see AutowireCapableBeanFactory#initializeBean
 * @since 1.0.0
 */
public abstract class BeanCapableImportCandidate implements BeanClassLoaderAware, BeanFactoryAware, EnvironmentAware,
        ApplicationContextAware, ResourceLoaderAware {

    /**
     * Indicates that no class to import
     */
    public static final String[] NO_CLASS_TO_IMPORT = EMPTY_STRING_ARRAY;

    protected final Logger logger = getLogger(this.getClass());

    protected ClassLoader classLoader;

    protected ConfigurableListableBeanFactory beanFactory;

    protected BeanDefinitionRegistry registry;

    protected ConfigurableApplicationContext applicationContext;

    protected ConfigurableEnvironment environment;

    protected ResourceLoader resourceLoader;

    /**
     * Sets the {@link ClassLoader} used by the bean. This method also asserts that the
     * subclass properly implements {@link ImportSelector} or {@link ImportBeanDefinitionRegistrar}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Automatically invoked by the Spring container during @Import processing.
     *   // For example, when using:
     *   @Import(MyImportSelector.class)
     *   public class AppConfig { }
     * }</pre>
     *
     * @param classLoader the owning class loader
     */
    @Override
    public final void setBeanClassLoader(ClassLoader classLoader) {
        if (this.classLoader == null) {
            this.classLoader = classLoader;
            assertImportCandidate();
        }
    }

    /**
     * Sets the {@link BeanFactory} that created this bean, stored as a
     * {@link ConfigurableListableBeanFactory}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Automatically invoked by the Spring container during @Import processing.
     *   // After this call, getBeanFactory() returns the ConfigurableListableBeanFactory.
     *   @Import(MyImportBeanDefinitionRegistrar.class)
     *   public class AppConfig { }
     * }</pre>
     *
     * @param beanFactory the owning {@link BeanFactory}
     * @throws BeansException if the bean factory cannot be cast to {@link ConfigurableListableBeanFactory}
     */
    @Override
    public final void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if (this.beanFactory == null) {
            DefaultListableBeanFactory defaultListableBeanFactory = asDefaultListableBeanFactory(beanFactory);
            this.beanFactory = defaultListableBeanFactory;
            this.registry = defaultListableBeanFactory;
        }
    }

    /**
     * Sets the {@link Environment} in which this bean operates, stored as a
     * {@link ConfigurableEnvironment}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Automatically invoked by the Spring container during @Import processing.
     *   // After this call, getEnvironment() returns the ConfigurableEnvironment.
     *   @Import(MyImportSelector.class)
     *   public class AppConfig { }
     * }</pre>
     *
     * @param environment the {@link Environment} for this bean
     */
    @Override
    public final void setEnvironment(Environment environment) {
        if (this.environment == null) {
            this.environment = asConfigurableEnvironment(environment);
        }
    }

    /**
     * Sets the {@link ResourceLoader} for this bean. This is the last callback in the
     * sequence, and triggers the self-initialization of this bean as a Spring-managed bean
     * to support full bean lifecycle (population, initialization, and destruction).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Automatically invoked by the Spring container during @Import processing.
     *   // After this call, getResourceLoader() returns the ResourceLoader and the
     *   // bean is fully initialized with Spring bean lifecycle support.
     *   @Import(MyImportBeanDefinitionRegistrar.class)
     *   public class AppConfig { }
     * }</pre>
     *
     * @param resourceLoader the {@link ResourceLoader} for this bean
     */
    @Override
    public final void setResourceLoader(ResourceLoader resourceLoader) {
        if (this.resourceLoader == null) {
            this.resourceLoader = resourceLoader;
            initializeSelfAsBean();
        }
    }

    @Override
    public final void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = (ConfigurableApplicationContext) applicationContext;
    }

    /**
     * Get the {@link ClassLoader} instance
     *
     * @return non-null
     */
    @Nonnull
    public final ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * The {@link ConfigurableListableBeanFactory} instance
     *
     * @return non-null
     */
    @Nonnull
    public final ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    /**
     * The {@link BeanDefinitionRegistry} instance
     *
     * @return non-null
     */
    @Nonnull
    public final BeanDefinitionRegistry getBeanDefinitionRegistry() {
        return registry;
    }

    /**
     * The {@link ConfigurableApplicationContext} instance
     *
     * @return non-null
     */
    @Nonnull
    public final ConfigurableApplicationContext getApplicationContext() {
        return this.applicationContext;
    }

    /**
     * The {@link ConfigurableEnvironment} instance
     *
     * @return non-null
     */
    @Nonnull
    public final ConfigurableEnvironment getEnvironment() {
        return environment;
    }

    /**
     * The {@link ResourceLoader} instance
     *
     * @return non-null
     */
    @Nonnull
    public final ResourceLoader getResourceLoader() {
        return resourceLoader;
    }

    /**
     * Retrieves the {@link ResolvablePlaceholderAnnotationAttributes} for the specified annotation type from the given metadata.
     * <p>
     * This method resolves placeholders in the annotation attributes using the current {@link Environment}.
     * It also checks for any {@link OverrideAnnotationAttributes} meta-annotation on the target annotation type
     * and applies the configured {@link OverrideAnnotationAttributesStrategy} if present.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Assume @MyConfig is a custom annotation with placeholder support
     * @MyConfig(value = "${my.config.value}")
     * @Import(MyImportSelector.class)
     * public class AppConfig { }
     *
     * public class MyImportSelector extends BeanCapableImportCandidate implements ImportSelector {
     *
     *     @Override
     *     public String[] selectImports(AnnotationMetadata importingClassMetadata) {
     *         // Retrieve resolved annotation attributes
     *         ResolvablePlaceholderAnnotationAttributes<MyConfig> attributes =
     *             getAnnotationAttributes(importingClassMetadata, MyConfig.class);
     *
     *         // Access the resolved value (placeholders replaced by environment properties)
     *         String value = attributes.getString("value");
     *         logger.info("Resolved config value: {}", value);
     *
     *         return new String[0];
     *     }
     * }
     * }</pre>
     *
     * @param metadata       the {@link AnnotationMetadata} of the importing class
     * @param annotationType the {@link Class} of the annotation to retrieve attributes for
     * @param <A>            the type of the annotation
     * @return the {@link ResolvablePlaceholderAnnotationAttributes} containing the resolved annotation attributes
     */
    @Nonnull
    protected <A extends Annotation> ResolvablePlaceholderAnnotationAttributes<A> getAnnotationAttributes(AnnotationMetadata metadata, Class<A> annotationType) {
        String annotationClassName = annotationType.getName();
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(annotationClassName);
        AnnotationAttributes overriddenAnnotationAttributes = getOverriddenAnnotationAttributes(annotationAttributes, annotationType, metadata);
        return of(overriddenAnnotationAttributes, annotationType, getEnvironment());
    }

    /**
     * Gets the overridden {@link AnnotationAttributes} for the specified annotation type.
     * <p>
     * If the annotation is meta-annotated with {@link OverrideAnnotationAttributes}, this method
     * instantiates the configured {@link OverrideAnnotationAttributesStrategy}, initializes it as a Spring bean,
     * and executes the strategy to override the original attributes. Otherwise, it returns the original attributes.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Define a custom annotation with an override strategy
     * @OverrideAnnotationAttributes(strategy = MyCustomStrategy.class)
     * @Import(MyImportRegistrar.class)
     * public @interface MyCustomImport {
     *     String value() default "";
     * }
     *
     * // In your ImportSelector or ImportBeanDefinitionRegistrar implementation:
     * public class MyImportRegistrar extends BeanCapableImportCandidate implements ImportBeanDefinitionRegistrar {
     *     public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
     *          // ...
     *     }
     * }
     * }</pre>
     *
     * @param annotationAttributes the original annotation attributes map, may be null
     * @param annotationType       the annotation type class
     * @param metadata             the annotation metadata of the importing class
     * @return the overridden {@link AnnotationAttributes}, or the original attributes if no override strategy is defined
     */
    protected AnnotationAttributes getOverriddenAnnotationAttributes(Map<String, Object> annotationAttributes,
                                                                     Class<? extends Annotation> annotationType,
                                                                     AnnotationMetadata metadata) {
        OverrideAnnotationAttributes overrideAnnotationAttributes = annotationType.getAnnotation(OverrideAnnotationAttributes.class);
        AnnotationAttributes originalAttributes = ofAnnotationAttributes(annotationAttributes);
        if (overrideAnnotationAttributes == null) {
            return originalAttributes;
        }
        Class<? extends OverrideAnnotationAttributesStrategy> strategyClass = overrideAnnotationAttributes.strategy();
        Entry<String, Boolean> nameAndRegistered = registerGenericBean(this.getBeanDefinitionRegistry(), strategyClass);
        String beanName = nameAndRegistered.getKey();
        OverrideAnnotationAttributesStrategy strategy = this.getBeanFactory().getBean(beanName, strategyClass);
        return strategy.override(originalAttributes, annotationType, metadata);
    }

    private void assertImportCandidate() {
        Class<?> klass = getClass();
        Class<?> interface1 = ImportSelector.class;
        Class<?> interface2 = ImportBeanDefinitionRegistrar.class;
        if (!interface1.isAssignableFrom(klass) && !interface2.isAssignableFrom(klass)) {
            String message = format("The @Import Candidate[class : '{}'] must implement the interface '{}' or '{}'", klass.getName(), interface1.getName(), interface2.getName());
            throw new IllegalStateException(message);
        }
    }

    private void initializeSelfAsBean() {
        String beanName = getClass().getName() + AT_CHAR + toHexString(hashCode());
        BeanDefinitionRegistry registry = this.getBeanDefinitionRegistry();
        // register the current instance as a Spring BeanDefinition
        registerBean(registry, beanName, this);
        // initialize bean from the BeanDefinition before registration
        BeanFactory beanFactory = this.getBeanFactory();
        beanFactory.getBean(beanName);
    }
}