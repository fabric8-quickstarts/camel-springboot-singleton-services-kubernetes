/**
 *  Copyright 2005-2016 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.quickstarts.camel.singleton;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cluster.CamelClusterEventListener;
import org.apache.camel.cluster.CamelClusterService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Component
    static class Routes extends RouteBuilder {
        public void configure() throws Exception {

            from("master:lock1:timer:clock")
              .log("Hello World!");
        }
    }


   @Configuration
    static class BeanConfiguration {

        @Bean
        public CustomService customService(CamelClusterService clusterService) throws Exception {
            CustomService service = new CustomService();
            clusterService.getView("lock2").addEventListener((CamelClusterEventListener.Leadership) (view, leader) -> {
                boolean weAreLeaders = leader.isPresent() && leader.get().isLocal();
                if (weAreLeaders && !service.isStarted()) {
                    service.start();
                } else if (!weAreLeaders && service.isStarted()) {
                    service.stop();
                }
            });
            return service;
        }
    }
}
