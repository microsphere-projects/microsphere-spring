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
package io.microsphere.spring.context.event;

import io.microsphere.util.StopWatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {BeanTimeStatistics.class, BeanTimeStatisticsTest.class},
        initializers = {BeanEventPublishingInitializer.class})
public class BeanTimeStatisticsTest {

    private static final Logger logger = LoggerFactory.getLogger(BeanTimeStatisticsTest.class);

    @Autowired
    private BeanFactory beanFactory;

    @Test
    public void test() {
        BeanTimeStatistics beanTimeStatistics = beanFactory.getBean(BeanTimeStatistics.class);
        StopWatch stopWatch = beanTimeStatistics.getStopWatch();
        logger.info(stopWatch.toString());
    }

}
