package io.microsphere.spring.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.EmbeddedValueResolver;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.ApplicationStartupAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.support.ApplicationContextAwareProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.spring.util.ApplicationContextUtils.asConfigurableApplicationContext;
import static java.lang.String.format;
import static java.util.Collections.unmodifiableList;
import static org.springframework.beans.factory.BeanFactoryUtils.beanNamesForTypeIncludingAncestors;
import static org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors;
import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;

/**
 * Bean Utilities Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2017.01.13
 */
public abstract class BeanUtils {

    private static final Log logger = LogFactory.getLog(BeanUtils.class);

    private static final String[] EMPTY_BEAN_NAMES = new String[0];

    /**
     * Is Bean Present or not?
     *
     * @param beanFactory {@link ListableBeanFactory}
     * @param beanClass   The  {@link Class} of Bean
     * @return If present , return <code>true</code> , or <code>false</code>
     */
    public static boolean isBeanPresent(ListableBeanFactory beanFactory,
                                        Class<?> beanClass) {

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
    public static boolean isBeanPresent(ListableBeanFactory beanFactory,
                                        Class<?> beanClass,
                                        boolean includingAncestors) {

        String[] beanNames = getBeanNames(beanFactory, beanClass, includingAncestors);

        return !ObjectUtils.isEmpty(beanNames);

    }

    /**
     * Is Bean Present or not?
     *
     * @param beanFactory        {@link ListableBeanFactory}
     * @param beanClassName      The  name of {@link Class} of Bean
     * @param includingAncestors including ancestors or not
     * @return If present , return <code>true</code> , or <code>false</code>
     */
    public static boolean isBeanPresent(ListableBeanFactory beanFactory,
                                        String beanClassName,
                                        boolean includingAncestors) {

        boolean present = false;

        ClassLoader classLoader = beanFactory.getClass().getClassLoader();

        if (ClassUtils.isPresent(beanClassName, classLoader)) {

            Class beanClass = ClassUtils.resolveClassName(beanClassName, classLoader);

            present = isBeanPresent(beanFactory, beanClass, includingAncestors);
        }


        return present;

    }

    /**
     * Is Bean Present or not?
     *
     * @param beanFactory   {@link ListableBeanFactory}
     * @param beanClassName The  name of {@link Class} of Bean
     * @return If present , return <code>true</code> , or <code>false</code>
     */
    public static boolean isBeanPresent(ListableBeanFactory beanFactory,
                                        String beanClassName) {

        return isBeanPresent(beanFactory, beanClassName, false);

    }

    /**
     * Is Bean Present or not by the specified name and class
     *
     * @param beanFactory {@link BeanFactory}
     * @param beanName    The bean name
     * @param beanClass   The bean class
     * @return If present , return <code>true</code> , or <code>false</code>
     * @since 1.0.0
     */
    public static boolean isBeanPresent(BeanFactory beanFactory, String beanName, Class<?> beanClass)
            throws NullPointerException {
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
    public static String[] getBeanNames(ListableBeanFactory beanFactory, Class<?> beanClass,
                                        boolean includingAncestors) {
        // Issue : https://github.com/alibaba/spring-context-support/issues/22
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
    public static String[] getBeanNames(ConfigurableListableBeanFactory beanFactory, Class<?> beanClass,
                                        boolean includingAncestors) {
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

        if (!StringUtils.hasText(beanClassName)) {
            return null;
        }

        Class<?> beanType = null;

        try {

            beanType = ClassUtils.resolveClassName(beanClassName, classLoader);

            beanType = ClassUtils.getUserClass(beanType);

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
    public static <T> T getOptionalBean(ListableBeanFactory beanFactory, Class<T> beanClass,
                                        boolean includingAncestors) throws BeansException {

        String[] beanNames = getBeanNames(beanFactory, beanClass, includingAncestors);

        if (ObjectUtils.isEmpty(beanNames)) {
            if (logger.isDebugEnabled()) {
                logger.debug("The bean [ class : " + beanClass.getName() + " ] can't be found ");
            }
            return null;
        }

        T bean = null;

        try {

            bean = includingAncestors ?
                    beanOfTypeIncludingAncestors(beanFactory, beanClass) :
                    beanFactory.getBean(beanClass);

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
     * @since 1.0.0
     */
    public static <T> T getBeanIfAvailable(BeanFactory beanFactory, String beanName, Class<T> beanType)
            throws BeansException {
        if (isBeanPresent(beanFactory, beanName, beanType)) {
            return beanFactory.getBean(beanName, beanType);
        }

        if (logger.isDebugEnabled()) {
            logger.debug(format("The bean[name : %s , type : %s] can't be found in Spring BeanFactory",
                    beanName, beanType.getName()));
        }
        return null;
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


    public static void invokeAwareInterfaces(Object bean, BeanFactory beanFactory) {
        if (bean instanceof BeanFactoryAware) {
            ((BeanFactoryAware) bean).setBeanFactory(beanFactory);
        }

        if (bean instanceof BeanClassLoaderAware) {
            if (beanFactory instanceof ConfigurableBeanFactory) {
                ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
                ClassLoader classLoader = configurableBeanFactory.getBeanClassLoader();
                ((BeanClassLoaderAware) bean).setBeanClassLoader(classLoader);
            }
        }
    }

    /**
     * Invoke {@link Aware} interfaces if the given bean implements
     * <p>
     * Current implementation keeps the order of invocation {@link Aware Aware interfaces}
     *
     * @param bean    the bean
     * @param context {@link ApplicationContext}
     * @see #invokeAwareInterfaces(Object, BeanFactory)
     * @see ApplicationContextAwareProcessor#invokeAwareInterfaces(Object)
     * @see AbstractAutowireCapableBeanFactory#invokeAwareMethods(String, Object)
     */
    public static void invokeAwareInterfaces(Object bean, ApplicationContext context) {
        if (bean == null || context == null) {
            return;
        }

        ConfigurableApplicationContext applicationContext = asConfigurableApplicationContext(context);

        ConfigurableListableBeanFactory beanFactory = applicationContext != null ? applicationContext.getBeanFactory() : null;

        invokeAwareInterfaces(bean, beanFactory);

        if (bean instanceof EnvironmentAware) {
            ((EnvironmentAware) bean).setEnvironment(context.getEnvironment());
        }
        if (bean instanceof EmbeddedValueResolverAware && beanFactory != null) {
            StringValueResolver embeddedValueResolver = new EmbeddedValueResolver(beanFactory);
            ((EmbeddedValueResolverAware) bean).setEmbeddedValueResolver(embeddedValueResolver);
        }
        if (bean instanceof ResourceLoaderAware) {
            ((ResourceLoaderAware) bean).setResourceLoader(context);
        }
        if (bean instanceof ApplicationEventPublisherAware) {
            ((ApplicationEventPublisherAware) bean).setApplicationEventPublisher(context);
        }
        if (bean instanceof MessageSourceAware) {
            ((MessageSourceAware) bean).setMessageSource(context);
        }
        if (bean instanceof ApplicationStartupAware && applicationContext != null) {
            ((ApplicationStartupAware) bean).setApplicationStartup(applicationContext.getApplicationStartup());
        }
        if (bean instanceof ApplicationContextAware) {
            ((ApplicationContextAware) bean).setApplicationContext(context);
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
