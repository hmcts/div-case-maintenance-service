package uk.gov.hmcts.reform.divorce.casemaintenanceservice.management.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "documentation.swagger.enabled", havingValue = "true")
public class SwaggerConfiguration {

    @Bean
    public OpenAPI springOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Financial Remedy Case Orchestration Service API")
                .description("Given a case data, This service will orchestrate the financial remedy features "
                    + "like notifications, fee lookUp and DocumentGenerator"));
    }
}
