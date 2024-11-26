package io.microsphere.spring.webmvc.config;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.http.MediaType;
import org.springframework.util.ReflectionUtils;
import org.springframework.validation.DataBinder;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ContentNegotiationManagerFactoryBean;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.beans.PropertyEditorSupport;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Configurable {@link ContentNegotiationManager} {@link WebMvcConfigurer}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ContentNegotiationManager
 * @see WebMvcConfigurer
 * @since 2017.03.23
 */
public class ConfigurableContentNegotiationManagerWebMvcConfigurer extends WebMvcConfigurerAdapter {

    /**
     * Property separator : "."
     */
    private static final String PROPERTY_SEPARATOR = ".";

    private static final Class<ContentNegotiationManagerFactoryBean> FACTORY_BEAN_FIELD_CLASS =
            ContentNegotiationManagerFactoryBean.class;

    private final Map<String, Object> propertyValues;

    public ConfigurableContentNegotiationManagerWebMvcConfigurer(Map<String, String> properties) {
        this.propertyValues = resolveNestedMap(properties);
    }

    public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {

        ReflectionUtils.doWithFields(configurer.getClass(), new ReflectionUtils.FieldCallback() {
            @Override
            public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {

                boolean accessible = field.isAccessible();

                try {

                    if (!accessible) {
                        field.setAccessible(true);
                    }

                    ContentNegotiationManagerFactoryBean factoryBean = (ContentNegotiationManagerFactoryBean) field.get(configurer);

                    configureContentNegotiationManagerFactoryBean(factoryBean);

                } finally {

                    if (!accessible) {
                        field.setAccessible(accessible);
                    }

                }

            }
        }, new ReflectionUtils.FieldFilter() {
            @Override
            public boolean matches(Field field) {
                Class<?> fieldType = field.getType();
                return FACTORY_BEAN_FIELD_CLASS.isAssignableFrom(fieldType);
            }
        });

    }


    protected void configureContentNegotiationManagerFactoryBean(ContentNegotiationManagerFactoryBean factoryBean) {

        DataBinder dataBinder = new DataBinder(factoryBean);

        dataBinder.setDisallowedFields("contentNegotiationManager", "servletContext");

        dataBinder.setAutoGrowNestedPaths(true);

        dataBinder.registerCustomEditor(MediaType.class, "defaultContentType", new MediaTypePropertyEditor());

        MutablePropertyValues propertyValues = new MutablePropertyValues();

        propertyValues.addPropertyValues(this.propertyValues);

        dataBinder.bind(propertyValues);

    }


    private static class MediaTypePropertyEditor extends PropertyEditorSupport {

        @Override
        public void setAsText(String text) {

            MediaType mediaType = MediaType.valueOf(text);

            setValue(mediaType);

        }
    }

    private static Map<String, String> extraProperties(Map<String, Object> map) {

        Map<String, String> properties = new LinkedHashMap<String, String>();

        if (map != null) {

            for (Map.Entry<String, Object> entry : map.entrySet()) {

                String key = entry.getKey();

                Object value = entry.getValue();

                if (value instanceof Map) {
                    Map<String, String> subProperties = extraProperties((Map) value);
                    for (Map.Entry<String, String> e : subProperties.entrySet()) {
                        String subKey = e.getKey();
                        String subValue = e.getValue();
                        properties.put(key + PROPERTY_SEPARATOR + subKey, subValue);
                    }
                } else if (value instanceof String) {

                    properties.put(key, value.toString());

                }

            }

        }

        return properties;

    }

    /**
     * <code>
     * properties.put("a.b.1", "1");
     * properties.put("a.b.2", "2");
     * properties.put("d.e.f.1", "1");
     * properties.put("d.e.f.2", "2");
     * properties.put("d.e.f.3", "3");
     * </code>
     * resolved result :
     * <code>
     * {a={b={1=1, 2=2}}, d={e={f={1=1, 2=2, 3=3}}}}
     * </code>
     *
     * @param properties Properties
     * @return Resolved properties
     */
    public static Map<String, Object> resolveNestedMap(Map<String, String> properties) {

        Map<String, Object> nestedMap = new LinkedHashMap<String, Object>();

        for (Map.Entry<String, String> entry : properties.entrySet()) {

            String propertyName = entry.getKey();

            String propertyValue = entry.getValue();

            int index = propertyName.indexOf(PROPERTY_SEPARATOR);

            if (index > 0) {

                String actualPropertyName = propertyName.substring(0, index);

                String subPropertyName = propertyName.substring(index + 1, propertyName.length());

                Object actualPropertyValue = nestedMap.get(actualPropertyName);

                if (actualPropertyValue == null) {

                    actualPropertyValue = new LinkedHashMap<String, Object>();

                    nestedMap.put(actualPropertyName, actualPropertyValue);

                }

                if (actualPropertyValue instanceof Map) {

                    Map<String, Object> nestedProperties = (Map<String, Object>) actualPropertyValue;

                    Map<String, String> subProperties = extraProperties(nestedProperties);

                    subProperties.put(subPropertyName, propertyValue);

                    Map<String, Object> subNestedMap = resolveNestedMap(subProperties);

                    nestedProperties.putAll(subNestedMap);


                }
            } else {

                nestedMap.put(propertyName, propertyValue);

            }
        }


        return nestedMap;

    }

}
