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

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.spring.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.UnsatisfiedDependencyException;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.beans.factory.annotation.InjectionMetadata;
import org.springframework.beans.factory.annotation.InjectionMetadata.InjectedElement;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.MethodParameter;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.MapUtils.newConcurrentHashMap;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.FieldUtils.setFieldValue;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.spring.beans.BeanUtils.findPrimaryConstructor;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableListableBeanFactory;
import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.ArrayUtils.combine;
import static java.lang.Integer.getInteger;
import static java.lang.Integer.parseInt;
import static java.lang.reflect.Modifier.isStatic;
import static java.util.Arrays.copyOf;
import static java.util.Collections.unmodifiableCollection;
import static org.springframework.beans.BeanUtils.findPropertyForMethod;
import static org.springframework.beans.factory.annotation.InjectionMetadata.needsRefresh;
import static org.springframework.core.BridgeMethodResolver.findBridgedMethod;
import static org.springframework.core.BridgeMethodResolver.isVisibilityBridgeMethodPair;
import static org.springframework.util.Assert.notEmpty;
import static org.springframework.util.ClassUtils.getMostSpecificMethod;
import static org.springframework.util.ClassUtils.getUserClass;
import static org.springframework.util.ReflectionUtils.doWithFields;
import static org.springframework.util.ReflectionUtils.doWithMethods;
import static org.springframework.util.StringUtils.hasLength;

/**
 * A {@link BeanPostProcessor} implementation that provides dependency injection support for custom annotation types.
 * <p>
 * This class extends {@link InstantiationAwareBeanPostProcessorAdapter}, making it compatible with Spring 6.x+,
 * and supports the following features:
 * </p>
 *
 * <ul>
 *     <li><b>Custom Annotation Injection</b>: Allows dependency injection based on one or more custom annotations.</li>
 *     <li><b>Annotation Processing Options</b>:
 *         <ul>
 *             <li>{@link #setClassValuesAsString(boolean)}: Whether to convert Class references to Strings for compatibility with {@link org.springframework.core.type.AnnotationMetadata}.</li>
 *             <li>{@link #setNestedAnnotationsAsMap(boolean)}: Whether to convert nested annotations into {@link AnnotationAttributes} maps.</li>
 *             <li>{@link #setIgnoreDefaultValue(boolean)}: Whether to ignore default values from annotations.</li>
 *             <li>{@link #setTryMergedAnnotation(boolean)}: Whether to attempt resolving merged annotations.</li>
 *         </ul>
 *     </li>
 *     <li><b>Caching Mechanism</b>: Metadata such as injection points and constructor information is cached to improve performance.</li>
 *     <li><b>Integration with Spring Container</b>: Implements various Spring extension interfaces like
 *         {@link MergedBeanDefinitionPostProcessor}, {@link BeanFactoryAware}, and {@link EnvironmentAware}.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 *
 * <h4>Defining a Custom Injection Annotation</h4>
 * <pre>{@code
 * @Retention(RetentionPolicy.RUNTIME)
 * @Target(ElementType.FIELD)
 * public @interface MyInject {}
 * }</pre>
 *
 * <h4>Registering the Processor in Spring Configuration</h4>
 * <pre>{@code
 * @Configuration
 * public class AppConfig {
 *
 *     @Bean
 *     public AnnotatedInjectionBeanPostProcessor myInjectBeanPostProcessor() {
 *         return new AnnotatedInjectionBeanPostProcessor(MyInject.class);
 *     }
 * }
 * }</pre>
 *
 * <h4>Using the Custom Injected Field</h4>
 * <pre>{@code
 * public class MyService {
 *     @MyInject
 *     private Dependency dependency;
 * }
 * }</pre>
 *
 * <h4>Customizing Behavior via Configuration</h4>
 * <pre>{@code
 * @Bean
 * public AnnotatedInjectionBeanPostProcessor myInjectBeanPostProcessor() {
 *     AnnotatedInjectionBeanPostProcessor processor = new AnnotatedInjectionBeanPostProcessor(MyInject.class);
 *     processor.setClassValuesAsString(true);
 *     processor.setNestedAnnotationsAsMap(true);
 *     processor.setCacheSize(128);
 *     return processor;
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AutowiredAnnotationBeanPostProcessor
 * @since 1.0.0
 */
public class AnnotatedInjectionBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
        implements MergedBeanDefinitionPostProcessor, PriorityOrdered, BeanFactoryAware, BeanClassLoaderAware,
        EnvironmentAware, InitializingBean, DisposableBean {

    /**
     * The property name of metadata cache size : "microsphere.spring.injection.metadata.cache.size"
     */
    public static final String CACHE_SIZE_PROPERTY_NAME = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "injection.metadata.cache.size";

    /**
     * The default cache size of metadata cache : "32"
     */
    public static final String DEFAULT_CACHE_SIZE_PROPERTY_VALUE = "32";

    /**
     * The property name of metadata cache expire time : "microsphere.spring.injection.metadata.cache.expire.time"
     */
    public static final int DEFAULT_CACHE_SIZE = parseInt(DEFAULT_CACHE_SIZE_PROPERTY_VALUE);

    @ConfigurationProperty(
            name = CACHE_SIZE_PROPERTY_NAME,
            defaultValue = DEFAULT_CACHE_SIZE_PROPERTY_VALUE,
            description = "The size of metadata cache"
    )
    public final static int CACHE_SIZE = getInteger(CACHE_SIZE_PROPERTY_NAME, DEFAULT_CACHE_SIZE);

    private final Logger logger = getLogger(getClass());

    private final Collection<Class<? extends Annotation>> annotationTypes;

    private ConcurrentMap<Class<?>, Constructor<?>[]> candidateConstructorsCache;

    private ConcurrentMap<String, InjectionMetadata> injectionMetadataCache;

    private ConfigurableListableBeanFactory beanFactory;

    private Environment environment;

    private ClassLoader classLoader;

    /**
     * make sure higher priority than {@link AutowiredAnnotationBeanPostProcessor}
     */
    private int order;

    /**
     * whether to turn Class references into Strings (for compatibility with {@link AnnotationMetadata} or to
     * preserve them as Class references
     */
    private boolean classValuesAsString;

    /**
     * whether to turn nested Annotation instances into {@link AnnotationAttributes} maps (for compatibility with
     * {@link AnnotationMetadata} or to preserve them as Annotation instances
     */
    private boolean nestedAnnotationsAsMap;

    /**
     * whether ignore default value or not
     */
    private boolean ignoreDefaultValue;

    /**
     * whether try merged annotation or not
     */
    private boolean tryMergedAnnotation;

    /**
     * The size of cache
     */
    private int cacheSize;

    /**
     * @param annotationType the single type of {@link Annotation annotation}
     */
    public AnnotatedInjectionBeanPostProcessor(Class<? extends Annotation> annotationType, Class<? extends Annotation>... otherAnnotationTypes) {
        this(ofList(combine(annotationType, otherAnnotationTypes)));
    }

    /**
     * @param annotationTypes the multiple types of {@link Annotation annotations}
     */
    public AnnotatedInjectionBeanPostProcessor(Collection<Class<? extends Annotation>> annotationTypes) {
        notEmpty(annotationTypes, "The argument of annotations' types must not empty");
        this.annotationTypes = annotationTypes;
        setOrder(LOWEST_PRECEDENCE - 3);
        setClassValuesAsString(false);
        setNestedAnnotationsAsMap(false);
        setIgnoreDefaultValue(true);
        setTryMergedAnnotation(true);
        setCacheSize(CACHE_SIZE);
    }

    @Override
    public final Constructor<?>[] determineCandidateConstructors(Class<?> beanClass, String beanName) throws BeansException {
        Constructor<?>[] candidateConstructors = this.candidateConstructorsCache.get(beanClass);
        if (candidateConstructors == null) {
            // Fully synchronized resolution now...
            synchronized (this.candidateConstructorsCache) {
                candidateConstructors = this.candidateConstructorsCache.get(beanClass);
                if (candidateConstructors == null) {
                    Constructor<?>[] rawCandidates;
                    try {
                        rawCandidates = beanClass.getDeclaredConstructors();
                    } catch (Throwable ex) {
                        throw new BeanCreationException(beanName,
                                "Resolution of declared constructors on bean Class [" + beanClass.getName() +
                                        "] from ClassLoader [" + beanClass.getClassLoader() + "] failed", ex);
                    }
                    List<Constructor<?>> candidates = new ArrayList<>(rawCandidates.length);
                    Constructor<?> requiredConstructor = null;
                    Constructor<?> defaultConstructor = null;
                    Constructor<?> primaryConstructor = findPrimaryConstructor(beanClass);
                    int nonSyntheticConstructors = 0;
                    for (Constructor<?> candidate : rawCandidates) {
                        if (!candidate.isSynthetic()) {
                            nonSyntheticConstructors++;
                        } else if (primaryConstructor != null) {
                            continue;
                        }
                        AnnotationAttributes ann = findInjectionAnnotationAttributes(candidate);
                        if (ann == null) {
                            Class<?> userClass = getUserClass(beanClass);
                            if (userClass != beanClass) {
                                try {
                                    Constructor<?> superCtor = userClass.getDeclaredConstructor(candidate.getParameterTypes());
                                    ann = findInjectionAnnotationAttributes(superCtor);
                                } catch (NoSuchMethodException ex) {
                                    // Simply proceed, no equivalent superclass constructor found...
                                }
                            }
                        }
                        if (ann != null) {
                            if (requiredConstructor != null) {
                                String message = format("Invalid injection constructors: {}. "
                                                + "Found constructor with 'required' @{} annotation already: {}"
                                        , candidate, ann.annotationType().getName(), requiredConstructor);
                                throw new BeanCreationException(message);
                            }
                            boolean required = determineRequiredStatus(ann);
                            if (required) {
                                if (!candidates.isEmpty()) {
                                    throw new BeanCreationException(beanName,
                                            "Invalid injection constructors: " + candidates +
                                                    ". Found constructor with 'required' Autowired annotation: " +
                                                    candidate);
                                }
                                requiredConstructor = candidate;
                            }
                            candidates.add(candidate);
                        } else if (candidate.getParameterCount() == 0) {
                            defaultConstructor = candidate;
                        }
                    }
                    if (!candidates.isEmpty()) {
                        // Add default constructor to list of optional constructors, as fallback.
                        if (requiredConstructor == null) {
                            if (defaultConstructor != null) {
                                candidates.add(defaultConstructor);
                            } else if (candidates.size() == 1) {
                                logger.info("Inconsistent constructor declaration on bean with name '{}': " +
                                        "single injection constructor flagged as optional - " +
                                        "this constructor is effectively required since there is no " +
                                        "default constructor to fall back to: {}", beanName, candidates.get(0));
                            }
                        }
                        candidateConstructors = candidates.toArray(new Constructor<?>[0]);
                    } else if (rawCandidates.length == 1 && rawCandidates[0].getParameterCount() > 0) {
                        candidateConstructors = new Constructor<?>[]{rawCandidates[0]};
                    } else if (nonSyntheticConstructors == 2 && primaryConstructor != null &&
                            defaultConstructor != null && !primaryConstructor.equals(defaultConstructor)) {
                        candidateConstructors = new Constructor<?>[]{primaryConstructor, defaultConstructor};
                    } else if (nonSyntheticConstructors == 1 && primaryConstructor != null) {
                        candidateConstructors = new Constructor<?>[]{primaryConstructor};
                    } else {
                        candidateConstructors = new Constructor<?>[0];
                    }
                    this.candidateConstructorsCache.put(beanClass, candidateConstructors);
                }
            }
        }
        return (candidateConstructors.length > 0 ? candidateConstructors : null);
    }

    public final PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeanCreationException {
        return postProcessProperties(pvs, bean, beanName);
    }

    public final PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        InjectionMetadata metadata = findInjectionMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of @" + getAnnotationTypes() + " dependencies is failed", ex);
        }
        return pvs;
    }

    /**
     * Finds {@link InjectedElement} Metadata from annotated fields
     *
     * @param beanClass The {@link Class} of Bean
     * @return non-null {@link List}
     */
    protected List<AnnotatedFieldElement> findFieldAnnotationMetadata(final Class<?> beanClass) {

        final List<AnnotatedFieldElement> elements = new LinkedList<>();

        doWithFields(beanClass, field -> {
            for (Class<? extends Annotation> annotationType : getAnnotationTypes()) {
                AnnotationAttributes attributes = doGetAnnotationAttributes(field, annotationType);
                if (attributes != null) {
                    if (isStatic(field.getModifiers())) {
                        logger.warn("@{} is not supported on static fields: {}", annotationType.getName(), field);
                        return;
                    }
                    boolean required = determineRequiredStatus(attributes);
                    elements.add(new AnnotatedFieldElement(field, attributes, required));
                }
            }
        });

        return elements;
    }

    /**
     * Determine if the annotated field or method requires its dependency.
     *
     * @param attributes the injected annotation attributes
     * @return whether the annotation indicates that a dependency is required
     */
    protected boolean determineRequiredStatus(AnnotationAttributes attributes) {
        return true;
    }

    /**
     * Finds {@link InjectedElement} Metadata from annotated methods
     *
     * @param beanClass The {@link Class} of Bean
     * @return non-null {@link List}
     */
    protected List<AnnotatedMethodElement> findAnnotatedMethodMetadata(final Class<?> beanClass) {

        final List<AnnotatedMethodElement> elements = new LinkedList<>();

        doWithMethods(beanClass, method -> {
            Method bridgedMethod = findBridgedMethod(method);
            if (!isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                return;
            }
            for (Class<? extends Annotation> annotationType : getAnnotationTypes()) {
                AnnotationAttributes attributes = doGetAnnotationAttributes(bridgedMethod, annotationType);
                if (attributes != null && method.equals(getMostSpecificMethod(method, beanClass))) {
                    if (isStatic(method.getModifiers())) {
                        logger.warn("@{} annotation is not supported on static methods: {}", annotationType.getName(), method);
                        return;
                    }
                    if (method.getParameterTypes().length == 0) {
                        logger.warn("@{} annotation should only be used on methods with parameters: {}", annotationType.getName(), method);
                    }
                    PropertyDescriptor pd = findPropertyForMethod(bridgedMethod, beanClass);
                    boolean required = determineRequiredStatus(attributes);
                    elements.add(new AnnotatedMethodElement(method, pd, attributes, required));
                }
            }
        });
        return elements;
    }

    protected final AnnotationAttributes findInjectionAnnotationAttributes(AnnotatedElement annotatedElement) {
        AnnotationAttributes annotationAttributes = null;
        for (Class<? extends Annotation> annotationType : getAnnotationTypes()) {
            annotationAttributes = doGetAnnotationAttributes(annotatedElement, annotationType);
            if (annotationAttributes != null) {
                break;
            }
        }
        return annotationAttributes;
    }

    /**
     * Get {@link AnnotationAttributes}
     *
     * @param annotatedElement {@link AnnotatedElement the annotated element}
     * @param annotationType   the {@link Class tyoe} pf {@link Annotation annotation}
     * @return if <code>annotatedElement</code> can't be found in <code>annotatedElement</code>, return <code>null</code>
     */
    protected final AnnotationAttributes doGetAnnotationAttributes(AnnotatedElement annotatedElement, Class<? extends Annotation> annotationType) {
        return getAnnotationAttributes(annotatedElement, annotationType, getEnvironment(), classValuesAsString, nestedAnnotationsAsMap, ignoreDefaultValue, tryMergedAnnotation);
    }

    private InjectionMetadata buildAnnotatedMetadata(final Class<?> beanClass) {
        Collection<AnnotatedFieldElement> fieldElements = findFieldAnnotationMetadata(beanClass);
        Collection<AnnotatedMethodElement> methodElements = findAnnotatedMethodMetadata(beanClass);
        List<InjectedElement> elements = new ArrayList<>(fieldElements.size() + methodElements.size());
        elements.addAll(fieldElements);
        elements.addAll(methodElements);
        return new InjectionMetadata(beanClass, elements);
    }

    private InjectionMetadata findInjectionMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        InjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (needsRefresh(metadata, clazz)) {
                    if (metadata != null) {
                        metadata.clear(pvs);
                    }
                    try {
                        metadata = buildAnnotatedMetadata(clazz);
                        this.injectionMetadataCache.put(cacheKey, metadata);
                    } catch (NoClassDefFoundError err) {
                        throw new IllegalStateException("Failed to introspect object class [" + clazz.getName() + "] for annotation metadata: could not find class that it depends on", err);
                    }
                }
            }
        }
        return metadata;
    }

    @Override
    public final void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (beanType != null) {
            InjectionMetadata metadata = findInjectionMetadata(beanName, beanType, null);
            metadata.checkConfigMembers(beanDefinition);
        }
    }

    @Override
    public final void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = asConfigurableListableBeanFactory(beanFactory);
    }

    @Override
    public final void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public final void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Set the order of this post-processor.
     *
     * @param order the order
     */
    public final void setOrder(int order) {
        this.order = order;
    }

    /**
     * @param classValuesAsString whether to turn Class references into Strings (for
     *                            compatibility with {@link org.springframework.core.type.AnnotationMetadata} or to
     *                            preserve them as Class references
     */
    public final void setClassValuesAsString(boolean classValuesAsString) {
        this.classValuesAsString = classValuesAsString;
    }

    /**
     * @param nestedAnnotationsAsMap whether to turn nested Annotation instances into
     *                               {@link AnnotationAttributes} maps (for compatibility with
     *                               {@link org.springframework.core.type.AnnotationMetadata} or to preserve them as
     *                               Annotation instances
     */
    public final void setNestedAnnotationsAsMap(boolean nestedAnnotationsAsMap) {
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
    }

    /**
     * @param ignoreDefaultValue whether ignore default value or not
     */
    public final void setIgnoreDefaultValue(boolean ignoreDefaultValue) {
        this.ignoreDefaultValue = ignoreDefaultValue;
    }

    /**
     * @param tryMergedAnnotation whether try merged annotation or not
     */
    public final void setTryMergedAnnotation(boolean tryMergedAnnotation) {
        this.tryMergedAnnotation = tryMergedAnnotation;
    }

    /**
     * Set the size of cache
     *
     * @param cacheSize the size of cache
     */
    public final void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    @Override
    public void afterPropertiesSet() {
        this.candidateConstructorsCache = newConcurrentHashMap(cacheSize);
        this.injectionMetadataCache = newConcurrentHashMap(cacheSize);
    }

    @Override
    public void destroy() {
        candidateConstructorsCache.clear();
        injectionMetadataCache.clear();
        logger.info("{} was destroying!", getClass().getName());
    }

    /**
     * Resolve the injected-object as the value of the annotated {@link Method Field}
     *
     * @param bean         The bean that will be injected
     * @param beanName     The name of requesting bean that will be injected
     * @param pvs          {@link PropertyValues}
     * @param fieldElement {@link AnnotationInjectedElement the field element} was annotated
     * @return An injected object
     * @throws Throwable If resolving is failed
     */
    protected Object resolveInjectedFieldValue(Object bean, String beanName, PropertyValues pvs,
                                               AnnotationInjectedElement<Field> fieldElement) throws Throwable {
        return null;
    }

    /**
     * Resolve the injected-objects as the arguments of the annotated {@link Method method}
     *
     * @param bean          The bean that will be injected
     * @param beanName      The name of the bean that will be injected
     * @param pvs           {@link PropertyValues}
     * @param methodElement {@link AnnotationInjectedElement the method element} was annotated
     * @return The array of the injected objects as the arguments of the annotated {@link Method method}
     * @throws Throwable If resolving is failed
     */
    protected Object[] resolveInjectedMethodArguments(Object bean, String beanName, PropertyValues pvs,
                                                      AnnotationInjectedElement<Method> methodElement) throws Throwable {
        return null;
    }

    protected final Object resolveDependency(DependencyDescriptor desc, String beanName, Set<String> injectedBeanNames) {
        TypeConverter typeConverter = beanFactory.getTypeConverter();
        Object value = null;
        try {
            value = beanFactory.resolveDependency(desc, beanName, injectedBeanNames, typeConverter);
        } catch (BeansException ex) {
            throw new UnsatisfiedDependencyException(null, beanName, desc, ex);
        }
        return value;
    }

    /**
     * Register the specified bean as dependent on the autowired beans.
     */
    private void registerDependentBeans(String beanName, Set<String> injectedBeanNames) {
        if (beanName != null) {
            for (String injectedBeanName : injectedBeanNames) {
                if (this.beanFactory.containsBean(injectedBeanName)) {
                    this.beanFactory.registerDependentBean(injectedBeanName, beanName);
                }
                logger.trace("Injected by type from bean name '{}' to bean named '{}'", beanName, injectedBeanName);
            }
        }
    }

    /**
     * Resolve the specified cached method argument or field value.
     */
    private Object resolvedCachedArgument(String beanName, Object cachedArgument) {
        if (cachedArgument instanceof DependencyDescriptor) {
            DependencyDescriptor descriptor = (DependencyDescriptor) cachedArgument;
            TypeConverter typeConverter = this.beanFactory.getTypeConverter();
            return this.beanFactory.resolveDependency(descriptor, beanName, null, typeConverter);
        } else if (cachedArgument instanceof RuntimeBeanReference) {
            return this.beanFactory.getBean(((RuntimeBeanReference) cachedArgument).getBeanName());
        } else {
            return cachedArgument;
        }
    }

    public final Collection<Class<? extends Annotation>> getAnnotationTypes() {
        return unmodifiableCollection(annotationTypes);
    }

    @Override
    public final int getOrder() {
        return order;
    }

    public final Environment getEnvironment() {
        return environment;
    }

    public final ClassLoader getClassLoader() {
        return classLoader;
    }

    public final ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    /**
     * Annotation {@link InjectedElement}
     *
     * @param <M> {@link Field} or {@link Method}
     */
    public abstract static class AnnotationInjectedElement<M extends Member> extends InjectedElement {

        private final AnnotationAttributes attributes;

        private final boolean required;

        protected AnnotationInjectedElement(M member, PropertyDescriptor pd, AnnotationAttributes attributes, boolean required) {
            super(member, pd);
            this.attributes = attributes;
            this.required = required;
        }

        public final AnnotationAttributes getAttributes() {
            return attributes;
        }

        public final boolean isRequired() {
            return required;
        }

        public final M getInjectionPoint() {
            return (M) getMember();
        }
    }

    /**
     * {@link Annotation Annotated} {@link Field} {@link InjectedElement}
     */
    private class AnnotatedFieldElement extends AnnotationInjectedElement<Field> {

        private volatile boolean cached = false;

        private volatile Object cachedFieldValue;

        protected AnnotatedFieldElement(Field field, AnnotationAttributes attributes, boolean required) {
            super(field, null, attributes, required);
        }

        @Override
        protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
            Field field = getInjectionPoint();
            Object value;
            if (this.cached) {
                try {
                    value = resolvedCachedArgument(beanName, this.cachedFieldValue);
                } catch (NoSuchBeanDefinitionException ex) {
                    // Unexpected removal of target bean for cached argument -> re-resolve
                    value = resolveFieldValue(field, bean, beanName, pvs);
                }
            } else {
                value = resolveFieldValue(field, bean, beanName, pvs);
            }
            setFieldValue(bean, field, value);
        }

        @Nullable
        private Object resolveFieldValue(Field field, Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
            Object value = resolveInjectedFieldValue(bean, beanName, pvs, this);
            if (value == null) {
                boolean required = isRequired();
                DependencyDescriptor desc = new DependencyDescriptor(field, required);
                desc.setContainingClass(bean.getClass());
                Set<String> injectedBeanNames = new LinkedHashSet<>(1);
                value = resolveDependency(desc, beanName, injectedBeanNames);
                cacheFieldValue(field, desc, beanName, injectedBeanNames, value, required);
            }
            return value;
        }

        private void cacheFieldValue(Field field, DependencyDescriptor desc, String beanName, Set<String> injectedBeanNames, Object value, boolean required) {
            synchronized (this) {
                if (!this.cached) {
                    Object cachedFieldValue = null;
                    if (value != null || required) {
                        cachedFieldValue = desc;
                        registerDependentBeans(beanName, injectedBeanNames);
                        if (injectedBeanNames.size() == 1) {
                            String autowiredBeanName = injectedBeanNames.iterator().next();
                            if (beanFactory.containsBean(autowiredBeanName) &&
                                    beanFactory.isTypeMatch(autowiredBeanName, field.getType())) {
                                cachedFieldValue = new ShortcutDependencyDescriptor(
                                        desc, autowiredBeanName, field.getType());
                            }
                        }
                    }
                    this.cachedFieldValue = cachedFieldValue;
                    this.cached = true;
                }
            }
        }
    }

    /**
     * {@link Annotation Annotated} {@link Method} {@link InjectedElement}
     */
    private class AnnotatedMethodElement extends AnnotationInjectedElement<Method> {

        private volatile boolean cached = false;

        private volatile Object[] cachedMethodArguments;

        protected AnnotatedMethodElement(Method method, PropertyDescriptor pd, AnnotationAttributes attributes, boolean required) {
            super(method, pd, attributes, required);
        }

        @Override
        protected void inject(Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
            if (checkPropertySkipping(pvs)) {
                return;
            }
            Method method = getInjectionPoint();
            Object[] arguments;
            if (this.cached) {
                try {
                    arguments = resolveCachedArguments(beanName);
                } catch (NoSuchBeanDefinitionException ex) {
                    // Unexpected removal of target bean for cached argument -> re-resolve
                    arguments = resolveMethodArguments(method, bean, beanName, pvs);
                }
            } else {
                arguments = resolveMethodArguments(method, bean, beanName, pvs);
            }
            if (arguments != null) {
                invokeMethod(bean, method, arguments);
            }
        }

        @Nullable
        private Object[] resolveCachedArguments(@Nullable String beanName) {
            Object[] cachedMethodArguments = this.cachedMethodArguments;
            if (cachedMethodArguments == null) {
                return null;
            }
            Object[] arguments = new Object[cachedMethodArguments.length];
            for (int i = 0; i < arguments.length; i++) {
                arguments[i] = resolvedCachedArgument(beanName, cachedMethodArguments[i]);
            }
            return arguments;
        }

        @Nullable
        private Object[] resolveMethodArguments(Method method, Object bean, @Nullable String beanName, @Nullable PropertyValues pvs) throws Throwable {
            Object[] arguments = resolveInjectedMethodArguments(bean, beanName, pvs, this);
            if (arguments == null) {
                boolean required = isRequired();
                int argumentCount = method.getParameterCount();
                arguments = new Object[argumentCount];
                DependencyDescriptor[] descriptors = new DependencyDescriptor[argumentCount];
                Set<String> injectedBeanNames = new LinkedHashSet<>(argumentCount);
                for (int i = 0; i < arguments.length; i++) {
                    MethodParameter methodParam = new MethodParameter(method, i);
                    DependencyDescriptor currDesc = new DependencyDescriptor(methodParam, required);
                    currDesc.setContainingClass(bean.getClass());
                    descriptors[i] = currDesc;
                    Object arg = resolveDependency(currDesc, beanName, injectedBeanNames);
                    if (arg == null && !required) {
                        arguments = null;
                        break;
                    }
                    arguments[i] = arg;
                }
                synchronized (this) {
                    if (!this.cached) {
                        if (arguments != null) {
                            DependencyDescriptor[] cachedMethodArguments = copyOf(descriptors, arguments.length);
                            registerDependentBeans(beanName, injectedBeanNames);
                            if (injectedBeanNames.size() == argumentCount) {
                                Iterator<String> it = injectedBeanNames.iterator();
                                Class<?>[] paramTypes = method.getParameterTypes();
                                for (int i = 0; i < paramTypes.length; i++) {
                                    String autowiredBeanName = it.next();
                                    if (beanFactory.containsBean(autowiredBeanName) &&
                                            beanFactory.isTypeMatch(autowiredBeanName, paramTypes[i])) {
                                        cachedMethodArguments[i] = new ShortcutDependencyDescriptor(
                                                descriptors[i], autowiredBeanName, paramTypes[i]);
                                    }
                                }
                            }
                            this.cachedMethodArguments = cachedMethodArguments;
                        } else {
                            this.cachedMethodArguments = null;
                        }
                        this.cached = true;
                    }
                }
            }
            return arguments;
        }
    }

    /**
     * DependencyDescriptor variant with a pre-resolved target bean name.
     */
    @SuppressWarnings("serial")
    private static class ShortcutDependencyDescriptor extends DependencyDescriptor {

        private final String shortcut;

        private final Class<?> requiredType;

        public ShortcutDependencyDescriptor(DependencyDescriptor original, String shortcut, Class<?> requiredType) {
            super(original);
            this.shortcut = shortcut;
            this.requiredType = requiredType;
        }

        @Override
        public Object resolveShortcut(BeanFactory beanFactory) {
            return beanFactory.getBean(this.shortcut, this.requiredType);
        }
    }
}