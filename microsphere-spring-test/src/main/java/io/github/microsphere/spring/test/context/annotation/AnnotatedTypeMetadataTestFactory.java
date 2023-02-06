package io.github.microsphere.spring.test.context.annotation;

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.core.type.StandardMethodMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * {@link AnnotatedTypeMetadata} Test Factory
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class AnnotatedTypeMetadataTestFactory implements BeanClassLoaderAware {

    private ClassLoader classLoader;

    public AnnotatedTypeMetadata createMethodAnnotatedTypeMetadata() {
        Method method = findTestMethod();
        return new StandardMethodMetadata(method);
    }

    private Method findTestMethod() {
        StackTraceElement[] stackTraces = Thread.currentThread().getStackTrace();
        StackTraceElement stackTrace = stackTraces[3];
        String className = stackTrace.getClassName();
        String methodName = stackTrace.getMethodName();
        Class<?> targetClass = ClassUtils.resolveClassName(className, classLoader);
        return ReflectionUtils.findMethod(targetClass, methodName);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
