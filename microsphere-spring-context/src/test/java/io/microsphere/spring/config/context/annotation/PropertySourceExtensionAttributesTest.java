package io.microsphere.spring.config.context.annotation;

import io.microsphere.spring.config.env.support.DefaultResourceComparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.microsphere.spring.config.context.annotation.PropertySourceExtensionAttributes.validateAnnotationType;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PropertySourceExtensionAttributes} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourceExtensionAttributes
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {
                PropertySourceExtensionAttributesTest.class
        })
@ResourcePropertySource(
        name = "test-property-source",
        value = {
                "classpath*:/META-INF/test/*.properties"
        },
        ignoreResourceNotFound = true,
        encoding = "UTF-8"
)
public class PropertySourceExtensionAttributesTest {

    private static final Class<ResourcePropertySource> annotationType = ResourcePropertySource.class;

    private PropertySourceExtensionAttributes<ResourcePropertySource> attributes;

    @Autowired
    private Environment environment;

    @BeforeEach
    public void before() {
        AnnotationAttributes attributes = getAnnotationAttributes(getClass(), annotationType, environment, false);
        this.attributes = new PropertySourceExtensionAttributes<>(attributes, annotationType, environment);
    }

    @Test
    public void testValidateAnnotationType() {
        assertSame(annotationType, validateAnnotationType(annotationType));
    }

    @Test
    public void testValidateAnnotationTypeOnIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> validateAnnotationType(Override.class));
    }

    @Test
    public void testGetName() {
        assertEquals("test-property-source", this.attributes.getName());
    }

    @Test
    public void testIsAutoRefreshed() {
        assertFalse(this.attributes.isAutoRefreshed());
    }

    @Test
    public void testIsFirstPropertySource() {
        assertFalse(this.attributes.isFirstPropertySource());
    }

    @Test
    public void testGetBeforePropertySourceName() {
        assertEquals(EMPTY_STRING, this.attributes.getBeforePropertySourceName());
    }

    @Test
    public void testGetAfterPropertySourceName() {
        assertEquals(EMPTY_STRING, this.attributes.getAfterPropertySourceName());
    }

    @Test
    public void testGetAnnotationType() {
        assertSame(annotationType, attributes.getAnnotationType());
    }

    @Test
    public void testGetValue() {
        assertArrayEquals(ofArray("classpath*:/META-INF/test/*.properties"), attributes.getValue());
    }

    @Test
    public void testGetResourceComparatorClass() {
        assertEquals(DefaultResourceComparator.class, attributes.getResourceComparatorClass());
    }

    @Test
    public void testIsIgnoreResourceNotFound() {
        assertTrue(this.attributes.isIgnoreResourceNotFound());
    }

    @Test
    public void testGetEncoding() {
        assertEquals("UTF-8", this.attributes.getEncoding());
    }

    @Test
    public void testGetPropertySourceFactoryClass() {
        assertEquals(DefaultPropertySourceFactory.class, this.attributes.getPropertySourceFactoryClass());
    }

    @Test
    public void testAnnotationType() {
        assertSame(annotationType, attributes.annotationType());
    }

    @Test
    public void testEquals() {
        AnnotationAttributes attributes = getAnnotationAttributes(getClass(), annotationType, environment, false);
        assertEquals(this.attributes, new PropertySourceExtensionAttributes<>(attributes, annotationType, environment));
    }

    @Test
    public void testHashCode() {
        AnnotationAttributes attributes = getAnnotationAttributes(getClass(), annotationType, environment, false);
        assertEquals(this.attributes.hashCode(), new PropertySourceExtensionAttributes<>(attributes, annotationType, environment).hashCode());
    }

    @Test
    public void testToString() {
        assertNotNull(this.attributes.toString());
    }
}