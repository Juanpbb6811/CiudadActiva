package com.ciudadactiva.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "BearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("CiudadActiva API - Reto 02 Ingeniería de Software")
                        .version("1.0.0")
                        .description("API REST para el reporte y seguimiento ciudadano de incidencias urbanas. " +
                                "Implementado bajo la metodología RUP con Spring Boot, Spring Security JWT, " +
                                "Spring Data JPA y PostgreSQL (Supabase / Render).")
                        .contact(new Contact()
                                .name("Equipo CiudadActiva - 501T SIS")
                                .email("contacto@ciudadactiva.gov.co"))
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa el token JWT obtenido del endpoint /api/auth/login")));
    }
}
