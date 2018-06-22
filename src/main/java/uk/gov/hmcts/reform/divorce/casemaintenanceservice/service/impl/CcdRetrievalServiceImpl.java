package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CcdRetrievalServiceImpl extends BaseCcdCaseService implements CcdRetrievalService {
    private static final String CASE_STATE = "state";
    private static final String AWAITING_PAYMENT_STATE = "awaitingpayment";

    @Autowired
    private CoreCaseDataApi coreCaseDataApi;

    @Override
    public Map<String, Object> retrievePetition(String authorisation){
        UserDetails userDetails = getUserDetails(authorisation);

        List<CaseDetails> caseDetailsList =
            coreCaseDataApi.searchForCitizen(
                getBearerUserToken(authorisation),
                getServiceAuthToken(),
                userDetails.getId(),
                jurisdictionId,
                caseType,
                null);

        List<CaseDetails> awaitingPaymentCases =
            caseDetailsList.stream()
                .filter(caseData -> {
                    Object status = caseData.getData().get(CASE_STATE);
                    return status != null && status.toString().equalsIgnoreCase(AWAITING_PAYMENT_STATE);
                }).collect(Collectors.toList());

        //TODO: Fix
        if (CollectionUtils.isEmpty(awaitingPaymentCases)){
            return null;
        } else if (awaitingPaymentCases.size() > 0) {
            // throw exception
        }

        return awaitingPaymentCases.get(0).getData();
    }

}
