package com.shobhanath;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

@EnableFeignClients
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableZuulProxy
@SpringBootApplication
public class DemoApplication {

    @Bean
    CommandLineRunner dc(DiscoveryClient dc) {
        return args ->
                dc.getInstances("hello-service")
                        .forEach(si -> System.out.println(
                                si.getHost() + ':' + si.getPort()));
    	//dc.getInstances("hello-service");
    }

    @Bean
    CommandLineRunner rt(RestTemplate restTemplate) {
        return args -> {
            ParameterizedTypeReference<String> ptr
                    = new ParameterizedTypeReference<String>() {
            };

   String s = restTemplate.exchange(
                    "http://hello-service/hello",
                    HttpMethod.GET, null, ptr).getBody();
   System.out.println(s);

           // reservations.forEach(System.out::println);
        };
    }

    @Bean
    CommandLineRunner feign(HelloRestClient client) {
        return args ->
                client.sayHello();
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}


@Component
class HelloIntegration {

    @Autowired
    private HelloRestClient helloRestClient;

    public String getHelloFallback() {
        return "";
    }

    @HystrixCommand(fallbackMethod = "getHelloFallback")
    public String getHello() {
        return helloRestClient.sayHello();
    }

}

@RestController
@RequestMapping ("/hello-client-service")
class HelloRestController {
	@RequestMapping ("/hello")
    String rs() {
        return this.helloIntegration.getHello();
    }

    @Autowired
    private HelloIntegration helloIntegration;

}


@FeignClient("hello-service")
interface HelloRestClient {
    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    String sayHello();
}
