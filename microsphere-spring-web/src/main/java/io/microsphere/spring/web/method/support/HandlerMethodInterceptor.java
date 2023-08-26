package io.microsphere.spring.web.method.support;

import org.springframework.lang.Nullable;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.HandlerMethod;

/**
 * {@link HandlerMethod} Interceptor that allows for customized {@link HandlerMethod} execution chains.
 * Applications can register any number of existing or custom interceptors
 * for certain groups of handlers, to add common preprocessing behavior
 * without needing to modify each handler implementation.
 *
 * <p>A HandlerInterceptor gets called before the appropriate HandlerAdapter
 * triggers the execution of the handler itself.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see HandlerMethod
 * @see org.springframework.web.servlet.DispatcherServlet
 * @see org.springframework.web.reactive.DispatcherHandler
 * @see org.springframework.web.servlet.HandlerMapping
 * @see org.springframework.web.reactive.HandlerMapping
 * @see org.springframework.web.servlet.HandlerAdapter
 * @see org.springframework.web.reactive.HandlerAdapter
 * @since 1.0.0
 */
public interface HandlerMethodInterceptor {

    /**
     * Interception point before the execution of a {@link HandlerMethod}. Called after
     * HandlerMapping determined an appropriate handler object, but before
     * HandlerAdapter invokes the handler.
     *
     * @param handlerMethod {@link HandlerMethod}
     * @param args          the resolved arguments of {@link HandlerMethod}
     * @param request       {@link WebRequest}
     * @throws Exception if any error caused
     */
    void beforeExecute(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Exception;

    /**
     * Interception point after successful execution of a {@link HandlerMethod}.
     * Called after HandlerAdapter actually invoked the handler.
     *
     * @param handlerMethod {@link HandlerMethod}
     * @param args          the resolved arguments of {@link HandlerMethod}
     * @param returnValue   the return value of {@link HandlerMethod}
     * @param error         the error after {@link HandlerMethod} invocation
     * @param request       {@link WebRequest}
     * @throws Exception if any error caused
     */
    void afterExecute(HandlerMethod handlerMethod, Object[] args, @Nullable Object returnValue, @Nullable Throwable error,
                      NativeWebRequest request) throws Exception;

}
