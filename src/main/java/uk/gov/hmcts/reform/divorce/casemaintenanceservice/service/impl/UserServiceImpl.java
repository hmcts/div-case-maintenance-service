package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;
import uk.gov.hmcts.reform.idam.client.IdamClient;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

@Service
public class UserServiceImpl implements UserService {

    @Value("${idam.caseworker.username}")
    private String caseworkerUserName;

    @Value("${idam.caseworker.password}")
    private String caseworkerPassword;

    @Autowired
    private IdamClient idamClient;

    @Override
    public User retrieveUser(String authorisation) {
        UserDetails userDetails = idamClient.getUserDetails(authorisation);

        return new User(authorisation, userDetails);
    }

    @Override
    public User retrieveAnonymousCaseWorkerDetails() {
        return retrieveUser(getIdamOauth2Token(caseworkerUserName, caseworkerPassword));
    }

    private String getIdamOauth2Token(String username, String password) {
        return idamClient.authenticateUser(username, password);
    }

}
