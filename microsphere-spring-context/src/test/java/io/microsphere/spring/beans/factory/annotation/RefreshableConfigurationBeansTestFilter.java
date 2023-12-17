package io.microsphere.spring.beans.factory.annotation;

import io.microsphere.spring.context.event.ConfigurationBeanRefreshedEvent;
import io.microsphere.spring.util.User;
import org.junit.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.EnableMBeanExport;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

@EnableConfigurationBeanBinding(prefix = "usr", type = User.class, refreshStrategy = EnableConfigurationBeanBinding.ConfigurationBeanRefreshStrategy.REINITIALIZE)
@EnableConfigurationBeanBinding(prefix = "users", type = User.class, multiple = true, ignoreUnknownFields = false,
        ignoreInvalidFields = false)
@EnableMBeanExport
public class RefreshableConfigurationBeansTestFilter extends AbstractEnableConfigurationBeanBindingTest implements ApplicationListener<ConfigurationBeanRefreshedEvent> {

    private final MapPropertySource override = new MapPropertySource("override", new HashMap<>());

    protected void overrideConfigurations(String key, String value) {
        MutablePropertySources propertySources = context.getEnvironment().getPropertySources();
        MapPropertySource propertySource = (MapPropertySource) propertySources.get("modify");
        if (propertySource == null) {
            propertySources.addFirst(override);
        }
        override.getSource().put(key, value);

    }

    @Test
    public void testRefresh() {
        User user = context.getBean("m", User.class);
        assertEquals("mercyblitz", user.getName());
        assertEquals(34, user.getAge());

        RefreshableConfigurationBeans refreshableConfigurationBeans = context.getBean(RefreshableConfigurationBeans.class);

        overrideConfigurations("usr.age", "26");
        refreshableConfigurationBeans.refreshByName("m", true);
        assertEquals(26, user.getAge());

        overrideConfigurations("users.a.name", "override-user-a");
        refreshableConfigurationBeans.refreshByProperty("users.a.name", true);
    }

    @Override
    public void onApplicationEvent(ConfigurationBeanRefreshedEvent event) {
        System.out.println("received ConfigurationBeanRefreshedEvent for bean :" + event.getBeanName());
    }
}