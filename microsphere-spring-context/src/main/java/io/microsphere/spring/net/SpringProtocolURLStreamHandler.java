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
package io.microsphere.spring.net;

import io.microsphere.net.ExtendableProtocolURLStreamHandler;
import io.microsphere.net.SubProtocolURLConnectionFactory;
import io.microsphere.spring.core.convert.SpringConverterAdapter;
import io.microsphere.spring.core.convert.support.ConversionServiceResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.Assert;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.List;

/**
 * The Spring {@link URLStreamHandler} component supports supports the "spring" sub-protocols,
 * like "spring:{sub-protocol}:{ext-1}: ... :{ext-n}://...",
 * {sub-protocol} is required, each between {ext-1} to {ext-n} is the optional extension part.
 * for instance, "spring:resource:classpath://abc.properties",
 * <ul>
 *     <li>{sub-protocol} : "resource"</li>
 *     <li>{ext-1} : "classpath"</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SpringProtocolURLStreamHandler extends ExtendableProtocolURLStreamHandler implements InitializingBean, ResourceLoaderAware, BeanFactoryAware, EnvironmentAware {

    public static final String PROTOCOL = "spring";

    private ResourceLoader resourceLoader;

    private ConfigurableListableBeanFactory beanFactory;

    private ConfigurableEnvironment environment;

    private ConfigurableConversionService conversionService;

    public SpringProtocolURLStreamHandler() {
        super(PROTOCOL);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initConversionService();
        super.init();
    }

    private void initConversionService() {
        ConfigurableConversionService conversionService = (ConfigurableConversionService) new ConversionServiceResolver(this.beanFactory).resolve();
        conversionService.addConverter(new SpringConverterAdapter());
        this.conversionService = conversionService;
    }

    @Override
    protected void initSubProtocolURLConnectionFactories(List<SubProtocolURLConnectionFactory> factories) {
        factories.add(new SpringResourceURLConnectionFactory(getResourceLoader()));
        factories.add(new SpringEnvironmentURLConnectionFactory(environment, conversionService));
        factories.add(new SpringDelegatingBeanProtocolURLConnectionFactory(beanFactory));
    }

    @Override
    protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        List<String> subProtocols = resolveSubProtocols(url);
        int size = subProtocols.size();
        if (size < 1) {
            throw new MalformedURLException("The Spring Protocol URLStreamHandler must contain the sub-protocol part , like 'spring:{sub-protocol}:...'");
        }
        return super.openConnection(url, proxy);
    }

    public ResourceLoader getResourceLoader() {
        ResourceLoader resourceLoader = this.resourceLoader;
        if (resourceLoader == null) {
            resourceLoader = new DefaultResourceLoader();
        }
        return resourceLoader;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        Assert.isInstanceOf(ConfigurableListableBeanFactory.class, beanFactory, "The 'beanFactory' argument must be an instance of " + ConfigurableListableBeanFactory.class.getName());
        this.beanFactory = (ConfigurableListableBeanFactory) beanFactory;
    }

    @Override
    public void setEnvironment(Environment environment) {
        Assert.isInstanceOf(ConfigurableEnvironment.class, environment, "The 'environment' argument must be an instance of " + ConfigurableEnvironment.class.getName());
        this.environment = (ConfigurableEnvironment) environment;
    }
}
