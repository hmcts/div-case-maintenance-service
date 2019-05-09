package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import lombok.extern.slf4j.Slf4j;
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

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_LETTER_HOLDER_ID_FIELD;

@Service
@Slf4j
public class CcdAccessServiceImpl extends BaseCcdCaseService implements CcdAccessService {

    @Autowired
    private CaseAccessApi caseAccessApi;

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

        if (!isValidUser(caseDetails, linkingUser.getEmail(), letterHolderId, caseId)) {
            throw new UnauthorizedException(
                String.format("Case with caseId [%s] and letter holder id [%s] already assigned or letter holder mismatch",
                    caseId, letterHolderId));//TODO - we could have two different errors for letter already assigned and letter holder mismatch, if possible
        }

        grantAccessToCase(caseworkerUser, caseId, linkingUser.getId());
    }

    private void isValidCaseAndLetterHolder(CaseDetails caseDetails, String letterHolderId) {
        if (caseDetails.getData() == null || StringUtils.isBlank(letterHolderId)) {
            throw new InvalidRequestException("Case details or letter holder data are invalid");
        }
    }

    private boolean isValidUser(CaseDetails caseDetails, String userEmailAddress, String letterHolderId, String caseId) {
        return isValidRespondentUser(caseId, caseDetails.getData(), userEmailAddress, letterHolderId)
            || isValidCoRespondentUser(caseId, caseDetails.getData(), userEmailAddress, letterHolderId);
    }

    private static boolean isValidRespondentUser(String caseId, Map<String, Object> caseData, String userEmailAddress, String letterHolderId) {
        Optional<String> emailAddressAssignedToCase = Optional.ofNullable(caseData.get(RESP_EMAIL_ADDRESS)).map(String.class::cast);

        if (!emailAddressAssignedToCase.isPresent()) {
            log.info("Case {} has not been been assigned a respondent yet.", caseId);

            return areLetterHolderIdsMatching(caseId, caseData.get(RESP_LETTER_HOLDER_ID_FIELD), letterHolderId);
        } else {
            log.info("Case {} has already been assigned a respondent. Checking if given e-mail address matches the assigned respondent's...", caseId);

            boolean emailAddressesMatch = userEmailAddress.equalsIgnoreCase(emailAddressAssignedToCase.get());

            if (emailAddressesMatch) {
                log.info("User's e-mail address matches the respondent's e-mail address in the case [{}].", caseId);
            } else {
                log.warn("User's e-mail address doesn't match the respondent's e-mail address in the case [{}].", caseId);
            }

            return emailAddressesMatch;
        }
    }

    private static boolean isValidCoRespondentUser(String caseId, Map<String, Object> caseData, String userEmailAddress, String letterHolderId) {
        Optional<String> emailAddressAssignedToCase = Optional.ofNullable(caseData.get(CO_RESP_EMAIL_ADDRESS)).map(String.class::cast);

        if (!emailAddressAssignedToCase.isPresent()) {
            log.info("Case {} has not been been assigned a co-respondent yet.", caseId);

            return areLetterHolderIdsMatching(caseId, caseData.get(CO_RESP_LETTER_HOLDER_ID_FIELD), letterHolderId);
        } else {
            log.info("Case {} has already been assigned a co-respondent. Checking if given e-mail address matches the assigned co-respondent's...", caseId);

            boolean emailAddressesMatch = userEmailAddress.equalsIgnoreCase(emailAddressAssignedToCase.get());

            if (emailAddressesMatch) {
                log.info("User's e-mail address matches the co-respondent's e-mail address in the case [{}].", caseId);
            } else {
                log.warn("User's e-mail address doesn't match the co-respondent's e-mail address in the case [{}].", caseId);
            }

            return emailAddressesMatch;
        }
    }

    private static boolean areLetterHolderIdsMatching(String caseId, Object caseLetterHolderId, String givenLetterHolderId) {
        boolean letterHolderIdsMatch = givenLetterHolderId.equals(caseLetterHolderId);

        if (letterHolderIdsMatch) {
            log.info("Letter holder ids match for case {}", caseId);
        } else {
            log.warn("Letter holder ids for case {} do not match. Given letter holder id is [{}] but case letter holder id is [{}].", caseId, givenLetterHolderId, caseLetterHolderId);
        }

        return letterHolderIdsMatch;
    }

}