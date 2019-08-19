package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service;

import uk.gov.hmcts.reform.idam.client.models.User;

public interface UserService {
    User retrieveUser(String authorisation);

    User retrieveAnonymousCaseWorkerDetails();
}
