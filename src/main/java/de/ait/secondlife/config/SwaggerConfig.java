package de.ait.secondlife.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Second Life",
                description = "API for final project in AIT school. Group - cohort 38",
                version = "1.0.0",
                contact = @Contact(
                        name = "John Smith",
                        email = "john.snmith@ait-tr.de",
                        url = "https://www.ait-tr.de/"
                )
        )
)
public class SwaggerConfig {
}
