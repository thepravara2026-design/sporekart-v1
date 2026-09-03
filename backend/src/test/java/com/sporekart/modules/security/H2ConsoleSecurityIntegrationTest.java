package com.sporekart.modules.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@TestPropertySource(properties = {
        "spring.h2.console.enabled=true",
        "spring.h2.console.path=/h2-console"
})
public class H2ConsoleSecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("H2-001: H2 console in dev profile allows inline scripts and styles via scoped CSP")
    void testH2ConsoleDevCsp() throws Exception {
        mockMvc.perform(get("/h2-console/"))
                .andExpect(header().string("Content-Security-Policy", containsString("script-src 'self' 'unsafe-inline'")))
                .andExpect(header().string("Content-Security-Policy", containsString("style-src 'self' 'unsafe-inline'")));
    }

    @Test
    @DisplayName("H2-002: Normal API endpoint retains strict default-src 'self' CSP without unsafe-inline")
    void testNormalRouteRetainsStrictCsp() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Security-Policy", "default-src 'self'"))
                .andExpect(header().string("Content-Security-Policy", not(containsString("unsafe-inline"))));
    }

    @Test
    @DisplayName("H2-003: Favicon request is permitted and does not return 401 Unauthorized")
    void testFaviconPermitted() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isNotFound()); // 404 because no static favicon file exists, but NOT 401 Unauthorized
    }

    @Test
    @DisplayName("H2-004: H2 Console favicon request does not return 401 Unauthorized")
    void testH2ConsoleFaviconPermitted() throws Exception {
        mockMvc.perform(get("/h2-console/favicon.ico"))
                .andExpect(header().string("Content-Security-Policy", containsString("unsafe-inline")));
    }

    @org.junit.jupiter.api.Nested
    @SpringBootTest
    @AutoConfigureMockMvc
    @TestPropertySource(properties = {
            "spring.h2.console.enabled=false"
    })
    static class H2ConsoleDisabledTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        @DisplayName("H2-005: When spring.h2.console.enabled=false, /h2-console is not reachable and uses default strict CSP")
        void testH2ConsoleDisabledReturns404WithStrictCsp() throws Exception {
            mockMvc.perform(get("/h2-console/"))
                    .andExpect(status().isNotFound())
                    .andExpect(header().string("Content-Security-Policy", "default-src 'self'"));
        }
    }
}
