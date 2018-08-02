package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;

public interface CcdRetrievalService {
    CaseDetails retrievePetition(String authorisation) throws DuplicateCaseException;
}
