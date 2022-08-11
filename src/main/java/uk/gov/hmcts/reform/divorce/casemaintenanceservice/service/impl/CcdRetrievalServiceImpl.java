package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.ApplicationStatus;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseStateGrouping;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivCaseRole;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.util.AuthUtil.getBearerToken;

@Service
@Slf4j
public class CcdRetrievalServiceImpl extends BaseCcdCaseService implements CcdRetrievalService {

    public static final String ALL_CASES_QUERY = "{\"query\":{\"match_all\": {}}}";

    @Override
    public CaseDetails retrieveCase(String authorisation, Map<CaseStateGrouping, List<CaseState>> caseStateGrouping,
                                    DivCaseRole role) {
        User userDetails = getUser(authorisation);
        List<CaseDetails> caseDetailsList = getCaseListForUser(userDetails, role);

        if (CollectionUtils.isEmpty(caseDetailsList)) {
            return null;
        }

        if (caseDetailsList.size() > 1) {
            log.warn("[{}] cases found for the user [{}]", caseDetailsList.size(), userDetails.getUserDetails().getId());
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
            List<CaseDetails> amendCases = statusCaseDetailsMap.get(CaseStateGrouping.AMEND);

            if (CollectionUtils.isNotEmpty(amendCases)) {
                // Sort by Created Date in descending order
                // so the first case is the latest created case in AmendPetition state
                Collections.sort(amendCases, Comparator.comparing(CaseDetails::getCreatedDate).reversed());
                return updateApplicationStatus(amendCases.get(0));
            }

            return null;
        } else if (incompleteCases.size() > 1) {
            String message = String.format("[%d] cases in incomplete status found for the user [%s]",
                incompleteCases.size(), userDetails.getUserDetails().getEmail());
            log.warn(message);
            throw new DuplicateCaseException(message);
        }

        return updateApplicationStatus(incompleteCases.get(0));
    }

    @Override
    public CaseDetails retrieveCase(String authorisation, DivCaseRole role) {
        User userDetails = getUser(authorisation);

        List<CaseDetails> caseDetailsList = getCaseListForUser(userDetails, role);

        caseDetailsList = filterOutAmendedCases(caseDetailsList);

        if (CollectionUtils.isEmpty(caseDetailsList)) {
            return null;
        }

        if (caseDetailsList.size() > 1) {
            throw new DuplicateCaseException(String.format("There are [%d] case for the user [%s]",
                caseDetailsList.size(), userDetails.getUserDetails().getId()));
        }

        return caseDetailsList.get(0);
    }

    @Override
    public CaseDetails retrieveCaseById(String authorisation, String caseId) {
        CaseDetails caseDetails = null;
        String searchQuery = buildQuery(caseId, "reference");
        SearchResult searchResult = coreCaseDataApi.searchCases(
            getBearerToken(authorisation),
            getServiceAuthToken(),
            caseType,
            searchQuery);
        if (searchResult.getCases() != null && !searchResult.getCases().isEmpty()) {
            caseDetails = searchResult.getCases().get(0);
        }
        return caseDetails;
    }

    @Override
    public SearchResult searchCase(String authorisation, String query) {
        return coreCaseDataApi.searchCases(
            getBearerToken(authorisation),
            getServiceAuthToken(),
            caseType,
            query
        );
    }

    private List<CaseDetails> filterOutAmendedCases(List<CaseDetails> caseDetailsList) {
        caseDetailsList = Optional.ofNullable(caseDetailsList)
            .orElse(Collections.emptyList())
            .stream()
            .filter(caseDetails -> !CaseState.AMEND_PETITION.getValue().equals(caseDetails.getState()))
            .collect(Collectors.toList());
        return caseDetailsList;
    }

    private List<CaseDetails> getCaseListForUser(User user, DivCaseRole role) {
        List<CaseDetails> cases = Optional.ofNullable(coreCaseDataApi.searchCases(
            getBearerToken(user.getAuthToken()),
            getServiceAuthToken(),
            caseType,
            ALL_CASES_QUERY).getCases()).orElse(Collections.emptyList());
        return cases.stream()
            .filter(caseDetails -> userHasSpecifiedRole(caseDetails, user.getUserDetails().getEmail(), role))
            .collect(Collectors.toList());
    }

    private boolean userHasSpecifiedRole(CaseDetails caseDetails, String userEmail, DivCaseRole role) {
        if (role == null || caseDetails == null) {
            return false;
        }

        switch (role) {
            case PETITIONER:
                return userEmail.equalsIgnoreCase((String) caseDetails.getData().get(D8_PETITIONER_EMAIL));
            case RESPONDENT:
                return userEmail.equalsIgnoreCase((String) caseDetails.getData().get(CO_RESP_EMAIL_ADDRESS))
                    || userEmail.equalsIgnoreCase((String) caseDetails.getData().get(RESP_EMAIL_ADDRESS));
            default:
                return false;
        }
    }

    private CaseDetails updateApplicationStatus(CaseDetails caseDetails) {
        String ccdStateName = caseDetails.getState();
        ApplicationStatus applicationStatus = CaseState.getState(ccdStateName).getStatus();

        caseDetails.setState(applicationStatus.getValue());

        return caseDetails;
    }

    public String buildQuery(String searchValue, String searchField) {
        return "{\"query\":{\"term\":{ \""
            + searchField
            + "\":\"" + searchValue + "\"}}}";
    }

}
