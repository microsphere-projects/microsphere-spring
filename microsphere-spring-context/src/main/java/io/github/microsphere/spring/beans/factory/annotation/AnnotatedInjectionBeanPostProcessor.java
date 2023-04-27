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
package io.github.microsphere.spring.beans.factory.annotation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeanUtils;
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
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.MergedBeanDefinitionPostProcessor;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static io.github.microsphere.spring.util.AnnotationUtils.getAnnotationAttributes;
import static org.springframework.core.BridgeMethodResolver.findBridgedMethod;
import static org.springframework.core.BridgeMethodResolver.isVisibilityBridgeMethodPair;

/**
 * The generic {@link BeanPostProcessor} implementation to support the dependency injection for the customized annotations.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AutowiredAnnotationBeanPostProcessor
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class AnnotatedInjectionBeanPostProcessor extends InstantiationAwareBeanPostProcessorAdapter
        implements MergedBeanDefinitionPostProcessor, PriorityOrdered, BeanFactoryAware, BeanClassLoaderAware,
        EnvironmentAware, InitializingBean, DisposableBean {

    private final static int CACHE_SIZE = Integer.getInteger("microsphere.spring.injection.metadata.cache.size", 32);

    private final Log logger = LogFactory.getLog(getClass());

    private final Class<? extends Annotation>[] annotationTypes;

    private ConcurrentMap<String, AnnotatedInjectionMetadata> injectionMetadataCache;

    private ConfigurableListableBeanFactory beanFactory;

    private Environment environment;

    private ClassLoader classLoader;

    private String requiredParameterName = "required";

    private boolean requiredParameterValue = true;

    /**
     * make sure higher priority than {@link AutowiredAnnotationBeanPostProcessor}
     */
    private int order = Ordered.LOWEST_PRECEDENCE - 3;

    /**
     * whether to turn Class references into Strings (for
     * compatibility with {@link org.springframework.core.type.AnnotationMetadata} or to
     * preserve them as Class references
     */
    private boolean classValuesAsString;

    /**
     * whether to turn nested Annotation instances into
     * {@link AnnotationAttributes} maps (for compatibility with
     * {@link org.springframework.core.type.AnnotationMetadata} or to preserve them as
     * Annotation instances
     */
    private boolean nestedAnnotationsAsMap;

    /**
     * whether ignore default value or not
     */
    private boolean ignoreDefaultValue = true;

    /**
     * whether try merged annotation or not
     */
    private boolean tryMergedAnnotation = true;

    /**
     * The size of cache
     */
    private int cacheSize = CACHE_SIZE;

    /**
     * @param annotationType the single type of {@link Annotation annotation}
     */
    public AnnotatedInjectionBeanPostProcessor(Class<? extends Annotation> annotationType) {
        this(new Class[]{annotationType});
    }

    /**
     * @param annotationTypes the multiple types of {@link Annotation annotations}
     */
    public AnnotatedInjectionBeanPostProcessor(Class<? extends Annotation>... annotationTypes) {
        Assert.notEmpty(annotationTypes, "The argument of annotations' types must not empty");
        this.annotationTypes = annotationTypes;
    }

    private static <T> Collection<T> combine(Collection<? extends T>... elements) {
        List<T> allElements = new ArrayList<T>();
        for (Collection<? extends T> e : elements) {
            allElements.addAll(e);
        }
        return allElements;
    }

    /**
     * Annotation type
     *
     * @return non-null
     */
    @Deprecated
    public final Class<? extends Annotation> getAnnotationType() {
        return annotationTypes[0];
    }

    protected final Class<? extends Annotation>[] getAnnotationTypes() {
        return annotationTypes;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory, "AnnotationInjectedBeanPostProcessor requires a ConfigurableListableBeanFactory");
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeanCreationException {
        return postProcessProperties(pvs, bean, beanName);
    }

    public PropertyValues postProcessProperties(PropertyValues pvs, Object bean, String beanName) throws BeansException {
        InjectionMetadata metadata = findInjectionMetadata(beanName, bean.getClass(), pvs);
        try {
            metadata.inject(bean, beanName, pvs);
        } catch (BeanCreationException ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new BeanCreationException(beanName, "Injection of @" + getAnnotationType().getSimpleName() + " dependencies is failed", ex);
        }
        return pvs;
    }

    /**
     * Finds {@link InjectionMetadata.InjectedElement} Metadata from annotated fields
     *
     * @param beanClass The {@link Class} of Bean
     * @return non-null {@link List}
     */
    private List<AnnotatedFieldElement> findFieldAnnotationMetadata(final Class<?> beanClass) {

        final List<AnnotatedFieldElement> elements = new LinkedList<AnnotatedFieldElement>();

        ReflectionUtils.doWithFields(beanClass, new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {

                for (Class<? extends Annotation> annotationType : getAnnotationTypes()) {

                    AnnotationAttributes attributes = doGetAnnotationAttributes(field, annotationType);

                    if (attributes != null) {

                        if (Modifier.isStatic(field.getModifiers())) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("@" + annotationType.getName() + " is not supported on static fields: " + field);
                            }
                            return;
                        }

                        boolean required = determineRequiredStatus(attributes);

                        elements.add(new AnnotatedFieldElement(field, attributes, required));
                    }
                }
            }
        });

        return elements;

    }

    /**
     * Determine if the annotated field or method requires its dependency.
     * <p>A 'required' dependency means that injection should fail when no beans
     * are found. Otherwise, the injection process will simply bypass the field
     * or method when no beans are found.
     *
     * @param attributes the injected annotation attributes
     * @return whether the annotation indicates that a dependency is required
     */
    protected boolean determineRequiredStatus(AnnotationAttributes attributes) {
        return (!attributes.containsKey(this.requiredParameterName) ||
                this.requiredParameterValue == attributes.getBoolean(this.requiredParameterName));
    }

    /**
     * Finds {@link InjectionMetadata.InjectedElement} Metadata from annotated methods
     *
     * @param beanClass The {@link Class} of Bean
     * @return non-null {@link List}
     */
    private List<AnnotatedMethodElement> findAnnotatedMethodMetadata(final Class<?> beanClass) {

        final List<AnnotatedMethodElement> elements = new LinkedList<AnnotatedMethodElement>();

        ReflectionUtils.doWithMethods(beanClass, new ReflectionUtils.MethodCallback() {
            @Override
            public void doWith(Method method) throws IllegalArgumentException, IllegalAccessException {

                Method bridgedMethod = findBridgedMethod(method);

                if (!isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                    return;
                }


                for (Class<? extends Annotation> annotationType : getAnnotationTypes()) {

                    AnnotationAttributes attributes = doGetAnnotationAttributes(bridgedMethod, annotationType);

                    if (attributes != null && method.equals(ClassUtils.getMostSpecificMethod(method, beanClass))) {
                        if (Modifier.isStatic(method.getModifiers())) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("@" + annotationType.getName() + " annotation is not supported on static methods: " + method);
                            }
                            return;
                        }
                        if (method.getParameterTypes().length == 0) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("@" + annotationType.getName() + " annotation should only be used on methods with parameters: " + method);
                            }
                        }
                        PropertyDescriptor pd = BeanUtils.findPropertyForMethod(bridgedMethod, beanClass);
                        boolean required = determineRequiredStatus(attributes);
                        elements.add(new AnnotatedMethodElement(method, pd, attributes, required));
                    }
                }
            }
        });

        return elements;
    }

    /**
     * Get {@link AnnotationAttributes}
     *
     * @param annotatedElement {@link AnnotatedElement the annotated element}
     * @param annotationType   the {@link Class tyoe} pf {@link Annotation annotation}
     * @return if <code>annotatedElement</code> can't be found in <code>annotatedElement</code>, return <code>null</code>
     */
    protected AnnotationAttributes doGetAnnotationAttributes(AnnotatedElement annotatedElement, Class<? extends Annotation> annotationType) {
        return getAnnotationAttributes(annotatedElement, annotationType, getEnvironment(), classValuesAsString, nestedAnnotationsAsMap, ignoreDefaultValue, tryMergedAnnotation);
    }

    private AnnotatedInjectionMetadata buildAnnotatedMetadata(final Class<?> beanClass) {
        Collection<AnnotatedFieldElement> fieldElements = findFieldAnnotationMetadata(beanClass);
        Collection<AnnotatedMethodElement> methodElements = findAnnotatedMethodMetadata(beanClass);
        return new AnnotatedInjectionMetadata(beanClass, fieldElements, methodElements);
    }

    private InjectionMetadata findInjectionMetadata(String beanName, Class<?> clazz, PropertyValues pvs) {
        // Fall back to class name as cache key, for backwards compatibility with custom callers.
        String cacheKey = (StringUtils.hasLength(beanName) ? beanName : clazz.getName());
        // Quick check on the concurrent map first, with minimal locking.
        AnnotatedInjectionMetadata metadata = this.injectionMetadataCache.get(cacheKey);
        if (InjectionMetadata.needsRefresh(metadata, clazz)) {
            synchronized (this.injectionMetadataCache) {
                metadata = this.injectionMetadataCache.get(cacheKey);
                if (InjectionMetadata.needsRefresh(metadata, clazz)) {
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
    public void postProcessMergedBeanDefinition(RootBeanDefinition beanDefinition, Class<?> beanType, String beanName) {
        if (beanType != null) {
            InjectionMetadata metadata = findInjectionMetadata(beanName, beanType, null);
            metadata.checkConfigMembers(beanDefinition);
        }
    }

    /**
     * Set the name of an attribute of the annotation that specifies whether it is required.
     *
     * @see #setRequiredParameterValue(boolean)
     */
    public void setRequiredParameterName(String requiredParameterName) {
        this.requiredParameterName = requiredParameterName;
    }

    /**
     * Set the boolean value that marks a dependency as required.
     * <p>For example if using 'required=true' (the default), this value should be
     * {@code true}; but if using 'optional=false', this value should be {@code false}.
     *
     * @see #setRequiredParameterName(String)
     */
    public void setRequiredParameterValue(boolean requiredParameterValue) {
        this.requiredParameterValue = requiredParameterValue;
    }

    /**
     * @param classValuesAsString whether to turn Class references into Strings (for
     *                            compatibility with {@link org.springframework.core.type.AnnotationMetadata} or to
     *                            preserve them as Class references
     */
    public void setClassValuesAsString(boolean classValuesAsString) {
        this.classValuesAsString = classValuesAsString;
    }

    /**
     * @param nestedAnnotationsAsMap whether to turn nested Annotation instances into
     *                               {@link AnnotationAttributes} maps (for compatibility with
     *                               {@link org.springframework.core.type.AnnotationMetadata} or to preserve them as
     *                               Annotation instances
     */
    public void setNestedAnnotationsAsMap(boolean nestedAnnotationsAsMap) {
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
    }

    /**
     * @param ignoreDefaultValue whether ignore default value or not
     */
    public void setIgnoreDefaultValue(boolean ignoreDefaultValue) {
        this.ignoreDefaultValue = ignoreDefaultValue;
    }

    /**
     * @param tryMergedAnnotation whether try merged annotation or not
     */
    public void setTryMergedAnnotation(boolean tryMergedAnnotation) {
        this.tryMergedAnnotation = tryMergedAnnotation;
    }

    /**
     * Set the size of cache
     *
     * @param cacheSize the size of cache
     */
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.injectionMetadataCache = new ConcurrentHashMap<String, AnnotatedInjectionMetadata>(cacheSize);
    }

    @Override
    public void destroy() throws Exception {
        injectionMetadataCache.clear();
        if (logger.isInfoEnabled()) {
            logger.info(getClass() + " was destroying!");
        }
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
                if (logger.isDebugEnabled()) {
                    logger.debug("Injected by type from bean name '" + beanName +
                            "' to bean named '" + injectedBeanName + "'");
                }
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
     * {@link Annotation Annotated} {@link InjectionMetadata} implementation
     */
    private class AnnotatedInjectionMetadata extends InjectionMetadata {

        private final Collection<AnnotatedFieldElement> fieldElements;

        private final Collection<AnnotatedMethodElement> methodElements;

        public AnnotatedInjectionMetadata(Class<?> targetClass, Collection<AnnotatedFieldElement> fieldElements, Collection<AnnotatedMethodElement> methodElements) {
            super(targetClass, combine(fieldElements, methodElements));
            this.fieldElements = fieldElements;
            this.methodElements = methodElements;
        }

        public Collection<AnnotatedFieldElement> getFieldElements() {
            return fieldElements;
        }

        public Collection<AnnotatedMethodElement> getMethodElements() {
            return methodElements;
        }
    }


    /**
     * Annotation {@link InjectionMetadata.InjectedElement}
     *
     * @param <M> {@link Field} or {@link Method}
     */
    public abstract static class AnnotationInjectedElement<M extends Member> extends InjectionMetadata.InjectedElement {

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
     * {@link Annotation Annotated} {@link Field} {@link InjectionMetadata.InjectedElement}
     */
    public class AnnotatedFieldElement extends AnnotationInjectedElement<Field> {

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
            if (value != null) {
                ReflectionUtils.makeAccessible(field);
                field.set(bean, value);
            }
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
     * {@link Annotation Annotated} {@link Method} {@link InjectionMetadata.InjectedElement}
     */
    public class AnnotatedMethodElement extends AnnotationInjectedElement<Method> {

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
                try {
                    ReflectionUtils.makeAccessible(method);
                    method.invoke(bean, arguments);
                } catch (InvocationTargetException ex) {
                    throw ex.getTargetException();
                }
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
                            DependencyDescriptor[] cachedMethodArguments = Arrays.copyOf(descriptors, arguments.length);
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
