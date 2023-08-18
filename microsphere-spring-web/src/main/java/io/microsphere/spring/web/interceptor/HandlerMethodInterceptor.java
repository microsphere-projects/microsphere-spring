package io.microsphere.spring.web.interceptor;

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
     * @param request       {@link WebRequest}
     * @param handlerMethod {@link HandlerMethod}
     * @return if <code>false</code>, it will interrupt the execution of a {@link HandlerMethod} and
     * the following {@link HandlerMethodInterceptor HandlerMethodInterceptors}.
     * @throws Throwable if any error caused
     */
    boolean beforeHandle(NativeWebRequest request, HandlerMethod handlerMethod) throws Throwable;

    /**
     * Interception point after successful execution of a {@link HandlerMethod}.
     * Called after HandlerAdapter actually invoked the handler.
     *
     * @param request       {@link WebRequest}
     * @param handlerMethod {@link HandlerMethod}
     * @throws Throwable if any error caused
     */
    void afterHandle(NativeWebRequest request, HandlerMethod handlerMethod) throws Throwable;

    /**
     * Callback after completion of request processing.
     * Will be called on any outcome of handler execution, thus allows for proper resource cleanup.
     *
     * @param request       {@link WebRequest}
     * @param handlerMethod {@link HandlerMethod}
     * @throws Throwable if any error caused
     */
    void onCompletion(NativeWebRequest request, HandlerMethod handlerMethod) throws Throwable;

    /**
     * Callback after completion of request processing if any error caused.
     * Will be called on any outcome of handler execution, thus allows for proper resource cleanup.
     *
     * @param request       {@link WebRequest}
     * @param handlerMethod {@link HandlerMethod}
     * @param error         {@link Throwable} if any error caused
     */
    void onError(NativeWebRequest request, HandlerMethod handlerMethod, Throwable error);
}
