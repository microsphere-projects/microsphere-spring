package io.microsphere.spring.beans.factory.config;

import io.microsphere.logging.test.junit4.LoggingLevelsRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.beans.factory.config.NamedBeanHolder;

import static io.microsphere.logging.test.junit4.LoggingLevelsRule.levels;
import static org.junit.Assert.assertEquals;

/**
 * {@link NamedBeanHolderComparator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see NamedBeanHolderComparator
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class NamedBeanHolderComparatorTest {

    @ClassRule
    public static final LoggingLevelsRule LOGGING_LEVELS_RULE = levels("TRACE", "INFO", "ERROR");


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