package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;

public interface PetitionService {
    CaseDetails retrievePetition(String authorisation, boolean checkCcd) throws DuplicateCaseException;
}
