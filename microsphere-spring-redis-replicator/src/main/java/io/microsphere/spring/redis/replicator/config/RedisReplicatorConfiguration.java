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
package io.microsphere.spring.redis.replicator.config;

import io.microsphere.spring.redis.config.RedisConfiguration;
import io.microsphere.spring.redis.context.RedisContext;
import io.microsphere.spring.redis.event.RedisConfigurationPropertyChangedEvent;
import io.microsphere.spring.redis.util.RedisConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.spring.redis.config.RedisConfiguration.getBoolean;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * Redis Replicator Configuration
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RedisConfiguration
 * @since 1.0.0
 */
public class RedisReplicatorConfiguration implements ApplicationListener<RedisConfigurationPropertyChangedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(RedisReplicatorConfiguration.class);

    public static final String BEAN_NAME = "redisReplicatorConfiguration";

    public static final String PROPERTY_NAME_PREFIX = RedisConstants.PROPERTY_NAME_PREFIX + "replicator.";

    public static final String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "enabled";

    public static final String CONSUMER_PROPERTY_NAME_PREFIX = PROPERTY_NAME_PREFIX + "consumer.";

    public static final String CONSUMER_ENABLED_PROPERTY_NAME = CONSUMER_PROPERTY_NAME_PREFIX + "enabled";

    public static final boolean DEFAULT_ENABLED = true;

    public static final boolean DEFAULT_CONSUMER_ENABLED = Boolean.getBoolean(CONSUMER_ENABLED_PROPERTY_NAME);

    /**
     * Business Domains
     */
    public static final String DOMAINS_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "domains";

    public static final String DEFAULT_DOMAIN = "default";

    public static final List<String> DEFAULT_DOMAINS = Arrays.asList(DEFAULT_DOMAIN);

    public static final String DOMAIN_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME_PREFIX = DOMAINS_PROPERTY_NAME + ".";

    public static final String DOMAIN_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME_SUFFIX = ".redis-templates";

    private final ConfigurableApplicationContext context;

    private final ConfigurableEnvironment environment;

    private final boolean consumerEnabled;

    private volatile boolean enabled;

    private volatile List<String> domains;

    private volatile Map<String, List<String>> sourceBeanDomains;

    private RedisContext redisContext;

    public RedisReplicatorConfiguration(ConfigurableApplicationContext context) {
        this.context = context;
        this.environment = context.getEnvironment();
        this.consumerEnabled = isConsumerEnabled(context);
        setEnabled();
        setDomains();
    }

    private void setEnabled() {
        this.enabled = isEnabled(context);
    }

    private void setDomains() {
        List<String> domains = getDomains(environment);
        setSourceBeanDomains(domains);
        this.domains = domains;
    }

    @Override
    public void onApplicationEvent(RedisConfigurationPropertyChangedEvent event) {
        Set<String> keys = event.getPropertyNames();
        for (String key : keys) {
            if (DOMAINS_PROPERTY_NAME.equals(key)) {
                setDomains();
            } else if (key.startsWith(DOMAIN_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME_PREFIX)) {
                setDomains();
            }
            // TODO More configuration changes are supported
        }
    }

    private void setSourceBeanDomains(List<String> domains) {
        Map<String, List<String>> sourceBeanDomains = new HashMap<>(domains.size());
        for (String domain : domains) {
            for (String redisTemplateBeanName : getDomainRedisTemplateBeanNames(environment, domain)) {
                List<String> beanDomains = sourceBeanDomains.computeIfAbsent(redisTemplateBeanName, n -> new LinkedList<>());
                if (!beanDomains.contains(domain)) {
                    beanDomains.add(domain);
                } else {
                    logger.warn("RedisTemplate Bean[name :{}] is repeatedly associated with domain :{}, this configuration will be ignored!", redisTemplateBeanName, domain);
                }
            }
        }
        this.sourceBeanDomains = unmodifiableMap(sourceBeanDomains);
    }

    /**
     * Get the configured service domain list
     *
     * @param environment {@link Environment}
     * @return If the configuration is not found, return {@link #DEFAULT_DOMAINS 默认域}
     */
    @NonNull
    public List<String> getDomains(Environment environment) {
        return unmodifiableList(environment.getProperty(DOMAINS_PROPERTY_NAME, List.class, DEFAULT_DOMAINS));
    }

    /**
     * Get the RedisTemplate Bean name list and configure the attribute name pattern according to the specified Domain:
     * microsphere.redis.replicator.domains.{domain}.redis-templates
     * <p>
     * Take the "default" service domain as an example:
     * microsphere.redis.replicator.domains.default.redis-templates = redisTemplate,stringRedisTemplate
     *
     * @param environment {@link Environment}
     * @param domain      The specified Domain is derived from{@link #getDomains(Environment)}
     * @return If no configuration is found, an empty list is returned
     */
    @NonNull
    public List<String> getDomainRedisTemplateBeanNames(Environment environment, String domain) {
        String propertyName = getDomainRedisTemplateBeanNamesPropertyName(domain);
        return unmodifiableList(environment.getProperty(propertyName, List.class, emptyList()));
    }

    protected String getDomainRedisTemplateBeanNamesPropertyName(String domain) {
        return DOMAIN_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME_PREFIX + domain + DOMAIN_REDIS_TEMPLATE_BEAN_NAMES_PROPERTY_NAME_SUFFIX;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isConsumerEnabled() {
        return consumerEnabled;
    }

    public List<String> getDomains() {
        return domains;
    }

    public List<String> getDomains(String sourceBeanName) {
        return sourceBeanDomains.getOrDefault(sourceBeanName, getDomains());
    }

    @NonNull
    public RedisContext getRedisContext() {
        RedisContext redisContext = this.redisContext;
        if (redisContext == null) {
            BeanFactory beanFactory = context.getBeanFactory();
            logger.debug("RedisContext is not initialized, it will be got from BeanFactory[{}]", beanFactory);
            redisContext = RedisContext.get(beanFactory);
            this.redisContext = redisContext;
        }
        return redisContext;
    }

    @NonNull
    public RedisConfiguration getRedisConfiguration() {
        return getRedisContext().getRedisConfiguration();
    }

    public static boolean isEnabled(ApplicationContext context) {
        return getBoolean(context, ENABLED_PROPERTY_NAME, DEFAULT_ENABLED, "Replicator", "enabled");
    }

    public static boolean isConsumerEnabled(ApplicationContext context) {
        return getBoolean(context, CONSUMER_ENABLED_PROPERTY_NAME, DEFAULT_CONSUMER_ENABLED, "Replicator Consumer", "enabled");
    }

    public static RedisReplicatorConfiguration get(BeanFactory beanFactory) {
        return beanFactory.getBean(BEAN_NAME, RedisReplicatorConfiguration.class);
    }
}
