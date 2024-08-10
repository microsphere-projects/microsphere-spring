package io.microsphere.spring.web.servlet.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

/**
 * Content Caching {@link Filter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see ContentCachingResponseWrapper
 * @since 1.0.0
 */
public class ContentCachingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ContentCachingFilter.class);

    public static final String RESPONSE_CONTENT_REQUEST_ATTRIBUTE_NAME = "_ContentCachingFilter_";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        try {
            filterChain.doFilter(request, responseWrapper);
        } finally {
            responseWrapper.copyBodyToResponse();
        }
    }

    public static String getResponseContentAsString(ServletRequest request, ServletResponse response) {

        // Usually, ContentCachingFilter will be auto-configured, and the priority is not the highest
        // However, it is not excluded that other Filter take precedence over ContentCachingFilter,
        // so that its response is ContentCachingResponseWrapper
        if (!(response instanceof ContentCachingResponseWrapper)) {
            return null;
        }

        // Most business scenarios concern JSON content, which has been written to the Response Content Cache by the
        // RequestResponseBodyMethodProcessor
        // Other content may also be written to ContentCachingResponseWrapper
        // Reduce duplicate calculation of the same request
        String content = (String) request.getAttribute(RESPONSE_CONTENT_REQUEST_ATTRIBUTE_NAME);

        if (content != null) {
            return content;
        }

        ContentCachingResponseWrapper responseWrapper = (ContentCachingResponseWrapper) response;

        try {
            content = new String(responseWrapper.getContentAsByteArray(), response.getCharacterEncoding());
            request.setAttribute(RESPONSE_CONTENT_REQUEST_ATTRIBUTE_NAME, content);
        } catch (Throwable e) {
            logger.error("ContentCachingResponseWrapper convert failed, msg={}", e.getMessage());
        }

        return content;
    }
}
