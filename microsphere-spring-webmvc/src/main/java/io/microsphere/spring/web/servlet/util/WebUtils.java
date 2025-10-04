package io.microsphere.spring.web.servlet.util;

import io.microsphere.annotation.Nonnull;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.Registration;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Map.Entry;

import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.isAssignableFrom;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

/**
 * Web Utilities Class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebUtils
 * @since 1.0.0
 */
public abstract class WebUtils {

    /**
     * Is Running below Servlet 3 Container
     *
     * @param servletContext {@link ServletContext}
     * @return If below , <code>true</code>
     */
    public static boolean isRunningBelowServlet3Container(@Nonnull ServletContext servletContext) {
        return servletContext.getMajorVersion() < 3;
    }

    /**
     * Get {@link ServletContext} from {@link HttpServletRequest}
     *
     * @param request {@link HttpServletRequest}
     * @return non-null
     */
    @Nonnull
    public static ServletContext getServletContext(@Nonnull HttpServletRequest request) {
        return request.getSession().getServletContext();
    }

    /**
     * Find the {@link Map map} of {@link FilterRegistration} in specified {@link Filter} {@link Class}
     *
     * @param servletContext {@link ServletContext}
     * @param filterClass    The {@link Class of class} of {@link Filter}
     * @return the {@link Map map} of {@link ServletRegistration}
     */
    @Nonnull
    public static Map<String, ? extends FilterRegistration> findFilterRegistrations(
            @Nonnull ServletContext servletContext, @Nonnull Class<? extends Filter> filterClass) {
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
            @Nonnull ServletContext servletContext, @Nonnull Class<? extends Servlet> servletClass) {
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
            return emptyMap();
        }

        ClassLoader classLoader = servletContext.getClassLoader();

        Map<String, R> foundRegistrationsMap = newLinkedHashMap();

        for (Entry<String, R> entry : registrationsMap.entrySet()) {
            R registration = entry.getValue();
            String className = registration.getClassName();
            Class<?> registeredRegistrationClass = resolveClass(className, classLoader);
            if (isAssignableFrom(targetClass, registeredRegistrationClass)) {
                String servletName = entry.getKey();
                foundRegistrationsMap.put(servletName, registration);
            }
        }

        return unmodifiableMap(foundRegistrationsMap);
    }

    private WebUtils() {
    }
}
