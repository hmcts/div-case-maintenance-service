package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.event.ccd.submission.NotifyCaseSubmission;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdSubmissionService;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Map;

import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.util.AuthUtil.getBearerToken;

@Service
public class CcdSubmissionServiceImpl extends BaseCcdCaseService implements CcdSubmissionService {

    private static final String HELP_WITH_FEES_FIELD = "D8HelpWithFeesNeedHelp";

    @NotifyCaseSubmission
    @Override
    public CaseDetails submitCase(Map<String, Object> data, String authorisation) {
        User userDetails = getUser(authorisation);

        StartEventResponse startEventResponse = coreCaseDataApi.startForCitizen(
            getBearerToken(authorisation),
            getServiceAuthToken(),
            userDetails.getUserDetails().getId(),
            jurisdictionId,
            caseType,
            getCaseCreationEventId(data)
        );

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
            getBearerToken(authorisation),
            getServiceAuthToken(),
            userDetails.getUserDetails().getId(),
            jurisdictionId,
            caseType,
            true,
            caseDataContent
        );
    }

    @Override
    public CaseDetails submitBulkCase(Map<String, Object> data, String authorisation) {
        User userDetails = getUser(authorisation);

        StartEventResponse startEventResponse = coreCaseDataApi.startForCaseworker(
            getBearerToken(authorisation),
            getServiceAuthToken(),
            userDetails.getUserDetails().getId(),
            jurisdictionId,
            bulkCaseType,
            createBulkCaseEventId);

        CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_BULK_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_BULK_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(data)
            .build();

        return coreCaseDataApi.submitForCaseworker(
            getBearerToken(authorisation),
            getServiceAuthToken(),
            userDetails.getUserDetails().getId(),
            jurisdictionId,
            bulkCaseType,
            true,
            caseDataContent);
    }

    private String getCaseCreationEventId(Map<String, Object> data) {
        String hwf = String.valueOf(data.get(HELP_WITH_FEES_FIELD));
        return "YES".equalsIgnoreCase(hwf) ? createHwfEventId  : createEventId;
    }
}
