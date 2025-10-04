package io.microsphere.spring.core.annotation;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.type.StandardAnnotationMetadata;
import org.springframework.mock.env.MockEnvironment;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static io.microsphere.spring.core.annotation.AnnotationUtils.findAnnotationType;
import static io.microsphere.spring.core.annotation.AnnotationUtils.findAnnotations;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAttribute;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAttributes;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getRequiredAttribute;
import static io.microsphere.spring.core.annotation.AnnotationUtils.isPresent;
import static io.microsphere.spring.core.annotation.AnnotationUtils.tryGetMergedAnnotation;
import static io.microsphere.spring.core.annotation.AnnotationUtils.tryGetMergedAnnotationAttributes;
import static io.microsphere.spring.util.SpringVersionUtils.SPRING_CONTEXT_VERSION;
import static io.microsphere.util.AnnotationUtils.getAttributesMap;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.of;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.springframework.beans.factory.annotation.Autowire.NO;
import static org.springframework.beans.factory.support.AbstractBeanDefinition.INFER_METHOD;
import static org.springframework.util.ReflectionUtils.findMethod;

/**
 * {@link AnnotationUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotationUtils
 * @since 1.0.0
 */
public class AnnotationUtilsTest {

    private static final String dummyBeanName = "dummy-bean";

    private static final String[] testNameAttribute = ofArray(dummyBeanName);

    private final Map<String, Object> defaultAttributeValuesOfBean = getAttributesMap(getAnnotation("dummyBean2", Bean.class));

    @Bean(name = dummyBeanName)
    public String dummyBean() {
        return "Dummy Bean";
    }

    @Bean
    public String dummyBean2() {
        return "Dummy Bean 2";
    }

    @Bean(name = "${beanName}")
    public String dummyBean3() {
        return "Dummy Bean 3";
    }

    private Bean annotation;

    private MockEnvironment environment;

    @Before
    public void setUp() {
        this.annotation = getAnnotation("dummyBean", Bean.class);
        this.environment = new MockEnvironment();
    }

    @Test
    public void testIsPresent() {

        Method method = findMethod(RuntimeAnnotationHandler.class, "handle", String.class, String.class);

        assertTrue(isPresent(method, RuntimeAnnotation.class));

        method = findMethod(RuntimeAnnotationHandler.class, "handle", String.class);

        assertTrue(isPresent(method, RuntimeAnnotation.class));

        method = findMethod(RuntimeAnnotationHandler.class, "handle");

        assertTrue(isPresent(method, RuntimeAnnotation.class));

        method = findMethod(ClassAnnotationHandler.class, "echo", String.class);

        assertFalse(isPresent(method, ClassAnnotation.class));
    }

    @Test
    public void testFindAnnotations() {

        Method method = findMethod(RuntimeAnnotationHandler.class, "handle", String.class, String.class);

        Map<ElementType, List<RuntimeAnnotation>> annotationsMap = findAnnotations(method, RuntimeAnnotation.class);

        assertEquals(3, annotationsMap.size());

        List<RuntimeAnnotation> annotationsList = annotationsMap.get(TYPE);

        assertEquals(1, annotationsList.size());

        RuntimeAnnotation runtimeAnnotation = annotationsList.get(0);

        assertEquals("type", runtimeAnnotation.value());

        annotationsList = annotationsMap.get(METHOD);

        assertEquals(1, annotationsList.size());

        runtimeAnnotation = annotationsList.get(0);

        assertEquals("method", runtimeAnnotation.value());

        annotationsList = annotationsMap.get(PARAMETER);

        assertEquals(2, annotationsList.size());

        runtimeAnnotation = annotationsList.get(0);

        assertEquals("parameter1", runtimeAnnotation.value());

        runtimeAnnotation = annotationsList.get(1);

        assertEquals("parameter2", runtimeAnnotation.value());


        annotationsList = annotationsMap.get(ElementType.PACKAGE);

        assertNull(annotationsList);


        method = findMethod(ClassAnnotationHandler.class, "handle", String.class);

        annotationsMap = findAnnotations(method, RuntimeAnnotation.class);

        assertTrue(annotationsMap.isEmpty());

        Map<ElementType, List<ClassAnnotation>> classAnnotationsMap = findAnnotations(method, ClassAnnotation.class);

        assertTrue(classAnnotationsMap.isEmpty());
    }

    @Test
    public void testFindAnnotationsOnMismatchAnnotation() {
        Method method = findMethod(RuntimeAnnotationHandler.class, "handle", String.class, String.class);
        Map<ElementType, List<Autowired>> annotationsMap = findAnnotations(method, Autowired.class);
        assertTrue(annotationsMap.isEmpty());
    }

    /**
     * Test {@link AnnotationUtils#getAttributes(Annotation)}
     */
    @Test
    public void testGetAttributesWithAnnotation() {
        Map<String, Object> attributes = getAttributes(annotation);
        assertAttributes(attributes, annotation);
        if (SPRING_CONTEXT_VERSION.getMajor() < 6) {
            assertEquals(NO, attributes.get("autowire"));
        }
        assertEquals("", attributes.get("initMethod"));
        assertEquals(INFER_METHOD, attributes.get("destroyMethod"));
    }

    /**
     * Test {@link AnnotationUtils#getAttributes(Annotation, boolean)}
     */
    @Test
    public void testGetAttributesWithAnnotationAndIgnoreDefaultValue() {
        Map<String, Object> attributes = getAttributes(annotation, true);
        AnnotationAttributes annotationAttributes = assertAttributes(attributes, annotation);
        assertArrayEquals(testNameAttribute, annotationAttributes.getStringArray("value"));

        annotation = getAnnotation("dummyBean2", Bean.class);
        attributes = getAttributes(annotation, true);
        assertTrue(attributes.isEmpty());
    }

    /**
     * Test {@link AnnotationUtils#getAttributes(Annotation, boolean, String...)}
     */
    @Test
    public void testGetAttributesWithAnnotationAndIgnoreDefaultValueAndIgnoreAttributeNames() {
        Map<String, Object> attributes = getAttributes(annotation, true, "not-found-name");
        assertAttributes(attributes, annotation);
        assertArrayEquals(testNameAttribute, (String[]) attributes.get("value"));

        getAttributes(annotation, true, "value");
        assertAttributes(attributes, annotation);
        assertArrayEquals(testNameAttribute, (String[]) attributes.get("value"));
    }

    /**
     * Test {@link AnnotationUtils#getAttributes(Annotation, PropertyResolver, boolean, String...)}
     */
    @Test
    public void testGetAttributesWithAnnotationAndPropertyResolverAndIgnoreDefaultValueAndIgnoreAttributeNames() {
        Map<String, Object> attributes = getAttributes(annotation, this.environment, true);
        assertAttributes(attributes, annotation);
        assertArrayEquals(testNameAttribute, (String[]) attributes.get("value"));

        annotation = getAnnotation("dummyBean3", Bean.class);
        attributes = getAttributes(annotation, this.environment, true);
        assertArrayEquals(ofArray("${beanName}"), (String[]) attributes.get("value"));

        this.environment.setProperty("beanName", dummyBeanName);
        attributes = getAttributes(annotation, this.environment, true);
        assertAttributes(attributes, annotation);
        assertArrayEquals(testNameAttribute, (String[]) attributes.get("value"));
    }

    /**
     * Test {@link AnnotationUtils#getAttributes(Annotation, PropertyResolver, boolean, boolean, boolean, String...)}
     */
    @Test
    public void testGetAttributes() {
        Map<String, Object> attributes = getAttributes(annotation, this.environment, true, true, true, EMPTY_STRING_ARRAY);
        assertAttributes(attributes, annotation);
        assertArrayEquals(testNameAttribute, (String[]) attributes.get("value"));

        attributes = getAttributes(annotation, this.environment, false, true, true, EMPTY_STRING_ARRAY);
        assertAttributes(attributes, annotation);
        assertArrayEquals(testNameAttribute, (String[]) attributes.get("value"));

        attributes = getAttributes(annotation, this.environment, false, false, true, EMPTY_STRING_ARRAY);
        assertAttributes(attributes, annotation);
        assertArrayEquals(testNameAttribute, (String[]) attributes.get("value"));

        attributes = getAttributes(annotation, this.environment, false, false, false, EMPTY_STRING_ARRAY);
        assertAttributes(attributes, annotation);
        assertArrayEquals(testNameAttribute, (String[]) attributes.get("value"));
    }

    @Test
    public void testGetAttribute() {
        assertArrayEquals(of(testNameAttribute), getAttribute(annotation, "name"));

        annotation = getAnnotation("dummyBean2", Bean.class);
        assertArrayEquals(of(), getAttribute(annotation, "name"));

        annotation = getAnnotation("dummyBean3", Bean.class);
        assertArrayEquals(of("${beanName}"), getAttribute(annotation, "name"));
    }

    @Test
    public void testGetAttributeOnNotFound() {
        assertNull(getAttribute(annotation, "not-found-name"));
    }

    @Test
    public void testGetRequiredAttribute() {
        Map<String, Object> attributes = getAttributes(annotation);
        String[] name = getRequiredAttribute(attributes, "name");
        assertArrayEquals(testNameAttribute, name);
    }

    @Test
    public void testGetRequiredAttributeOnNotFound() {
        Map<String, Object> attributes = getAttributes(annotation);
        assertThrows(IllegalStateException.class, () -> getAttribute(attributes, "not-found-name", true));
    }

    @Test
    public void testGetAnnotationAttributes() {

        MockEnvironment environment = new MockEnvironment();

        Bean annotation = getAnnotation("dummyBean", Bean.class);

        // case 1 : PropertyResolver(null) , ignoreDefaultValue(true) , ignoreAttributeName(empty)
        AnnotationAttributes annotationAttributes = getAnnotationAttributes(annotation, true);
        assertArrayEquals(testNameAttribute, annotationAttributes.getStringArray("name"));

        // case 2 : PropertyResolver , ignoreDefaultValue(true) , ignoreAttributeName(empty)
        annotationAttributes = getAnnotationAttributes(annotation, environment, true);
        assertArrayEquals(testNameAttribute, annotationAttributes.getStringArray("name"));

        // case 3 : PropertyResolver , ignoreDefaultValue(true) , ignoreAttributeName(name)
        annotationAttributes = getAnnotationAttributes(annotation, environment, true, "name");

        // case 4 : PropertyResolver(null) , ignoreDefaultValue(false) , ignoreAttributeName(empty)
        annotationAttributes = getAnnotationAttributes(annotation, false);
        assertArrayEquals(testNameAttribute, annotationAttributes.getStringArray("name"));
        if (SPRING_CONTEXT_VERSION.getMajor() < 6) {
            assertEquals(NO, annotationAttributes.get("autowire"));
        }
        assertEquals("", annotationAttributes.getString("initMethod"));
        assertEquals(INFER_METHOD, annotationAttributes.getString("destroyMethod"));

        // case 5 : PropertyResolver , ignoreDefaultValue(false) , ignoreAttributeName(empty)
        annotationAttributes = getAnnotationAttributes(annotation, environment, false);
        assertArrayEquals(testNameAttribute, annotationAttributes.getStringArray("name"));
        if (SPRING_CONTEXT_VERSION.getMajor() < 6) {
            assertEquals(NO, annotationAttributes.get("autowire"));
        }
        assertEquals("", annotationAttributes.getString("initMethod"));
        assertEquals(INFER_METHOD, annotationAttributes.getString("destroyMethod"));

        // case 6 : PropertyResolver , ignoreDefaultValue(false) , ignoreAttributeName(name,autowire,initMethod)
        annotationAttributes = getAnnotationAttributes(annotation, environment, false, "name", "autowire", "initMethod");
        assertEquals(INFER_METHOD, annotationAttributes.getString("destroyMethod"));

        // getAnnotationAttributes(AnnotatedElement, java.lang.Class, PropertyResolver, boolean, String...)
        annotationAttributes = getAnnotationAttributes(getMethod("dummyBean"), Bean.class, environment, true);
        assertArrayEquals(testNameAttribute, annotationAttributes.getStringArray("name"));

        annotationAttributes = getAnnotationAttributes(getMethod("dummyBean"), Configuration.class, environment, true);
        assertNull(annotationAttributes);

        // getAnnotationAttributes(AnnotatedElement, java.lang.Class, PropertyResolver, boolean, boolean, String...)
        annotationAttributes = getAnnotationAttributes(getMethod("dummyBean"), Bean.class, environment, true, true);
        assertArrayEquals(testNameAttribute, annotationAttributes.getStringArray("name"));

        annotationAttributes = getAnnotationAttributes(getMethod("dummyBean"), Bean.class, environment, true, false);
        assertArrayEquals(testNameAttribute, annotationAttributes.getStringArray("name"));

        annotationAttributes = getAnnotationAttributes(getMethod("dummyBean"), Configuration.class, environment, true, true);
        assertNull(annotationAttributes);
    }

    @Test
    public void testGetAnnotationAttributesWithAnnotationMetadataAndAnnotationType() {
        StandardAnnotationMetadata annotationMetadata = new StandardAnnotationMetadata(RuntimeAnnotationHandler.class);
        AnnotationAttributes annotationAttributes = getAnnotationAttributes(annotationMetadata, RuntimeAnnotation.class);
        assertEquals("type", annotationAttributes.getString("value"));
    }

    @Test
    public void testGetAnnotationAttributesWithAnnotationMetadataAndAnnotationTypeOnNotFound() {
        StandardAnnotationMetadata annotationMetadata = new StandardAnnotationMetadata(RuntimeAnnotationHandler.class);
        AnnotationAttributes annotationAttributes = getAnnotationAttributes(annotationMetadata, Bean.class);
        assertNull(annotationAttributes);
    }

    @Test
    public void testGetAnnotationAttributesWithAnnotationMetadataAndAnnotationName() {
        StandardAnnotationMetadata annotationMetadata = new StandardAnnotationMetadata(RuntimeAnnotationHandler.class);
        AnnotationAttributes annotationAttributes = getAnnotationAttributes(annotationMetadata, RuntimeAnnotation.class.getName());
        assertEquals("type", annotationAttributes.getString("value"));
    }

    @Test
    public void testTryGetMergedAnnotation() {
        AnnotatedElement annotatedElement = getMethod("dummyBean");
        Annotation annotation = tryGetMergedAnnotation(annotatedElement, Bean.class);
        Bean bean = (Bean) annotation;
        assertEquals(Bean.class, annotation.annotationType());
        assertArrayEquals(testNameAttribute, bean.value());
        assertArrayEquals(testNameAttribute, bean.name());
        assertEquals(NO, bean.autowire());
        assertEquals("", bean.initMethod());
        assertEquals(INFER_METHOD, bean.destroyMethod());
    }

    @Test
    public void testTryGetMergedAnnotationAttributes() {
        AnnotatedElement annotatedElement = getMethod("dummyBean");
        AnnotationAttributes annotationAttributes = tryGetMergedAnnotationAttributes(annotatedElement, Bean.class, this.environment, false, EMPTY_STRING_ARRAY);
        assertArrayEquals(testNameAttribute, annotationAttributes.getStringArray("value"));
        assertArrayEquals(testNameAttribute, annotationAttributes.getStringArray("name"));
        assertEquals(NO, annotationAttributes.getEnum("autowire"));
        assertEquals("", annotationAttributes.getString("initMethod"));
        assertEquals(INFER_METHOD, annotationAttributes.getString("destroyMethod"));
    }

    @Test
    public void testFindAnnotationType() {
        AnnotationAttributes annotationAttributes = getAnnotationAttributes(annotation);
        Class<? extends Annotation> annotationType = findAnnotationType(annotationAttributes);
        assertEquals(Bean.class, annotationType);
    }

    @Test
    public void testFindAnnotationTypeWithNull() {
        assertNull(findAnnotationType(null));
    }

    private <A extends Annotation> A getAnnotation(String methodName, Class<A> annotationClass) {
        Method method = getMethod(methodName);
        return method.getAnnotation(annotationClass);
    }

    private AnnotationAttributes assertAttributes(Map<String, Object> attributes, Annotation annotation) {
        return assertAttributes(attributes, annotation.annotationType());
    }

    private AnnotationAttributes assertAttributes(Map<String, Object> attributes, Class<? extends Annotation> annotationType) {
        assertNotNull(attributes);
        assertTrue(attributes instanceof AnnotationAttributes);
        AnnotationAttributes annotationAttributes = (AnnotationAttributes) attributes;
        assertEquals(annotationType, annotationAttributes.annotationType());
        assertArrayEquals(testNameAttribute, annotationAttributes.getStringArray("name"));
        return annotationAttributes;
    }

    private Method getMethod(String methodName) {
        return findMethod(getClass(), methodName);
    }

    @RuntimeAnnotation("type")
    private static class RuntimeAnnotationHandler {

        @RuntimeAnnotation("method")
        public String handle() {
            return "";
        }

        @RuntimeAnnotation("method")
        public String handle(@RuntimeAnnotation("parameter") String message) {
            return message;
        }


        @RuntimeAnnotation("method")
        public String handle(@RuntimeAnnotation("parameter1") String message,
                             @RuntimeAnnotation("parameter2") String message2) {
            return message + message2;
        }

        public void echo() {
        }
    }

    @ClassAnnotation
    private static class ClassAnnotationHandler {

        @ClassAnnotation
        public String handle(@ClassAnnotation String message) {
            return message;
        }
    }


    @Target({TYPE, PARAMETER, METHOD})
    @Retention(RUNTIME)
    private @interface RuntimeAnnotation {

        String value();
    }

    @Target({TYPE, PARAMETER, METHOD})
    @Retention(CLASS)
    private @interface ClassAnnotation {
    }
}