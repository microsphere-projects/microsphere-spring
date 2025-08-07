package io.microsphere.spring.beans.factory.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.NamedBeanHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link NamedBeanHolderComparator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NamedBeanHolderComparator
 * @since 1.0.0
 */
class NamedBeanHolderComparatorTest {

    @Test
    void testCompare() {
        NamedBeanHolder namedBeanHolder1 = createNamedBeanHolder("bean1");
        NamedBeanHolder namedBeanHolder2 = createNamedBeanHolder("bean2");
        assertEquals(0, NamedBeanHolderComparator.INSTANCE.compare(namedBeanHolder1, namedBeanHolder2));
    }

    private NamedBeanHolder createNamedBeanHolder(String name) {
        return new NamedBeanHolder(name, name);
    }
}