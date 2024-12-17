package io.microsphere.spring.util;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link FieldUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see FieldUtils
 * @since 2017.01.22
 */
public class FieldUtilsTest {

    private Object data;

    @Test
    public void testGetField() {

        FieldUtilsTest instance = new FieldUtilsTest();

        int hash = FieldUtils.getFieldValue(instance, "data", 2);

        Assert.assertEquals(2, hash);

        instance.data = 1;

        hash = FieldUtils.getFieldValue(instance, "data", 2);

        Assert.assertEquals(1, hash);

    }
}
