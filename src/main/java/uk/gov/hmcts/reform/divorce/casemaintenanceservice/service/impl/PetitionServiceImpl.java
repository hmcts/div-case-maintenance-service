package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.FormatterServiceClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.AmendCaseRemovedProps;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseStateGrouping;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivorceSessionProperties;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.event.ccd.submission.CaseSubmittedEvent;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.PetitionService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

@Service
@Slf4j
public class PetitionServiceImpl implements PetitionService,
    ApplicationListener<CaseSubmittedEvent> {

    public static final String IS_DRAFT_KEY =   "fetchedDraft";

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

        Draft draft = draftService.getDraft(authorisation);

        if ((draft == null || !isAmendPetitionDraft(draft)) && checkCcd) {
            caseDetails = ccdRetrievalService.retrieveCase(authorisation, caseStateGrouping);
        }

        if (caseDetails == null && draft != null) {
            Map<String, Object> formattedDraft = new HashMap<>(getFormattedPetition(draft, authorisation));
            formattedDraft.put(IS_DRAFT_KEY, true);
            caseDetails = CaseDetails.builder()
                .data(formattedDraft)
                .build();
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
        if (userDetails == null) {
            log.warn("No user found for token");
            return null;
        }
        final CaseDetails oldCase = this.retrievePetition(authorisation);

        if (oldCase == null) {
            log.warn("No case found for the user [{}]", userDetails.getId());
            return null;
        } else if (!oldCase.getData().containsKey(CcdCaseProperties.D8_CASE_REFERENCE)) {
            log.warn("Case [{}] has not progressed to have a Family Man reference found for the user [{}]",
                oldCase.getId(), userDetails.getId());
            return null;
        }

        final Map<String, Object> amendmentCaseDraft = this.getDraftAmendmentCase(oldCase, authorisation);
        this.deleteDraft(authorisation);
        this.createDraft(authorisation, amendmentCaseDraft, true);

        return amendmentCaseDraft;
    }

    @SuppressWarnings(value = "unchecked")
    private Map<String, Object> getDraftAmendmentCase(CaseDetails oldCase, String authorisation) {
        Map<String, Object> caseData = oldCase.getData();
        List<String> previousReasons = (ArrayList<String>) caseData
            .get(CcdCaseProperties.PREVIOUS_REASONS_DIVORCE);

        if (previousReasons == null) {
            previousReasons = new ArrayList<>();
        } else {
            // clone to avoid updating old case
            previousReasons = new ArrayList<>(previousReasons);
        }
        previousReasons.add((String) caseData.get(CcdCaseProperties.D8_REASON_FOR_DIVORCE));

        // remove all props from old case we do not want in new draft case
        AmendCaseRemovedProps.getProps().forEach(caseData::remove);

        caseData.put(CcdCaseProperties.D8_DIVORCE_UNIT, CmsConstants.CTSC_SERVICE_CENTRE);

        final Map<String, Object> amendmentCaseDraft = formatterServiceClient
            .transformToDivorceFormat(caseData, authorisation);

        amendmentCaseDraft.put(DivorceSessionProperties.PREVIOUS_CASE_ID, String.valueOf(oldCase.getId()));
        amendmentCaseDraft.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE, previousReasons);

        return amendmentCaseDraft;
    }

    private boolean isAmendPetitionDraft(Draft draft) {
        return draft.getDocument() != null && draft.getDocument()
            .containsKey(DivorceSessionProperties.PREVIOUS_CASE_ID);
    }

    private Map<String, Object> getFormattedPetition(Draft draft, String authorisation) {
        if (draftService.isInCcdFormat(draft)) {
            return formatterServiceClient.transformToDivorceFormat(draft.getDocument(), authorisation);
        } else {
            return draft.getDocument();
        }
    }
}
