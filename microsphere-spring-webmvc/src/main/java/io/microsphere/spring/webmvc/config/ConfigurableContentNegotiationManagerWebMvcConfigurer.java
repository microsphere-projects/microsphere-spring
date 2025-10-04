package io.microsphere.spring.webmvc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.validation.DataBinder;
import org.springframework.web.accept.ContentNegotiationManager;
import org.springframework.web.accept.ContentNegotiationManagerFactoryBean;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.beans.PropertyEditorSupport;
import java.util.Map;
import java.util.Properties;

import static io.microsphere.constants.SymbolConstants.LEFT_CURLY_BRACE_CHAR;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.spring.core.env.EnvironmentUtils.asConfigurableEnvironment;
import static io.microsphere.spring.core.env.PropertySourcesUtils.getSubProperties;
import static io.microsphere.spring.webmvc.constants.PropertyConstants.MICROSPHERE_SPRING_WEBMVC_PROPERTY_NAME_PREFIX;
import static org.springframework.util.ReflectionUtils.doWithFields;


/**
 * Configurable {@link ContentNegotiationManager} {@link WebMvcConfigurer}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ContentNegotiationManagerFactoryBean
 * @see ContentNegotiationManager
 * @see WebMvcConfigurer
 * @see DataBinder
 * @since 1.0.0
 */
public class ConfigurableContentNegotiationManagerWebMvcConfigurer implements WebMvcConfigurer, EnvironmentAware {

    /**
     * The property name prefix of {@link ContentNegotiationManager} : "microsphere.spring.webmvc.content-negotiation."
     */
    static final String PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_WEBMVC_PROPERTY_NAME_PREFIX + "content-negotiation.";

    static final Class<ContentNegotiationManagerFactoryBean> FACTORY_BEAN_FIELD_CLASS =
            ContentNegotiationManagerFactoryBean.class;

    private Map<String, Object> propertyValues;

    public void configureContentNegotiation(final ContentNegotiationConfigurer configurer) {
        doWithFields(configurer.getClass(), field -> {
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
        }, field -> {
            Class<?> fieldType = field.getType();
            return FACTORY_BEAN_FIELD_CLASS.isAssignableFrom(fieldType);
        });
    }

    @Override
    public void setEnvironment(Environment environment) {
        ConfigurableEnvironment configurableEnvironment = asConfigurableEnvironment(environment);
        Map<String, Object> properties = getSubProperties(configurableEnvironment, PROPERTY_NAME_PREFIX);
        this.setProperties(properties);
    }

    public void setProperties(Map<String, Object> properties) {
        this.propertyValues = properties;
    }

    protected void configureContentNegotiationManagerFactoryBean(ContentNegotiationManagerFactoryBean factoryBean) {

        DataBinder dataBinder = new DataBinder(factoryBean);

        dataBinder.setDisallowedFields("contentNegotiationManager", "servletContext");

        dataBinder.setAutoGrowNestedPaths(true);

        dataBinder.registerCustomEditor(Map.class, "mediaTypes", new MediaTypesMapPropertyEditor());

        MutablePropertyValues propertyValues = new MutablePropertyValues();

        propertyValues.addPropertyValues(this.propertyValues);

        dataBinder.bind(propertyValues);
    }

    static class MediaTypesMapPropertyEditor extends PropertyEditorSupport {

        @Override
        public void setAsText(String text) {
            if (text.indexOf(LEFT_CURLY_BRACE_CHAR) == 0) {
                ObjectMapper objectMapper = new ObjectMapper();
                Properties mediaTypes = execute(() -> objectMapper.readValue(text, Properties.class));
                setValue(mediaTypes);
            } else {
                setValue(text);
            }
        }
    }

}
