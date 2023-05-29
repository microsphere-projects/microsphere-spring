package io.microsphere.spring.redis.connection.dynamic.web;

import io.microsphere.spring.redis.connection.dynamic.DynamicRedisConnectionFactory;

import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.WebListener;

/**
 * {@link DynamicRedisConnectionFactory} State cleaner
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@WebListener
public class DynamicRedisConnectionFactoryCleanerListener implements ServletRequestListener {

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        // 清除 ThreadLocal
        DynamicRedisConnectionFactory.clearTarget();
    }
}
