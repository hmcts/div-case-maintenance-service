package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdUpdateService;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.CASEWORKER_ROLE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.CITIZEN_ROLE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.util.AuthUtil.getBearerToken;

@Slf4j
@Service
public class CcdUpdateServiceImpl extends BaseCcdCaseService implements CcdUpdateService {

    @Override
    public CaseDetails update(String caseId, Object data, String eventId, String authorisation) {
        User userDetails = getUser(authorisation);
        List<String> userRoles = Optional.ofNullable(
            userDetails.getUserDetails().getRoles()).orElse(Collections.emptyList()
        );
        String call = "TEST VALUE";
        if (userRoles.contains(CASEWORKER_ROLE) && !userRoles.contains(CITIZEN_ROLE)) {
            try {
                call = "startEventForCaseWorker";
                log.info("Calling coreCaseDataApi.{} event: {} - CaseID: {}",call , eventId, caseId);
                StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
                    getBearerToken(authorisation),
                    getServiceAuthToken(),
                    userDetails.getUserDetails().getId(),
                    jurisdictionId,
                    caseType,
                    caseId,
                    eventId);

                CaseDataContent caseDataContent = buildCaseDataContent(startEventResponse, data);

                log.info("Calling coreCaseDataApi.{} event: {} - CaseID: {}", call, eventId, caseId);
                call = "submitEventForCaseWorker";
                return coreCaseDataApi.submitEventForCaseWorker(
                    getBearerToken(authorisation),
                    getServiceAuthToken(),
                    userDetails.getUserDetails().getId(),
                    jurisdictionId,
                    caseType,
                    caseId,
                    true,
                    caseDataContent
                );
            } catch (FeignException exception) {
                log.error("Call for coreCaseDataApi.{} failed. event: {} - CaseID: {} - Error: {}", call, eventId, caseId, exception.getMessage());
            }
        }

        log.error("TEST EXECUTE FOR CITIZEN");

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCitizen(
            getBearerToken(authorisation),
            getServiceAuthToken(),
            userDetails.getUserDetails().getId(),
            jurisdictionId,
            caseType,
            caseId,
            eventId
        );

        CaseDataContent caseDataContent = buildCaseDataContent(startEventResponse, data);

        return coreCaseDataApi.submitEventForCitizen(
            getBearerToken(authorisation),
            getServiceAuthToken(),
            userDetails.getUserDetails().getId(),
            jurisdictionId,
            caseType,
            caseId,
            true,
            caseDataContent
        );
    }

    @Override
    public CaseDetails updateBulkCase(String caseId, Object data, String eventId, String authorisation) {
        User userDetails = getUser(authorisation);

        StartEventResponse startEventResponse = coreCaseDataApi.startEventForCaseWorker(
            getBearerToken(authorisation),
            getServiceAuthToken(),
            userDetails.getUserDetails().getId(),
            jurisdictionId,
            bulkCaseType,
            caseId,
            eventId
        );

        CaseDataContent caseDataContent = buildCaseDataContent(startEventResponse, data);

        return coreCaseDataApi.submitEventForCaseWorker(
            getBearerToken(authorisation),
            getServiceAuthToken(),
            userDetails.getUserDetails().getId(),
            jurisdictionId,
            bulkCaseType,
            caseId,
            true,
            caseDataContent
        );
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
