package io.microsphere.spring.context.annotation;

import io.microsphere.spring.test.TestBean;
import io.microsphere.spring.test.TestBean2;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static io.microsphere.spring.context.annotation.AnnotatedBeanDefinitionRegistryUtils.registerBeans;
import static io.microsphere.spring.context.annotation.AnnotatedBeanDefinitionRegistryUtils.scanBasePackages;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.context.annotation.AnnotationConfigUtils.registerAnnotationConfigProcessors;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * {@link AnnotatedBeanDefinitionRegistryUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotatedBeanDefinitionRegistryUtils
 * @since 2017.01.13
 */
public class AnnotatedBeanDefinitionRegistryUtilsTest {

    private DefaultListableBeanFactory registry = null;

    @Before
    public void init() {
        registry = new DefaultListableBeanFactory();
        registry.setAllowBeanDefinitionOverriding(false);
        registerAnnotationConfigProcessors(registry);
    }

    @Test
    public void testRegisterBeans() {

        for (int i = 0; i < 100; i++) {
            registerBeans(registry, this.getClass());
        }

        String[] beanNames = registry.getBeanNamesForType(this.getClass());

        assertEquals(1, beanNames.length);

        beanNames = registry.getBeanNamesForType(AnnotatedBeanDefinitionRegistryUtils.class);

        assertTrue(isEmpty(beanNames));

        registerBeans(registry);

    }

    @Test
    public void testScanBasePackages() {

        int count = scanBasePackages(registry, TestBean.class.getPackage().getName());

        assertEquals(2, count);

        String[] beanNames = registry.getBeanNamesForType(TestBean.class);

        assertEquals(1, beanNames.length);

        beanNames = registry.getBeanNamesForType(TestBean2.class);

        assertEquals(1, beanNames.length);

        count = scanBasePackages(registry);

        assertEquals(0, count);
    }

}
