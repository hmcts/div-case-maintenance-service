package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("unit-tests")
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
public class UnitTestFeignClientConfig {
    // Additional configuration for functional tests if needed
}
