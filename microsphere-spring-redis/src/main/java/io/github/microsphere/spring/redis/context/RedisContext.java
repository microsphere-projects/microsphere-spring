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
package io.github.microsphere.spring.redis.context;

import io.github.microsphere.spring.redis.config.RedisConfiguration;
import io.github.microsphere.spring.redis.interceptor.RedisCommandInterceptor;
import io.github.microsphere.spring.redis.interceptor.RedisConnectionInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static io.github.microsphere.spring.util.BeanUtils.getBeanNames;
import static io.github.microsphere.spring.util.BeanUtils.getSortedBeans;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

/**
 * Redis Context
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class RedisContext implements SmartInitializingSingleton, ApplicationContextAware, BeanFactoryAware, BeanClassLoaderAware {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfiguration.class);

    public static final String BEAN_NAME = "redisContext";

    private static final Class<RedisTemplate> REDIS_TEMPLATE_CLASS = RedisTemplate.class;

    private static final Class<RedisConfiguration> REDIS_CONFIGURATION_CLASS = RedisConfiguration.class;

    private ConfigurableListableBeanFactory beanFactory;

    private ConfigurableApplicationContext context;

    private ClassLoader classLoader;

    private RedisConfiguration redisConfiguration;

    private Set<String> redisTemplateBeanNames;

    private Set<String> redisConnectionFactoryBeanNames;

    private List<RedisConnectionInterceptor> redisConnectionInterceptors;

    private List<RedisCommandInterceptor> redisCommandInterceptors;

    @Override
    public void afterSingletonsInstantiated() {
        this.redisConfiguration = RedisConfiguration.get(context);
        this.redisTemplateBeanNames = findRestTemplateBeanNames(beanFactory);
        this.redisConnectionFactoryBeanNames = findRedisConnectionFactoryBeanNames(beanFactory);
        this.redisConnectionInterceptors = findRedisConnectionInterceptors(beanFactory);
        this.redisCommandInterceptors = findRedisCommandInterceptors(beanFactory);
    }

    @NonNull
    public RedisConfiguration getRedisConfiguration() {
        RedisConfiguration redisConfiguration = this.redisConfiguration;
        if (redisConfiguration == null) {
            logger.debug("RedisConfiguration is not initialized, it will be got from BeanFactory[{}]", beanFactory);
            redisConfiguration = RedisConfiguration.get(beanFactory);
            this.redisConfiguration = redisConfiguration;
        }
        return redisConfiguration;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = (ConfigurableApplicationContext) applicationContext;
    }

    public ConfigurableListableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    public ConfigurableApplicationContext getApplicationContext() {
        return context;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Set<String> getRedisTemplateBeanNames() {
        return redisTemplateBeanNames;
    }

    public Set<String> getRedisConnectionFactoryBeanNames() {
        return redisConnectionFactoryBeanNames;
    }

    public boolean isEnabled() {
        return getRedisConfiguration().isEnabled();
    }

    public ConfigurableEnvironment getEnvironment() {
        return getRedisConfiguration().getEnvironment();
    }

    public boolean isCommandEventExposed() {
        return getRedisConfiguration().isCommandEventExposed();
    }

    public String getApplicationName() {
        return getRedisConfiguration().getApplicationName();
    }

    public List<RedisConnectionInterceptor> getRedisConnectionInterceptors() {
        return redisConnectionInterceptors;
    }

    public List<RedisCommandInterceptor> getRedisCommandInterceptors() {
        return redisCommandInterceptors;
    }

    public RedisTemplate<?, ?> getRedisTemplate(String redisTemplateBeanName) {
        return getRedisTemplate(context, redisTemplateBeanName);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public static RedisContext get(BeanFactory beanFactory) {
        return beanFactory.getBean(RedisContext.class);
    }

    public static RedisTemplate<?, ?> getRedisTemplate(BeanFactory beanFactory, String redisTemplateBeanName) {
        if (beanFactory.containsBean(redisTemplateBeanName)) {
            return beanFactory.getBean(redisTemplateBeanName, REDIS_TEMPLATE_CLASS);
        }
        return null;
    }

    public static Set<String> findRestTemplateBeanNames(ConfigurableListableBeanFactory beanFactory) {
        List<String> redisTemplateBeanNames = asList(getBeanNames(beanFactory, RedisTemplate.class));
        logger.debug("The all bean names of RedisTemplate : {}", redisTemplateBeanNames);
        return unmodifiableSet(new HashSet<>(redisTemplateBeanNames));
    }

    public Set<String> findRedisConnectionFactoryBeanNames(ConfigurableListableBeanFactory beanFactory) {
        List<String> redisConnectionFactoryBeanNames = asList(getBeanNames(beanFactory, RedisConnectionFactory.class));
        logger.debug("The all bean names of RedisConnectionFactory : {}", redisConnectionFactoryBeanNames);
        return unmodifiableSet(new HashSet<>(redisConnectionFactoryBeanNames));
    }


    public static List<RedisCommandInterceptor> findRedisCommandInterceptors(ListableBeanFactory beanFactory) {
        return getSortedBeans(beanFactory, RedisCommandInterceptor.class);
    }

    public static List<RedisConnectionInterceptor> findRedisConnectionInterceptors(ListableBeanFactory beanFactory) {
        return getSortedBeans(beanFactory, RedisConnectionInterceptor.class);
    }
}
