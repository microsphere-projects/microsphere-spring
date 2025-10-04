package io.microsphere.spring.context.event;

import io.microsphere.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

/**
 * {@link ApplicationListenerInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationListenerInterceptor
 * @since 1.0.0
 */
class ApplicationListenerInterceptorTest {

    private static final Logger logger = getLogger(ApplicationListenerInterceptorTest.class);

    private ApplicationListenerInterceptor interceptor;

    @BeforeEach
    void setUp() {
        this.interceptor = (applicationListener, event, chain) -> {
            if (logger.isTraceEnabled()) {
                logger.trace("applicationListener : {} , event : {} , chain : {}", applicationListener, event, chain);
            }
        };
    }

    @Test
    void testIntercept() {
        this.interceptor.intercept(null, null, null);
    }

    @Test
    void testGetOrder() {
        assertEquals(LOWEST_PRECEDENCE, this.interceptor.getOrder());
    }
}