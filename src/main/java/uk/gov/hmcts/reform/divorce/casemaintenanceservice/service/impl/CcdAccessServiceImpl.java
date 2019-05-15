package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import lombok.extern.slf4j.Slf4j;
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
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_SOLICITOR_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_SOLICITOR_LETTER_HOLDER_ID_FIELD;

@Service
@Slf4j
public class CcdAccessServiceImpl extends BaseCcdCaseService implements CcdAccessService {

    @Autowired
    private CaseAccessApi caseAccessApi;

    @Autowired
    private CaseUserApi caseUserApi;

    private enum RespondentType {
        RESPONDENT,
        CO_RESPONDENT,
        RESP_SOLICITOR
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
                format("Case with caseId [%s] and letter holder id [%s] not found",
                    caseId, letterHolderId));
        }

        RespondentType respondentType = validateLetterIdAndUserType(letterHolderId, caseDetails, caseId);

        UserDetails linkingUser = getUserDetails(authorisation);

        if (!isValidRespondent(caseDetails, linkingUser.getEmail(), respondentType)) {
            throw new UnauthorizedException(format("Case with caseId [%s] and letter holder id [%s] already assigned for [%s] "
                + "Check previous logs for more information.", caseId, letterHolderId, respondentType));
        }

        Set<String> caseRoles = new HashSet<>();
        caseRoles.add(findUsersCaseRole(respondentType));

        updateCaseRoles(caseworkerUser, caseId, linkingUser.getId(), caseRoles);
    }

    private String findUsersCaseRole(RespondentType respondentType) {
        if (respondentType == RespondentType.RESP_SOLICITOR) {
            return CmsConstants.CREATOR_ROLE;
        }
        return CmsConstants.RESP_SOL_ROLE;
    }

    private RespondentType validateLetterIdAndUserType(String letterHolderId, CaseDetails caseDetails, String caseId) {
        if (caseDetails.getData() == null || StringUtils.isBlank(letterHolderId)) {
            throw new InvalidRequestException(format("Case details or letter holder data are invalid for case ID: [%s]", caseId));
        }
        String respondentLetterHolderId = (String) caseDetails.getData().get(RESP_LETTER_HOLDER_ID_FIELD);
        String coRespondentLetterHolderId = (String) caseDetails.getData().get(CO_RESP_LETTER_HOLDER_ID_FIELD);
        String solLetterHolderId = (String) caseDetails.getData().get(RESP_SOLICITOR_LETTER_HOLDER_ID_FIELD);

        if (letterHolderId.equals(respondentLetterHolderId)) {
            return RespondentType.RESPONDENT;
        } else if (letterHolderId.equals(coRespondentLetterHolderId)) {
            return RespondentType.CO_RESPONDENT;
        } else if (letterHolderId.equals(solLetterHolderId)) {
            return RespondentType.RESP_SOLICITOR;
        } else {
            throw new UnauthorizedException(
                format("Case with caseId [%s] and letter holder id [%s] mismatch.", caseDetails.getId(), letterHolderId));
        }
    }

    private boolean isValidRespondent(CaseDetails caseDetails, String userEmailAddress, RespondentType respondentType) {
        String emailField = null;
        if (respondentType == RespondentType.RESPONDENT) {
            emailField = RESP_EMAIL_ADDRESS;
        } else if (respondentType == RespondentType.RESP_SOLICITOR) {
            emailField = RESP_SOLICITOR_EMAIL_ADDRESS;
        } else {
            emailField = CO_RESP_EMAIL_ADDRESS;
        }
        Map<String, Object> caseData = caseDetails.getData();
        String caseId = Long.toString(caseDetails.getId());
        String emailAddressAssignedToCase = (String) caseData.get(emailField);

        if (emailAddressAssignedToCase == null) {
            log.info("Case {} has not been been assigned a {} yet.", caseId, respondentType);
            return true;
        } else {
            boolean emailAddressesMatch = userEmailAddress.equalsIgnoreCase(emailAddressAssignedToCase);
            log.info("Case {} has already been assigned a {}. Checking if given e-mail address matches existing...",
                caseId, respondentType);

            if (emailAddressesMatch) {
                log.info("User's e-mail address matches the {} e-mail address in the case [{}].", respondentType, caseId);
            } else {
                log.warn("User's e-mail address doesn't match the {} e-mail address in the case [{}].", respondentType, caseId);
            }

            return emailAddressesMatch;
        }
    }
}
