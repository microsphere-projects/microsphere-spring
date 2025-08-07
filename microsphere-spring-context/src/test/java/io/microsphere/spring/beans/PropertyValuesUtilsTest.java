package io.microsphere.spring.beans;

import org.junit.jupiter.api.Test;
import org.springframework.beans.PropertyValues;
import org.springframework.mock.env.MockEnvironment;

import static io.microsphere.spring.beans.PropertyValuesUtils.getSubPropertyValues;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link PropertyValuesUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertyValuesUtils
 * @since 1.0.0
 */
class PropertyValuesUtilsTest {

    @Test
    void testGetSubPropertyValues() {

        MockEnvironment environment = new MockEnvironment();

        PropertyValues propertyValues = getSubPropertyValues(environment, "user");

        assertNotNull(propertyValues);

        assertFalse(propertyValues.contains("name"));
        assertFalse(propertyValues.contains("age"));

        environment.setProperty("user.name", "Mercy");
        environment.setProperty("user.age", "30");

        propertyValues = getSubPropertyValues(environment, "user");

        assertEquals("Mercy", propertyValues.getPropertyValue("name").getValue());
        assertEquals("30", propertyValues.getPropertyValue("age").getValue());

    }

}
