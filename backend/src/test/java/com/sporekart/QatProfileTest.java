package com.sporekart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("qat")
class QatProfileTest {

    @Autowired
    private Environment environment;

    @Test
    void shouldStartWithQatProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        assertEquals(1, activeProfiles.length);
        assertEquals("qat", activeProfiles[0]);
    }
}
