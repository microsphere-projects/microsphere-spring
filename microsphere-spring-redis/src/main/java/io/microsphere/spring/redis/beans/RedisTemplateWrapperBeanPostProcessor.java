package io.microsphere.spring.redis.beans;

import io.microsphere.spring.redis.context.RedisContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.Assert;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static io.microsphere.spring.redis.util.RedisConstants.ALL_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES;
import static io.microsphere.spring.redis.util.RedisConstants.WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.springframework.aop.framework.AopProxyUtils.ultimateTargetClass;

/**
 * {@link BeanPostProcessor} implements Wrapper {@link RedisTemplate} and {@link StringRedisTemplate}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see RedisTemplateWrapper
 * @see StringRedisTemplateWrapper
 * @see BeanPostProcessor
 * @since 1.0.0
 */
public class RedisTemplateWrapperBeanPostProcessor implements BeanPostProcessor, InitializingBean, ApplicationContextAware {

    public static final String BEAN_NAME = "redisTemplateWrapperBeanPostProcessor";

    private ConfigurableApplicationContext context;

    private RedisContext redisContext;

    private Set<String> wrappedRedisTemplateBeanNames;

    private WrapperProcessors wrapperProcessors;

    public RedisTemplateWrapperBeanPostProcessor() {
    }

    public RedisTemplateWrapperBeanPostProcessor(Collection<String> wrappedRedisTemplateBeanNames) {
        this.wrappedRedisTemplateBeanNames = new HashSet<>(wrappedRedisTemplateBeanNames);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (wrappedRedisTemplateBeanNames.contains(beanName)) {
            Class<?> beanClass = ultimateTargetClass(bean);
            if (StringRedisTemplate.class.equals(beanClass)) {
                StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) bean;
                return process(new StringRedisTemplateWrapper(beanName, stringRedisTemplate, redisContext));
            } else if (RedisTemplate.class.equals(beanClass)) {
                RedisTemplate redisTemplate = (RedisTemplate) bean;
                return process(new RedisTemplateWrapper(beanName, redisTemplate, redisContext));
            }
            // TODO Support for more custom RedisTemplate types
        }
        return bean;
    }

    private <W extends Wrapper> W process(W wrapper) {
        return wrapperProcessors.process(wrapper);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        Assert.isInstanceOf(ConfigurableApplicationContext.class, context, "The 'context' argument must be an instance of ConfigurableApplicationContext");
        this.context = (ConfigurableApplicationContext) context;
    }

    /**
     * Resolve the wrapped {@link RedisTemplate} Bean Name list, the default value is from {@link Collections#emptySet()}
     *
     * @param context {@link ConfigurableApplicationContext}
     * @return If no configuration is found, {@link Collections#emptySet()} is returned
     */
    private Set<String> resolveWrappedRedisTemplateBeanNames(ConfigurableApplicationContext context) {
        Environment environment = context.getEnvironment();
        Set<String> wrappedRedisTemplateBeanNames = environment.getProperty(WRAPPED_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME, Set.class);
        if (wrappedRedisTemplateBeanNames == null) {
            return emptySet();
        } else if (ALL_WRAPPED_REDIS_TEMPLATE_BEAN_NAMES.equals(wrappedRedisTemplateBeanNames)) {
            return redisContext.getRedisTemplateBeanNames();
        } else {
            return unmodifiableSet(wrappedRedisTemplateBeanNames);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.redisContext = RedisContext.get(context);
        if (this.wrappedRedisTemplateBeanNames == null) {
            this.wrappedRedisTemplateBeanNames = resolveWrappedRedisTemplateBeanNames(context);
        }
        this.wrapperProcessors = context.getBean(WrapperProcessors.BEAN_NAME, WrapperProcessors.class);
    }

    public Set<String> getWrappedRedisTemplateBeanNames() {
        return wrappedRedisTemplateBeanNames;
    }
}
