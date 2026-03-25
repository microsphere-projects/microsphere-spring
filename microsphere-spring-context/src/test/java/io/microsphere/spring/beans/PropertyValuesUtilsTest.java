package io.microsphere.spring.beans;

import io.microsphere.logging.test.junit4.LoggingLevelsRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.beans.PropertyValues;
import org.springframework.mock.env.MockEnvironment;

import static io.microsphere.logging.test.junit4.LoggingLevelsRule.levels;
import static io.microsphere.spring.beans.PropertyValuesUtils.getSubPropertyValues;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * {@link PropertyValuesUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertyValuesUtils
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class PropertyValuesUtilsTest {

    @ClassRule
    public static final LoggingLevelsRule LOGGING_LEVELS_RULE = levels("TRACE", "INFO", "ERROR");


    @Test
    public void testGetSubPropertyValues() {

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
