package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.util.AuthUtil;

class BaseCcdCaseService {
    static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY = "Divorce case submission event";
    static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting Divorce Case";

    @Value("${ccd.jurisdictionid}")
    String jurisdictionId;

    @Value("${ccd.casetype}")
    String caseType;

    @Value("${ccd.eventid.create}")
    String createEventId;

    @Value("${ccd.eventid.createhwf}")
    String createHwfEventId;

    @Autowired
    CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    UserDetails getUserDetails(String userToken) {
        return userService.retrieveUserDetails(getBearerUserToken(userToken));
    }

    UserDetails getAnonymousCaseWorkerDetails() {
        return userService.retrieveAnonymousCaseWorkerDetails();
    }

    String getBearerUserToken(String userToken) {
        return AuthUtil.getBearToken(userToken);
    }

    String getServiceAuthToken() {
        return authTokenGenerator.generate();
    }
}
