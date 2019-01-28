package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.FormatterServiceClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivorceCaseProperties;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseStateGrouping;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.event.ccd.submission.CaseSubmittedEvent;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.PetitionService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

@Service
@Slf4j
public class PetitionServiceImpl implements PetitionService, ApplicationListener<CaseSubmittedEvent> {

    @Autowired
    private CcdRetrievalService ccdRetrievalService;

    @Autowired
    private DraftServiceImpl draftService;

    @Autowired
    private FormatterServiceClient formatterServiceClient;

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
    @SuppressWarnings (value="unchecked")
    public Map<String, Object> createAmendedPetitionDraft(String authorisation) throws DuplicateCaseException {
        CaseDetails oldCase = this.retrievePetition(authorisation);
        if (oldCase == null || oldCase.getData().get(DivorceCaseProperties.D8_CASE_REFERENCE.getValue()) == null) {
            return null;
        }
        final String oldCaseRef = oldCase.getData().get(DivorceCaseProperties.D8_CASE_REFERENCE.getValue()).toString();

        HashMap<String, Object> draftDocument = (HashMap<String, Object>) formatterServiceClient
            .transformToDivorceFormat(oldCase.getData(), authorisation);

        List<String> previousReasons = (List<String>) oldCase.getData()
            .get(DivorceCaseProperties.CCD_PREVIOUS_REASONS_FOR_DIVORCE.getValue());

        if (previousReasons == null) {
            previousReasons = new ArrayList<>();
        }
        previousReasons.add((String) oldCase.getData().get(DivorceCaseProperties.D8_REASON_FOR_DIVORCE.getValue()));

        draftDocument.put(DivorceCaseProperties.PREVIOUS_REASONS_FOR_DIVORCE.getValue(), previousReasons);
        draftDocument.put(DivorceCaseProperties.PREVIOUS_CASE_ID.getValue(), oldCaseRef);
        draftDocument.put(DivorceCaseProperties.CASE_REFERENCE.getValue(), null);
        draftDocument.put(DivorceCaseProperties.REASON_FOR_DIVORCE.getValue(), null);
        draftDocument.put(DivorceCaseProperties.HWF_NEED_HELP.getValue(), null);
        draftDocument.put(DivorceCaseProperties.HWF_APPLIED_FOR_FEES.getValue(), null);
        draftDocument.put(DivorceCaseProperties.HWF_REFERENCE.getValue(), null);

        this.createDraft(authorisation, draftDocument, true);
        return draftDocument;
    }

    private Map<String, Object> getFormattedPetition(Draft draft, String authorisation) {
        if (draftService.isInCcdFormat(draft)) {
            return formatterServiceClient.transformToDivorceFormat(draft.getDocument(), authorisation);
        } else {
            return draft.getDocument();
        }
    }
}
