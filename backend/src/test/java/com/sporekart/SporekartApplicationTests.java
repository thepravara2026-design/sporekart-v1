package com.sporekart;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SporekartApplicationTests {

    @Test
    void shouldLoadApplicationContext() {
        // Verifies Spring Boot Application Context loads successfully
    }
}
