package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.InvalidRequestException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.UnauthorizedException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdAccessService;

import javax.naming.AuthenticationException;

import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_RECEIVED_AOS_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_RECEIVED_AOS_FIELD;

@Service
public class CcdAccessServiceImpl extends BaseCcdCaseService implements CcdAccessService {

    @Autowired
    private CaseAccessApi caseAccessApi;

    @Override
    public void linkRespondent(String authorisation, String caseId, String letterHolderId) {
        UserDetails caseworkerUser = getAnonymousCaseWorkerDetails();

        CaseDetails caseDetails = coreCaseDataApi.readForCaseWorker(
            caseworkerUser.getAuthToken(),
            getServiceAuthToken(),
            caseworkerUser.getId(),
            jurisdictionId,
            caseType,
            caseId
        );

        if (caseDetails == null) {
            throw new CaseNotFoundException(String.format("Case with caseId [%s] and letter holder id [%s] not found",
                caseId, letterHolderId));
        }

        if (!isValidCaseAndLetterHolder(caseDetails, letterHolderId)) {
            throw new InvalidRequestException(String.format("Invalid request for caseId [%s] and letter holder id [%s]",
                caseId, letterHolderId));
        }

        UserDetails linkingUser = getUserDetails(authorisation);

        if (!isValidUser(caseDetails, linkingUser.getEmail(), letterHolderId)) {
            throw new UnauthorizedException(String.format("Case with caseId [%s] and letter holder id [%s] "
                    + "already assigned",
                caseId, letterHolderId));
        }

        grantAccessToCase(caseworkerUser, caseId, linkingUser.getId());
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

    private boolean isValidCaseAndLetterHolder(CaseDetails caseDetails, String letterHolderId) {
        if (caseDetails == null || caseDetails.getData() == null || StringUtils.isBlank(letterHolderId)) {
            throw new InvalidRequestException("Case details or letter holder data are invalid");
        }
        return true;
    }

    private boolean isValidUser(CaseDetails caseDetails, String respondentEmail, String letterHolderId) {
        return isValidRespondentUser(caseDetails, respondentEmail, letterHolderId)
            || isValidCoRespondentUser(caseDetails, respondentEmail, letterHolderId);
    }

    private boolean isValidRespondentUser(CaseDetails caseDetails, String respondentEmail, String letterHolderId) {
        return this.respondentIsValid(caseDetails, letterHolderId)
            && (caseDetails.getData().get(RESP_EMAIL_ADDRESS) == null
                || respondentEmail.equalsIgnoreCase((String) caseDetails.getData().get(RESP_EMAIL_ADDRESS))
            );
    }

    private boolean isValidCoRespondentUser(CaseDetails caseDetails, String coRespondentEmail, String letterHolderId) {
        return this.coRespondentIsValid(caseDetails, letterHolderId)
            && (caseDetails.getData().get(CO_RESP_EMAIL_ADDRESS) == null
                || coRespondentEmail.equalsIgnoreCase((String) caseDetails.getData().get(CO_RESP_EMAIL_ADDRESS))
            );
    }

    private boolean respondentIsValid(CaseDetails caseDetails, String letterHolderId) {
        if (letterHolderId.equals(caseDetails.getData().get(RESP_LETTER_HOLDER_ID_FIELD))) {
            return true;
        } else {
            throw new UnauthorizedException(String.format("Respondent letter holder ID [%s] is invalid for case [%s]",
                letterHolderId, caseDetails.getId()));
        }
    }

    private boolean coRespondentIsValid(CaseDetails caseDetails, String letterHolderId) {
        if (letterHolderId.equals(caseDetails.getData().get(CO_RESP_LETTER_HOLDER_ID_FIELD))) {
            return true;
        } else {
            throw new UnauthorizedException(String.format("Co-Respondent letter holder ID [%s] is invalid for case [%s]",
                letterHolderId, caseDetails.getId()));
        }
    }
}
