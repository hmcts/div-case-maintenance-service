package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.IdamApiClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.AuthenticateUserResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.TokenExchangeResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;

import java.util.Base64;

@Service
public class UserServiceImpl implements UserService {
    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String CODE = "code";
    private static final String BASIC = "Basic ";

    @Value("${idam.api.redirect-url}")
    private String authRedirectUrl;

    @Value("${auth2.client.id}")
    private String authClientId;

    @Value("${auth2.client.secret}")
    private String authClientSecret;

    @Value("${idam.caseworker.username}")
    private String caseworkerUserName;

    @Value("${idam.caseworker.password}")
    private String caseworkerPassword;

    @Autowired
    private IdamApiClient idamApiClient;

    @Override
    public UserDetails retrieveUserDetails(String authorisation) {
        UserDetails userDetails = idamApiClient.retrieveUserDetails(authorisation);
        userDetails.setAuthToken(authorisation);

        return userDetails;
    }

    @Override
    public UserDetails retrieveAnonymousCaseWorkerDetails() {
        return retrieveUserDetails(getIdamOauth2Token(caseworkerUserName, caseworkerPassword));
    }

    private String getIdamOauth2Token(String username, String password) {
        AuthenticateUserResponse authenticateUserResponse = idamApiClient.authenticateUser(
            getBasicAuthHeader(username, password),
            CODE,
            authClientId,
            authRedirectUrl
        );

        TokenExchangeResponse tokenExchangeResponse = idamApiClient.exchangeCode(
            authenticateUserResponse.getCode(),
            AUTHORIZATION_CODE,
            authRedirectUrl,
            authClientId,
            authClientSecret
        );

        return BEARER + tokenExchangeResponse.getAccessToken();
    }

    private String getBasicAuthHeader(String username, String password) {
        String authorisation = username + ":" + password;
        return BASIC + Base64.getEncoder().encodeToString(authorisation.getBytes());
    }
}
