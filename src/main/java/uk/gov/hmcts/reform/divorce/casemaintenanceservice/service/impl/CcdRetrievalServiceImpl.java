package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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

        List<CaseDetails> caseDetailsList = coreCaseDataApi.searchForCitizen(
            getBearerUserToken(authorisation),
            getServiceAuthToken(),
            userDetails.getId(),
            jurisdictionId,
            caseType,
            Collections.emptyMap());

        if (CollectionUtils.isEmpty(caseDetailsList)) {
            return null;
        }

        if (caseDetailsList.size() > 1) {
            log.warn("[{}] cases found for the user [{}]", caseDetailsList.size(), userDetails.getForename());
        }

        Map<CaseStateGrouping, List<CaseDetails>> statusCaseDetailsMap =
            caseDetailsList.stream()
                .collect(Collectors.groupingBy(
                    caseDetails -> caseStateGrouping.entrySet().stream()
                        .filter(caseStateGroupingEntry -> caseStateGroupingEntry.getValue()
                            .contains(CaseState.getState(caseDetails.getState())))
                            .map(Map.Entry::getKey)
                            .findFirst()
                        .orElse(CaseStateGrouping.UNKNOWN)));

        List<CaseDetails> completedCases = statusCaseDetailsMap.get(CaseStateGrouping.COMPLETE);

        if (CollectionUtils.isNotEmpty(completedCases)) {
            return completedCases.get(0);
        }

        List<CaseDetails> inCompleteCases = statusCaseDetailsMap.get(CaseStateGrouping.INCOMPLETE);

        if (CollectionUtils.isEmpty(inCompleteCases)) {
            return null;
        } else if (inCompleteCases.size() > 1) {
            String message = String.format("[%d] cases in incomplete status found for the user [%s]",
                inCompleteCases.size(), userDetails.getEmail());
            log.warn(message);
            throw new DuplicateCaseException(message);
        }

        return inCompleteCases.get(0);
    }
}
