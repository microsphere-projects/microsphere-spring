package io.microsphere.spring.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class WrapperUtilsTest {

    @Test
    public void testUnwrapWithConfigurableListableBeanFactory() {
        // Arrange
        ConfigurableListableBeanFactory configurableListableBeanFactoryMock = mock(ConfigurableListableBeanFactory.class);
        BeanFactory beanFactory = configurableListableBeanFactoryMock;

        // Act
        ConfigurableListableBeanFactory result = WrapperUtils.unwrap(beanFactory);

        // Assert
        assertEquals(configurableListableBeanFactoryMock, result, "The unwrap method should return the same instance when passed a ConfigurableListableBeanFactory.");
    }

    @Test
    public void testUnwrapWithNonConfigurableListableBeanFactory() {
        // Arrange
        BeanFactory nonConfigurableBeanFactoryMock = mock(BeanFactory.class);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> WrapperUtils.unwrap(nonConfigurableBeanFactoryMock),
                "The unwrap method should throw an IllegalArgumentException when the bean factory is not an instance of ConfigurableListableBeanFactory.");
    }

}
