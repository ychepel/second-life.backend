package de.ait.secondlife.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                title = "Second Life",
                description = "API for final project in AIT school. Group - cohort 38<br><br>" +
                        "GitHub repository: <a href='https://github.com/ychepel/second-life.backend' target='_blank'>https://github.com/ychepel/second-life.backend</a>",
                version = "1.0.0",
                contact = @Contact(
                        name = "Second Life",
                        email = "admin@second-life.space",
                        url = "https://www.second-life.space/"
                )
        )
)
public class SwaggerConfig {
}
