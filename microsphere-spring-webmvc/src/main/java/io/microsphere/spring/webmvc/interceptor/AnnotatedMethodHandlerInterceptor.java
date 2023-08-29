package io.microsphere.spring.webmvc.interceptor;

import org.springframework.core.ResolvableType;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;

/**
 * The annotation method {@link HandlerInterceptor} abstract implementation
 *
 * @param <A> Annotation type
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class AnnotatedMethodHandlerInterceptor<A extends Annotation> extends MethodHandlerInterceptor {

    private final Class<A> annotationType;

    public AnnotatedMethodHandlerInterceptor() {
        this.annotationType = resolveAnnotationType();
    }

    private Class<A> resolveAnnotationType() {
        ResolvableType resolvableType = ResolvableType.forType(getClass());
        return (Class<A>) resolvableType.getSuperType().getGeneric(0).resolve();
    }

    @Override
    protected final boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod)
            throws Exception {
        A annotation = getMethodAnnotation(request, handlerMethod);
        if (annotation != null) {
            return preHandle(request, response, handlerMethod, annotation);
        }
        return true;
    }

    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                HandlerMethod handlerMethod, A annotation) throws Exception {
        return true;
    }

    @Override
    protected final void postHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod,
                                    ModelAndView modelAndView) throws Exception {
        A annotation = getMethodAnnotation(request, handlerMethod);
        if (annotation != null) {
            postHandle(request, response, handlerMethod, modelAndView, annotation);
        }
    }

    protected void postHandle(HttpServletRequest request, HttpServletResponse response,
                              HandlerMethod handlerMethod, ModelAndView modelAndView, A annotation) throws Exception {
        // DO NOTHING
    }

    @Override
    protected final void afterCompletion(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod,
                                         Exception ex) throws Exception {
        A annotation = getMethodAnnotation(request, handlerMethod);
        if (annotation != null) {
            afterCompletion(request, response, handlerMethod, ex, annotation);
        }
    }

    protected void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                   HandlerMethod handlerMethod, Exception ex, A annotation) throws Exception {
        // DO NOTHING
    }

    public final Class<A> getAnnotationType() {
        return annotationType;
    }

    protected final A getMethodAnnotation(HttpServletRequest request, HandlerMethod handlerMethod) {
        ServletContext servletContext = request.getServletContext();
        String attributeName = getAnnotationAttributeName(handlerMethod);
        A annotation = (A) servletContext.getAttribute(attributeName);
        if (annotation == null) {
            annotation = handlerMethod.getMethodAnnotation(getAnnotationType());
            servletContext.setAttribute(attributeName, annotation);
        }
        return annotation;
    }

    private String getAnnotationAttributeName(HandlerMethod handlerMethod) {
        return getAnnotationType() + "@" + handlerMethod.toString();
    }

    public boolean isDelegate(HandlerMethod handlerMethod) {
        return handlerMethod.hasMethodAnnotation(annotationType);
    }

}
