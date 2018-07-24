package uk.gov.hmcts.reform.divorce.casemaintenanceservice.formatterservice;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(name = "draft-store-client", url = "${case.formatter.service.api.baseurl}")
public interface FormatterServiceClient {

}
