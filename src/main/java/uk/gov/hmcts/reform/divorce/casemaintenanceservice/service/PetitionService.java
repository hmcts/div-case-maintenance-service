package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseStateGrouping;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;

import java.util.List;
import java.util.Map;

public interface PetitionService {
    CaseDetails retrievePetition(String authorisation, Map<CaseStateGrouping, List<CaseState>> caseStateGrouping);

    CaseDetails retrievePetition(String authorisation);

    CaseDetails retrievePetitionForAos(String authorisation);

    CaseDetails retrievePetitionByCaseId(String authorisation, String caseId);

    void saveDraft(String authorisation, Map<String, Object> data, boolean divorceFormat);

    void createDraft(String authorisation, Map<String, Object> data, boolean divorceFormat);

    DraftList getAllDrafts(String authorisation);

    void deleteDraft(String authorisation);

    Map<String, Object> createAmendedPetitionDraft(String authorisation);

    Map<String, Object> createAmendedPetitionDraftRefusalForDivorce(String authorisation);

    Map<String, Object> createAmendedPetitionDraftRefusalForCCD(String authorisation, String caseId);
}
