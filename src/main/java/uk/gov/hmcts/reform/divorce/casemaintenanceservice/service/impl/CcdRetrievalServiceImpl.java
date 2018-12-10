package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.ApplicationStatus;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseStateGrouping;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

@Service
@Slf4j
public class CcdRetrievalServiceImpl extends BaseCcdCaseService implements CcdRetrievalService {

    @Override
    public CaseDetails retrieveCase(String authorisation, Map<CaseStateGrouping, List<CaseState>> caseStateGrouping)
            throws DuplicateCaseException {

        CaseDetails relevantCase = null;

        UserDetails userDetails = getUserDetails(authorisation);
        List<CaseDetails> caseDetailsList = getCaseListForUser(authorisation, userDetails.getId());

        if (CollectionUtils.isNotEmpty(caseDetailsList)) {
            if (caseDetailsList.size() > 1) {
                log.warn("[{}] cases found for the user [{}]", caseDetailsList.size(), userDetails.getForename());
            }

            Map<CaseStateGrouping, List<CaseDetails>> statusCaseDetailsMap = caseDetailsList.stream()
                .collect(Collectors.groupingBy(
                    caseDetails -> caseStateGrouping.entrySet().stream()
                        .filter(caseStateGroupingEntry -> caseStateGroupingEntry.getValue()
                            .contains(
                                CaseState.getState(caseDetails.getState())
                            )
                        )
                        .map(Map.Entry::getKey)
                        .findFirst()
                        .orElse(CaseStateGrouping.UNKNOWN)
                ));

            relevantCase = retrieveRelevantCaseDetails(statusCaseDetailsMap, userDetails);
            if (relevantCase != null) {
                relevantCase = translateCcdCaseStateToDivorceState(relevantCase);
            }
        }

        return relevantCase;
    }

    @Override
    public CaseDetails retrieveCase(String authorisation) throws DuplicateCaseException {
        UserDetails userDetails = getUserDetails(authorisation);

        List<CaseDetails> caseDetailsList = getCaseListForUser(authorisation, userDetails.getId());

        if (CollectionUtils.isEmpty(caseDetailsList)) {
            return null;
        }

        if (caseDetailsList.size() > 1) {
            throw new DuplicateCaseException(String.format("There are [%d] case for the user [%s]",
                    caseDetailsList.size(), userDetails.getForename()));
        }

        return caseDetailsList.get(0);
    }

    @Nullable
    private CaseDetails retrieveRelevantCaseDetails(Map<CaseStateGrouping, List<CaseDetails>> statusCaseDetailsMap,
                                            UserDetails userDetails) throws DuplicateCaseException {
        CaseDetails relevantCase = null;

        List<CaseDetails> completedCases = statusCaseDetailsMap.get(CaseStateGrouping.COMPLETE);
        if (CollectionUtils.isNotEmpty(completedCases)) {
            relevantCase = completedCases.get(0);
        } else {
            List<CaseDetails> incompleteCases = statusCaseDetailsMap.get(CaseStateGrouping.INCOMPLETE);

            if (CollectionUtils.isNotEmpty(incompleteCases)) {
                if (incompleteCases.size() > 1) {
                    String message = String.format("[%d] cases in incomplete status found for the user [%s]",
                            incompleteCases.size(), userDetails.getEmail());
                    log.warn(message);
                    throw new DuplicateCaseException(message);
                } else {
                    relevantCase = incompleteCases.get(0);
                }
            }
        }

        return relevantCase;
    }

    private List<CaseDetails> getCaseListForUser(String authorisation, String userId) {

        return coreCaseDataApi.searchForCitizen(
                getBearerUserToken(authorisation),
                getServiceAuthToken(),
                userId,
                jurisdictionId,
                caseType,
                Collections.emptyMap());
    }

    private CaseDetails translateCcdCaseStateToDivorceState(CaseDetails caseDetails) {
        String ccdStateName = caseDetails.getState();
        ApplicationStatus applicationStatus = CaseState.getState(ccdStateName).getStatus();

        caseDetails.setState(applicationStatus.getValue());

        return caseDetails;
    }

}