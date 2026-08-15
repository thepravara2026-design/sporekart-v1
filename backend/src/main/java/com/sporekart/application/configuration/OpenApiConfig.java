package com.sporekart.application.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Sprint 6D — API & Contract Hardening.
 *
 * Provides comprehensive OpenAPI 3.0 configuration including:
 * - JWT Bearer token security scheme for authenticated endpoints
 * - Server declarations for local dev, staging, and production
 * - Full API metadata (contact, license, version)
 * - Global security requirement applied to all operations
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI sporekartOpenAPI() {
        return new OpenAPI()
                .info(buildApiInfo())
                .servers(buildServers())
                .components(buildComponents())
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }

    private Info buildApiInfo() {
        return new Info()
                .title("Sporekart v3.0 REST API")
                .description("""
                        **Sporekart v3.0** — Production E-Commerce Modular Monolith API.

                        This API covers the full commerce lifecycle:
                        - **Authentication & Identity** — JWT-based stateless auth with session management
                        - **Catalog** — Product and category browsing (public, no auth required)
                        - **Cart** — Active cart management (authenticated)
                        - **Checkout** — Order preview and total calculation
                        - **Orders** — Customer order placement, history, timeline, and cancellation
                        - **Payments** — Razorpay-backed payment initiation and verification
                        - **Shipments** — Shiprocket shipment tracking
                        - **Returns** — Customer return requests and lifecycle management
                        - **Reviews** — Product reviews and ratings
                        - **Notifications** — Customer notification preferences and delivery
                        - **Support** — Customer support ticket management

                        **Authentication:** All endpoints except `GET /api/v1/catalog/**`, auth endpoints,
                        health endpoints, and webhooks require a JWT Bearer token in the `Authorization` header.
                        """)
                .version("v3.0.0")
                .contact(new Contact()
                        .name("Sporekart Engineering Team")
                        .email("engineering@sporekart.com")
                        .url("https://sporekart.com"))
                .license(new License()
                        .name("Proprietary — All Rights Reserved")
                        .url("https://sporekart.com/license"));
    }

    private List<Server> buildServers() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("Local Development Server"),
                new Server()
                        .url("https://api-staging.sporekart.com")
                        .description("Staging Environment"),
                new Server()
                        .url("https://api.sporekart.com")
                        .description("Production Environment")
        );
    }

    private Components buildComponents() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name(BEARER_SCHEME)
                .description(
                        "JWT Bearer token obtained from `POST /api/v1/auth/login`. " +
                        "Provide in `Authorization: Bearer <token>` header. " +
                        "Token expires per server-configured TTL (default: 24h)."
                );

        return new Components()
                .addSecuritySchemes(BEARER_SCHEME, bearerScheme);
    }
}
