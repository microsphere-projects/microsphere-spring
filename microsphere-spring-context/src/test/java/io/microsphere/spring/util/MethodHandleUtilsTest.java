package io.microsphere.spring.util;

import org.junit.Test;

import java.lang.invoke.MethodHandle;

import static io.microsphere.invoke.MethodHandleUtils.findVirtual;
import static io.microsphere.spring.util.MethodHandleUtils.handleInvokeExactFailure;

/**
 * {@link MethodHandleUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MethodHandleUtils
 * @since 1.0.0
 */
public class MethodHandleUtilsTest {

    private static final MethodHandle methodHandle = findVirtual(MethodHandleUtilsTest.class, "testHandleInvokeExactFailure");

    @Test
    public void testHandleInvokeExactFailure() {
        handleInvokeExactFailure(new RuntimeException("For testing..."), methodHandle, "testing");
    }
}