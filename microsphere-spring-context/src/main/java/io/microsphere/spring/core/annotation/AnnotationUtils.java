package io.microsphere.spring.core.annotation;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.type.AnnotationMetadata;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.valueOf;
import static java.util.Arrays.asList;
import static org.springframework.core.annotation.AnnotatedElementUtils.getMergedAnnotationAttributes;
import static org.springframework.core.annotation.AnnotationAttributes.fromMap;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.getDefaultValue;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.ObjectUtils.nullSafeEquals;
import static org.springframework.util.StringUtils.trimWhitespace;

/**
 * {@link Annotation} Utilities
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Annotation
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public abstract class AnnotationUtils {

    /**
     * Is specified {@link Annotation} present on {@link Method}'s declaring class or parameters or itself.
     *
     * @param method          {@link Method}
     * @param annotationClass {@link Annotation} type
     * @param <A>             {@link Annotation} type
     * @return If present , return <code>true</code> , or <code>false</code>
     */
    public static <A extends Annotation> boolean isPresent(Method method, Class<A> annotationClass) {

        Map<ElementType, List<A>> annotationsMap = findAnnotations(method, annotationClass);

        return !annotationsMap.isEmpty();

    }

    /**
     * Find specified {@link Annotation} type maps from {@link Method}
     *
     * @param method          {@link Method}
     * @param annotationClass {@link Annotation} type
     * @param <A>             {@link Annotation} type
     * @return {@link Annotation} type maps , the {@link ElementType} as key ,
     * the list of {@link Annotation} as value.
     * If {@link Annotation} was annotated on {@link Method}'s parameters{@link ElementType#PARAMETER} ,
     * the associated {@link Annotation} list may contain multiple elements.
     */
    public static <A extends Annotation> Map<ElementType, List<A>> findAnnotations(Method method,
                                                                                   Class<A> annotationClass) {

        Retention retention = annotationClass.getAnnotation(Retention.class);

        RetentionPolicy retentionPolicy = retention.value();

        if (!RetentionPolicy.RUNTIME.equals(retentionPolicy)) {
            return Collections.emptyMap();
        }

        Map<ElementType, List<A>> annotationsMap = new LinkedHashMap<ElementType, List<A>>();

        Target target = annotationClass.getAnnotation(Target.class);

        ElementType[] elementTypes = target.value();


        for (ElementType elementType : elementTypes) {

            List<A> annotationsList = new LinkedList<A>();

            switch (elementType) {
                case PARAMETER:
                    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
                    for (Annotation[] annotations : parameterAnnotations) {
                        for (Annotation annotation : annotations) {
                            if (annotationClass.equals(annotation.annotationType())) {
                                annotationsList.add((A) annotation);
                            }
                        }
                    }
                    break;

                case METHOD:
                    A annotation = findAnnotation(method, annotationClass);
                    if (annotation != null) {
                        annotationsList.add(annotation);
                    }
                    break;

                case TYPE:
                    Class<?> beanType = method.getDeclaringClass();
                    A annotation2 = findAnnotation(beanType, annotationClass);
                    if (annotation2 != null) {
                        annotationsList.add(annotation2);
                    }
                    break;

            }

            if (!annotationsList.isEmpty()) {
                annotationsMap.put(elementType, annotationsList);
            }
        }

        return Collections.unmodifiableMap(annotationsMap);

    }

    /**
     * Get the {@link Annotation} attributes
     *
     * @param annotation           specified {@link Annotation}
     * @param ignoreDefaultValue   whether ignore default value or not
     * @param ignoreAttributeNames the attribute names of annotation should be ignored
     * @return non-null
     */
    public static Map<String, Object> getAttributes(Annotation annotation, boolean ignoreDefaultValue,
                                                    String... ignoreAttributeNames) {
        return getAttributes(annotation, null, ignoreDefaultValue, ignoreAttributeNames);
    }

    /**
     * Get the {@link Annotation} attributes
     *
     * @param annotation           specified {@link Annotation}
     * @param propertyResolver     {@link PropertyResolver} instance, e.g {@link Environment}
     * @param ignoreDefaultValue   whether ignore default value or not
     * @param ignoreAttributeNames the attribute names of annotation should be ignored
     * @return non-null
     */
    public static Map<String, Object> getAttributes(Annotation annotation, PropertyResolver propertyResolver,
                                                    boolean ignoreDefaultValue, String... ignoreAttributeNames) {
        return getAttributes(annotation, propertyResolver, false, false, ignoreDefaultValue, ignoreAttributeNames);
    }

    /**
     * Get the {@link Annotation} attributes
     *
     * @param annotationAttributes the attributes of specified {@link Annotation}
     * @param propertyResolver     {@link PropertyResolver} instance, e.g {@link Environment}
     * @param ignoreAttributeNames the attribute names of annotation should be ignored
     * @return non-null
     */
    public static Map<String, Object> getAttributes(Map<String, Object> annotationAttributes,
                                                    PropertyResolver propertyResolver, String... ignoreAttributeNames) {

        Set<String> ignoreAttributeNamesSet = new HashSet<>(Arrays.asList(ignoreAttributeNames));

        Map<String, Object> actualAttributes = new LinkedHashMap<>();

        for (Map.Entry<String, Object> annotationAttribute : annotationAttributes.entrySet()) {

            String attributeName = annotationAttribute.getKey();
            Object attributeValue = annotationAttribute.getValue();

            // ignore attribute name
            if (ignoreAttributeNamesSet.contains(attributeName)) {
                continue;
            }

            if (attributeValue instanceof String) {
                attributeValue = resolvePlaceholders(valueOf(attributeValue), propertyResolver);
            } else if (attributeValue instanceof String[]) {
                String[] values = (String[]) attributeValue;
                for (int i = 0; i < values.length; i++) {
                    values[i] = resolvePlaceholders(values[i], propertyResolver);
                }
                attributeValue = values;
            }
            actualAttributes.put(attributeName, attributeValue);
        }
        return actualAttributes;
    }

    /**
     * @param annotation             specified {@link Annotation}
     * @param propertyResolver       {@link PropertyResolver} instance, e.g {@link Environment}
     * @param classValuesAsString    whether to turn Class references into Strings (for
     *                               compatibility with {@link org.springframework.core.type.AnnotationMetadata} or to
     *                               preserve them as Class references
     * @param nestedAnnotationsAsMap whether to turn nested Annotation instances into
     *                               {@link AnnotationAttributes} maps (for compatibility with
     *                               {@link org.springframework.core.type.AnnotationMetadata} or to preserve them as
     *                               Annotation instances
     * @param ignoreDefaultValue     whether ignore default value or not
     * @param ignoreAttributeNames   the attribute names of annotation should be ignored
     * @return non-null
     */
    public static Map<String, Object> getAttributes(Annotation annotation,
                                                    PropertyResolver propertyResolver,
                                                    boolean classValuesAsString,
                                                    boolean nestedAnnotationsAsMap,
                                                    boolean ignoreDefaultValue,
                                                    String... ignoreAttributeNames) {

        Map<String, Object> annotationAttributes = org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes(annotation, classValuesAsString, nestedAnnotationsAsMap);

        String[] actualIgnoreAttributeNames = ignoreAttributeNames;

        if (ignoreDefaultValue && !isEmpty(annotationAttributes)) {

            List<String> attributeNamesToIgnore = new LinkedList<>(asList(ignoreAttributeNames));

            for (Map.Entry<String, Object> annotationAttribute : annotationAttributes.entrySet()) {
                String attributeName = annotationAttribute.getKey();
                Object attributeValue = annotationAttribute.getValue();
                if (nullSafeEquals(attributeValue, getDefaultValue(annotation, attributeName))) {
                    attributeNamesToIgnore.add(attributeName);
                }
            }
            // extends the ignored list
            actualIgnoreAttributeNames = attributeNamesToIgnore.toArray(new String[0]);
        }

        return getAttributes(annotationAttributes, propertyResolver, actualIgnoreAttributeNames);
    }

    private static String resolvePlaceholders(String attributeValue, PropertyResolver propertyResolver) {
        String resolvedValue = attributeValue;
        if (propertyResolver != null) {
            resolvedValue = propertyResolver.resolvePlaceholders(resolvedValue);
            resolvedValue = trimWhitespace(resolvedValue);
        }
        return resolvedValue;
    }

    /**
     * Get the attribute value
     *
     * @param annotation    {@link Annotation annotation}
     * @param attributeName the name of attribute
     * @param <T>           the type of attribute value
     * @return the attribute value if found
     */
    public static <T> T getAttribute(Annotation annotation, String attributeName) {
        return getAttribute(org.springframework.core.annotation.AnnotationUtils.getAnnotationAttributes(annotation), attributeName);
    }

    /**
     * Get the attribute value
     *
     * @param attributes    {@link Map the annotation attributes} or {@link AnnotationAttributes}
     * @param attributeName the name of attribute
     * @param <T>           the type of attribute value
     * @return the attribute value if found
     */
    public static <T> T getAttribute(Map<String, Object> attributes, String attributeName) {
        return getAttribute(attributes, attributeName, false);
    }

    /**
     * Get the attribute value the will
     *
     * @param attributes    {@link Map the annotation attributes} or {@link AnnotationAttributes}
     * @param attributeName the name of attribute
     * @param required      the required attribute or not
     * @param <T>           the type of attribute value
     * @return the attribute value if found
     * @throws IllegalStateException if attribute value can't be found
     */
    public static <T> T getAttribute(Map<String, Object> attributes, String attributeName, boolean required) {
        T value = getAttribute(attributes, attributeName, null);
        if (required && value == null) {
            throw new IllegalStateException("The attribute['" + attributeName + "] is required!");
        }
        return value;
    }

    /**
     * Get the attribute value with default value
     *
     * @param attributes    {@link Map the annotation attributes} or {@link AnnotationAttributes}
     * @param attributeName the name of attribute
     * @param defaultValue  the default value of attribute
     * @param <T>           the type of attribute value
     * @return the attribute value if found
     */
    public static <T> T getAttribute(Map<String, Object> attributes, String attributeName, T defaultValue) {
        T value = (T) attributes.get(attributeName);
        return value == null ? defaultValue : value;
    }

    /**
     * Get the required attribute value
     *
     * @param attributes    {@link Map the annotation attributes} or {@link AnnotationAttributes}
     * @param attributeName the name of attribute
     * @param <T>           the type of attribute value
     * @return the attribute value if found
     * @throws IllegalStateException if attribute value can't be found
     */
    public static <T> T getRequiredAttribute(Map<String, Object> attributes, String attributeName) {
        return getAttribute(attributes, attributeName, true);
    }

    /**
     * Get the {@link AnnotationAttributes}
     *
     * @param annotation           specified {@link Annotation}
     * @param ignoreDefaultValue   whether ignore default value or not
     * @param ignoreAttributeNames the attribute names of annotation should be ignored
     * @return non-null
     * @see #getAnnotationAttributes(Annotation, PropertyResolver, boolean, String...)
     */
    public static AnnotationAttributes getAnnotationAttributes(Annotation annotation, boolean ignoreDefaultValue,
                                                               String... ignoreAttributeNames) {
        return getAnnotationAttributes(annotation, null, ignoreDefaultValue, ignoreAttributeNames);
    }

    /**
     * Get the {@link AnnotationAttributes}
     *
     * @param annotation             specified {@link Annotation}
     * @param propertyResolver       {@link PropertyResolver} instance, e.g {@link Environment}
     * @param classValuesAsString    whether to turn Class references into Strings (for
     *                               compatibility with {@link org.springframework.core.type.AnnotationMetadata} or to
     *                               preserve them as Class references
     * @param nestedAnnotationsAsMap whether to turn nested Annotation instances into
     *                               {@link AnnotationAttributes} maps (for compatibility with
     *                               {@link org.springframework.core.type.AnnotationMetadata} or to preserve them as
     *                               Annotation instances
     * @param ignoreAttributeNames   the attribute names of annotation should be ignored
     * @param ignoreDefaultValue     whether ignore default value or not
     * @return non-null
     * @see #getAttributes(Annotation, PropertyResolver, boolean, String...)
     * @see #getAnnotationAttributes(AnnotatedElement, Class, PropertyResolver, boolean, String...)
     */
    public static AnnotationAttributes getAnnotationAttributes(Annotation annotation,
                                                               PropertyResolver propertyResolver,
                                                               boolean classValuesAsString,
                                                               boolean nestedAnnotationsAsMap,
                                                               boolean ignoreDefaultValue,
                                                               String... ignoreAttributeNames) {
        return fromMap(getAttributes(annotation, propertyResolver, classValuesAsString, nestedAnnotationsAsMap,
                ignoreDefaultValue, ignoreAttributeNames));
    }

    /**
     * Get the {@link AnnotationAttributes}
     *
     * @param annotation           specified {@link Annotation}
     * @param propertyResolver     {@link PropertyResolver} instance, e.g {@link Environment}
     * @param ignoreDefaultValue   whether ignore default value or not
     * @param ignoreAttributeNames the attribute names of annotation should be ignored
     * @return non-null
     * @see #getAttributes(Annotation, PropertyResolver, boolean, String...)
     * @see #getAnnotationAttributes(AnnotatedElement, Class, PropertyResolver, boolean, String...)
     */
    public static AnnotationAttributes getAnnotationAttributes(Annotation annotation, PropertyResolver propertyResolver,
                                                               boolean ignoreDefaultValue, String... ignoreAttributeNames) {
        return getAnnotationAttributes(annotation, propertyResolver, false, false, ignoreDefaultValue, ignoreAttributeNames);
    }

    /**
     * Get the {@link AnnotationAttributes}
     *
     * @param annotatedElement     {@link AnnotatedElement the annotated element}
     * @param annotationType       the {@link Class tyoe} pf {@link Annotation annotation}
     * @param propertyResolver     {@link PropertyResolver} instance, e.g {@link Environment}
     * @param ignoreDefaultValue   whether ignore default value or not
     * @param ignoreAttributeNames the attribute names of annotation should be ignored
     * @return if <code>annotatedElement</code> can't be found in <code>annotatedElement</code>, return <code>null</code>
     */
    public static AnnotationAttributes getAnnotationAttributes(AnnotatedElement annotatedElement,
                                                               Class<? extends Annotation> annotationType,
                                                               PropertyResolver propertyResolver,
                                                               boolean ignoreDefaultValue,
                                                               String... ignoreAttributeNames) {
        return getAnnotationAttributes(annotatedElement, annotationType, propertyResolver,
                false, false, ignoreDefaultValue, ignoreAttributeNames);
    }

    /**
     * Get the {@link AnnotationAttributes}
     *
     * @param annotatedElement     {@link AnnotatedElement the annotated element}
     * @param annotationType       the {@link Class tyoe} pf {@link Annotation annotation}
     * @param propertyResolver     {@link PropertyResolver} instance, e.g {@link Environment}
     * @param ignoreDefaultValue   whether ignore default value or not
     * @param ignoreAttributeNames the attribute names of annotation should be ignored
     * @return if <code>annotatedElement</code> can't be found in <code>annotatedElement</code>, return <code>null</code>
     */
    public static AnnotationAttributes getAnnotationAttributes(AnnotatedElement annotatedElement,
                                                               Class<? extends Annotation> annotationType,
                                                               PropertyResolver propertyResolver,
                                                               boolean classValuesAsString,
                                                               boolean nestedAnnotationsAsMap,
                                                               boolean ignoreDefaultValue,
                                                               String... ignoreAttributeNames) {
        Annotation annotation = annotatedElement.getAnnotation(annotationType);
        return annotation == null ? null : getAnnotationAttributes(annotation, propertyResolver,
                classValuesAsString, nestedAnnotationsAsMap, ignoreDefaultValue, ignoreAttributeNames);
    }

    /**
     * Get the {@link AnnotationAttributes}, if the argument <code>tryMergedAnnotation</code> is <code>true</code>,
     * the {@link AnnotationAttributes} will be got from
     * {@link #tryGetMergedAnnotationAttributes(AnnotatedElement, Class, PropertyResolver, boolean, String...) merged annotation} first,
     * if failed, and then to get from
     * {@link #getAnnotationAttributes(AnnotatedElement, Class, PropertyResolver, boolean, boolean, String...) normal one}
     *
     * @param annotatedElement     {@link AnnotatedElement the annotated element}
     * @param annotationType       the {@link Class tyoe} pf {@link Annotation annotation}
     * @param propertyResolver     {@link PropertyResolver} instance, e.g {@link Environment}
     * @param ignoreDefaultValue   whether ignore default value or not
     * @param tryMergedAnnotation  whether try merged annotation or not
     * @param ignoreAttributeNames the attribute names of annotation should be ignored
     * @return if <code>annotatedElement</code> can't be found in <code>annotatedElement</code>, return <code>null</code>
     */
    public static AnnotationAttributes getAnnotationAttributes(AnnotatedElement annotatedElement,
                                                               Class<? extends Annotation> annotationType,
                                                               PropertyResolver propertyResolver,
                                                               boolean ignoreDefaultValue,
                                                               boolean tryMergedAnnotation,
                                                               String... ignoreAttributeNames) {
        return getAnnotationAttributes(annotatedElement, annotationType, propertyResolver,
                false, false, ignoreDefaultValue, tryMergedAnnotation, ignoreAttributeNames);
    }

    /**
     * Get the {@link AnnotationAttributes}, if the argument <code>tryMergedAnnotation</code> is <code>true</code>,
     * the {@link AnnotationAttributes} will be got from
     * {@link #tryGetMergedAnnotationAttributes(AnnotatedElement, Class, PropertyResolver, boolean, String...) merged annotation} first,
     * if failed, and then to get from
     * {@link #getAnnotationAttributes(AnnotatedElement, Class, PropertyResolver, boolean, boolean, String...) normal one}
     *
     * @param annotatedElement       {@link AnnotatedElement the annotated element}
     * @param annotationType         the {@link Class tyoe} pf {@link Annotation annotation}
     * @param propertyResolver       {@link PropertyResolver} instance, e.g {@link Environment}
     * @param classValuesAsString    whether to turn Class references into Strings (for
     *                               compatibility with {@link org.springframework.core.type.AnnotationMetadata} or to
     *                               preserve them as Class references
     * @param nestedAnnotationsAsMap whether to turn nested Annotation instances into
     *                               {@link AnnotationAttributes} maps (for compatibility with
     *                               {@link org.springframework.core.type.AnnotationMetadata} or to preserve them as
     *                               Annotation instances
     * @param ignoreDefaultValue     whether ignore default value or not
     * @param tryMergedAnnotation    whether try merged annotation or not
     * @param ignoreAttributeNames   the attribute names of annotation should be ignored
     * @return if <code>annotatedElement</code> can't be found in <code>annotatedElement</code>, return <code>null</code>
     */
    public static AnnotationAttributes getAnnotationAttributes(AnnotatedElement annotatedElement,
                                                               Class<? extends Annotation> annotationType,
                                                               PropertyResolver propertyResolver,
                                                               boolean classValuesAsString,
                                                               boolean nestedAnnotationsAsMap,
                                                               boolean ignoreDefaultValue,
                                                               boolean tryMergedAnnotation,
                                                               String... ignoreAttributeNames) {

        AnnotationAttributes attributes = null;

        if (tryMergedAnnotation) {
            attributes = tryGetMergedAnnotationAttributes(annotatedElement, annotationType, propertyResolver,
                    classValuesAsString, nestedAnnotationsAsMap, ignoreDefaultValue, ignoreAttributeNames);
        }

        if (attributes == null) {
            attributes = getAnnotationAttributes(annotatedElement, annotationType, propertyResolver,
                    classValuesAsString, nestedAnnotationsAsMap, ignoreDefaultValue, ignoreAttributeNames);
        }

        return attributes;
    }

    /**
     * Try to get the merged {@link Annotation annotation}
     *
     * @param annotatedElement {@link AnnotatedElement the annotated element}
     * @param annotationType   the {@link Class tyoe} pf {@link Annotation annotation}
     * @return <code>null</code> If not found
     */
    @Nullable
    public static Annotation tryGetMergedAnnotation(AnnotatedElement annotatedElement,
                                                    Class<? extends Annotation> annotationType) {
        return tryGetMergedAnnotation(annotatedElement, annotationType, false, false);
    }

    /**
     * Try to get the merged {@link Annotation annotation}
     *
     * @param annotatedElement       {@link AnnotatedElement the annotated element}
     * @param annotationType         the {@link Class tyoe} pf {@link Annotation annotation}
     * @param classValuesAsString    whether to turn Class references into Strings (for
     *                               compatibility with {@link org.springframework.core.type.AnnotationMetadata} or to
     *                               preserve them as Class references
     * @param nestedAnnotationsAsMap whether to turn nested Annotation instances into
     *                               {@link AnnotationAttributes} maps (for compatibility with
     *                               {@link org.springframework.core.type.AnnotationMetadata} or to preserve them as
     *                               Annotation instances
     * @return <code>null</code> If not found
     */
    @Nullable
    public static Annotation tryGetMergedAnnotation(AnnotatedElement annotatedElement,
                                                    Class<? extends Annotation> annotationType,
                                                    boolean classValuesAsString,
                                                    boolean nestedAnnotationsAsMap) {
        AnnotationAttributes annotationAttributes = getMergedAnnotationAttributes(annotatedElement,
                annotationType.getName(), classValuesAsString, nestedAnnotationsAsMap);
        return annotationAttributes == null ? null : synthesizeAnnotation(annotationAttributes, annotationType, annotatedElement);
    }

    /**
     * Try to get {@link AnnotationAttributes the annotation attributes} after merging and resolving the placeholders
     *
     * @param annotatedElement     {@link AnnotatedElement the annotated element}
     * @param annotationType       the {@link Class tyoe} pf {@link Annotation annotation}
     * @param propertyResolver     {@link PropertyResolver} instance, e.g {@link Environment}
     * @param ignoreDefaultValue   whether ignore default value or not
     * @param ignoreAttributeNames the attribute names of annotation should be ignored
     * @return If the specified annotation type is not found, return <code>null</code>
     */
    public static AnnotationAttributes tryGetMergedAnnotationAttributes(AnnotatedElement annotatedElement,
                                                                        Class<? extends Annotation> annotationType,
                                                                        PropertyResolver propertyResolver,
                                                                        boolean ignoreDefaultValue,
                                                                        String... ignoreAttributeNames) {
        return tryGetMergedAnnotationAttributes(annotatedElement, annotationType, propertyResolver,
                false, false, ignoreDefaultValue, ignoreAttributeNames);
    }

    /**
     * Try to get {@link AnnotationAttributes the annotation attributes} after merging and resolving the placeholders
     *
     * @param annotatedElement       {@link AnnotatedElement the annotated element}
     * @param annotationType         the {@link Class tyoe} pf {@link Annotation annotation}
     * @param propertyResolver       {@link PropertyResolver} instance, e.g {@link Environment}
     * @param classValuesAsString    whether to turn Class references into Strings (for
     *                               compatibility with {@link org.springframework.core.type.AnnotationMetadata} or to
     *                               preserve them as Class references
     * @param nestedAnnotationsAsMap whether to turn nested Annotation instances into
     *                               {@link AnnotationAttributes} maps (for compatibility with
     *                               {@link org.springframework.core.type.AnnotationMetadata} or to preserve them as
     *                               Annotation instances
     * @param ignoreDefaultValue     whether ignore default value or not
     * @param ignoreAttributeNames   the attribute names of annotation should be ignored
     * @return If the specified annotation type is not found, return <code>null</code>
     */
    public static AnnotationAttributes tryGetMergedAnnotationAttributes(AnnotatedElement annotatedElement,
                                                                        Class<? extends Annotation> annotationType,
                                                                        PropertyResolver propertyResolver,
                                                                        boolean classValuesAsString,
                                                                        boolean nestedAnnotationsAsMap,
                                                                        boolean ignoreDefaultValue,
                                                                        String... ignoreAttributeNames) {
        Annotation annotation = tryGetMergedAnnotation(annotatedElement, annotationType, classValuesAsString, nestedAnnotationsAsMap);
        return annotation == null ? null : getAnnotationAttributes(annotation, propertyResolver,
                classValuesAsString, nestedAnnotationsAsMap, ignoreDefaultValue, ignoreAttributeNames);
    }

    /**
     * Get an instance of {@link AnnotationAttributes} from {@link AnnotationMetadata} with the specified class name of
     * {@link Annotation annotation}.
     *
     * @param metadata       {@link AnnotationMetadata}
     * @param annotationType The {@link Class class}  of {@link Annotation annotation}
     * @return non-null
     */
    public static AnnotationAttributes getAnnotationAttributes(AnnotationMetadata metadata, Class<? extends Annotation> annotationType) {
        return getAnnotationAttributes(metadata, annotationType.getName());
    }

    /**
     * Get an instance of {@link AnnotationAttributes} from {@link AnnotationMetadata} with the specified class name of
     * {@link Annotation annotation}.
     *
     * @param metadata            {@link AnnotationMetadata}
     * @param annotationClassName The class name of {@link Annotation annotation}
     * @return non-null
     */
    public static AnnotationAttributes getAnnotationAttributes(AnnotationMetadata metadata, String annotationClassName) {
        return fromMap(metadata.getAnnotationAttributes(annotationClassName));
    }

    /**
     * Find the {@link Class class} of {@link Annotation} from the specified {@link AnnotationAttributes}
     *
     * @param annotationAttributes {@link AnnotationAttributes}
     * @param <A>                  The {@link Class class} of {@link Annotation}
     * @return <code>null</code> if not found
     */
    @Nullable
    public static <A extends Annotation> Class<A> findAnnotationType(AnnotationAttributes annotationAttributes) {
        if (annotationAttributes == null) {
            return null;
        }
        return (Class<A>) annotationAttributes.annotationType();
    }

}
