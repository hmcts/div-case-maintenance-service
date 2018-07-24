package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdSubmissionService;

@Service
public class CcdSubmissionServiceImpl extends BaseCcdCaseService implements CcdSubmissionService {

    @Override
    public CaseDetails submitCase(Object data, String authorisation) {
        UserDetails userDetails = getUserDetails(authorisation);

        StartEventResponse startEventResponse = coreCaseDataApi.startForCitizen(
            getBearerUserToken(authorisation),
            getServiceAuthToken(),
            userDetails.getId(),
            jurisdictionId,
            caseType,
            createEventId);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(data)
            .build();

        return coreCaseDataApi.submitForCitizen(
            getBearerUserToken(authorisation),
            getServiceAuthToken(),
            userDetails.getId(),
            jurisdictionId,
            caseType,
            true,
            caseDataContent);
    }
}
