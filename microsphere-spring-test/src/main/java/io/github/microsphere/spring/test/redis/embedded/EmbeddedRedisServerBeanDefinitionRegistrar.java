package io.github.microsphere.spring.test.redis.embedded;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;
import redis.embedded.RedisServer;
import redis.embedded.RedisServerBuilder;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;

import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * Embedded Redis server {@link ImportBeanDefinitionRegistrar}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableEmbeddedRedisServer
 * @since 1.0.0
 */
class EmbeddedRedisServerBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private static final Class<? extends Annotation> ANNOTATION_TYPE = EnableEmbeddedRedisServer.class;

    private ResourceLoader resourceLoader;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = fromMap(metadata.getAnnotationAttributes(ANNOTATION_TYPE.getName()));
        RedisServer redisServer = resolveRedisServer(attributes);
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(RedisServer.class, () -> redisServer);
        beanDefinitionBuilder.setInitMethodName("start");
        beanDefinitionBuilder.setDestroyMethodName("stop");

        String beanName = attributes.getString("name");
        registry.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
    }

    RedisServer resolveRedisServer(AnnotationAttributes attributes) {
        RedisServerBuilder builder = RedisServer.builder();
        Integer port = attributes.getNumber("port");
        builder.port(port);

        String slaveOf = attributes.getString("slaveOf");
        if (!EnableEmbeddedRedisServer.NO_DEFINITION.equals(slaveOf)) {
            String[] values = StringUtils.split(slaveOf, ":");
            builder.slaveOf(values[0], Integer.decode(values[1]));
        }

        String configFile = attributes.getString("configFile");
        if (!EnableEmbeddedRedisServer.NO_DEFINITION.equals(configFile)) {
            Resource resource = resourceLoader.getResource(configFile);
            try {
                File redisConfFile = resource.getFile();
                builder.configFile(redisConfFile.getAbsolutePath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String setting = attributes.getString("setting");
        if (!EnableEmbeddedRedisServer.NO_DEFINITION.equals(setting)) {
            builder.setting(setting);
        }

        return builder.build();
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
