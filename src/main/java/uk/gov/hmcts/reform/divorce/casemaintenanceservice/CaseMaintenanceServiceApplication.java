package uk.gov.hmcts.reform.divorce.casemaintenanceservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import uk.gov.hmcts.reform.authorisation.healthcheck.ServiceAuthHealthIndicator;

@SpringBootApplication(exclude = {ServiceAuthHealthIndicator.class})
public class CaseMaintenanceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CaseMaintenanceServiceApplication.class, args);
    }
}
