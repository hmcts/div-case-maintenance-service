package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdUpdateService;

@Service
public class CcdUpdateServiceImpl extends BaseCcdCaseService implements CcdUpdateService {

    @Override
    public CaseDetails update(String caseId, Object data, String eventId, String authorisation) {
        UserDetails userDetails = getUserDetails(authorisation);

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(
            getBearerUserToken(authorisation),
            getServiceAuthToken(),
            userDetails.getId(),
            jurisdictionId,
            caseType,
            caseId,
            eventId);

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

        return coreCaseDataApi.submitEventForCitizen(
            getBearerUserToken(authorisation),
            getServiceAuthToken(),
            userDetails.getId(),
            jurisdictionId,
            caseType,
            caseId,
            true,
            caseDataContent);
    }
}
