package io.microsphere.spring.webmvc.config;

import io.microsphere.spring.webmvc.event.EventPublishingWebMvcListener;
import io.microsphere.spring.webmvc.interceptor.DelegatingMethodHandlerInterceptor;
import io.microsphere.spring.webmvc.interceptor.MethodHandlerInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * General {@link WebMvcConfigurer Spring WebMVC configuration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@Import(value = {
        EventPublishingWebMvcListener.class,
        DelegatingMethodHandlerInterceptor.class
})
public class CommonWebMvcConfigurer implements WebMvcConfigurer {

    @Autowired
    private ObjectProvider<MethodHandlerInterceptor> methodHandlerInterceptors;

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
