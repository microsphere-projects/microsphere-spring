package io.microsphere.spring.webmvc.method.support;

import io.microsphere.spring.webmvc.method.HandlerMethodArgumentsResolvedEvent;
import io.microsphere.spring.webmvc.util.WebMvcUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.lang.reflect.Method;

/**
 * {@link HandlerMethodArgumentResolver} Wrapper
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class HandlerMethodArgumentResolverWrapper implements HandlerMethodArgumentResolver {

    private final HandlerMethodArgumentResolver resolver;

    private final ApplicationContext applicationContext;

    public HandlerMethodArgumentResolverWrapper(HandlerMethodArgumentResolver handlerMethodArgumentResolver, ApplicationContext applicationContext) {
        this.resolver = handlerMethodArgumentResolver;
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return resolver.supportsParameter(parameter);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) throws Exception {
        Object argument = resolver.resolveArgument(parameter, mavContainer, webRequest, binderFactory);

        int index = parameter.getParameterIndex();
        Object[] arguments = null;
        if (argument != null) {
            arguments = WebMvcUtils.getHandlerMethodArguments(webRequest, parameter);
            if (index < arguments.length) {
                arguments[index] = argument;
            }
        }

        Method method = parameter.getMethod();
        int parameterCount = method.getParameterCount();

        if (index == parameterCount - 1) {
            if (arguments == null) {
                arguments = WebMvcUtils.getHandlerMethodArguments(webRequest, parameter);
            }
            applicationContext.publishEvent(new HandlerMethodArgumentsResolvedEvent(method, arguments, webRequest));
        }

        return argument;
    }

}
