package io.microsphere.spring.context.event;

import io.microsphere.logging.Logger;
import io.microsphere.logging.test.junit4.LoggingLevelsRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.logging.test.junit4.LoggingLevelsRule.levels;
import static org.junit.Assert.assertEquals;
import static org.springframework.core.Ordered.LOWEST_PRECEDENCE;

/**
 * {@link ApplicationEventInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationEventInterceptor
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class ApplicationEventInterceptorTest {

    @ClassRule
    public static final LoggingLevelsRule LOGGING_LEVELS_RULE = levels("TRACE", "INFO", "ERROR");


    private static final Logger logger = getLogger(ApplicationEventInterceptorTest.class);

    private ApplicationEventInterceptor interceptor;

    @Before
    public void setUp() {
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