package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CaseAccessApi;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.ccd.client.model.UserId;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.InvalidRequestException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.UnauthorizedException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdAccessService;

import java.util.HashSet;
import java.util.Set;

import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_SOLICITOR_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_SOLICITOR_LETTER_HOLDER_ID_FIELD;

@Service
public class CcdAccessServiceImpl extends BaseCcdCaseService implements CcdAccessService {

    @Autowired
    private CaseAccessApi caseAccessApi;

    @Autowired
    private CaseUserApi caseUserApi;

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

    private void updateCaseRoles(UserDetails anonymousCaseWorker, String caseId, String userId, Set<String> caseRoles) {
        caseUserApi.updateCaseRolesForUser(
            anonymousCaseWorker.getAuthToken(),
            getServiceAuthToken(),
            caseId,
            userId,
            new CaseUser(userId, caseRoles)
        );
    }

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
            throw new CaseNotFoundException(
                String.format("Case with caseId [%s] and letter holder id [%s] not found",
                    caseId, letterHolderId));
        }

        isValidCaseAndLetterHolder(caseDetails, letterHolderId);

        UserDetails linkingUser = getUserDetails(authorisation);

        String caseRoleToBeUpdated = findUsersCaseRole(caseDetails, linkingUser.getEmail(), letterHolderId);
        if (caseRoleToBeUpdated == null) {
            throw new UnauthorizedException(
                String.format("Case with caseId [%s] and letter holder id [%s] already assigned or letter holder mismatch",
                    caseId, letterHolderId));
        }


        Set<String> caseRoles = new HashSet<>();
        caseRoles.add(caseRoleToBeUpdated);

        updateCaseRoles(caseworkerUser, caseId, linkingUser.getId(), caseRoles);
    }

    private void isValidCaseAndLetterHolder(CaseDetails caseDetails, String letterHolderId) {
        if (caseDetails.getData() == null || StringUtils.isBlank(letterHolderId)) {
            throw new InvalidRequestException("Case details or letter holder data are invalid");
        }
    }

    private String findUsersCaseRole(CaseDetails caseDetails, String userEmail, String letterHolderId) {
        if (isValidRespondentUser(caseDetails, userEmail, letterHolderId)) {
            return CmsConstants.RESP_SOL_ROLE;
        } else if (isValidCoRespondentUser(caseDetails, userEmail, letterHolderId)) {
            return CmsConstants.CREATOR_ROLE;
        } else if (isValidRespSolicitor(caseDetails, userEmail, letterHolderId)) {
            return CmsConstants.RESP_SOL_ROLE;
        }
        return null;
    }

    private boolean isValidRespondentUser(CaseDetails caseDetails, String respondentEmail, String letterHolderId) {
        return this.respondentIsValid(caseDetails, letterHolderId)
            && caseDetails.getData().get(RESP_EMAIL_ADDRESS) == null
            || respondentEmail.equalsIgnoreCase((String) caseDetails.getData().get(RESP_EMAIL_ADDRESS));
    }

    private boolean respondentIsValid(CaseDetails caseDetails, String letterHolderId) {
        return letterHolderId.equals(caseDetails.getData().get(RESP_LETTER_HOLDER_ID_FIELD));
    }

    private boolean isValidCoRespondentUser(CaseDetails caseDetails, String coRespondentEmail, String letterHolderId) {
        return this.coRespondentIsValid(caseDetails, letterHolderId)
            && caseDetails.getData().get(CO_RESP_EMAIL_ADDRESS) == null
            || coRespondentEmail.equalsIgnoreCase((String) caseDetails.getData().get(CO_RESP_EMAIL_ADDRESS));
    }

    private boolean coRespondentIsValid(CaseDetails caseDetails, String letterHolderId) {
        return letterHolderId.equals(caseDetails.getData().get(CO_RESP_LETTER_HOLDER_ID_FIELD));
    }

    private boolean isValidRespSolicitor(CaseDetails caseDetails, String respSolicitorEmail, String letterHolderId) {
        return this.respondentSolicitorIsValid(caseDetails, letterHolderId)
            && caseDetails.getData().get(RESP_SOLICITOR_EMAIL_ADDRESS) == null
            || respSolicitorEmail.equalsIgnoreCase((String) caseDetails.getData().get(RESP_SOLICITOR_EMAIL_ADDRESS));
    }

    private boolean respondentSolicitorIsValid(CaseDetails caseDetails, String letterHolderId) {
        return letterHolderId.equals(caseDetails.getData().get(RESP_SOLICITOR_LETTER_HOLDER_ID_FIELD));
    }
}
