package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.PetitionService;

@Service
public class PetitionServiceImpl implements PetitionService {

    @Autowired
    private CcdRetrievalService ccdRetrievalService;

    @Override
    public CaseDetails retrievePetition(String authorisation, boolean checkCcd) throws DuplicateCaseException {
        if(checkCcd) {
            return ccdRetrievalService.retrievePetition(authorisation);
        }

        return null;
    }
}
