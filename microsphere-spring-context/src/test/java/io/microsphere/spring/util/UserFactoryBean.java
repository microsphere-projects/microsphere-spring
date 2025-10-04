package io.microsphere.spring.util;

import io.microsphere.spring.test.domain.User;
import org.springframework.beans.factory.FactoryBean;

/**
 * {@link User}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see User
 * @since 1.0.0
 */
public class UserFactoryBean implements FactoryBean<User> {

    private String name;

    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public User getObject() throws Exception {
        User user = new User();
        user.setName(name);
        user.setAge(age);
        return user;
    }

    @Override
    public Class<?> getObjectType() {
        return User.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

}
