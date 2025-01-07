package io.microsphere.spring.beans;

import io.microsphere.logging.Logger;
import io.microsphere.util.BaseUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.invoke.MethodHandleUtils.findStatic;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableBeanFactory;
import static io.microsphere.spring.context.ApplicationContextUtils.asConfigurableApplicationContext;
import static io.microsphere.spring.context.ApplicationContextUtils.getApplicationContextAwareProcessor;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.springframework.beans.factory.BeanFactoryUtils.beanNamesForTypeIncludingAncestors;
import static org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors;
import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.generateBeanName;
import static org.springframework.util.ClassUtils.getUserClass;
import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.StringUtils.hasText;

/**
 * Bean Utilities Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2017.01.13
 */
public abstract class BeanUtils extends BaseUtils {

    private static final Logger logger = getLogger(BeanUtils.class);

    /**
     * The {@link MethodHandle} of {@linkplain org.springframework.beans.BeanUtils#findPrimaryConstructor(Class)}
     *
     * @since Spring Framework 5.0
     */
    private static final MethodHandle FIND_PRIMARY_CONSTRUCTOR_METHOD_HANDLE = findStatic(org.springframework.beans.BeanUtils.class, "findPrimaryConstructor", Class.class);

    /**
     * Is Bean Present or not?
     *
     * @param beanFactory {@link ListableBeanFactory}
     * @param beanClass   The  {@link Class} of Bean
     * @return If present , return <code>true</code> , or <code>false</code>
     */
    public static boolean isBeanPresent(ListableBeanFactory beanFactory, Class<?> beanClass) {
        return isBeanPresent(beanFactory, beanClass, false);
    }

    /**
     * Is Bean Present or not?
     *
     * @param beanFactory        {@link ListableBeanFactory}
     * @param beanClass          The  {@link Class} of Bean
     * @param includingAncestors including ancestors or not
     * @return If present , return <code>true</code> , or <code>false</code>
     */
    public static boolean isBeanPresent(ListableBeanFactory beanFactory, Class<?> beanClass, boolean includingAncestors) {
        String[] beanNames = getBeanNames(beanFactory, beanClass, includingAncestors);
        return !isEmpty(beanNames);
    }

    /**
     * Is Bean Present or not?
     *
     * @param beanFactory   {@link ListableBeanFactory}
     * @param beanClassName The  name of {@link Class} of Bean
     * @return If present , return <code>true</code> , or <code>false</code>
     */
    public static boolean isBeanPresent(ListableBeanFactory beanFactory, String beanClassName) {
        return isBeanPresent(beanFactory, beanClassName, false);
    }

    /**
     * Is Bean Present or not?
     *
     * @param beanFactory        {@link ListableBeanFactory}
     * @param beanClassName      The  name of {@link Class} of Bean
     * @param includingAncestors including ancestors or not
     * @return If present , return <code>true</code> , or <code>false</code>
     */
    public static boolean isBeanPresent(ListableBeanFactory beanFactory, String beanClassName, boolean includingAncestors) {
        ClassLoader classLoader = null;
        if (beanFactory instanceof ConfigurableBeanFactory) {
            ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
            classLoader = configurableBeanFactory.getBeanClassLoader();
        }

        Class beanClass = resolveClass(beanClassName, classLoader);
        if (beanClass == null) {
            return false;
        }
        return isBeanPresent(beanFactory, beanClass, includingAncestors);
    }


    /**
     * Is Bean Present or not by the specified name and class
     *
     * @param beanFactory {@link BeanFactory}
     * @param beanName    The bean name
     * @param beanClass   The bean class
     * @return If present , return <code>true</code> , or <code>false</code>
     */
    public static boolean isBeanPresent(BeanFactory beanFactory, String beanName, Class<?> beanClass) throws NullPointerException {
        return beanFactory.containsBean(beanName) && beanFactory.isTypeMatch(beanName, beanClass);
    }

    /**
     * Get Bean Names from {@link ListableBeanFactory} by type.
     *
     * @param beanFactory {@link ListableBeanFactory}
     * @param beanClass   The  {@link Class} of Bean
     * @return If found , return the array of Bean Names , or empty array.
     */
    public static String[] getBeanNames(ListableBeanFactory beanFactory, Class<?> beanClass) {
        return getBeanNames(beanFactory, beanClass, false);
    }

    /**
     * Get Bean Names from {@link ListableBeanFactory} by type.
     *
     * @param beanFactory        {@link ListableBeanFactory}
     * @param beanClass          The  {@link Class} of Bean
     * @param includingAncestors including ancestors or not
     * @return If found , return the array of Bean Names , or empty array.
     */
    public static String[] getBeanNames(ListableBeanFactory beanFactory, Class<?> beanClass, boolean includingAncestors) {
        if (includingAncestors) {
            return beanNamesForTypeIncludingAncestors(beanFactory, beanClass, true, false);
        } else {
            return beanFactory.getBeanNamesForType(beanClass, true, false);
        }
    }

    /**
     * Get Bean Names from {@link ListableBeanFactory} by type.
     *
     * @param beanFactory {@link ConfigurableListableBeanFactory}
     * @param beanClass   The  {@link Class} of Bean
     * @return If found , return the array of Bean Names , or empty array.
     */
    public static String[] getBeanNames(ConfigurableListableBeanFactory beanFactory, Class<?> beanClass) {
        return getBeanNames(beanFactory, beanClass, false);
    }

    /**
     * Get Bean Names from {@link ListableBeanFactory} by type.
     *
     * @param beanFactory        {@link ConfigurableListableBeanFactory}
     * @param beanClass          The  {@link Class} of Bean
     * @param includingAncestors including ancestors or not
     * @return If found , return the array of Bean Names , or empty array.
     */
    public static String[] getBeanNames(ConfigurableListableBeanFactory beanFactory, Class<?> beanClass, boolean includingAncestors) {
        return getBeanNames((ListableBeanFactory) beanFactory, beanClass, includingAncestors);
    }

    /**
     * Resolve Bean Type
     *
     * @param beanClassName the class name of Bean
     * @param classLoader   {@link ClassLoader}
     * @return Bean type if can be resolved , or return <code>null</code>.
     */
    public static Class<?> resolveBeanType(String beanClassName, ClassLoader classLoader) {
        if (!hasText(beanClassName)) {
            return null;
        }

        Class<?> beanType = null;
        try {
            beanType = resolveClass(beanClassName, classLoader);
            beanType = getUserClass(beanType);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            }
        }
        return beanType;
    }

    /**
     * Get Optional Bean by {@link Class} including ancestors(BeanFactory).
     *
     * @param beanFactory        {@link ListableBeanFactory}
     * @param beanClass          The  {@link Class} of Bean
     * @param includingAncestors including ancestors or not
     * @param <T>                The  {@link Class} of Bean
     * @return Bean object if found , or return <code>null</code>.
     * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
     * @see BeanFactoryUtils#beanOfTypeIncludingAncestors(ListableBeanFactory, Class)
     */
    public static <T> T getOptionalBean(ListableBeanFactory beanFactory, Class<T> beanClass, boolean includingAncestors) throws BeansException {
        String[] beanNames = getBeanNames(beanFactory, beanClass, includingAncestors);
        if (isEmpty(beanNames)) {
            if (logger.isTraceEnabled()) {
                logger.trace("The bean [ class : " + beanClass.getName() + " ] can't be found ");
            }
            return null;
        }

        T bean = null;

        try {
            bean = includingAncestors ? beanOfTypeIncludingAncestors(beanFactory, beanClass) : beanFactory.getBean(beanClass);
        } catch (Exception e) {
            if (logger.isErrorEnabled()) {
                logger.error(e.getMessage(), e);
            }
        }

        return bean;
    }

    /**
     * Get Optional Bean by {@link Class}.
     *
     * @param beanFactory {@link ListableBeanFactory}
     * @param beanClass   The  {@link Class} of Bean
     * @param <T>         The  {@link Class} of Bean
     * @return Bean object if found , or return <code>null</code>.
     * @throws NoUniqueBeanDefinitionException if more than one bean of the given type was found
     */
    public static <T> T getOptionalBean(ListableBeanFactory beanFactory, Class<T> beanClass) throws BeansException {
        return getOptionalBean(beanFactory, beanClass, false);
    }

    /**
     * Get the bean via the specified bean name and type if available
     *
     * @param beanFactory {@link BeanFactory}
     * @param beanName    the bean name
     * @param beanType    the class of bean
     * @param <T>         the bean type
     * @return the bean if available, or <code>null</code>
     * @throws BeansException in case of creation errors
     */
    public static <T> T getBeanIfAvailable(BeanFactory beanFactory, String beanName, Class<T> beanType) throws BeansException {
        if (isBeanPresent(beanFactory, beanName, beanType)) {
            return beanFactory.getBean(beanName, beanType);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("The bean[name : '{}' , type : {}] can't be found in Spring BeanFactory", beanName, beanType.getName());
        }
        return null;
    }

    /**
     * Get all sorted Beans of {@link BeanFactory} in specified bean type.
     *
     * @param beanFactory {@link BeanFactory}
     * @param type        bean type
     * @param <T>         bean type
     * @return all sorted Beans
     */
    public static <T> List<T> getSortedBeans(BeanFactory beanFactory, Class<T> type) {
        if (beanFactory instanceof ListableBeanFactory) {
            return getSortedBeans((ListableBeanFactory) beanFactory, type);
        }
        return emptyList();
    }

    /**
     * Get all sorted Beans of {@link ListableBeanFactory} in specified bean type.
     *
     * @param beanFactory {@link ListableBeanFactory}
     * @param type        bean type
     * @param <T>         bean type
     * @return all sorted Beans
     */
    public static <T> List<T> getSortedBeans(ListableBeanFactory beanFactory, Class<T> type) {
        Map<String, T> beansOfType = beansOfTypeIncludingAncestors(beanFactory, type);
        List<T> beansList = new ArrayList<T>(beansOfType.values());
        AnnotationAwareOrderComparator.sort(beansList);
        return unmodifiableList(beansList);
    }

    /**
     * Invoke Spring Bean interfaces in order:
     * <ul>
     *     <li>{@link BeanNameAware}</li>
     *     <li>{@link BeanClassLoaderAware}</li>
     *     <li>{@link BeanFactoryAware}</li>
     *     <li>{@link EnvironmentAware}</li>
     *     <li>{@link EmbeddedValueResolverAware}</li>
     *     <li>{@link ResourceLoaderAware}</li>
     *     <li>{@link ApplicationEventPublisherAware}</li>
     *     <li>{@link MessageSourceAware}</li>
     *     <li>{@link ApplicationStartupAware} (Spring Framework 5.3+)</li>
     *     <li>{@link ApplicationContextAware}</li>
     *     <li>{@link InitializingBean}</li>
     * </ul>
     *
     * @param bean    the bean
     * @param context {@link ApplicationContext}
     */
    public static void invokeBeanInterfaces(Object bean, ApplicationContext context) {
        ConfigurableApplicationContext configurableApplicationContext = asConfigurableApplicationContext(context);
        invokeBeanInterfaces(bean, configurableApplicationContext);
    }

    /**
     * Invoke Spring Bean interfaces in order:
     * <ul>
     *     <li>{@link BeanNameAware}</li>
     *     <li>{@link BeanClassLoaderAware}</li>
     *     <li>{@link BeanFactoryAware}</li>
     *     <li>{@link EnvironmentAware}</li>
     *     <li>{@link EmbeddedValueResolverAware}</li>
     *     <li>{@link ResourceLoaderAware}</li>
     *     <li>{@link ApplicationEventPublisherAware}</li>
     *     <li>{@link MessageSourceAware}</li>
     *     <li>{@link ApplicationStartupAware} (Spring Framework 5.3+)</li>
     *     <li>{@link ApplicationContextAware}</li>
     *     <li>{@link InitializingBean}</li>
     * </ul>
     *
     * @param bean    the bean
     * @param context {@link ConfigurableApplicationContext}
     * @see #invokeBeanInterfaces(Object, ApplicationContext)
     * @see #invokeAwareInterfaces(Object, ApplicationContext)
     * @see #invokeInitializingBean(Object)
     */
    public static void invokeBeanInterfaces(Object bean, ConfigurableApplicationContext context) {
        invokeAwareInterfaces(bean, context);
        try {
            invokeInitializingBean(bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void invokeInitializingBean(Object bean) throws Exception {
        if (bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
        }
    }

    /**
     * Invoke the {@link Aware} interfaces in order :
     * <ul>
     *     <li>{@link BeanNameAware}</li>
     *     <li>{@link BeanClassLoaderAware}</li>
     *     <li>{@link BeanFactoryAware}</li>
     * </ul>
     * <p>
     * if the argument <coode>beanFactory</coode> is an instance of {@link ApplicationContext}, the more {@link Aware} interfaces
     * will be involved :
     * <ul>
     *     <li>{@link EnvironmentAware}</li>
     *     <li>{@link EmbeddedValueResolverAware}</li>
     *     <li>{@link ResourceLoaderAware}</li>
     *     <li>{@link ApplicationEventPublisherAware}</li>
     *     <li>{@link MessageSourceAware}</li>
     *     <li>{@link ApplicationStartupAware} (Spring Framework 5.3+)</li>
     *     <li>{@link ApplicationContextAware}</li>
     * </ul>
     *
     * @param bean        the bean
     * @param beanFactory {@link BeanFactory}
     */
    public static void invokeAwareInterfaces(Object bean, BeanFactory beanFactory) {
        invokeAwareInterfaces(bean, beanFactory, asConfigurableBeanFactory(beanFactory));
    }

    /**
     * Invoke the {@link Aware} interfaces in order :
     * <ul>
     *     <li>{@link BeanNameAware}</li>
     *     <li>{@link BeanClassLoaderAware}</li>
     *     <li>{@link BeanFactoryAware}</li>
     * </ul>
     * <p>
     * if the argument <coode>beanFactory</coode> is an instance of {@link ApplicationContext}, the more {@link Aware} interfaces
     * will be involved :
     * <ul>
     *     <li>{@link EnvironmentAware}</li>
     *     <li>{@link EmbeddedValueResolverAware}</li>
     *     <li>{@link ResourceLoaderAware}</li>
     *     <li>{@link ApplicationEventPublisherAware}</li>
     *     <li>{@link MessageSourceAware}</li>
     *     <li>{@link ApplicationStartupAware} (Spring Framework 5.3+)</li>
     *     <li>{@link ApplicationContextAware}</li>
     * </ul>
     *
     * @param bean        the bean
     * @param beanFactory {@link ConfigurableBeanFactory}
     */
    public static void invokeAwareInterfaces(Object bean, ConfigurableBeanFactory beanFactory) {
        invokeAwareInterfaces(bean, beanFactory, beanFactory);
    }

    /**
     * Invoke the {@link Aware} interfaces in order :
     * <ul>
     *     <li>{@link BeanNameAware}</li>
     *     <li>{@link BeanClassLoaderAware}</li>
     *     <li>{@link BeanFactoryAware}</li>
     * </ul>
     * <p>
     * if the argument <coode>beanFactory</coode> is an instance of {@link ApplicationContext}, the more {@link Aware} interfaces
     * will be involved :
     * <ul>
     *     <li>{@link EnvironmentAware}</li>
     *     <li>{@link EmbeddedValueResolverAware}</li>
     *     <li>{@link ResourceLoaderAware}</li>
     *     <li>{@link ApplicationEventPublisherAware}</li>
     *     <li>{@link MessageSourceAware}</li>
     *     <li>{@link ApplicationStartupAware} (Spring Framework 5.3+)</li>
     *     <li>{@link ApplicationContextAware}</li>
     * </ul>
     *
     * @param bean
     * @param beanFactory
     * @param configurableBeanFactory
     */
    public static void invokeAwareInterfaces(Object bean, BeanFactory beanFactory, @Nullable ConfigurableBeanFactory configurableBeanFactory) {
        if (beanFactory instanceof ApplicationContext) {
            invokeAwareInterfaces(bean, (ApplicationContext) beanFactory);
        } else {
            invokeBeanFactoryAwareInterfaces(bean, beanFactory, configurableBeanFactory);
        }
    }

    /**
     * Return the primary constructor of the provided class. For Kotlin classes, this
     * returns the Java constructor corresponding to the Kotlin primary constructor
     * (as defined in the Kotlin specification). Otherwise, in particular for non-Kotlin
     * classes, this simply returns {@code null}.
     *
     * @param clazz the class to check
     * @see <a href="https://kotlinlang.org/docs/reference/classes.html#constructors">Kotlin docs</a>
     * @see org.springframework.beans.BeanUtils#findPrimaryConstructor(Class)
     * @since Spring Framework 5.0
     */
    public static <T> Constructor<T> findPrimaryConstructor(Class<T> clazz) {
        if (FIND_PRIMARY_CONSTRUCTOR_METHOD_HANDLE == null) {
            return null;
        }
        Constructor<T> constructor = null;
        try {
            constructor = (Constructor<T>) FIND_PRIMARY_CONSTRUCTOR_METHOD_HANDLE.invokeExact(clazz);
        } catch (Throwable e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to execute invokeExact on {} with arg : '{}'", FIND_PRIMARY_CONSTRUCTOR_METHOD_HANDLE, clazz, e);
            }
        }
        return constructor;
    }

    /**
     * @see AbstractAutowireCapableBeanFactory#invokeAwareMethods(String, Object)
     */
    static void invokeBeanFactoryAwareInterfaces(Object bean, BeanFactory beanFactory, @Nullable ConfigurableBeanFactory configurableBeanFactory) {
        invokeBeanNameAware(bean, beanFactory);
        invokeBeanClassLoaderAware(bean, configurableBeanFactory);
        invokeBeanFactoryAware(bean, beanFactory);
    }

    static void invokeBeanNameAware(Object bean, BeanFactory beanFactory) {
        if (bean instanceof BeanNameAware) {
            BeanDefinitionRegistry registry = asBeanDefinitionRegistry(beanFactory);
            BeanDefinition beanDefinition = rootBeanDefinition(bean.getClass()).getBeanDefinition();
            String beanName = generateBeanName(beanDefinition, registry);
            ((BeanNameAware) bean).setBeanName(beanName);
        }
    }

    public static void invokeBeanNameAware(Object bean, String beanName) {
        if (bean instanceof BeanNameAware) {
            ((BeanNameAware) bean).setBeanName(beanName);
        }
    }

    static void invokeBeanFactoryAware(Object bean, BeanFactory beanFactory) {
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(beanFactory);
        }
    }

    static void invokeBeanClassLoaderAware(Object bean, @Nullable ConfigurableBeanFactory configurableBeanFactory) {
        if (bean instanceof BeanClassLoaderAware && configurableBeanFactory != null) {
            ClassLoader classLoader = configurableBeanFactory.getBeanClassLoader();
            ((BeanClassLoaderAware) bean).setBeanClassLoader(classLoader);
        }
    }

    /**
     * Invoke {@link Aware} interfaces if the given bean implements
     * <p>
     * Current implementation keeps the order of invocation {@link Aware Aware interfaces}:
     * <ul>
     *     <li>{@link BeanNameAware}</li>
     *     <li>{@link BeanClassLoaderAware}</li>
     *     <li>{@link BeanFactoryAware}</li>
     *     <li>{@link EnvironmentAware}</li>
     *     <li>{@link EmbeddedValueResolverAware}</li>
     *     <li>{@link ResourceLoaderAware}</li>
     *     <li>{@link ApplicationEventPublisherAware}</li>
     *     <li>{@link MessageSourceAware}</li>
     *     <li>{@link ApplicationStartupAware} (Spring Framework 5.3+)</li>
     *     <li>{@link ApplicationContextAware}</li>
     * </ul>
     *
     * @param bean    the bean
     * @param context {@link ApplicationContext}
     * @see #invokeAwareInterfaces(Object, BeanFactory)
     * @see org.springframework.context.support.ApplicationContextAwareProcessor#invokeAwareInterfaces(Object)
     * @see AbstractAutowireCapableBeanFactory#invokeAwareMethods(String, Object)
     */
    static void invokeAwareInterfaces(Object bean, ApplicationContext context) {
        invokeAwareInterfaces(bean, context, asConfigurableApplicationContext(context));
    }

    /**
     * Invoke {@link Aware} interfaces if the given bean implements
     * <p>
     * Current implementation keeps the order of invocation {@link Aware Aware interfaces}:
     * <ul>
     *     <li>{@link BeanNameAware}</li>
     *     <li>{@link BeanClassLoaderAware}</li>
     *     <li>{@link BeanFactoryAware}</li>
     *     <li>{@link EnvironmentAware}</li>
     *     <li>{@link EmbeddedValueResolverAware}</li>
     *     <li>{@link ResourceLoaderAware}</li>
     *     <li>{@link ApplicationEventPublisherAware}</li>
     *     <li>{@link MessageSourceAware}</li>
     *     <li>{@link ApplicationStartupAware} (Spring Framework 5.3+)</li>
     *     <li>{@link ApplicationContextAware}</li>
     * </ul>
     *
     * @param bean    the bean
     * @param context {@link ApplicationContext}
     * @see #invokeAwareInterfaces(Object, BeanFactory)
     * @see org.springframework.context.support.ApplicationContextAwareProcessor#invokeAwareInterfaces(Object)
     * @see AbstractAutowireCapableBeanFactory#invokeAwareMethods(String, Object)
     */
    public static void invokeAwareInterfaces(Object bean, ConfigurableApplicationContext context) {
        invokeAwareInterfaces(bean, context, context);
    }

    static void invokeAwareInterfaces(Object bean, ApplicationContext context, @Nullable ConfigurableApplicationContext applicationContext) {
        if (bean == null || context == null) {
            return;
        }

        ConfigurableListableBeanFactory beanFactory = applicationContext != null ? applicationContext.getBeanFactory() : null;

        invokeBeanFactoryAwareInterfaces(bean, beanFactory, beanFactory);

        BeanPostProcessor beanPostProcessor = getApplicationContextAwareProcessor(beanFactory);

        if (beanPostProcessor != null) {
            beanPostProcessor.postProcessBeforeInitialization(bean, "");
        }
    }

    /**
     * Sort Beans {@link Map} via {@link AnnotationAwareOrderComparator#sort(List)} rule
     *
     * @param beansMap Beans {@link Map}
     * @param <T>      the type of Bean
     * @return sorted Beans {@link Map}
     */
    static <T> Map<String, T> sort(final Map<String, T> beansMap) {

        Map<String, T> unmodifiableBeansMap = Collections.unmodifiableMap(beansMap);

        List<NamingBean<T>> namingBeans = new ArrayList<NamingBean<T>>(unmodifiableBeansMap.size());

        for (Map.Entry<String, T> entry : unmodifiableBeansMap.entrySet()) {
            String beanName = entry.getKey();
            T bean = entry.getValue();
            NamingBean<T> namingBean = new NamingBean<T>(beanName, bean);
            namingBeans.add(namingBean);
        }

        AnnotationAwareOrderComparator.sort(namingBeans);

        Map<String, T> sortedBeansMap = new LinkedHashMap<String, T>(beansMap.size());

        for (NamingBean<T> namingBean : namingBeans) {
            sortedBeansMap.put(namingBean.name, namingBean.bean);
        }

        return sortedBeansMap;

    }

    static class NamingBean<T> extends AnnotationAwareOrderComparator implements Comparable<NamingBean>, Ordered {

        private final String name;

        private final T bean;

        NamingBean(String name, T bean) {
            this.name = name;
            this.bean = bean;
        }


        @Override
        public int compareTo(NamingBean o) {
            return compare(this, o);
        }

        @Override
        public int getOrder() {
            return getOrder(bean);
        }
    }

}
