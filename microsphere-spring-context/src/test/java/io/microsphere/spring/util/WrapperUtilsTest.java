package io.microsphere.spring.util;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;

public class WrapperUtilsTest {

    @Test
    public void testUnwrapWithConfigurableListableBeanFactory() {
        // Arrange
        ConfigurableListableBeanFactory configurableListableBeanFactoryMock = mock(ConfigurableListableBeanFactory.class);
        BeanFactory beanFactory = configurableListableBeanFactoryMock;

        // Act
        ConfigurableListableBeanFactory result = WrapperUtils.unwrap(beanFactory);

        // Assert
        assertEquals("The unwrap method should return the same instance when passed a ConfigurableListableBeanFactory.", configurableListableBeanFactoryMock, result);
    }

    @Test
    public void testUnwrapWithNonConfigurableListableBeanFactory() {
        // Arrange
        BeanFactory nonConfigurableBeanFactoryMock = mock(BeanFactory.class);

        // Act & Assert
        assertThrows("The unwrap method should throw an IllegalArgumentException when the bean factory is not an instance of ConfigurableListableBeanFactory.",
                IllegalArgumentException.class, () -> WrapperUtils.unwrap(nonConfigurableBeanFactoryMock));
    }

}
