package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;

public interface UserService {
    UserDetails retrieveUserDetails(String authorisation);

    UserDetails retrieveAnonymousCaseWorkerDetails();
}
