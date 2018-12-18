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

@Service
@Slf4j
public class CcdRetrievalServiceImpl extends BaseCcdCaseService implements CcdRetrievalService {

    @Override
    public CaseDetails retrieveCase(String authorisation, Map<CaseStateGrouping, List<CaseState>> caseStateGrouping)
        throws DuplicateCaseException {
        UserDetails userDetails = getUserDetails(authorisation);

        List<CaseDetails> caseDetailsList = getCaseListForUser(authorisation, userDetails.getId());

        if (CollectionUtils.isEmpty(caseDetailsList)) {
            return null;
        }

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

        List<CaseDetails> completedCases = statusCaseDetailsMap.get(CaseStateGrouping.COMPLETE);

        if (CollectionUtils.isNotEmpty(completedCases)) {
            return updateApplicationStatus(completedCases.get(0));
        }

        List<CaseDetails> incompleteCases = statusCaseDetailsMap.get(CaseStateGrouping.INCOMPLETE);

        if (CollectionUtils.isEmpty(incompleteCases)) {
            return null;
        } else if (incompleteCases.size() > 1) {
            String message = String.format("[%d] cases in incomplete status found for the user [%s]",
                incompleteCases.size(), userDetails.getEmail());
            log.warn(message);
            throw new DuplicateCaseException(message);
        }

        return updateApplicationStatus(incompleteCases.get(0));
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

    @Override
    public CaseDetails retrieveCaseById(String authorisation, String caseId) {
        UserDetails userDetails = getUserDetails(authorisation);

        return coreCaseDataApi.readForCitizen(
            getBearerUserToken(authorisation),
            getServiceAuthToken(),
            userDetails.getId(),
            jurisdictionId,
            caseType,
            caseId
        );
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

    private CaseDetails updateApplicationStatus(CaseDetails caseDetails) {
        String ccdStateName = caseDetails.getState();
        ApplicationStatus applicationStatus = CaseState.getState(ccdStateName).getStatus();

        caseDetails.setState(applicationStatus.getValue());

        return caseDetails;
    }

}
