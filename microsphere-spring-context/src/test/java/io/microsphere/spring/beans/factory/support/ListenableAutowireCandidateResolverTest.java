package io.microsphere.spring.beans.factory.support;


import io.microsphere.spring.beans.test.TestBean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.reflect.Field;

import static io.microsphere.reflect.FieldUtils.findField;
import static io.microsphere.spring.beans.factory.support.ListenableAutowireCandidateResolver.ENABLED_PROPERTY_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
class ListenableAutowireCandidateResolverTest implements AutowireCandidateResolvingListener, EnvironmentAware {

    @Value("${test.name}")
    private String testName;

    @Autowired
    @Qualifier("testBean")
    @Lazy
    private TestBean testBean;

    @Autowired
    private ObjectProvider<ListenableAutowireCandidateResolver> resolverProvider;

    private ConfigurableEnvironment environment;

    @Autowired
    private DefaultListableBeanFactory beanFactory;

    private static Object resolvedTestName;

    @Test
    void test() {
        assertEquals(testName, resolvedTestName);
        assertNotNull(testBean.getResolver());

        ListenableAutowireCandidateResolver resolver = resolverProvider.getIfAvailable();
        assertNotNull(resolver.cloneIfNecessary());
    }

    @Test
    void testAddListeners() {
        ListenableAutowireCandidateResolver resolver = resolverProvider.getIfAvailable();
        resolver.addListener(new LoggingAutowireCandidateResolvingListener());
    }

    @Test
    void testIsRequired() {
        testIsRequired(true);
        testIsRequired(false);
    }

    void testIsRequired(boolean required) {
        ListenableAutowireCandidateResolver resolver = resolverProvider.getIfAvailable();
        Field field = findField(this.getClass(), "testName");
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(field, required);
        assertEquals(required, resolver.isRequired(dependencyDescriptor));
    }

    @Test
    void testHasQualifier() {
        ListenableAutowireCandidateResolver resolver = resolverProvider.getIfAvailable();
        Field field = findField(this.getClass(), "testBean");
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(field, true);
        assertTrue(resolver.hasQualifier(dependencyDescriptor));

        field = findField(this.getClass(), "testName");
        dependencyDescriptor = new DependencyDescriptor(field, true);
        assertFalse(resolver.hasQualifier(dependencyDescriptor));
    }

    @Test
    void testHasQualifierOnNull() {
        ListenableAutowireCandidateResolver resolver = resolverProvider.getIfAvailable();
        assertThrows(NullPointerException.class, () -> assertFalse(resolver.hasQualifier(null)));
    }

    @Test
    void testWrap() {
        // wrap(BeanFactory) method was invoked on BeanFactoryPostProcessor lifecycle
        ListenableAutowireCandidateResolver resolver = resolverProvider.getIfAvailable();
        AutowireCandidateResolver autowireCandidateResolver = beanFactory.getAutowireCandidateResolver();
        assertSame(autowireCandidateResolver, resolver);

        // rewrap , however it's not necessary
        resolver.wrap(beanFactory);
        autowireCandidateResolver = beanFactory.getAutowireCandidateResolver();
        assertSame(autowireCandidateResolver, resolver);
    }

    @Test
    void testWrapOnDisabled() {
        MutablePropertySources mutablePropertySources = this.environment.getPropertySources();
        MockPropertySource mockPropertySource = new MockPropertySource();
        mutablePropertySources.addFirst(mockPropertySource);

        // wrap(BeanFactory) method was invoked on BeanFactoryPostProcessor lifecycle
        ListenableAutowireCandidateResolver resolver = resolverProvider.getIfAvailable();
        AutowireCandidateResolver autowireCandidateResolver = beanFactory.getAutowireCandidateResolver();
        assertSame(autowireCandidateResolver, resolver);

        // set "microsphere.spring.listenable-autowire-candidate-resolver.enabled" to be "false"
        mockPropertySource.withProperty(ENABLED_PROPERTY_NAME, "false");

        // rewrap , obviously it does not work
        resolver.wrap(beanFactory);
        autowireCandidateResolver = beanFactory.getAutowireCandidateResolver();
        assertSame(autowireCandidateResolver, resolver);
    }

    @Override
    public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
        if (descriptor.getAnnotation(Value.class) != null && suggestedValue instanceof String) {
            if ("testName".equals(descriptor.getField().getName())) {
                resolvedTestName = environment.resolvePlaceholders((String) suggestedValue);
            }
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }
}