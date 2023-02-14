package io.github.microsphere.spring.webmvc.advice;

import io.github.microsphere.spring.webmvc.util.WebMvcUtils;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

/**
 * Store {@ link HandlerMethod} return value {@ link ResponseBodyAdviceAdapter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
@RestControllerAdvice
public class StoringHandlerMethodReturnValueResponseBodyAdvice extends ResponseBodyAdviceAdapter<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return WebMvcUtils.supportedConverterTypes.contains(converterType);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        Method method = returnType.getMethod();
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest serverHttpRequest = (ServletServerHttpRequest) request;
            HttpServletRequest httpServletRequest = serverHttpRequest.getServletRequest();
            WebMvcUtils.setHandlerMethodReturnValue(httpServletRequest, method, body);
        }
        return body;
    }
}
