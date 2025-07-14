package io.microsphere.spring.beans.factory.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import static org.springframework.aop.framework.AopProxyUtils.ultimateTargetClass;
import static org.springframework.core.GenericTypeResolver.resolveTypeArgument;

/**
 * Generic adapter implementation of the {@link BeanPostProcessor} interface, providing type-safe
 * processing for beans of a specific type. This class simplifies the development of typed
 * {@link BeanPostProcessor} implementations by allowing subclasses to handle only the bean types
 * they are interested in.
 *
 * <p>
 * Subclasses can override either the pre / post initialization hook methods ({@link #processBeforeInitialization(Object, String)}
 * and {@link #processAfterInitialization(Object, String)}) if they wish to perform processing without returning a modified bean instance,
 * or the more advanced {@link #doPostProcessBeforeInitialization(Object, String)} and
 * {@link #doPostProcessAfterInitialization(Object, String)} methods if they need to return a modified bean instance.
 * </p>
 *
 * <h3>Example Usage</h3>
 *
 * <pre>{@code
 * public class MyBeanPostProcessor extends GenericBeanPostProcessorAdapter<MyBean> {
 *     protected void processBeforeInitialization(MyBean bean, String beanName) {
 *         // Custom logic before bean initialization
 *     }
 *
 *     protected void processAfterInitialization(MyBean bean, String beanName) {
 *         // Custom logic after bean initialization
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of beans this post-processor will operate on
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanPostProcessor
 * @since 2017.01.22
 */
@SuppressWarnings("unchecked")
public abstract class GenericBeanPostProcessorAdapter<T> implements BeanPostProcessor {

    private final Class<T> beanType;

    public GenericBeanPostProcessorAdapter() {
        this.beanType = (Class<T>) resolveTypeArgument(getClass(), GenericBeanPostProcessorAdapter.class);
    }

    @Override
    public final Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = ultimateTargetClass(bean);
        if (beanType.isAssignableFrom(beanClass)) {
            return doPostProcessBeforeInitialization((T) bean, beanName);
        }
        return bean;
    }

    @Override
    public final Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = ultimateTargetClass(bean);
        if (beanType.isAssignableFrom(beanClass)) {
            return doPostProcessAfterInitialization((T) bean, beanName);
        }
        return bean;
    }

    /**
     * Bean Type
     *
     * @return Bean Type
     */
    public final Class<T> getBeanType() {
        return beanType;
    }

    /**
     * Adapter BeanPostProcessor#postProcessBeforeInitialization(Object, String) method , sub-type
     * could override this method.
     *
     * @param bean     Bean Object
     * @param beanName Bean Name
     * @return Bean Object
     * @see BeanPostProcessor#postProcessBeforeInitialization(Object, String)
     */
    protected T doPostProcessBeforeInitialization(T bean, String beanName) throws BeansException {

        processBeforeInitialization(bean, beanName);

        return bean;

    }

    /**
     * Adapter BeanPostProcessor#postProcessAfterInitialization(Object, String) method , sub-type
     * could override this method.
     *
     * @param bean     Bean Object
     * @param beanName Bean Name
     * @return Bean Object
     * @see BeanPostProcessor#postProcessAfterInitialization(Object, String)
     */
    protected T doPostProcessAfterInitialization(T bean, String beanName) throws BeansException {

        processAfterInitialization(bean, beanName);

        return bean;

    }

    /**
     * Process {@link T Bean} with name without return value before initialization,
     * <p>
     * This method will be invoked by BeanPostProcessor#postProcessBeforeInitialization(Object, String)
     *
     * @param bean     Bean Object
     * @param beanName Bean Name
     * @throws BeansException in case of errors
     */
    protected void processBeforeInitialization(T bean, String beanName) throws BeansException {
    }

    /**
     * Process {@link T Bean} with name without return value after initialization,
     * <p>
     * This method will be invoked by BeanPostProcessor#postProcessAfterInitialization(Object, String)
     *
     * @param bean     Bean Object
     * @param beanName Bean Name
     * @throws BeansException in case of errors
     */
    protected void processAfterInitialization(T bean, String beanName) throws BeansException {
    }

}
