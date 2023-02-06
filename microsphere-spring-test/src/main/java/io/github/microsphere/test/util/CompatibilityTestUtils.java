package io.github.microsphere.test.util;

import org.slf4j.Logger;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Compatibility test tool
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public class CompatibilityTestUtils {

    public static final Predicate<Method> PUBLIC_METHOD_FILTER = method -> Modifier.isPublic(method.getModifiers())
            && !Object.class.equals(method.getDeclaringClass());

    public static final Predicate<Field> PUBLIC_STATIC_FIELD_FILTER = field -> {
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers) &&
                Modifier.isPublic(modifiers) &&
                !field.getType().equals(Logger.class) // Excluding log objects
                ;
    };

    /**
     * Test method compatibility
     *
     * @param originalClass Original defined class
     * @param testedClass   Class under test
     * @param methodFilter  Method filter
     */
    public static void testCompatibilityOnMethods(Class<?> originalClass, Class<?> testedClass, Predicate<Method> methodFilter) {
        ReflectionUtils.doWithMethods(originalClass, method -> {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();
            Class<?>[] parameterTypes = method.getParameterTypes();
            Method targetMethod = ReflectionUtils.findMethod(testedClass, methodName, parameterTypes);
            assertNotNull(String.format("Method [Name: %s, parameter: %s] is not defined in target class [%s]!", methodName, Arrays.asList(parameterTypes), testedClass.getName()), targetMethod);
            assertTrue(String.format("The return type of the original method [Name: %s, parameter: %s] is not compatible with the target method [%s]!", methodName, Arrays.asList(parameterTypes), method),
                    targetMethod.getReturnType().isAssignableFrom(returnType));
        }, method -> methodFilter == null ? true : methodFilter.test(method));
    }

    /**
     * Testing field Compatibility
     *
     * @param originalClass Original defined class
     * @param testedClass   Class under test
     */
    public static void testCompatibilityOnFields(Class<?> originalClass, Class<?> testedClass) {
        testCompatibilityOnFields(originalClass, testedClass, null);
    }

    /**
     * Testing field Compatibility
     *
     * @param originalClass Original defined class
     * @param testedClass   Class under test
     * @param fieldFilter   Field filter
     */
    public static void testCompatibilityOnFields(Class<?> originalClass, Class<?> testedClass, Predicate<Field> fieldFilter) {
        List<String> errorMessages = new LinkedList<>();
        ReflectionUtils.doWithFields(originalClass, field -> {
            String fieldName = field.getName();
            Class<?> fieldType = field.getType();
            Field targetField = ReflectionUtils.findField(testedClass, fieldName, fieldType);
            if (targetField == null) {
                errorMessages.add(String.format("Field [Name: %s, Type: %s] not defined in target class [%s]!",
                        fieldName, fieldType.getName(), testedClass.getName()));
                return;
            }


            if (Modifier.isStatic(field.getModifiers())) {
                Object fieldValue = field.get(null);
                Object targetValue = targetField.get(null);
                if (!Objects.equals(fieldValue, targetValue)) {
                    errorMessages.add(originalClass.getName() + " , " + fieldName + " = " + fieldValue);
                }
            }

        }, field -> fieldFilter == null ? true : fieldFilter.test(field));

        errorMessages.forEach(System.err::println);

    }
}
