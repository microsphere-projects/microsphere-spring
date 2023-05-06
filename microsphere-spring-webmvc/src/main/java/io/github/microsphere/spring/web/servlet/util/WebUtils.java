package io.github.microsphere.spring.web.servlet.util;

import org.springframework.util.ClassUtils;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.Registration;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Web Utilities Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebUtils
 * @since 1.0.0
 */
public abstract class WebUtils {

    /**
     * Is Running in Servlet 3 or Above
     */
    private static final boolean servlet3OrAbove;

    /**
     * javax.servlet.ServletContainerInitializer @since 3.0
     */
    private static final String SERVLET_CONTAINER_INITIALIZER_CLASS_NAME = "javax.servlet.ServletContainerInitializer";

    static {

        ClassLoader classLoader = WebUtils.class.getClassLoader();

        servlet3OrAbove = ClassUtils.isPresent(SERVLET_CONTAINER_INITIALIZER_CLASS_NAME, classLoader);

    }

    /**
     * Is Running below Servlet 3 Container
     *
     * @return If below , <code>true</code>
     */
    public static boolean isRunningBelowServlet3Container() {
        return !servlet3OrAbove;
    }


    /**
     * Get {@link ServletContext} from {@link HttpServletRequest}
     *
     * @param request {@link HttpServletRequest}
     * @return non-null
     */
    public static ServletContext getServletContext(HttpServletRequest request) {

        if (isRunningBelowServlet3Container()) { // below Servlet 3.x

            return request.getSession().getServletContext();

        }

        return request.getServletContext();
    }

    /**
     * Find the {@link Map map} of {@link FilterRegistration} in specified {@link Filter} {@link Class}
     *
     * @param servletContext {@link ServletContext}
     * @param filterClass    The {@link Class of class} of {@link Filter}
     * @return the {@link Map map} of {@link ServletRegistration}
     */
    public static Map<String, ? extends FilterRegistration> findFilterRegistrations(
            ServletContext servletContext, Class<? extends Filter> filterClass) {
        Map<String, ? extends FilterRegistration> filterRegistrationsMap = servletContext.getFilterRegistrations();
        return findRegistrations(servletContext, filterRegistrationsMap, filterClass);
    }

    /**
     * Find the {@link Map map} of {@link ServletRegistration} in specified {@link Servlet} {@link Class}
     *
     * @param servletContext {@link ServletContext}
     * @param servletClass   The {@link Class of class} of {@link Servlet}
     * @return the unmodifiable {@link Map map} of {@link ServletRegistration}
     */
    public static Map<String, ? extends ServletRegistration> findServletRegistrations(
            ServletContext servletContext, Class<? extends Servlet> servletClass) {
        Map<String, ? extends ServletRegistration> servletRegistrationsMap = servletContext.getServletRegistrations();
        return findRegistrations(servletContext, servletRegistrationsMap, servletClass);
    }


    /**
     * Find the {@link Map map} of {@link Registration} in specified {@link Class}
     *
     * @param servletContext   {@link ServletContext}
     * @param registrationsMap the {@link Map map} of {@link Registration}
     * @param targetClass      the target {@link Class}
     * @param <R>              the subtype of {@link Registration}
     * @return the unmodifiable {@link Map map} of {@link Registration} i
     */
    protected static <R extends Registration> Map<String, R> findRegistrations(
            ServletContext servletContext, Map<String, R> registrationsMap, Class<?> targetClass) {

        if (registrationsMap.isEmpty()) {
            return Collections.emptyMap();
        }

        ClassLoader classLoader = servletContext.getClassLoader();

        Map<String, R> foundRegistrationsMap = new LinkedHashMap<String, R>();

        for (Map.Entry<String, R> entry : registrationsMap.entrySet()) {
            R registration = entry.getValue();
            String className = registration.getClassName();
            Class<?> registeredRegistrationClass = ClassUtils.resolveClassName(className, classLoader);
            if (ClassUtils.isAssignable(targetClass, registeredRegistrationClass)) {
                String servletName = entry.getKey();
                foundRegistrationsMap.put(servletName, registration);
            }
        }

        return Collections.unmodifiableMap(foundRegistrationsMap);

    }


}
