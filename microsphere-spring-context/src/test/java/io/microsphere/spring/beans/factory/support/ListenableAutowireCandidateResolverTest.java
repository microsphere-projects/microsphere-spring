package io.microsphere.spring.beans.factory.support;


import io.microsphere.spring.util.TestBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extensions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * {@link ListenableAutowireCandidateResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ListenableAutowireCandidateResolver
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@TestPropertySource(
        properties = {
                "microsphere.spring.listenable-autowire-candidate-resolver.enabled=true",
                "test.name=test_value"
        })
@ContextConfiguration(
        classes = {
                TestBean.class,
                ListenableAutowireCandidateResolverTest.class
        },
        initializers = {
                ListenableAutowireCandidateResolverInitializer.class
        }
)
public class ListenableAutowireCandidateResolverTest implements AutowireCandidateResolvingListener, EnvironmentAware {

    @Value("${test.name}")
    private String testName;

    @Autowired
    @Lazy
    private TestBean testBean;

    private Environment environment;

    private static Object resolvedTestName;

    @Test
    public void test() {
        assertEquals(testName, resolvedTestName);
        assertNotNull(testBean.getResolver());
    }

    @Override
    public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
        if (descriptor.getAnnotation(Value.class) != null && suggestedValue instanceof String rawValue) {
            if ("testName".equals(descriptor.getField().getName())) {
                resolvedTestName = environment.resolvePlaceholders(rawValue);
            }
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }
}