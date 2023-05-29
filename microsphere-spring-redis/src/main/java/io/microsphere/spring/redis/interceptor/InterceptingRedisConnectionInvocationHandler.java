package io.microsphere.spring.redis.interceptor;

import io.microsphere.spring.redis.context.RedisContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;

/**
 * {@link InvocationHandler} for Intercepting {@link RedisConnection}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class InterceptingRedisConnectionInvocationHandler implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(InterceptingRedisConnectionInvocationHandler.class);

    private static final String HASH_CODE = "hashCode";

    private static final String EQUALS = "equals";

    private final RedisConnection rawRedisConnection;

    private final RedisContext redisContext;

    private final String sourceBeanName;

    private final List<RedisConnectionInterceptor> redisConnectionInterceptors;

    private final List<RedisCommandInterceptor> redisCommandInterceptors;

    private final int redisConnectionInterceptorCount;

    private final int redisCommandInterceptorCount;

    private final boolean hasRedisConnectionInterceptors;

    private final boolean hasRedisCommandInterceptors;

    public InterceptingRedisConnectionInvocationHandler(RedisConnection rawRedisConnection, RedisContext redisContext, String sourceBeanName) {
        this.rawRedisConnection = rawRedisConnection;
        this.redisContext = redisContext;
        this.sourceBeanName = sourceBeanName;
        this.redisConnectionInterceptors = redisContext.getRedisConnectionInterceptors();
        this.redisCommandInterceptors = redisContext.getRedisCommandInterceptors();

        this.redisConnectionInterceptorCount = redisConnectionInterceptors.size();
        this.redisCommandInterceptorCount = redisCommandInterceptors.size();

        this.hasRedisConnectionInterceptors = redisConnectionInterceptorCount > 0;
        this.hasRedisCommandInterceptors = redisCommandInterceptorCount > 0;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String methodName = method.getName();

        if (EQUALS.equals(methodName)) {
            // Only consider equal when proxies are identical.
            return (proxy == args[0]);
        } else if (HASH_CODE.equals(methodName)) {
            // Use hashCode of PersistenceManager proxy.
            return System.identityHashCode(proxy);
        }

        if (!method.isAccessible()) {
            method.setAccessible(true);
        }

        RedisMethodContext<RedisConnection> redisMethodContext = createRedisMethodContext(method, args);

        Object result = null;
        Throwable failure = null;
        try {
            beforeExecute(redisMethodContext);
            result = method.invoke(rawRedisConnection, args);
        } catch (Throwable e) {
            failure = e;
            throw e.getCause();
        } finally {
            afterExecute(redisMethodContext, result, failure);
        }
        return result;
    }

    private RedisMethodContext<RedisConnection> createRedisMethodContext(Method method, Object[] args) {
        RedisMethodContext<RedisConnection> redisMethodContext = new RedisMethodContext<>(rawRedisConnection, method, args, redisContext, sourceBeanName);
        return redisMethodContext;
    }


    private void beforeExecute(RedisMethodContext<RedisConnection> redisMethodContext) {
        beforeExecute(redisConnectionInterceptors, redisConnectionInterceptorCount, hasRedisConnectionInterceptors, redisMethodContext);
        beforeExecute(redisCommandInterceptors, redisCommandInterceptorCount, hasRedisCommandInterceptors, redisMethodContext);
    }

    private void beforeExecute(List<? extends RedisMethodInterceptor> redisMethodInterceptors, int size, boolean exists, RedisMethodContext<RedisConnection> redisMethodContext) {
        if (exists) {
            for (int i = 0; i < size; i++) {
                RedisMethodInterceptor interceptor = redisMethodInterceptors.get(i);
                try {
                    interceptor.beforeExecute(redisMethodContext);
                } catch (Throwable e) {
                    interceptor.handleError(redisMethodContext, true, null, null, e);
                    logger.error("The execution of RedisMethodInterceptor[class : '{}'] afterExecute method is failed, context : {}", interceptor.getClass().getName(), redisMethodContext);
                }
            }
        }
    }

    private void afterExecute(RedisMethodContext<RedisConnection> redisMethodContext, Object result, Throwable failure) {
        afterExecute(redisConnectionInterceptors, redisConnectionInterceptorCount, hasRedisConnectionInterceptors, redisMethodContext, result, failure);
        afterExecute(redisCommandInterceptors, redisCommandInterceptorCount, hasRedisCommandInterceptors, redisMethodContext, result, failure);
    }

    private void afterExecute(List<? extends RedisMethodInterceptor> redisMethodInterceptors, int size, boolean exists, RedisMethodContext<RedisConnection> redisMethodContext, Object result, Throwable failure) {
        if (exists) {
            for (int i = 0; i < size; i++) {
                RedisMethodInterceptor interceptor = redisMethodInterceptors.get(i);
                try {
                    interceptor.afterExecute(redisMethodContext, result, failure);
                } catch (Throwable e) {
                    interceptor.handleError(redisMethodContext, false, result, failure, e);
                    logger.error("The execution of RedisMethodInterceptor[class : '{}'] afterExecute method is failed, context : {}, result : {} , failure : {}", interceptor.getClass().getName(), redisMethodContext, result, failure);
                }
            }
        }
    }
}
