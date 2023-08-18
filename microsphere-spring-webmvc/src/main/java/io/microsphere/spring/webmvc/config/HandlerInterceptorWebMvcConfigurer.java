package io.microsphere.spring.webmvc.config;

import io.microsphere.spring.webmvc.event.WebMvcEventPublisher;
import io.microsphere.spring.webmvc.handler.ReversedProxyHandlerMapping;
import io.microsphere.spring.webmvc.interceptor.DelegatingMethodHandlerInterceptor;
import io.microsphere.spring.webmvc.interceptor.MethodHandlerInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * {@link WebMvcConfigurer Spring WebMVC configuration} for {@link HandlerInterceptor} beans
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MethodHandlerInterceptor
 * @since 1.0.0
 */
@Import(value = {
        WebMvcEventPublisher.class,
        DelegatingMethodHandlerInterceptor.class,
        ReversedProxyHandlerMapping.class
})
public class HandlerInterceptorWebMvcConfigurer implements WebMvcConfigurer {

    private final Class<? extends HandlerInterceptor>[] interceptorClasses;

    @Autowired
    private ObjectProvider<MethodHandlerInterceptor> methodHandlerInterceptors;

    public HandlerInterceptorWebMvcConfigurer(Class<? extends HandlerInterceptor>[] interceptorClasses) {
        this.interceptorClasses = interceptorClasses;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        methodHandlerInterceptors.forEach(interceptor -> {
            // Non-Delegate MethodHandlerInterceptor instances are registered
            if (!interceptor.isDelegate()) {
                registry.addInterceptor(interceptor);
            }
        });
    }

}
