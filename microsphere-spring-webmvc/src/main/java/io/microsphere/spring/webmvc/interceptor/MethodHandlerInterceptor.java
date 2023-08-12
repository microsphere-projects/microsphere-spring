package io.microsphere.spring.webmvc.interceptor;

import io.microsphere.spring.webmvc.config.GenericWebMvcConfigurer;
import io.microsphere.spring.webmvc.util.WebMvcUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link HandlerMethod} {@link HandlerInterceptor} abstract implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class MethodHandlerInterceptor implements HandlerInterceptor {

    /**
     * Whether the current {@link HandlerInterceptor} is a delegate object
     */
    private final boolean delegate;

    public MethodHandlerInterceptor() {
        this(Boolean.FALSE);
    }

    public MethodHandlerInterceptor(boolean delegate) {
        this.delegate = delegate;
    }

    @Override
    public final boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (supports(request, response, handlerMethod)) {
                return preHandle(request, response, handlerMethod);
            }
        }
        return true;
    }

    protected abstract boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception;

    @Override
    public final void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (supports(request, response, handlerMethod)) {
                postHandle(request, response, handlerMethod, modelAndView);
            }
        }
    }

    protected abstract void postHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, ModelAndView modelAndView) throws Exception;

    @Override
    public final void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            if (supports(request, response, handlerMethod)) {
                afterCompletion(request, response, handlerMethod, ex);
            }
        }
    }

    protected abstract void afterCompletion(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception ex) throws Exception;

    protected boolean supports(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
        return true;
    }

    /**
     * Whether the current {@link HandlerInterceptor} is a Spring Bean as the delegate
     *
     * @return If <code>true<code>, current instance will be looked up by {@link DelegatingMethodHandlerInterceptor} as a delegate
     * for intercepting, otherwise, current instance will be registered into {@link InterceptorRegistry} when
     * the {@link GenericWebMvcConfigurer#addInterceptors(InterceptorRegistry)} method will be called.
     */
    public boolean isDelegate() {
        return delegate;
    }

    /**
     * Whether the current handler is delegate {@link HandlerMethod}
     *
     * @param handlerMethod {@link HandlerMethod}
     * @return <code>true<code> If the proxy {@link HandlerInterceptor}, default <code>false<code>
     */
    public boolean isDelegate(HandlerMethod handlerMethod) {
        return false;
    }

    /**
     * Gets the {@link HandlerMethod} method parameter
     *
     * @param request       {@link ServletRequest}
     * @param handlerMethod {@link HandlerMethod}
     * @return non-null
     */
    protected Object[] getHandlerMethodArguments(ServletRequest request, HandlerMethod handlerMethod) {
        return WebMvcUtils.getHandlerMethodArguments(request, handlerMethod);
    }

    /**
     * Gets the value returned by the {@link HandlerMethod} method
     *
     * @param request       {@link ServletRequest}
     * @param handlerMethod {@link HandlerMethod}
     * @param <T>           Method return value type
     * @return {@link HandlerMethod} Method return value
     */
    protected <T> T getHandlerMethodReturnValue(ServletRequest request, HandlerMethod handlerMethod) {
        return WebMvcUtils.getHandlerMethodReturnValue(request, handlerMethod);
    }

    /**
     * Gets the {@link RequestBody @RequestBody} method parameter from the {@link ServletRequest} context
     *
     * @param request       {@link ServletRequest}
     * @param handlerMethod {@link HandlerMethod}
     * @param <T>           {@link RequestBody @RequestBody} Method parameter Types
     * @return {@link RequestBody @RequestBody} Method argument if present, otherwise <code>null<code>
     */
    protected <T> T getHandlerMethodRequestBodyArgument(ServletRequest request, HandlerMethod handlerMethod) {
        return WebMvcUtils.getHandlerMethodRequestBodyArgument(request, handlerMethod);
    }
}
