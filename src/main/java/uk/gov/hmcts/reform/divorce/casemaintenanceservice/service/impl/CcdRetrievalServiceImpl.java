package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.CitizenCaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.UserDetails;
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
    public CaseDetails retrievePetition(String authorisation) throws DuplicateCaseException {
        UserDetails userDetails = getUserDetails(authorisation);

        List<CaseDetails> caseDetailsList =
            coreCaseDataApi.searchForCitizen(
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

        Map<CitizenCaseState, List<CaseDetails>> statusCaseDetailsMap =
            caseDetailsList.stream()
                .collect(Collectors.groupingBy(caseDetails -> CitizenCaseState.getState(caseDetails.getState())));

        List<CaseDetails> submittedCases = statusCaseDetailsMap.get(CitizenCaseState.COMPLETE);

        if (CollectionUtils.isNotEmpty(submittedCases)) {
            return submittedCases.get(0);
        }

        List<CaseDetails> awaitingPaymentCases = statusCaseDetailsMap.get(CitizenCaseState.INCOMPLETE);

        if (CollectionUtils.isEmpty(awaitingPaymentCases)) {
            return null;
        } else if (awaitingPaymentCases.size() > 1) {
            String message = String.format("[%d] cases in awaiting payment found for the user [%s]",
                awaitingPaymentCases.size(), userDetails.getEmail());
            log.warn(message);
            throw new DuplicateCaseException(message);
        }

        return awaitingPaymentCases.get(0);
    }
}
