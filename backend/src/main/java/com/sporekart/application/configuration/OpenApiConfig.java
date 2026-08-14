package com.sporekart.application.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sporekartOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sporekart v3.0 REST API")
                        .description("Production E-Commerce Modular Monolith API for Sporekart v3.0 Catalog Domain.")
                        .version("v3.0.0")
                        .contact(new Contact()
                                .name("Sporekart Engineering Team")
                                .email("engineering@sporekart.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://sporekart.com/license")));
    }
}
