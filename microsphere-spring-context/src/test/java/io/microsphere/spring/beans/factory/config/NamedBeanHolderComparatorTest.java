package io.microsphere.spring.beans.factory.config;

import org.junit.Test;
import org.springframework.beans.factory.config.NamedBeanHolder;

import static org.junit.Assert.assertEquals;

/**
 * {@link NamedBeanHolderComparator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NamedBeanHolderComparator
 * @since 1.0.0
 */
public class NamedBeanHolderComparatorTest {

    @Test
    public void testCompare() {
        NamedBeanHolder namedBeanHolder1 = createNamedBeanHolder("bean1");
        NamedBeanHolder namedBeanHolder2 = createNamedBeanHolder("bean2");
        assertEquals(0, NamedBeanHolderComparator.INSTANCE.compare(namedBeanHolder1, namedBeanHolder2));
    }

    private NamedBeanHolder createNamedBeanHolder(String name) {
        return new NamedBeanHolder(name, name);
    }
}