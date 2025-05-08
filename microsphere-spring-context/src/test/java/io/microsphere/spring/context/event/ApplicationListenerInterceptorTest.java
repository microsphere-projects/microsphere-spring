package io.microsphere.spring.context.event;

import io.microsphere.logging.Logger;
import org.junit.Before;
import org.junit.Test;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static org.junit.Assert.assertEquals;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

/**
 * {@link ApplicationListenerInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationListenerInterceptor
 * @since 1.0.0
 */
public class ApplicationListenerInterceptorTest {

    private static final Logger logger = getLogger(ApplicationListenerInterceptorTest.class);

    private ApplicationListenerInterceptor interceptor;

    @Before
    public void before() {
        this.interceptor = (applicationListener, event, chain) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("applicationListener : {} , event : {} , chain : {}", applicationListener, event, chain);
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