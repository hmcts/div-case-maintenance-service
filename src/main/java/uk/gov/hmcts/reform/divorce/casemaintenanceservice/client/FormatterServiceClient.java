package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "formatter-service-client", url = "${case.formatter.service.api.baseurl}")
public interface FormatterServiceClient {

    @PostMapping(
        value = "caseformatter/version/1/to-ccd-format",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> transformToCCDFormat(@RequestBody Object data,
                                             @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);

    @PostMapping(
        value = "caseformatter/version/1/to-divorce-format",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    Map<String, Object> transformToDivorceFormat(@RequestBody Object data,
                                             @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation);
}
