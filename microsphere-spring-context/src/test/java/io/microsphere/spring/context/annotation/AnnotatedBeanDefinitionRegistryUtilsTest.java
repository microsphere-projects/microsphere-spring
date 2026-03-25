package io.microsphere.spring.context.annotation;

import io.microsphere.logging.test.junit4.LoggingLevelsRule;
import io.microsphere.spring.test.domain.User;
import io.microsphere.spring.test.web.controller.TestController;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;

import java.util.Set;

import static io.microsphere.logging.test.junit4.LoggingLevelsRule.levels;
import static io.microsphere.spring.context.annotation.AnnotatedBeanDefinitionRegistryUtils.findBeanDefinitionHolders;
import static io.microsphere.spring.context.annotation.AnnotatedBeanDefinitionRegistryUtils.isPresentBean;
import static io.microsphere.spring.context.annotation.AnnotatedBeanDefinitionRegistryUtils.registerBeans;
import static io.microsphere.spring.context.annotation.AnnotatedBeanDefinitionRegistryUtils.resolveAnnotatedBeanNameGenerator;
import static io.microsphere.spring.context.annotation.AnnotatedBeanDefinitionRegistryUtils.scanBasePackages;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.registerBeanDefinition;
import static org.springframework.context.annotation.AnnotationConfigUtils.registerAnnotationConfigProcessors;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * {@link AnnotatedBeanDefinitionRegistryUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotatedBeanDefinitionRegistryUtils
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class AnnotatedBeanDefinitionRegistryUtilsTest {

    @ClassRule
    public static final LoggingLevelsRule LOGGING_LEVELS_RULE = levels("TRACE", "INFO", "ERROR");


    private DefaultListableBeanFactory beanFactory = null;

    @Before
    public void setUp() {
        this.beanFactory = new DefaultListableBeanFactory();
        this.beanFactory.setAllowBeanDefinitionOverriding(false);
        registerAnnotationConfigProcessors(this.beanFactory);
    }

    @Test
    public void testRegisterBeans() {

        for (int i = 0; i < 100; i++) {
            registerBeans(this.beanFactory, this.getClass());
        }

        String[] beanNames = this.beanFactory.getBeanNamesForType(this.getClass());

        assertEquals(1, beanNames.length);

        beanNames = this.beanFactory.getBeanNamesForType(AnnotatedBeanDefinitionRegistryUtils.class);

        assertTrue(isEmpty(beanNames));

        registerBeans(this.beanFactory);

    }

    @Test
    public void testScanBasePackages() {
        String packageName = TestController.class.getPackage().getName();

        int count = scanBasePackages(this.beanFactory, packageName);
        assertEquals(1, count);

        String[] beanNames = this.beanFactory.getBeanNamesForType(TestController.class);
        assertEquals(1, beanNames.length);

        count = scanBasePackages(this.beanFactory);
        assertEquals(0, count);
    }

    @Test
    public void testResolveAnnotatedBeanNameGenerator() {
        testInSpringContainer(context -> {
            AnnotationConfigApplicationContext ctx = (AnnotationConfigApplicationContext) context;
            AnnotationBeanNameGenerator annotationBeanNameGenerator = new AnnotationBeanNameGenerator();
            ctx.setBeanNameGenerator(annotationBeanNameGenerator);
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
            BeanNameGenerator beanNameGenerator = resolveAnnotatedBeanNameGenerator(beanFactory);
            assertSame(annotationBeanNameGenerator, beanNameGenerator);
        });
    }

    @Test
    public void testIsPresentBean() {
        assertFalse(isPresentBean(this.beanFactory, TestController.class));
    }

    @Test
    public void testResolveAnnotatedBeanNameGeneratorWithNull() {
        BeanNameGenerator beanNameGenerator = resolveAnnotatedBeanNameGenerator(null);
        assertNotNull(beanNameGenerator);

        assertEquals(beanNameGenerator.getClass(), resolveAnnotatedBeanNameGenerator(this.beanFactory).getClass());
    }

    @Test
    public void testFindBeanDefinitionHolders() {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(this.beanFactory);
        String packageName = TestController.class.getPackage().getName();
        Set<BeanDefinitionHolder> beanDefinitionHolders = findBeanDefinitionHolders(scanner, packageName);
        assertEquals(1, beanDefinitionHolders.size());
        assertFalse(isPresentBean(this.beanFactory, TestController.class));

        beanDefinitionHolders.forEach(h -> registerBeanDefinition(h, this.beanFactory));
        assertTrue(isPresentBean(this.beanFactory, TestController.class));
        assertFalse(isPresentBean(this.beanFactory, User.class));
    }
}
