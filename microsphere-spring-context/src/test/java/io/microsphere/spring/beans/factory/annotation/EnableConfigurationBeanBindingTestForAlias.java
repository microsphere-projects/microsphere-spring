package io.microsphere.spring.beans.factory.annotation;

import io.microsphere.spring.test.domain.User;
import org.junit.Test;

import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link EnableConfigurationBeanBinding} Test
 *
 * @author
 * @since 1.0.0
 */
@EnableConfigurationBeanBinding(prefix = "users",
        type = User.class, multiple = true, ignoreUnknownFields = false, ignoreInvalidFields = false)
public class EnableConfigurationBeanBindingTestForAlias extends AbstractEnableConfigurationBeanBindingTest {


    @Test
    public void test() {
        testAlice("a", () -> ofList("UserA", "users-a", "users_a"));
        testAlice("b", () -> ofList("UserB", "users-b", "users_b"));

    }

    private List<String> ofList(String... names) {
        return stream(names).collect(toList());
    }

    private void testAlice(String beanName, Supplier<List<String>> aliasSupplier) {
        assertTrue(context.containsBeanDefinition(beanName));
        assertTrue(context.containsBean(beanName));

        User user = context.getBean(beanName, User.class);

        List<String> aliasList = aliasSupplier.get();
        aliasList.forEach(alias -> {
            assertFalse(context.containsBeanDefinition(alias));
            assertTrue(context.containsBean(alias));
            User another = context.getBean(alias, User.class);
            assertEquals(user, another);
        });
    }
}
