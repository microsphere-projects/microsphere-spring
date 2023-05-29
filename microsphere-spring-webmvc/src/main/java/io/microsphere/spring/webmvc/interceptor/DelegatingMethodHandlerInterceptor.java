package io.microsphere.spring.webmvc.interceptor;

import io.microsphere.spring.webmvc.method.HandlerMethodsInitializedEvent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;

/**
 * Delegating {@link RequestMapping}  {@link HandlerMethod} {@link HandlerInterceptor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see MethodHandlerInterceptor
 * @since 1.0.0
 */
public class DelegatingMethodHandlerInterceptor extends MethodHandlerInterceptor implements ApplicationContextAware,
        ApplicationListener<HandlerMethodsInitializedEvent> {

    private ApplicationContext applicationContext;

    private Map<Method, List<MethodHandlerInterceptor>> delegateHandlerInterceptorsMap;

    @Override
    protected boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod)
            throws Exception {

        boolean hasNext = true;

        getHandlerMethodArguments(request, handlerMethod);

        for (MethodHandlerInterceptor delegateHandlerInterceptor : getDelegateHandlerInterceptors(handlerMethod)) {
            if (!delegateHandlerInterceptor.preHandle(request, response, handlerMethod)) {
                hasNext = false;
                break;
            }
        }

        return hasNext;
    }

    @Override
    protected void postHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod,
                              ModelAndView modelAndView) throws Exception {
        for (MethodHandlerInterceptor delegateHandlerInterceptor : getDelegateHandlerInterceptors(handlerMethod)) {
            delegateHandlerInterceptor.postHandle(request, response, handlerMethod, modelAndView);
        }
    }

    @Override
    protected void afterCompletion(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod,
                                   Exception ex) throws Exception {
        for (MethodHandlerInterceptor delegateHandlerInterceptor : getDelegateHandlerInterceptors(handlerMethod)) {
            delegateHandlerInterceptor.afterCompletion(request, response, handlerMethod, ex);
        }
    }

    @Override
    public void onApplicationEvent(HandlerMethodsInitializedEvent event) {

        List<MethodHandlerInterceptor> delegateHandlerInterceptors = getDelegateHandlerInterceptors();

        Set<HandlerMethod> handlerMethods = event.getHandlerMethods();

        initDelegateHandlerInterceptors(handlerMethods, delegateHandlerInterceptors);
    }

    private List<MethodHandlerInterceptor> getDelegateHandlerInterceptors() {
        Collection<MethodHandlerInterceptor> methodHandlerInterceptors = getBeans(MethodHandlerInterceptor.class);
        List<MethodHandlerInterceptor> delegates = new LinkedList<>();
        for (MethodHandlerInterceptor methodHandlerInterceptor : methodHandlerInterceptors) {
            // Filter Delegate Instance
            if (methodHandlerInterceptor.isDelegate()) {
                delegates.add(methodHandlerInterceptor);
            }
        }
        AnnotationAwareOrderComparator.sort(delegates);
        return delegates;
    }

    private void initDelegateHandlerInterceptors(Set<HandlerMethod> handlerMethods,
                                                 List<MethodHandlerInterceptor> delegateHandlerInterceptors) {

        delegateHandlerInterceptorsMap = new HashMap<>();

        for (HandlerMethod handlerMethod : handlerMethods) {
            Method method = handlerMethod.getMethod();

            for (MethodHandlerInterceptor handlerInterceptor : delegateHandlerInterceptors) {
                if (handlerInterceptor.isDelegate(handlerMethod)) {
                    List<MethodHandlerInterceptor> interceptors = delegateHandlerInterceptorsMap.computeIfAbsent(method,
                            m -> new LinkedList<>());
                    interceptors.add(handlerInterceptor);
                }
            }
        }
    }

    private List<MethodHandlerInterceptor> getDelegateHandlerInterceptors(HandlerMethod handlerMethod) {
        Method method = handlerMethod.getMethod();
        return delegateHandlerInterceptorsMap.getOrDefault(method, emptyList());
    }

    private <T> Collection<T> getBeans(Class<T> beanType) {
        return beansOfTypeIncludingAncestors(applicationContext, beanType, true, false).values();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
