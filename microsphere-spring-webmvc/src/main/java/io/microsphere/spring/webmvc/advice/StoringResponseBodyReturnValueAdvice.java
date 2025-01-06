package io.microsphere.spring.webmvc.advice;

import io.microsphere.spring.webmvc.util.WebMvcUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.lang.reflect.Method;

import static io.microsphere.spring.webmvc.util.WebMvcUtils.setHandlerMethodReturnValue;
import static io.microsphere.spring.webmvc.util.WebMvcUtils.supportedConverterTypes;

/**
 * Store {@ link HandlerMethod} return value {@ link ResponseBodyAdviceAdapter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@RestControllerAdvice
public class StoringResponseBodyReturnValueAdvice extends ResponseBodyAdviceAdapter<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return supportedConverterTypes.contains(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        Method method = returnType.getMethod();
        if (request instanceof ServletServerHttpRequest serverHttpRequest) {
            HttpServletRequest httpServletRequest = serverHttpRequest.getServletRequest();
            setHandlerMethodReturnValue(httpServletRequest, method, body);
        }
        return body;
    }
}
