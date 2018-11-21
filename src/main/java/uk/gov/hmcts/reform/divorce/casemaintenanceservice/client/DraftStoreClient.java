package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.CreateDraft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.UpdateDraft;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@FeignClient(name = "draft-store-client", url = "${draft.store.api.baseurl}")
public interface DraftStoreClient {
    String SERVICE_AUTHORIZATION_HEADER_NAME = "ServiceAuthorization";
    String SECRET_HEADER_NAME = "Secret";

    @RequestMapping(
        method = RequestMethod.GET,
        value = "/drafts",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    DraftList getAllDrafts(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                           @RequestHeader(SERVICE_AUTHORIZATION_HEADER_NAME) String serviceAuthorisation,
                           @RequestHeader(SECRET_HEADER_NAME) String secret);

    @RequestMapping(
        method = RequestMethod.GET,
        value = "/drafts",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    DraftList getAllDrafts(@RequestParam("after") String after,
                           @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                           @RequestHeader(SERVICE_AUTHORIZATION_HEADER_NAME) String serviceAuthorisation,
                           @RequestHeader(SECRET_HEADER_NAME) String secret);

    @RequestMapping(
        method = RequestMethod.POST,
        value = "/drafts",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    void createSingleDraft(@RequestBody CreateDraft draft,
                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                @RequestHeader(SERVICE_AUTHORIZATION_HEADER_NAME) String serviceAuthorisation,
                                @RequestHeader(SECRET_HEADER_NAME) String secret);

    @RequestMapping(
        method = RequestMethod.PUT,
        value = "/drafts/{draftId}",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    void updateSingleDraft(@PathVariable("draftId") String draftId,
                                @RequestBody UpdateDraft draft,
                                @RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                                @RequestHeader(SERVICE_AUTHORIZATION_HEADER_NAME) String serviceAuthorisation,
                                @RequestHeader(SECRET_HEADER_NAME) String secret);

    @RequestMapping(
        method = RequestMethod.DELETE,
        value = "/drafts",
        headers = CONTENT_TYPE + "=" + APPLICATION_JSON_VALUE
    )
    void deleteAllDrafts(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorisation,
                         @RequestHeader(SERVICE_AUTHORIZATION_HEADER_NAME) String serviceAuthorisation);
}
