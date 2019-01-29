package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.FormatterServiceClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseStateGrouping;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivorceCaseProperties;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.event.ccd.submission.CaseSubmittedEvent;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.PetitionService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

@Service
@Slf4j
public class PetitionServiceImpl implements PetitionService,
    ApplicationListener<CaseSubmittedEvent> {

    @Autowired
    private CcdRetrievalService ccdRetrievalService;

    @Autowired
    private DraftServiceImpl draftService;

    @Autowired
    private FormatterServiceClient formatterServiceClient;

    @Autowired
    private UserService userService;

    @Override
    public CaseDetails retrievePetition(String authorisation, Map<CaseStateGrouping, List<CaseState>> caseStateGrouping,
                                        boolean checkCcd) throws DuplicateCaseException {
        CaseDetails caseDetails = null;

        if (checkCcd) {
            caseDetails = ccdRetrievalService.retrieveCase(authorisation, caseStateGrouping);
        }

        if (caseDetails == null) {
            Draft draft = draftService.getDraft(authorisation);

            if (draft != null) {
                caseDetails = CaseDetails.builder()
                    .data(
                        getFormattedPetition(draft, authorisation))
                    .build();
            }
        }

        return caseDetails;
    }

    @Override
    public CaseDetails retrievePetition(String authorisation) throws DuplicateCaseException {
        return ccdRetrievalService.retrieveCase(authorisation);
    }

    @Override
    public CaseDetails retrievePetitionByCaseId(String authorisation, String caseId) {
        return ccdRetrievalService.retrieveCaseById(authorisation, caseId);
    }

    @Override
    public void saveDraft(String authorisation, Map<String, Object> data, boolean divorceFormat) {
        draftService.saveDraft(authorisation, data, divorceFormat);
    }

    @Override
    public void createDraft(String authorisation, Map<String, Object> data, boolean divorceFormat) {
        draftService.createDraft(authorisation, data, divorceFormat);
    }

    @Override
    public DraftList getAllDrafts(String authorisation) {
        return draftService.getAllDrafts(authorisation);
    }

    @Override
    public void deleteDraft(String authorisation) {
        draftService.deleteDraft(authorisation);
    }

    @Override
    public void onApplicationEvent(@Nonnull CaseSubmittedEvent event) {
        deleteDraft(event.getAuthToken());
    }

    @Override
    public Map<String, Object> createAmendedPetitionDraft(String authorisation) throws DuplicateCaseException {
        final UserDetails userDetails = userService.retrieveUserDetails(authorisation);
        final CaseDetails oldCase = this.retrievePetition(authorisation);

        if (oldCase == null) {
            log.warn("No case found for the user [{}]", userDetails.getForename());
            return null;
        } else if (oldCase.getData().get(DivorceCaseProperties.D8_CASE_REFERENCE) == null) {
            log.warn("No case which has progressed to have a Family Man reference found for the user [{}]",
                userDetails.getForename());
            return null;
        }

        final Map<String, Object> amendmentCaseDraft = this.getDraftAmendmentCase(oldCase, authorisation);
        this.deleteDraft(authorisation);
        this.createDraft(authorisation, amendmentCaseDraft, true);

        return amendmentCaseDraft;
    }

    @SuppressWarnings(value = "unchecked")
    private Map<String, Object> getDraftAmendmentCase(CaseDetails oldCase, String authorisation) {
        ArrayList<String> previousReasons = (ArrayList<String>) oldCase.getData()
            .get(DivorceCaseProperties.CCD_PREVIOUS_REASONS_FOR_DIVORCE);

        if (previousReasons == null) {
            previousReasons = new ArrayList<>();
        } else {
            // clone to avoid updating old case
            previousReasons = (ArrayList<String>) previousReasons.clone();
        }
        previousReasons.add((String) oldCase.getData().get(DivorceCaseProperties.D8_REASON_FOR_DIVORCE));

        final String oldCaseRef = oldCase.getData().get(DivorceCaseProperties.D8_CASE_REFERENCE).toString();
        final Map<String, Object> amendmentCaseDraft = formatterServiceClient
            .transformToDivorceFormat(oldCase.getData(), authorisation);

        amendmentCaseDraft.put(DivorceCaseProperties.PREVIOUS_REASONS_FOR_DIVORCE, previousReasons);
        amendmentCaseDraft.put(DivorceCaseProperties.PREVIOUS_CASE_ID, oldCaseRef);
        amendmentCaseDraft.remove(DivorceCaseProperties.CASE_REFERENCE);
        amendmentCaseDraft.remove(DivorceCaseProperties.REASON_FOR_DIVORCE);
        amendmentCaseDraft.remove(DivorceCaseProperties.HWF_NEED_HELP);
        amendmentCaseDraft.remove(DivorceCaseProperties.HWF_APPLIED_FOR_FEES);
        amendmentCaseDraft.remove(DivorceCaseProperties.HWF_REFERENCE);

        return amendmentCaseDraft;
    }

    private Map<String, Object> getFormattedPetition(Draft draft, String authorisation) {
        if (draftService.isInCcdFormat(draft)) {
            return formatterServiceClient.transformToDivorceFormat(draft.getDocument(), authorisation);
        } else {
            return draft.getDocument();
        }
    }
}
