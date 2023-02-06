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
package io.github.microsphere.spring.boot.security.autoconfigure.web.servlet;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * {@link ServletWebSecurityAutoConfiguration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@SpringBootTest(classes = {
        ServletWebSecurityAutoConfigurationTest.class,
        ServletWebSecurityAutoConfigurationTest.TestController.class
},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "microsphere.security.web.enabled=true",

                "microsphere.security.web.cors.enabled=true",

                "microsphere.security.web.xss.enabled=true",

                "microsphere.security.web.x-content-type.enabled=true",
                "microsphere.security.web.x-frame.enabled=true",
                "microsphere.security.web.x-frame.option=SAMEORIGIN",
                "microsphere.security.web.x-frame.uri=/",

                "microsphere.security.web.hsts.enabled=true",

                "microsphere.security.web.csp.enabled=true",
                "microsphere.security.web.csp.policy-directives=default-src 'none'; style-src cdn.example.com; report-uri /_/csp-reports"
        })
@EnableAutoConfiguration
public class ServletWebSecurityAutoConfigurationTest {

    @Test
    void test(@Autowired WebTestClient webClient) {
        webClient.get()
                .uri("/")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().value("X-Frame-Options", value -> assertEquals("SAMEORIGIN", value))
                .expectHeader().value("X-Content-Type-Options", value -> assertEquals("nosniff", value))
                .expectHeader().value("X-XSS-Protection", value -> assertEquals("1; mode=block", value))
                .expectHeader().value("Content-Security-Policy", value -> assertEquals("default-src 'none'; style-src cdn.example.com; report-uri /_/csp-reports", value))
                .expectHeader().values("Vary", value -> assertEquals(Arrays.asList("Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"), value))
        ;
    }


    /**
     * < X-Frame-Options: [SAMEORIGIN]
     * < X-Content-Type-Options: [nosniff]
     * < X-XSS-Protection: [1; mode=block]
     * < Content-Security-Policy: [default-src 'none'; style-src cdn.example.com; report-uri /_/csp-reports]
     * < Vary: [Origin, Access-Control-Request-Method, Access-Control-Request-Headers]
     * < Content-Type: [text/plain;charset=UTF-8]
     * < Content-Length: [11]
     * < Date: [Mon, 09 Jan 2023 05:55:14 GMT]
     */

    @RestController
    static class TestController {

        @GetMapping("")
        public String index() {
            return "Hello,World";
        }
    }
}
