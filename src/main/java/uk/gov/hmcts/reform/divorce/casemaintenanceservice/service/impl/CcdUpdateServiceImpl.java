package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdUpdateService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CcdUpdateServiceImpl extends BaseCcdCaseService implements CcdUpdateService {

    private static final String CASEWORKER_ROLE = "caseworker";
    private static final String CITIZEN_ROLE = "citizen";

    @Override
    public CaseDetails update(String caseId, Object data, String eventId, String authorisation) {
        UserDetails userDetails = getUserDetails(authorisation);
        List<String> userRoles = Optional.ofNullable(userDetails.getRoles()).orElse(Collections.emptyList());

        if (userRoles.contains(CASEWORKER_ROLE) && !userRoles.contains(CITIZEN_ROLE)) {
            StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
                getBearerUserToken(authorisation),
                getServiceAuthToken(),
                userDetails.getId(),
                jurisdictionId,
                caseType,
                caseId,
                eventId);

            CaseDataContent caseDataContent = buildCaseDataContent(startEventResponse, data);

            return coreCaseDataApi.submitEventForCaseWorker(
                getBearerUserToken(authorisation),
                getServiceAuthToken(),
                userDetails.getId(),
                jurisdictionId,
                caseType,
                caseId,
                true,
                caseDataContent);
        } else {
            StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(
                getBearerUserToken(authorisation),
                getServiceAuthToken(),
                userDetails.getId(),
                jurisdictionId,
                caseType,
                caseId,
                eventId);

            CaseDataContent caseDataContent = buildCaseDataContent(startEventResponse, data);

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

    private CaseDataContent buildCaseDataContent(StartEventResponse startEventResponse, Object data) {
        return CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(data)
            .build();
    }
}
