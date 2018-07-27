package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.event.ccd.submission.CaseSubmittedEvent;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.formatterservice.FormatterServiceClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.PetitionService;

import java.util.Map;

@Service
public class PetitionServiceImpl implements PetitionService, ApplicationListener<CaseSubmittedEvent> {

    @Autowired
    private CcdRetrievalService ccdRetrievalService;

    @Autowired
    private DraftServiceImpl draftService;

    @Autowired
    private FormatterServiceClient formatterServiceClient;

    @Override
    public CaseDetails retrievePetition(String authorisation, boolean checkCcd) throws DuplicateCaseException {
        CaseDetails caseDetails = null;

        if (checkCcd) {
            caseDetails = ccdRetrievalService.retrievePetition(authorisation);
        }

        if(caseDetails == null) {
            //check it in the draft store
            Draft draft = draftService.getDraft(authorisation);

            if(draft != null){
                caseDetails = CaseDetails.builder()
                    .data(
                        getFormattedPetition(draft, authorisation))
                    .build();
            }
        }

        return caseDetails;
    }

    @Override
    public void saveDraft(String authorisation, Map<String, Object> data){
        draftService.saveDraft(authorisation, data);
    }

    @Override
    public DraftList getAllDrafts(String authorisation){
        return draftService.getAllDrafts(authorisation);
    }

    @Override
    public void deleteDraft(String authorisation) {
        draftService.deleteDraft(authorisation);
    }

    @Override
    public void onApplicationEvent(CaseSubmittedEvent event) {
        deleteDraft(event.getAuthToken());
    }

    private Map<String, Object> getFormattedPetition(Draft draft, String authorisation){
        if(draft.isInCcdFormat()){
            return draft.getDocument();
        } else {
            return formatterServiceClient.transformToCCDFormat(draft.getDocument(), authorisation);
        }
    }
}
