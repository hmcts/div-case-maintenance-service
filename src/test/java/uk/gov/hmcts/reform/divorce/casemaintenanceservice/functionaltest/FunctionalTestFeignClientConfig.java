package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("functional-tests")
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
public class FunctionalTestFeignClientConfig {
    // Additional configuration for functional tests if needed
}
