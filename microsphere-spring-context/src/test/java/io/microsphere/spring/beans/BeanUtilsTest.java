package io.microsphere.spring.beans;

import io.microsphere.spring.beans.BeanUtils.NamingBean;
import io.microsphere.spring.beans.test.TestBean;
import io.microsphere.spring.beans.test.TestBean2;
import io.microsphere.spring.test.domain.User;
import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static io.microsphere.spring.beans.BeanUtils.findPrimaryConstructor;
import static io.microsphere.spring.beans.BeanUtils.getBeanIfAvailable;
import static io.microsphere.spring.beans.BeanUtils.getBeanNames;
import static io.microsphere.spring.beans.BeanUtils.getOptionalBean;
import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;
import static io.microsphere.spring.beans.BeanUtils.invokeAwareInterfaces;
import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static io.microsphere.spring.beans.BeanUtils.resolveBeanType;
import static io.microsphere.spring.beans.BeanUtils.sort;
import static io.microsphere.spring.context.annotation.AnnotatedBeanDefinitionRegistryUtils.registerBeans;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.springframework.util.ClassUtils.isAssignable;

/**
 * {@link BeanUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanUtils
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class BeanUtilsTest {


    @Configuration
    public static class Config {

        @Bean(name = "testString")
        public String testString(Environment environment) {
            return "test";
        }

    }

    @Test
    public void testIsBeanPresent() {

        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();

        assertFalse(isBeanPresent(registry, TestBean.class.getName(), true));
        assertFalse(isBeanPresent(registry, TestBean.class.getName()));

        registerBeans(registry, TestBean.class, TestBean2.class);


        assertTrue(isBeanPresent(registry, TestBean.class.getName(), true));
        assertTrue(isBeanPresent(registry, TestBean.class.getName()));

        assertTrue(isBeanPresent(registry, TestBean.class, true));
        assertTrue(isBeanPresent(registry, TestBean.class));

        assertTrue(isBeanPresent(registry, TestBean2.class.getName(), true));
        assertTrue(isBeanPresent(registry, TestBean2.class.getName()));

        assertTrue(isBeanPresent(registry, TestBean2.class, true));
        assertTrue(isBeanPresent(registry, TestBean2.class));

        assertFalse(isBeanPresent(registry, BeanUtils.class.getName(), true));
        assertFalse(isBeanPresent(registry, BeanUtils.class.getName()));

        assertFalse(isBeanPresent(registry, BeanUtils.class, true));
        assertFalse(isBeanPresent(registry, BeanUtils.class));

        assertTrue(isBeanPresent(registry, "testBean", TestBean.class));
        assertTrue(isBeanPresent(registry, "testBean2", TestBean2.class));
        assertFalse(isBeanPresent(registry, "beanUtils", BeanUtils.class));
    }

    @Test
    public void testGetBeanNamesOnAnnotationBean() {

        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(Config.class);

        String[] beanNames = getBeanNames(applicationContext, String.class);

        assertTrue(asList(beanNames).contains("testString"));

        applicationContext.close();
    }

    @Test
    public void testGetBeanNamesOnXmlBean() {

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring-context.xml");

        String[] beanNames = getBeanNames(context, User.class);

        assertTrue(asList(beanNames).contains("user"));

        context.close();

    }

    @Test
    public void testGetBeanNames() {

        DefaultListableBeanFactory parentBeanFactory = new DefaultListableBeanFactory();

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        beanFactory.setParentBeanFactory(parentBeanFactory);

        registerBeans(parentBeanFactory, TestBean.class);

        registerBeans(beanFactory, TestBean2.class);

        ListableBeanFactory listableBeanFactory = parentBeanFactory;

        String[] beanNames = getBeanNames(listableBeanFactory, TestBean.class);

        assertEquals(1, beanNames.length);

        String beanName = beanNames[0];

        assertEquals("testBean", beanName);

        beanNames = getBeanNames(listableBeanFactory, TestBean.class, true);

        assertEquals(1, beanNames.length);

        beanName = beanNames[0];

        assertEquals("testBean", beanName);

        listableBeanFactory = beanFactory;

        beanNames = getBeanNames(listableBeanFactory, TestBean.class);

        assertEquals(0, beanNames.length);

        beanNames = getBeanNames(listableBeanFactory, TestBean.class, true);

        assertEquals(1, beanNames.length);

        beanName = beanNames[0];

        assertEquals("testBean", beanName);

        beanNames = getBeanNames(listableBeanFactory, TestBean2.class, true);

        assertEquals(1, beanNames.length);

        beanName = beanNames[0];

        assertEquals("testBean2", beanName);

        beanNames = getBeanNames(listableBeanFactory, io.microsphere.spring.beans.test.Bean.class, true);

        assertEquals(2, beanNames.length);

        beanName = beanNames[0];

        assertEquals("testBean2", beanName);

        beanName = beanNames[1];

        assertEquals("testBean", beanName);

        beanNames = getBeanNames(beanFactory, io.microsphere.spring.beans.test.Bean.class, true);

        assertEquals(2, beanNames.length);

        beanName = beanNames[0];

        assertEquals("testBean2", beanName);

        beanName = beanNames[1];

        assertEquals("testBean", beanName);

        beanNames = getBeanNames(beanFactory, io.microsphere.spring.beans.test.Bean.class);

        assertEquals(1, beanNames.length);

        beanName = beanNames[0];

        assertEquals("testBean2", beanName);
    }

    @Test
    public void testResolveBeanType() {
        ClassLoader classLoader = getDefaultClassLoader();

        Class<?> beanType = resolveBeanType(this.getClass().getName(), classLoader);
        assertEquals(beanType, this.getClass());

        beanType = resolveBeanType("", classLoader);
        assertNull(beanType);

        beanType = resolveBeanType("     ", classLoader);
        assertNull(beanType);

        beanType = resolveBeanType("java.lang.Abc", classLoader);
        assertNull(beanType);
    }


    @Test
    public void testGetOptionalBean() {

        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();

        TestBean testBean = getOptionalBean(registry, TestBean.class, true);

        assertNull(testBean);

        testBean = getOptionalBean(registry, TestBean.class);

        assertNull(testBean);

        registerBeans(registry, TestBean.class);

        testBean = getOptionalBean(registry, TestBean.class);

        assertNotNull(testBean);

    }

    @Test
    public void testGetOptionalBeanExcludingAncestors() {

        DefaultListableBeanFactory registry = new DefaultListableBeanFactory();

        registerBeans(registry, TestBean.class, TestBean2.class);

        List<io.microsphere.spring.beans.test.Bean> beans = getSortedBeans(registry, io.microsphere.spring.beans.test.Bean.class);

        assertEquals(2, beans.size());

        TestBean testBean = getOptionalBean(registry, TestBean.class);

        assertEquals(testBean, beans.get(0));

        TestBean2 testBean2 = getOptionalBean(registry, TestBean2.class);

        assertEquals(testBean2, beans.get(1));

    }

    @Test
    public void testGetBeanIfAvailable() {

        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();

        registerBeans(beanFactory, TestBean.class, TestBean2.class);

        assertTrue(isAssignable(TestBean.class, getBeanIfAvailable(beanFactory, "testBean", TestBean.class).getClass()));
        assertTrue(isAssignable(TestBean2.class, getBeanIfAvailable(beanFactory, "testBean2", TestBean2.class).getClass()));

    }

    @Test
    public void testSort() {

        int times = 9;

        Map<String, OrderedBean> orderedBeansMap = new LinkedHashMap<String, OrderedBean>(times);

        for (int i = times; i > 0; i--) {
            OrderedBean orderedBean = new OrderedBean(i);
            orderedBeansMap.put(orderedBean.toString(), orderedBean);
        }

        Map<String, OrderedBean> expectedBeansMap = new LinkedHashMap<String, OrderedBean>(times);

        for (int i = 1; i <= times; i++) {
            OrderedBean orderedBean = new OrderedBean(i);
            expectedBeansMap.put(orderedBean.toString(), orderedBean);
        }

        Map<String, OrderedBean> sortedBeansMap = sort(orderedBeansMap);

        assertArrayEquals(expectedBeansMap.values().toArray(), sortedBeansMap.values().toArray());

    }

    @Test
    public void testInvokeAwareInterfaces() {
        TestBean testBean = new TestBean();
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.refresh();
        BeanFactory beanFactory = context;
        invokeAwareInterfaces(testBean, beanFactory);
        assertSame(testBean.getMessageSource(), context);
        assertSame(testBean.getApplicationContext(), context);
        assertSame(testBean.getApplicationEventPublisher(), context);
        assertSame(testBean.getBeanFactory(), context.getBeanFactory());
        assertSame(testBean.getClassLoader(), context.getClassLoader());
        assertSame(testBean.getEnvironment(), context.getEnvironment());
        assertSame(testBean.getResourceLoader(), context);
        assertNotNull(testBean.getResolver());
        context.close();
    }


    private static class OrderedBean implements Ordered {

        private final int order;

        private OrderedBean(int order) {
            this.order = order;
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        public String toString() {
            return "Bean #" + order;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            OrderedBean that = (OrderedBean) o;

            return order == that.order;
        }

        @Override
        public int hashCode() {
            return order;
        }
    }

    @Test
    public void testNamingBean() {

        NamingBean namingBean = new NamingBean("testBean", new TestBean());

        NamingBean namingBean2 = new NamingBean("testBean2", new TestBean2());

        List<NamingBean> namingBeans = asList(namingBean, namingBean2);

        AnnotationAwareOrderComparator.sort(namingBeans);

        assertEquals(1, namingBean.getOrder());
        assertEquals(2, namingBean2.getOrder());

        assertEquals(-1, namingBean.compareTo(namingBean2));

    }

    @Test
    public void testFindPrimaryConstructor() {
        Constructor<BeanUtilsTest> constructor = findPrimaryConstructor(BeanUtilsTest.class);
        assertNull(constructor);
    }

}
