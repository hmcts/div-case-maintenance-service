package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
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
    private static final String AWAITING_PAYMENT_STATE = "AwaitingPayment";
    private static final String SUBMITTED_PAYMENT_STATE = "Submitted";

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

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

        if (CollectionUtils.isEmpty(caseDetailsList)){
            return null;
        }

        if(caseDetailsList.size() > 1){
            log.warn("[{}] cases found for the user [{}]", caseDetailsList.size(), userDetails.getForename());
        }

        Map<String, List<CaseDetails>> statusCaseDetailsMap =
            caseDetailsList.stream()
                .collect(Collectors.groupingBy(CaseDetails::getState));

        List<CaseDetails> submittedCases = statusCaseDetailsMap.get(SUBMITTED_PAYMENT_STATE);

        if(CollectionUtils.isNotEmpty(submittedCases)){
            return caseDetailsList.get(0);
        }

        List<CaseDetails> awaitingPaymentCases = statusCaseDetailsMap.get(AWAITING_PAYMENT_STATE);

        if (CollectionUtils.isEmpty(awaitingPaymentCases)){
            return null;
        } else if (awaitingPaymentCases.size() > 1) {
            String message = String.format("[%d] cases in awaiting payment found for the user [%s]",
                awaitingPaymentCases.size(), userDetails.getForename());
            log.warn(message);
            throw new DuplicateCaseException(message);
        }

        return caseDetailsList.get(0);
    }
}
