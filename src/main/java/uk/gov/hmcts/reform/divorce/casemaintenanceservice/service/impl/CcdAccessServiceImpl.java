package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.CaseUserApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.CaseUser;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.CaseNotFoundException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.InvalidRequestException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.UnauthorizedException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdAccessService;
import uk.gov.hmcts.reform.idam.client.models.User;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_RESPONDENT_SOLICITOR_COMPANY;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_RESPONDENT_SOLICITOR_NAME;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_LETTER_HOLDER_ID_FIELD;

@Service
@Slf4j
public class CcdAccessServiceImpl extends BaseCcdCaseService implements CcdAccessService {

    @Autowired
    private CaseUserApi caseUserApi;

    private enum RespondentType {
        RESPONDENT,
        CO_RESPONDENT,
        RESP_SOLICITOR
    }

    @Override
    public void unlinkRespondent(String authorisation, String caseId) {
        User caseworkerUser = getAnonymousCaseWorkerDetails();

        User linkedUser = getUser(authorisation);

        updateCaseRoles(caseworkerUser, caseId, linkedUser.getUserDetails().getId(), null);
    }

    @Override
    public void addPetitionerSolicitorRole(String authorisation, String caseId) {
        User solicitorUser = getUser(authorisation);
        User caseworkerUser = getAnonymousCaseWorkerDetails();
        Set<String> caseRoles = new HashSet<>();
        caseRoles.add(CmsConstants.CREATOR_ROLE);
        caseRoles.add(CmsConstants.PET_SOL_ROLE);
        updateCaseRoles(caseworkerUser, caseId, solicitorUser.getUserDetails().getId(), caseRoles);
    }

    @Override
    public void linkRespondent(String authorisation, String caseId, String letterHolderId) {
        User caseworkerUser = getAnonymousCaseWorkerDetails();

        CaseDetails caseDetails = coreCaseDataApi.readForCaseWorker(
            caseworkerUser.getAuthToken(),
            getServiceAuthToken(),
            caseworkerUser.getUserDetails().getId(),
            jurisdictionId,
            caseType,
            caseId
        );

        if (caseDetails == null) {
            throw new CaseNotFoundException(
                format("Case with caseId [%s] and letter holder id [%s] not found", caseId, letterHolderId)
            );
        }

        RespondentType respondentType = validateLetterIdAndUserType(letterHolderId, caseDetails, caseId);

        User linkingUser = getUser(authorisation);

        if (!isValidRespondent(caseDetails, linkingUser.getUserDetails().getEmail(), respondentType)) {
            throw new UnauthorizedException(
                format(
                    "Case with caseId [%s] and letter holder id [%s] already assigned for [%s] "
                        + "or Petitioner attempted to link case. Check previous logs for more information.",
                    caseId,
                    letterHolderId,
                    respondentType
                )
            );
        }

        updateCaseRoles(caseworkerUser, caseId, linkingUser.getUserDetails().getId(), getRolesForRespondentType(respondentType));
    }

    private void updateCaseRoles(User anonymousCaseWorker, String caseId, String userId, Set<String> caseRoles) {
        caseUserApi.updateCaseRolesForUser(
            anonymousCaseWorker.getAuthToken(),
            getServiceAuthToken(),
            caseId,
            userId,
            new CaseUser(userId, caseRoles)
        );
    }

    private Set<String> getRolesForRespondentType(RespondentType respondentType) {

        Set<String> caseRoles = new HashSet<>();
        caseRoles.add(CmsConstants.CREATOR_ROLE);

        if (respondentType == RespondentType.RESP_SOLICITOR) {
            caseRoles.add(CmsConstants.RESP_SOL_ROLE);
        }
        return caseRoles;
    }

    private RespondentType validateLetterIdAndUserType(String letterHolderId, CaseDetails caseDetails, String caseId) {
        if (caseDetails.getData() == null || StringUtils.isBlank(letterHolderId)) {
            throw new InvalidRequestException(format("Case details or letter holder data are invalid for case ID: [%s]", caseId));
        }
        String respondentLetterHolderId = (String) caseDetails.getData().get(RESP_LETTER_HOLDER_ID_FIELD);
        String coRespondentLetterHolderId = (String) caseDetails.getData().get(CO_RESP_LETTER_HOLDER_ID_FIELD);

        if (letterHolderId.equals(respondentLetterHolderId)) {
            if (isRespondentSolicitorInformationPresent(caseDetails.getData())) {
                return RespondentType.RESP_SOLICITOR;
            }
            return RespondentType.RESPONDENT;
        } else if (letterHolderId.equals(coRespondentLetterHolderId)) {
            return RespondentType.CO_RESPONDENT;
        }

        throw new UnauthorizedException(
            format("Case with caseId [%s] and letter holder id [%s] mismatch.", caseDetails.getId(), letterHolderId)
        );
    }

    private boolean isValidRespondent(CaseDetails caseDetails, String userEmailAddress, RespondentType respondentType) {
        String emailField = (respondentType == RespondentType.RESPONDENT) ? RESP_EMAIL_ADDRESS : CO_RESP_EMAIL_ADDRESS;
        Map<String, Object> caseData = caseDetails.getData();
        String caseId = Long.toString(caseDetails.getId());
        String emailAddressAssignedToCase = (String) caseData.get(emailField);

        if (emailAddressAssignedToCase == null) {
            log.info("Case {} has not been been assigned a {} yet.", caseId, respondentType);
            String petitionerEmail = (String) caseData.get(D8_PETITIONER_EMAIL);

            if (userEmailAddress.equalsIgnoreCase(petitionerEmail)) {
                log.warn("Attempt made to link petitioner as {} to case {}. Failed validation.", respondentType, caseId);
                return false;
            }

            return true;
        }

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

    private boolean isRespondentSolicitorInformationPresent(Map<String, Object> caseData) {
        // temporary fix until we implement setting respondentSolicitorRepresented from CCD for RespSols

        final String respondentSolicitorName = (String) caseData.get(D8_RESPONDENT_SOLICITOR_NAME);
        final String respondentSolicitorCompany = (String) caseData.get(D8_RESPONDENT_SOLICITOR_COMPANY);

        return ((respondentSolicitorName != null) && (respondentSolicitorCompany != null));
    }
}
