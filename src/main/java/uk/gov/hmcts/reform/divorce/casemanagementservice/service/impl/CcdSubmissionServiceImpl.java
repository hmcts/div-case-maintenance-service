package uk.gov.hmcts.reform.divorce.casemanagementservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemanagementservice.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemanagementservice.service.CcdSubmissionService;
import uk.gov.hmcts.reform.divorce.casemanagementservice.service.IdamUserService;
import uk.gov.hmcts.reform.divorce.casemanagementservice.util.AuthUtil;

@Service
public class CcdSubmissionServiceImpl implements CcdSubmissionService {

    @Value("${ccd.jurisdictionid}")
    private String jurisdictionId;

    @Value("${ccd.casetype}")
    private String caseType;

    @Value("${ccd.eventid.create}")
    private String createEventId;

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Autowired
    private IdamUserService idamUserService;

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Override
    public CaseDetails submitCase(CaseDataContent caseDataContent, String authorisation) {
        UserDetails userDetails = idamUserService.retrieveUserDetails(authorisation);

        String bearerUserToken = AuthUtil.getBearToken(authorisation);
        String serviceToken = authTokenGenerator.generate();
        String userId = userDetails.getId();

        coreCaseDataApi.startForCitizen(bearerUserToken, serviceToken, userId, jurisdictionId, caseType, createEventId);

        return coreCaseDataApi.submitForCitizen(bearerUserToken, serviceToken, userId, jurisdictionId, caseType,
            true, caseDataContent);
    }
}
