/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.microsphere.spring.test.tomcat.embedded;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReader;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotatedBeanDefinitionReader;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.test.context.MergedContextConfiguration;
import org.springframework.test.context.support.AbstractGenericContextLoader;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;

import javax.servlet.ServletContext;
import java.util.Enumeration;

import static io.microsphere.lang.function.ThrowableAction.execute;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static io.microsphere.util.ShutdownHookUtils.addShutdownHookCallback;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext;
import static org.springframework.web.servlet.FrameworkServlet.SERVLET_CONTEXT_PREFIX;

/**
 * {@link AbstractGenericContextLoader} class for {@link EmbeddedTomcatConfiguration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EmbeddedTomcatConfiguration
 * @see AbstractGenericContextLoader
 * @since 1.0.0
 */
class EmbeddedTomcatContextLoader extends AbstractGenericContextLoader {

    private static final Logger logger = getLogger(EmbeddedTomcatContextLoader.class);

    @Override
    protected void prepareContext(ConfigurableApplicationContext applicationContext, MergedContextConfiguration mergedConfig) {
        super.prepareContext(applicationContext, mergedConfig);

        EmbeddedTomcatMergedContextConfiguration config = (EmbeddedTomcatMergedContextConfiguration) mergedConfig;

        Context context = execute(() -> deployContext(applicationContext, config));

        ServletContext servletContext = context.getServletContext();

        WebApplicationContext webApplicationContext = findWebApplicationContext(servletContext);

        setParent(applicationContext, webApplicationContext);
    }

    static void setParent(ConfigurableApplicationContext context, WebApplicationContext webApplicationContext) {
        ApplicationContext parentContext = context.getParent();
        if (parentContext instanceof ConfigurableApplicationContext) {
            ((ConfigurableApplicationContext) parentContext).setParent(webApplicationContext);
        } else {
            // set WebApplicationContext as parent
            context.setParent(webApplicationContext);
        }
    }

    @Override
    protected BeanDefinitionReader createBeanDefinitionReader(GenericApplicationContext context) {
        DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
        return new XmlBeanDefinitionReader(beanFactory);
    }

    @Override
    protected void customizeContext(GenericApplicationContext context) {
        // register ResolvableDependency
        registerResolvableDependency(context);
    }

    @Override
    protected void customizeContext(ConfigurableApplicationContext context, MergedContextConfiguration mergedConfig) {
        super.customizeContext(context, mergedConfig);
        registerConfigClasses((GenericApplicationContext) context, mergedConfig);
    }

    protected void registerConfigClasses(GenericApplicationContext context, MergedContextConfiguration config) {
        Class<?>[] configClasses = config.getClasses();
        if (isNotEmpty(configClasses)) {
            DefaultListableBeanFactory beanFactory = context.getDefaultListableBeanFactory();
            AnnotatedBeanDefinitionReader reader = new AnnotatedBeanDefinitionReader(beanFactory);
            reader.register(configClasses);
        }
    }

    private void registerResolvableDependency(ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        beanFactory.registerResolvableDependency(ApplicationContext.class, context);
        beanFactory.registerResolvableDependency(ConfigurableApplicationContext.class, context);
        beanFactory.registerResolvableDependency(WebApplicationContext.class, context.getParent());
        beanFactory.registerResolvableDependency(ConfigurableWebApplicationContext.class, context.getParent());
    }

    /**
     * Try to find the {@link WebApplicationContext} from {@link FrameworkServlet} or it subclass creation first,
     * if it can't be found, then try to find the {@link WebApplicationContext} from {@link ServletContext}.
     *
     * @param servletContext {@link ServletContext}
     * @return the {@link WebApplicationContext} if found
     * @see ContextLoaderListener
     * @see DispatcherServlet
     * @see FrameworkServlet#initWebApplicationContext()
     * @see FrameworkServlet#SERVLET_CONTEXT_PREFIX
     * @see WebApplicationContextUtils#getWebApplicationContext(ServletContext, String)
     * @see WebApplicationContextUtils#getRequiredWebApplicationContext(ServletContext)
     */
    @Nullable
    protected WebApplicationContext findWebApplicationContext(ServletContext servletContext) {
        Enumeration<String> attributeNames = servletContext.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            if (attributeName.startsWith(SERVLET_CONTEXT_PREFIX)) {
                return getWebApplicationContext(servletContext, attributeName);
            }
        }
        return getWebApplicationContext(servletContext);
    }

    /**
     * Deploy Tomcat Context from the {@link EmbeddedTomcatConfiguration} config
     *
     * @param config the {@link EmbeddedTomcatConfiguration} config
     * @return the instance of {@link Context}
     * @throws Exception
     */
    @Nonnull
    protected Context deployContext(ConfigurableApplicationContext applicationContext, EmbeddedTomcatMergedContextConfiguration config) throws Exception {

        Environment environment = applicationContext.getEnvironment();

        int port = config.getPort();
        String contextPath = environment.resolvePlaceholders(config.getContextPath());
        String basedir = environment.resolvePlaceholders(config.getBasedir());
        String resourceBasePath = environment.resolvePlaceholders(config.getResourceBasePath());


        Tomcat tomcat = new Tomcat();
        tomcat.setAddDefaultWebXmlToWebapp(false);
        tomcat.setPort(port);

        if (hasText(basedir)) {
            tomcat.setBaseDir(basedir);
        }

        String docBase = resourceBasePath;
        if (hasText(resourceBasePath)) {
            Resource resource = applicationContext.getResource(resourceBasePath);
            if (resource.exists()) {
                docBase = resource.getFile().getAbsolutePath();
            }
        }

        logger.trace("Tomcat will startup with port : {} , contextPath : '{}' , basedir : '{}', resourceBasePath : '{}' , docBase : '{}'",
                port, contextPath, basedir, resourceBasePath, docBase);


        Context context = tomcat.addWebapp(contextPath, docBase);

        tomcat.start();

        addShutdownHookCallback(() -> execute(tomcat::stop));

        return context;
    }

    /**
     * Returns {@code "-context.xml"} in order to support detection of a
     * default XML config file.
     */
    @Override
    protected String getResourceSuffix() {
        return "-context.xml";
    }
}