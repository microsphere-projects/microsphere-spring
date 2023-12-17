package io.microsphere.spring.context.event;

import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * @author <a href="mailto:maimengzzz@gmail.com">韩超</a>
 * @since 1.0.0
 */
public class ConfigurationBeanRefreshedEvent extends ApplicationEvent {

    private final String beanName;
    private final Object configurationBean;
    private final Map<String, Object> previousConfigurationProperties;

    public ConfigurationBeanRefreshedEvent(String beanName, Object configurationBean, Map<String, Object> previousConfigurationProperties) {
        super(configurationBean);
        this.beanName = beanName;
        this.configurationBean = configurationBean;
        this.previousConfigurationProperties = previousConfigurationProperties;
    }

    public String getBeanName() {
        return beanName;
    }

    public Object getConfigurationBean() {
        return configurationBean;
    }

    public Map<String, Object> getPreviousConfigurationProperties() {
        return previousConfigurationProperties;
    }
}
