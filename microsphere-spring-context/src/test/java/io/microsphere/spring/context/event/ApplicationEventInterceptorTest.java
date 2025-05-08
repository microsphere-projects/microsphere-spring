package io.microsphere.spring.context.event;

import io.microsphere.logging.Logger;
import org.junit.Before;
import org.junit.Test;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static org.junit.Assert.assertEquals;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

/**
 * {@link ApplicationEventInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationEventInterceptor
 * @since 1.0.0
 */
public class ApplicationEventInterceptorTest {

    private static final Logger logger = getLogger(ApplicationEventInterceptorTest.class);

    private ApplicationEventInterceptor interceptor;

    @Before
    public void before() {
        this.interceptor = (event, eventType, chain) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("event : {} , eventType : {} , chain : {}", event, eventType, chain);
            }
        };
    }

    @Test
    public void testIntercept() {
        this.interceptor.intercept(null, null, null);
    }

    @Test
    public void testGetOrder() {
        assertEquals(LOWEST_PRECEDENCE, this.interceptor.getOrder());
    }
}