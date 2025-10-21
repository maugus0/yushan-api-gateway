package com.yushan.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "eureka.client.enabled=false"
})
class YushanApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
