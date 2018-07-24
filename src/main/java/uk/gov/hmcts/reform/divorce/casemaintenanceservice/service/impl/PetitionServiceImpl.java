package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.PetitionService;

@Service
public class PetitionServiceImpl implements PetitionService {

    @Autowired
    private CcdRetrievalService ccdRetrievalService;

    @Autowired
    private DraftServiceImpl draftService;

    @Override
    public CaseDetails retrievePetition(String authorisation, boolean checkCcd) throws DuplicateCaseException {
        if (checkCcd) {
            return ccdRetrievalService.retrievePetition(authorisation);
//        } else {
//            return draftService.
        }

        return null;
    }

    @Override
    public void saveDraft(String authorisation, JsonNode data){
        draftService.saveDraft(authorisation, data);
    }

    @Override
    public DraftList testMethod(String authorisation){
        return draftService.getAllDrafts(authorisation);
    }
}
