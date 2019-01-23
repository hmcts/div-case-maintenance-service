package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseStateGrouping;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.CreateDraft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;

import java.util.List;
import java.util.Map;

public interface PetitionService {
    CaseDetails retrievePetition(String authorisation, Map<CaseStateGrouping, List<CaseState>> caseStateGrouping,
                                 boolean checkCcd) throws DuplicateCaseException;

    CaseDetails retrievePetition(String authorisation) throws DuplicateCaseException;

    CaseDetails retrievePetitionByCaseId(String authorisation, String caseId);

    void saveDraft(String authorisation, Map<String, Object> data, boolean divorceFormat);

    void createDraft(String authorisation, Map<String, Object> data, boolean divorceFormat);

    DraftList getAllDrafts(String authorisation);

    void deleteDraft(String authorisation);

    Map<String, Object> createAmendPetitionDraft(String authorisation) throws DuplicateCaseException;
}
