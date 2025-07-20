package io.microsphere.spring.beans;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.Aware;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
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
import static io.microsphere.util.ArrayUtils.isEmpty;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.springframework.beans.factory.BeanFactoryUtils.beanNamesForTypeIncludingAncestors;
import static org.springframework.beans.factory.BeanFactoryUtils.beanOfTypeIncludingAncestors;
import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.generateBeanName;
import static org.springframework.util.ClassUtils.getUserClass;

/**
 * Bean Utilities Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 2017.01.13
 */
public abstract class BeanUtils implements Utils {

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
    public static boolean isBeanPresent(@Nonnull ListableBeanFactory beanFactory, @Nonnull Class<?> beanClass) {
        return isBeanPresent(beanFactory, beanClass, false);
    }

    /**
     * Check if a bean of the specified class is present in the given {@link ListableBeanFactory}.
     * <p>
     * This method checks whether there is at least one bean of the specified type in the bean factory.
     * If the parameter {@code includingAncestors} is set to {@code true}, it will also check ancestor bean factories.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean present = isBeanPresent(beanFactory, MyService.class, true);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>If the bean class is null or not resolvable, an empty array will be returned.</li>
     *     <li>If no beans are found and logging is enabled at trace level, a log message will be recorded.</li>
     * </ul>
     *
     * @param beanFactory        the {@link ListableBeanFactory} to search for beans
     * @param beanClass          the class of the bean to check presence for
     * @param includingAncestors whether to include ancestor bean factories in the search
     * @return true if at least one bean of the specified type exists; false otherwise
     */
    public static boolean isBeanPresent(@Nonnull ListableBeanFactory beanFactory, @Nonnull Class<?> beanClass,
                                        boolean includingAncestors) {
        String[] beanNames = getBeanNames(beanFactory, beanClass, includingAncestors);
        return isNotEmpty(beanNames);
    }

    /**
     * Check if a bean with the specified class name is present in the given {@link ListableBeanFactory}.
     *
     * <p>
     * This method checks whether there is at least one bean with the specified class name in the bean factory.
     * The check includes only the current bean factory and does not search in ancestor factories.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean present = isBeanPresent(beanFactory, "com.example.MyService");
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>The method resolves the class using the bean factory's class loader (if available).</li>
     *     <li>If the class cannot be resolved or no bean is found, it returns false.</li>
     * </ul>
     *
     * @param beanFactory   the {@link ListableBeanFactory} to search for beans
     * @param beanClassName the fully qualified name of the class to check presence for
     * @return true if at least one bean of the specified class name exists; false otherwise
     */
    public static boolean isBeanPresent(@Nonnull ListableBeanFactory beanFactory, @Nonnull String beanClassName) {
        return isBeanPresent(beanFactory, beanClassName, false);
    }

    /**
     * Check if a bean with the specified class name is present in the given {@link ListableBeanFactory}.
     *
     * <p>
     * This method checks whether there is at least one bean with the specified class name in the bean factory.
     * If the parameter {@code includingAncestors} is set to {@code true}, it will also check ancestor bean factories.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean present = isBeanPresent(beanFactory, "com.example.MyService", true);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>The method resolves the class using the bean factory's class loader (if available).</li>
     *     <li>If the class cannot be resolved or no bean is found, it returns false.</li>
     *     <li>If logging is enabled at trace level and no bean is found, a log message will be recorded.</li>
     * </ul>
     *
     * @param beanFactory        the {@link ListableBeanFactory} to search for beans
     * @param beanClassName      the fully qualified name of the class to check presence for
     * @param includingAncestors whether to include ancestor bean factories in the search
     * @return true if at least one bean of the specified class name exists; false otherwise
     */
    public static boolean isBeanPresent(@Nonnull ListableBeanFactory beanFactory, @Nonnull String beanClassName,
                                        boolean includingAncestors) {
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
     * Check if a bean with the specified name and class is present in the given {@link BeanFactory}.
     *
     * <p>
     * This method verifies whether a bean exists in the bean factory by both its name and type.
     * It ensures that the bean is present and matches the expected class type.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean present = isBeanPresent(beanFactory, "myService", MyService.class);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>Returns false if either the bean factory, bean name, or bean class is null.</li>
     *     <li>If logging is enabled at trace level, a message will be logged when the bean is not found.</li>
     * </ul>
     *
     * @param beanFactory the {@link BeanFactory} to check for the presence of the bean
     * @param beanName    the name of the bean to check
     * @param beanClass   the class type of the bean to match
     * @param <T>         the type of the bean
     * @return true if the bean is present and matches the specified class; false otherwise
     */
    public static <T> boolean isBeanPresent(@Nonnull BeanFactory beanFactory, @Nonnull String beanName,
                                            @Nonnull Class<?> beanClass) throws NullPointerException {
        return beanFactory.containsBean(beanName) && beanFactory.isTypeMatch(beanName, beanClass);
    }

    /**
     * Get bean names of the specified type from the given {@link ListableBeanFactory}.
     *
     * <p>
     * This method retrieves the names of all beans that match the specified class type.
     * The search is limited to the current bean factory and does not include ancestor factories.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] beanNames = getBeanNames(beanFactory, MyService.class);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>Returns an empty array if no beans of the specified type are found.</li>
     *     <li>If logging is enabled at trace level, a message will be logged when no beans are found.</li>
     * </ul>
     *
     * @param beanFactory the {@link ListableBeanFactory} to retrieve bean names from
     * @param beanClass   the class type to match
     * @return an array of bean names matching the specified type; returns an empty array if none are found
     */
    @Nonnull
    public static String[] getBeanNames(@Nonnull ListableBeanFactory beanFactory, @Nonnull Class<?> beanClass) {
        return getBeanNames(beanFactory, beanClass, false);
    }

    /**
     * Get Bean Names from {@link ListableBeanFactory} by type.
     *
     * <p>
     * This method retrieves the names of all beans that match the specified class type.
     * If the parameter {@code includingAncestors} is set to {@code true}, it will also check ancestor bean factories.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] beanNames = getBeanNames(beanFactory, MyService.class, true);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>If no beans are found and logging is enabled at trace level, a log message will be recorded.</li>
     *     <li>Returns an empty array if no beans of the specified type are found.</li>
     * </ul>
     *
     * @param beanFactory        the {@link ListableBeanFactory} to retrieve bean names from
     * @param beanClass          the class type to match
     * @param includingAncestors whether to include ancestor bean factories in the search
     * @return an array of bean names matching the specified type; returns an empty array if none are found
     */
    @Nonnull
    public static String[] getBeanNames(@Nonnull ListableBeanFactory beanFactory, @Nonnull Class<?> beanClass,
                                        @Nonnull boolean includingAncestors) {
        if (includingAncestors) {
            return beanNamesForTypeIncludingAncestors(beanFactory, beanClass, true, false);
        } else {
            return beanFactory.getBeanNamesForType(beanClass, true, false);
        }
    }

    /**
     * Get bean names of the specified type from the given {@link ConfigurableListableBeanFactory}.
     *
     * <p>
     * This method retrieves the names of all beans that match the specified class type.
     * The search is limited to the current bean factory and does not include ancestor factories.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] beanNames = getBeanNames(beanFactory, MyService.class);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>Returns an empty array if no beans of the specified type are found.</li>
     *     <li>If logging is enabled at trace level, a message will be logged when no beans are found.</li>
     * </ul>
     *
     * @param beanFactory the {@link ConfigurableListableBeanFactory} to retrieve bean names from
     * @param beanClass   the class type to match
     * @return an array of bean names matching the specified type; returns an empty array if none are found
     */
    public static String[] getBeanNames(@Nonnull ConfigurableListableBeanFactory beanFactory, @Nonnull Class<?> beanClass) {
        return getBeanNames(beanFactory, beanClass, false);
    }

    /**
     * Get bean names of the specified type from the given {@link ConfigurableListableBeanFactory}.
     *
     * <p>
     * This method retrieves the names of all beans that match the specified class type.
     * If the parameter {@code includingAncestors} is set to {@code true}, it will also check ancestor bean factories.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * String[] beanNames = getBeanNames(beanFactory, MyService.class, true);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>If no beans are found and logging is enabled at trace level, a log message will be recorded.</li>
     *     <li>Returns an empty array if no beans of the specified type are found.</li>
     * </ul>
     *
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to retrieve bean names from
     * @param beanClass          the class type to match
     * @param includingAncestors whether to include ancestor bean factories in the search
     * @return an array of bean names matching the specified type; returns an empty array if none are found
     */
    @Nonnull
    public static String[] getBeanNames(@Nonnull ConfigurableListableBeanFactory beanFactory, @Nonnull Class<?> beanClass,
                                        boolean includingAncestors) {
        return getBeanNames((ListableBeanFactory) beanFactory, beanClass, includingAncestors);
    }

    /**
     * Resolve the bean type from the given class name using the specified ClassLoader.
     *
     * <p>This method attempts to load the class with the provided name using the given ClassLoader.
     * If the class cannot be found, it returns null. Otherwise, it returns the resolved class.
     * Additionally, if the provided class name is null or empty, the method will return null.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> beanType = resolveBeanType("com.example.MyService", classLoader);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>If the class name is null or empty, it returns null.</li>
     *     <li>If the class cannot be loaded, it logs a warning and returns null.</li>
     *     <li>Otherwise, it returns the resolved class.</li>
     * </ul>
     *
     * @param beanClassName The fully qualified name of the class to resolve.
     * @param classLoader   The ClassLoader to use for loading the class.
     * @return The resolved class, or null if resolution fails.
     */
    @Nullable
    public static Class<?> resolveBeanType(@Nullable String beanClassName, @Nullable ClassLoader classLoader) {
        Class<?> beanType = resolveClass(beanClassName, classLoader);
        return beanType == null ? null : getUserClass(beanType);
    }

    /**
     * Retrieve an optional bean of the specified type from the given {@link ListableBeanFactory}.
     *
     * <p>
     * This method attempts to find and return a single bean of the specified class.
     * If no beans are found, it returns {@code null}. If multiple beans are found,
     * it throws a {@link NoUniqueBeanDefinitionException}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyService myService = getOptionalBean(beanFactory, MyService.class);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>If no beans of the specified type are found, returns {@code null}.</li>
     *     <li>If multiple beans are found, throws a {@link NoUniqueBeanDefinitionException}.</li>
     *     <li>If logging is enabled at trace level, logs a message when no bean is found.</li>
     *     <li>If logging is enabled at error level, logs any exception thrown during bean retrieval.</li>
     * </ul>
     *
     * @param beanFactory        the {@link ListableBeanFactory} to retrieve the bean from
     * @param beanClass          the class of the bean to look up
     * @param includingAncestors whether to include ancestor bean factories in the search
     * @param <T>                the type of the bean
     * @return the matching bean instance if found; otherwise, returns {@code null}
     * @throws BeansException if there is an issue retrieving the bean or multiple beans are found
     */
    public static <T> T getOptionalBean(@Nonnull ListableBeanFactory beanFactory, @Nonnull Class<T> beanClass,
                                        boolean includingAncestors) throws BeansException {
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
     * Retrieve an optional bean of the specified type from the given {@link ListableBeanFactory}.
     *
     * <p>
     * This method attempts to find and return a single bean of the specified class.
     * If no beans are found, it returns {@code null}. If multiple beans are found,
     * it throws a {@link NoUniqueBeanDefinitionException}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyService myService = BeanUtils.getOptionalBean(beanFactory, MyService.class);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>If no beans of the specified type are found, returns {@code null}.</li>
     *     <li>If multiple beans are found, throws a {@link NoUniqueBeanDefinitionException}.</li>
     *     <li>If logging is enabled at trace level, logs a message when no bean is found.</li>
     *     <li>If logging is enabled at error level, logs any exception thrown during bean retrieval.</li>
     * </ul>
     *
     * @param beanFactory the {@link ListableBeanFactory} to retrieve the bean from
     * @param beanClass   the class of the bean to look up
     * @param <T>         the type of the bean
     * @return the matching bean instance if found; otherwise, returns {@code null}
     * @throws BeansException if there is an issue retrieving the bean or multiple beans are found
     */
    @Nullable
    public static <T> T getOptionalBean(@Nonnull ListableBeanFactory beanFactory, @Nonnull Class<T> beanClass) throws BeansException {
        return getOptionalBean(beanFactory, beanClass, false);
    }

    /**
     * Retrieve the bean with the specified name and type from the given {@link BeanFactory} if it exists.
     *
     * <p>
     * This method checks whether a bean with the specified name and type exists in the bean factory.
     * If it does, the bean is returned; otherwise, {@code null} is returned.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyService myService = getBeanIfAvailable(beanFactory, "myService", MyService.class);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>If the bean is not present, returns {@code null}.</li>
     *     <li>If logging is enabled at trace level, logs a message when the bean is not found.</li>
     * </ul>
     *
     * @param beanFactory the {@link BeanFactory} to retrieve the bean from
     * @param beanName    the name of the bean to look up
     * @param beanType    the class type of the bean to match
     * @param <T>         the type of the bean
     * @return the matching bean instance if found; otherwise, returns {@code null}
     * @throws BeansException if there is an issue retrieving the bean
     */
    @Nullable
    public static <T> T getBeanIfAvailable(@Nonnull BeanFactory beanFactory, @Nonnull String beanName,
                                           @Nonnull Class<T> beanType) throws BeansException {
        if (isBeanPresent(beanFactory, beanName, beanType)) {
            return beanFactory.getBean(beanName, beanType);
        }

        if (logger.isTraceEnabled()) {
            logger.trace("The bean[name : '{}' , type : {}] can't be found in Spring BeanFactory", beanName, beanType.getName());
        }
        return null;
    }

    /**
     * Retrieve a list of beans of the specified type from the given {@link BeanFactory}, sorted based on their order.
     *
     * <p>
     * If the provided {@link BeanFactory} is an instance of {@link ListableBeanFactory}, it delegates to
     * {@link #getSortedBeans(ListableBeanFactory, Class)} for retrieving and sorting the beans. Otherwise,
     * it returns an empty list.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<MyService> myServices = getSortedBeans(beanFactory, MyService.class);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>Returns an unmodifiable list of beans sorted by their order.</li>
     *     <li>If no beans are found, returns an empty list.</li>
     * </ul>
     *
     * @param beanFactory the {@link BeanFactory} to retrieve the beans from
     * @param type        the class type of the beans to look up
     * @param <T>         the type of the beans
     * @return a sorted list of beans matching the specified type; returns an empty list if none are found
     */
    @Nonnull
    public static <T> List<T> getSortedBeans(@Nonnull BeanFactory beanFactory, @Nonnull Class<T> type) {
        if (beanFactory instanceof ListableBeanFactory) {
            return getSortedBeans((ListableBeanFactory) beanFactory, type);
        }
        return emptyList();
    }

    /**
     * Retrieve all beans of the specified type from the given {@link ListableBeanFactory}, sorted based on their order.
     *
     * <p>
     * This method retrieves a map of bean names to bean instances matching the specified type and converts it into a list.
     * The list is then sorted using {@link AnnotationAwareOrderComparator} based on the beans' order.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<MyService> myServices = getSortedBeans(beanFactory, MyService.class);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>Returns an unmodifiable list of beans sorted by their order.</li>
     *     <li>If no beans are found, returns an empty list.</li>
     * </ul>
     *
     * @param beanFactory the {@link ListableBeanFactory} to retrieve the beans from
     * @param type        the class type of the beans to look up
     * @param <T>         the type of the beans
     * @return a sorted list of beans matching the specified type; returns an empty list if none are found
     */
    @Nonnull
    public static <T> List<T> getSortedBeans(@Nonnull ListableBeanFactory beanFactory, @Nonnull Class<T> type) {
        Map<String, T> beansOfType = beansOfTypeIncludingAncestors(beanFactory, type);
        List<T> beansList = new ArrayList<T>(beansOfType.values());
        AnnotationAwareOrderComparator.sort(beansList);
        return unmodifiableList(beansList);
    }

    /**
     * Invokes the standard Spring bean lifecycle interfaces in a specific order for the given bean and application context.
     *
     * <p>This method delegates to {@link #invokeBeanInterfaces(Object, ConfigurableApplicationContext)} after converting
     * the provided {@link ApplicationContext} into a {@link ConfigurableApplicationContext}, allowing further processing
     * if needed.</p>
     * <p>
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
     *     <li>{@link org.springframework.context.ApplicationStartupAware} (Spring Framework 5.3+)</li>
     *     <li>{@link ApplicationContextAware}</li>
     *     <li>{@link InitializingBean}</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyBean myBean = new MyBean();
     * ApplicationContext context = ...; // Obtain or create an ApplicationContext
     * invokeBeanInterfaces(myBean, context);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>If the provided bean implements any of the supported lifecycle interfaces, their respective methods will be invoked.</li>
     *     <li>The order of invocation follows the standard Spring lifecycle contract.</li>
     * </ul>
     *
     * @param bean    The bean instance whose lifecycle interfaces should be invoked.
     * @param context The application context associated with the bean, may be null.
     */
    public static void invokeBeanInterfaces(@Nullable Object bean, @Nullable ApplicationContext context) {
        ConfigurableApplicationContext configurableApplicationContext = asConfigurableApplicationContext(context);
        invokeBeanInterfaces(bean, configurableApplicationContext);
    }

    /**
     * Invokes the standard Spring bean lifecycle interfaces in a specific order for the given bean and application context.
     *
     * <p>This method ensures that all relevant lifecycle callbacks are executed in the correct sequence,
     * starting with the {@link Aware} interface methods followed by the {@link InitializingBean#afterPropertiesSet()} method.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyBean myBean = new MyBean();
     * ConfigurableApplicationContext context = ...; // Obtain or create a ConfigurableApplicationContext
     * BeanUtils.invokeBeanInterfaces(myBean, context);
     * }</pre>
     *
     * <h3>Lifecycle Invocation Order</h3>
     * <ol>
     *     <li>{@link BeanNameAware}</li>
     *     <li>{@link BeanClassLoaderAware}</li>
     *     <li>{@link BeanFactoryAware}</li>
     *     <li>{@link EnvironmentAware}</li>
     *     <li>{@link EmbeddedValueResolverAware}</li>
     *     <li>{@link ResourceLoaderAware}</li>
     *     <li>{@link ApplicationEventPublisherAware}</li>
     *     <li>{@link MessageSourceAware}</li>
     *     <li>{@link org.springframework.context.ApplicationStartupAware} (Spring Framework 5.3+)</li>
     *     <li>{@link ApplicationContextAware}</li>
     *     <li>{@link InitializingBean#afterPropertiesSet()}</li>
     * </ol>
     *
     * @param bean    the bean instance whose lifecycle interfaces should be invoked
     * @param context the application context associated with the bean, may be null
     * @see #invokeBeanInterfaces(Object, ApplicationContext)
     * @see #invokeAwareInterfaces(Object, ApplicationContext)
     * @see #invokeInitializingBean(Object)
     */
    public static void invokeBeanInterfaces(@Nullable Object bean, @Nullable ConfigurableApplicationContext context) {
        invokeAwareInterfaces(bean, context);
        try {
            invokeInitializingBean(bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Invoke the {@link InitializingBean#afterPropertiesSet()} method if the given bean implements the interface.
     *
     * <p>This method is typically used to trigger post-processing logic after all bean properties have been set.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyInitializingBean myBean = new MyInitializingBean();
     * BeanUtils.invokeInitializingBean(myBean);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>If the provided bean does not implement {@link InitializingBean}, this method has no effect.</li>
     *     <li>If an exception occurs during execution of {@link InitializingBean#afterPropertiesSet()}, it will be propagated.</li>
     * </ul>
     *
     * @param bean the bean object to invoke the initialization method on, may be null
     * @throws Exception if an error occurs while invoking the initialization method
     */
    public static void invokeInitializingBean(@Nullable Object bean) throws Exception {
        if (bean instanceof InitializingBean) {
            ((InitializingBean) bean).afterPropertiesSet();
        }
    }

    /**
     * Invokes the Spring {@link Aware} interfaces implemented by the given bean if applicable.
     *
     * <p>This method supports a standard lifecycle invocation order for common Spring Aware interfaces:
     * <ul>
     *     <li>{@link BeanNameAware}</li>
     *     <li>{@link BeanClassLoaderAware}</li>
     *     <li>{@link BeanFactoryAware}</li>
     * </ul>
     *
     * <p>If the provided {@link BeanFactory} is also an instance of {@link ApplicationContext}, additional
     * context-specific Aware interfaces will be invoked in sequence:
     * <ul>
     *     <li>{@link EnvironmentAware}</li>
     *     <li>{@link EmbeddedValueResolverAware}</li>
     *     <li>{@link ResourceLoaderAware}</li>
     *     <li>{@link ApplicationEventPublisherAware}</li>
     *     <li>{@link MessageSourceAware}</li>
     *     <li>{@link org.springframework.context.ApplicationStartupAware} (Spring Framework 5.3+)</li>
     *     <li>{@link ApplicationContextAware}</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyBean myBean = new MyBean();
     * ConfigurableBeanFactory beanFactory = context.getBeanFactory();
     * BeanUtils.invokeAwareInterfaces(myBean, beanFactory);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>No action is taken if the bean is null.</li>
     *     <li>Only relevant Aware interfaces are invoked based on the bean's implementation.</li>
     * </ul>
     *
     * @param bean        the bean object that may implement one or more {@link Aware} interfaces
     * @param beanFactory the factory used to configure the bean; may be null
     */
    public static void invokeAwareInterfaces(@Nullable Object bean, @Nullable BeanFactory beanFactory) {
        invokeAwareInterfaces(bean, beanFactory, asConfigurableBeanFactory(beanFactory));
    }

    /**
     * Invokes the Spring {@link Aware} interfaces implemented by the given bean if applicable.
     *
     * <p>This method ensures that all relevant lifecycle callbacks are executed in the correct sequence,
     * starting with the basic {@link BeanNameAware}, {@link BeanClassLoaderAware}, and
     * {@link BeanFactoryAware} interfaces. If the provided bean factory is also an application context,
     * additional context-specific aware interfaces will be invoked, including:
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
     *     <li>{@link org.springframework.context.ApplicationStartupAware} (Spring Framework 5.3+)</li>
     *     <li>{@link ApplicationContextAware}</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyBean myBean = new MyBean();
     * ConfigurableBeanFactory beanFactory = context.getBeanFactory();
     * BeanUtils.invokeAwareInterfaces(myBean, beanFactory);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>No action is taken if the bean is null.</li>
     *     <li>If the bean implements any of the supported interfaces, their respective methods will be invoked.</li>
     *     <li>If the bean factory is a context, more specific aware interfaces will be processed.</li>
     * </ul>
     *
     * @param bean        the bean instance whose lifecycle interfaces should be invoked
     * @param beanFactory the associated bean factory, may be null
     */
    public static void invokeAwareInterfaces(@Nullable Object bean, @Nullable ConfigurableBeanFactory beanFactory) {
        invokeAwareInterfaces(bean, beanFactory, beanFactory);
    }

    /**
     * Invokes the Spring {@link Aware} interfaces implemented by the given bean if applicable.
     *
     * <p>This method supports a standard lifecycle invocation order for common Spring Aware interfaces:
     * <ul>
     *     <li>{@link BeanNameAware}</li>
     *     <li>{@link BeanClassLoaderAware}</li>
     *     <li>{@link BeanFactoryAware}</li>
     * </ul>
     *
     * <p>If the provided {@link BeanFactory} is also an instance of {@link ApplicationContext}, additional
     * context-specific Aware interfaces will be invoked in sequence:
     * <ul>
     *     <li>{@link EnvironmentAware}</li>
     *     <li>{@link EmbeddedValueResolverAware}</li>
     *     <li>{@link ResourceLoaderAware}</li>
     *     <li>{@link ApplicationEventPublisherAware}</li>
     *     <li>{@link MessageSourceAware}</li>
     *     <li>{@link org.springframework.context.ApplicationStartupAware} (Spring Framework 5.3+)</li>
     *     <li>{@link ApplicationContextAware}</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyBean myBean = new MyBean();
     * ConfigurableBeanFactory beanFactory = context.getBeanFactory();
     * BeanUtils.invokeAwareInterfaces(myBean, beanFactory, beanFactory);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>No action is taken if the bean is null.</li>
     *     <li>Only relevant Aware interfaces are invoked based on the bean's implementation.</li>
     * </ul>
     *
     * @param bean                    the bean object that may implement one or more {@link Aware} interfaces
     * @param beanFactory             the factory used to configure the bean; may be null
     * @param configurableBeanFactory the configurable version of the bean factory; may be null
     */
    public static void invokeAwareInterfaces(@Nullable Object bean, @Nullable BeanFactory beanFactory,
                                             @Nullable ConfigurableBeanFactory configurableBeanFactory) {
        if (beanFactory instanceof ApplicationContext) {
            invokeAwareInterfaces(bean, (ApplicationContext) beanFactory);
        } else {
            invokeBeanFactoryAwareInterfaces(bean, beanFactory, configurableBeanFactory);
        }
    }

    /**
     * Find the primary constructor of the specified class.
     *
     * <p>
     * For Kotlin classes, this method returns the Java constructor corresponding to the Kotlin primary constructor,
     * as defined in the Kotlin specification. For non-Kotlin classes, this method typically returns {@code null}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Constructor<MyClass> constructor = findPrimaryConstructor(MyClass.class);
     * if (constructor != null) {
     *     MyClass instance = constructor.newInstance();
     * }
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>Returns the primary constructor if available.</li>
     *     <li>Returns {@code null} if no primary constructor is found or if the class is not a Kotlin class.</li>
     *     <li>If an error occurs during invocation, it is logged at warn level and null is returned.</li>
     * </ul>
     *
     * @param clazz the class to check for a primary constructor, must not be {@code null}
     * @param <T>   the type of the class
     * @return the primary constructor of the class, or {@code null} if none is found
     * @see org.springframework.beans.BeanUtils#findPrimaryConstructor(Class)
     * @since Spring Framework 5.0
     */
    public static <T> Constructor<T> findPrimaryConstructor(@Nonnull Class<T> clazz) {
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
    static void invokeBeanFactoryAwareInterfaces(@Nonnull Object bean, BeanFactory beanFactory,
                                                 @Nullable ConfigurableBeanFactory configurableBeanFactory) {
        invokeBeanNameAware(bean, beanFactory);
        invokeBeanClassLoaderAware(bean, configurableBeanFactory);
        invokeBeanFactoryAware(bean, beanFactory);
    }

    static void invokeBeanNameAware(@Nonnull Object bean, @Nonnull BeanFactory beanFactory) {
        if (bean instanceof BeanNameAware) {
            BeanDefinitionRegistry registry = asBeanDefinitionRegistry(beanFactory);
            BeanDefinition beanDefinition = rootBeanDefinition(bean.getClass()).getBeanDefinition();
            String beanName = generateBeanName(beanDefinition, registry);
            ((BeanNameAware) bean).setBeanName(beanName);
        }
    }

    /**
     * Invokes the {@link BeanNameAware#setBeanName(String)} method if the given bean implements the interface.
     *
     * <p>This method is typically used during bean initialization to set the bean's name as defined in the
     * Spring configuration or generated by the container. If the provided bean does not implement
     * {@link BeanNameAware}, this method has no effect.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyBean myBean = new MyBean();
     * BeanUtils.invokeBeanNameAware(myBean, "myBean");
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>If the bean implements {@link BeanNameAware}, the method sets the bean's name using the provided value.</li>
     *     <li>If the bean is null or does not implement the interface, it returns silently without any action.</li>
     * </ul>
     *
     * @param bean     the bean object that may implement {@link BeanNameAware}
     * @param beanName the name to be assigned to the bean, may be null
     */
    public static void invokeBeanNameAware(@Nullable Object bean, @Nullable String beanName) {
        if (bean instanceof BeanNameAware && beanName != null) {
            ((BeanNameAware) bean).setBeanName(beanName);
        }
    }

    static void invokeBeanFactoryAware(@Nullable Object bean, @Nullable BeanFactory beanFactory) {
        if (bean instanceof BeanFactoryAware && beanFactory != null) {
            ((BeanFactoryAware) bean).setBeanFactory(beanFactory);
        }
    }

    static void invokeBeanClassLoaderAware(@Nullable Object bean, @Nullable ConfigurableBeanFactory configurableBeanFactory) {
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
     *     <li>{@link org.springframework.context.ApplicationStartupAware} (Spring Framework 5.3+)</li>
     *     <li>{@link ApplicationContextAware}</li>
     * </ul>
     *
     * @param bean    the bean
     * @param context {@link ApplicationContext}
     * @see #invokeAwareInterfaces(Object, BeanFactory)
     * @see org.springframework.context.support.ApplicationContextAwareProcessor#invokeAwareInterfaces(Object)
     * @see AbstractAutowireCapableBeanFactory#invokeAwareMethods(String, Object)
     */
    static void invokeAwareInterfaces(@Nullable Object bean, @Nullable ApplicationContext context) {
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
     *     <li>{@link org.springframework.context.ApplicationStartupAware} (Spring Framework 5.3+)</li>
     *     <li>{@link ApplicationContextAware}</li>
     * </ul>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyBean myBean = new MyBean();
     * ConfigurableApplicationContext context = ...; // Obtain or create a ConfigurableApplicationContext
     * BeanUtils.invokeAwareInterfaces(myBean, context);
     * }</pre>
     *
     * <h3>Behavior</h3>
     * <ul>
     *     <li>No action is taken if the bean is null.</li>
     *     <li>If the bean implements any of the supported interfaces, their respective methods will be invoked.</li>
     *     <li>If the bean factory is a context, more specific aware interfaces will be processed.</li>
     * </ul>
     *
     * @param bean    the bean instance whose lifecycle interfaces should be invoked
     * @param context the application context associated with the bean, may be null
     */
    public static void invokeAwareInterfaces(@Nullable Object bean, @Nullable ConfigurableApplicationContext context) {
        invokeAwareInterfaces(bean, context, context);
    }

    static void invokeAwareInterfaces(@Nullable Object bean, @Nullable ApplicationContext context,
                                      @Nullable ConfigurableApplicationContext applicationContext) {
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

    private BeanUtils() {
    }
}
