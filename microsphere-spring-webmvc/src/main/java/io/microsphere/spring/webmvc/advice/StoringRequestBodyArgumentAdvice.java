package io.microsphere.spring.webmvc.advice;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static io.microsphere.spring.webmvc.util.WebMvcUtils.setHandlerMethodRequestBodyArgument;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.supportedConverterTypes;

/**
 * Store the {@link HandlerMethod} {@link RequestBody} parameter {@link RequestBodyAdvice} implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@RestControllerAdvice
public final class StoringRequestBodyArgumentAdvice extends RequestBodyAdviceAdapter {

    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return supportedConverterTypes.contains(converterType);
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType,
                                Class<? extends HttpMessageConverter<?>> converterType) {
        // Store @RequestBody HandlerMethod Argument
        Method method = parameter.getMethod();
        setHandlerMethodRequestBodyArgument(method, body);
        return body;
    }
}
