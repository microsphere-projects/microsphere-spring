package io.microsphere.spring.beans.factory.support;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class ConfigurationBeanAliasGeneratorTest {

    private final Class<?> clazz = Foo.class;
    private final String beanName = "bean";

    @Test
    public void testClassNameAndBeanNameConnect() {
        ConfigurationBeanAliasGenerator generator = new DefaultConfigurationBeanAliasGenerator();
        String prefix = "any";

        String aliasA = generator.generateAlias(prefix, "a", clazz);
        assertEquals("FooA", aliasA);

        String aliasApple = generator.generateAlias(prefix, "apple", clazz);
        assertEquals("FooApple", aliasApple);

    }


    @Test
    public void testPrefixAndBeanNameConnectUseLine() {
        JoinAliasGenerator generator = new HyphenAliasGenerator();
        Map<String, String> table = new HashMap<>();
        table.put("users", "users-bean");
        table.put("spring.users", "springUsers-bean");
        table.put("spring.users.zhangsan", "springUsersZhangsan-bean");

        table.forEach((key, value) -> {
            String alias = generator.generateAlias(key, beanName, clazz);
            assertEquals(value, alias);
        });

    }
    @Test
    public void testPrefixAndBeanNameConnectUseUnderscore() {
        JoinAliasGenerator generator = new UnderScoreJoinAliasGenerator();

        Map<String, String> table = new HashMap<>();
        table.put("users", "users_bean");
        table.put("spring.users", "springUsers_bean");
        table.put("spring.users.zhangsan", "springUsersZhangsan_bean");

        table.forEach((key, value) -> {
            String alias = generator.generateAlias(key, beanName, clazz);
            assertEquals(value, alias);
        });

    }


    static class Foo {
    }
}