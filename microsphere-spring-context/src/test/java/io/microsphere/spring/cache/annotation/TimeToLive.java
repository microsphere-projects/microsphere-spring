///*
// * Licensed to the Apache Software Foundation (ASF) under one or more
// * contributor license agreements.  See the NOTICE file distributed with
// * this work for additional information regarding copyright ownership.
// * The ASF licenses this file to You under the Apache License, Version 2.0
// * (the "License"); you may not use this file except in compliance with
// * the License.  You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package io.microsphere.spring.cache.annotation;
//
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.CachePut;
//import org.springframework.cache.annotation.Cacheable;
//
//import java.lang.annotation.Documented;
//import java.lang.annotation.ElementType;
//import java.lang.annotation.Inherited;
//import java.lang.annotation.Retention;
//import java.lang.annotation.RetentionPolicy;
//import java.lang.annotation.Target;
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
///**
// * a.k.a TTL
// *
// * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
// * @since TODO
// */
//@Target({ElementType.TYPE, ElementType.METHOD})
//@Retention(RetentionPolicy.RUNTIME)
//@Inherited
//@Documented
//public @interface TimeToLive {
//
//    Cacheable cacheable();
//
//    CachePut cachePut();
//
//    CacheEvict cacheEvict();
//
//    Value[] value() default {};
//
//    @interface Cacheable {
//
//        long value();
//
//        TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
//    }
//
//    @interface CachePut {
//
//        long value();
//
//        TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
//    }
//
//    @interface CacheEvict {
//
//        long value();
//
//        TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
//    }
//
//    @interface Value {
//
//        long value();
//
//        TimeUnit timeUnit() default TimeUnit.MILLISECONDS;
//
//        CacheOperation operation();
//    }
//
//    enum CacheOperation {
//
//        Cacheable,
//
//        CachePut,
//
//        CacheEvict
//
//    }
//}
//
//class A {
//
//    @TimeToLive(
//            // Solution 1
//            cacheable = @TimeToLive.Cacheable(100),
//            cachePut = @TimeToLive.CachePut(200),
//            cacheEvict = @TimeToLive.CacheEvict(300),
//
//            // Solution 2
//            value = {
//                    @TimeToLive.Value(value = 100, operation = TimeToLive.CacheOperation.Cacheable),
//                    @TimeToLive.Value(value = 200, operation = TimeToLive.CacheOperation.CachePut),
//                    @TimeToLive.Value(value = 300, operation = TimeToLive.CacheOperation.CacheEvict),
//            }
//    )
//    @Cacheable // @TimeToLive(100)
//    @CachePut  // @TimeToLive(200)
//    @CacheEvict // @TimeToLive(300)
//
//    // Solution 3
//    @TTLCacheable(expire = 100)
//    public List<String> getNames() {
//        return Collections.emptyList();
//    }
//
//}
