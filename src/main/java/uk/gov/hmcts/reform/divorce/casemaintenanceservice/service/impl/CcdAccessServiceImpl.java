package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdAccessService;

import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_RECEIVED_AOS_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_RECEIVED_AOS_FIELD;

@Service
public class CcdAccessServiceImpl extends BaseCcdCaseService implements CcdAccessService {
    private static final String YES_ANSWER = "Yes";

    @Autowired
    private CaseAccessApi caseAccessApi;

    @Override
    public void linkRespondent(String authorisation, String caseId, String letterHolderId, boolean isCoRespondent) {
        UserDetails caseworkerUser = getAnonymousCaseWorkerDetails();

        CaseDetails caseDetails = coreCaseDataApi.readForCaseWorker(
            caseworkerUser.getAuthToken(),
            getServiceAuthToken(),
            caseworkerUser.getId(),
            jurisdictionId,
            caseType,
            caseId
        );

        if (!linkingIsValid(caseDetails, letterHolderId, isCoRespondent) ) {
            throw new CaseNotFoundException(String.format("Case with caseId [%s] and letter holder id [%s] not found "
                    + "or case already has linked respondent",
                caseId, letterHolderId));
        }

        UserDetails respondentUser = getUserDetails(authorisation);

        grantAccessToCase(caseworkerUser, caseId, respondentUser.getId());
    }

    @Override
    public void unlinkRespondent(String authorisation, String caseId) {
        UserDetails caseworkerUser = getAnonymousCaseWorkerDetails();

        UserDetails respondentUser = getUserDetails(authorisation);

        caseAccessApi.revokeAccessToCase(
            caseworkerUser.getAuthToken(),
            getServiceAuthToken(),
            caseworkerUser.getId(),
            jurisdictionId,
            caseType,
            caseId,
            respondentUser.getId()
        );
    }

    private void grantAccessToCase(UserDetails anonymousCaseWorker, String caseId, String respondentId) {
        caseAccessApi.grantAccessToCase(
            anonymousCaseWorker.getAuthToken(),
            getServiceAuthToken(),
            anonymousCaseWorker.getId(),
            jurisdictionId,
            caseType,
            caseId,
            new UserId(respondentId)
        );
    }

    private boolean linkingIsValid(CaseDetails caseDetails, String letterHolderId, boolean isCoRespondent) {
        if (caseDetails == null || caseDetails.getData() == null || StringUtils.isBlank(letterHolderId)) {
            return false;
        }

        return isCoRespondent ? coRespondentIsValid(caseDetails, letterHolderId) :
            respondentIsValid(caseDetails, letterHolderId);
    }

    private boolean respondentIsValid(CaseDetails caseDetails, String letterHolderId) {
        return !(String.valueOf(caseDetails.getData().get(RESP_RECEIVED_AOS_FIELD)).equals(YES_ANSWER))
            && letterHolderId.equals(caseDetails.getData().get(RESP_LETTER_HOLDER_ID_FIELD));
    }

    private boolean coRespondentIsValid(CaseDetails caseDetails, String letterHolderId) {
        return !(String.valueOf(caseDetails.getData().get(CO_RESP_RECEIVED_AOS_FIELD)).equals(YES_ANSWER))
            && letterHolderId.equals(caseDetails.getData().get(CO_RESP_LETTER_HOLDER_ID_FIELD));
    }
}
