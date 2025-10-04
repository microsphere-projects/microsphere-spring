package io.microsphere.spring.webmvc.interceptor;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static io.microsphere.spring.webmvc.util.WebMvcUtils.isPageRenderRequest;

/**
 * Abstract Page Render Context {@link HandlerInterceptor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerInterceptor
 * @since 1.0.0
 */
public abstract class AbstractPageRenderContextHandlerInterceptor extends HandlerInterceptorAdapter {


    public final void postHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
        if (isPageRenderRequest(modelAndView)) {
            postHandleOnPageRenderContext(request, response, handler, modelAndView);
        }
    }

    /**
     * post-handle on page render context.
     *
     * @param request      current HTTP request
     * @param response     current HTTP response
     * @param handler      handler (or {@link HandlerMethod}) that started async
     *                     execution, for type and/or instance examination
     * @param modelAndView the {@code ModelAndView} that the handler returned
     *                     (can also be {@code null})
     * @throws Exception in case of errors
     * @see #postHandle(HttpServletRequest, HttpServletResponse, Object, ModelAndView)
     */
    protected abstract void postHandleOnPageRenderContext(
            HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception;

}
