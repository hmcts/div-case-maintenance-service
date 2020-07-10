package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.User;

import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.util.AuthUtil.getBearerToken;

class BaseCcdCaseService {
    static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "Divorce case submission event";
    static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting Divorce Case";

    static final String DIVORCE_BULK_CASE_SUBMISSION_EVENT_SUMMARY = "Divorce Bulk case submission event";
    static final String DIVORCE_BULK_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting divorce bulk Case";

    @Value("${ccd.jurisdictionid}")
    String jurisdictionId;

    @Value("${ccd.casetype}")
    String caseType;

    @Value("${ccd.eventid.create}")
    String createEventId;

    @Value("${ccd.eventid.createhwf}")
    String createHwfEventId;

    @Value("${ccd.eventid.solicitorCreate}")
    String solicitorCreateEventId;

    @Value("${ccd.bulk.casetype}")
    String bulkCaseType;

    @Value("${ccd.bulk.eventid.create}")
    String createBulkCaseEventId;

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    User getUser(String userToken) {
        return userService.retrieveUser(getBearerToken(userToken));
    }

    User getAnonymousCaseWorkerDetails() {
        return userService.retrieveAnonymousCaseWorkerDetails();
    }

    String getServiceAuthToken() {
        return authTokenGenerator.generate();
    }
}
