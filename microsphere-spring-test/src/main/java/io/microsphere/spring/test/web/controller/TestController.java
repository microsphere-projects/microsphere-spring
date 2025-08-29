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

package io.microsphere.spring.test.web.controller;

import io.microsphere.spring.test.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.ResponseEntity.ok;

/**
 * Test {@link RestController @RestController}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Controller
 * @since 1.0.0
 */
@Controller
@RequestMapping("/test")
public class TestController {

    @GetMapping("/helloworld")
    @ResponseBody
    public String helloWorld() {
        return "Hello World";
    }

    @GetMapping("/greeting/{message}")
    @ResponseBody
    public String greeting(@PathVariable String message) {
        return "Greeting : " + message;
    }

    @PostMapping(path = "/user")
    @ResponseBody
    public User user(@RequestBody User user) {
        return user;
    }

    @GetMapping("/error")
    @ResponseBody
    public String error(@RequestParam String message) {
        throw new RuntimeException(message);
    }

    @PutMapping("/response-entity")
    public ResponseEntity<String> responseEntity() {
        return ok("OK");
    }

    @GetMapping("/view")
    public String view() {
        return "test-view";
    }
}
