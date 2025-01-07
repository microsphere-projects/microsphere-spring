package io.microsphere.spring.webmvc.interceptor;

import io.microsphere.spring.webmvc.util.WebMvcUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * {@link HandlerMethod} {@link HandlerInterceptor} abstract implementation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class MethodHandlerInterceptor implements HandlerInterceptor {

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
