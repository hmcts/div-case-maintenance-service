package uk.gov.hmcts.reform.divorce.support;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.idam.utils.IdamUtils;
import uk.gov.hmcts.reform.divorce.model.PinResponse;
import uk.gov.hmcts.reform.divorce.model.RegisterUserRequest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.model.UserGroup;

import java.util.Locale;
import java.util.UUID;

public class IdamTestSupport {
    private static final String CASE_WORKER_USERNAME = "CASE_WORKER_USER_NAME";
    private static final String CASE_WORKER_PASSWORD = "CASE_WORKER_PASSWORD";

    private UserDetails defaultCaseWorkerUser;

    @Autowired
    private IdamUtils idamUtils;

    public UserDetails createRespondentUser(String username, String pin) {
        final UserDetails respondentUser = createNewUser(username,
            UUID.randomUUID().toString().toUpperCase(Locale.ENGLISH), false);

        final String pinAuthToken = idamUtils.authenticatePinUser(pin);

        idamUtils.upliftUser(respondentUser.getEmailAddress(),
            respondentUser.getPassword(),
            pinAuthToken);

        String upliftedUserToken = idamUtils.authenticateUser(respondentUser.getEmailAddress(),
            respondentUser.getPassword());

        respondentUser.setAuthToken(upliftedUserToken);

        return respondentUser;
    }

    public PinResponse createPinUser(String firstName) {
        final UserDetails caseWorkerUser = createAnonymousCaseWorkerUser();
        return idamUtils.generatePin(firstName, "",  caseWorkerUser.getAuthToken());
    }

    public UserDetails createAnonymousCaseWorkerUser() {
        synchronized (this) {
            if (defaultCaseWorkerUser == null) {
                defaultCaseWorkerUser = createNewUser(CASE_WORKER_USERNAME, CASE_WORKER_PASSWORD, true);
            }

            return defaultCaseWorkerUser;
        }
    }

    public UserDetails createAnonymousCitizenUser() {
        synchronized (this) {
            final String username = "simulate-delivered" + UUID.randomUUID();
            final String password = UUID.randomUUID().toString().toUpperCase(Locale.ENGLISH);

            return createNewUser(username, password, false);
        }
    }

    private UserDetails createNewUser(String username, String password, boolean caseworker) {
        final String emailAddress =  username + "@notifications.service.gov.uk";

        if (caseworker) {
            createCaseWorkerCourtAdminUserInIdam(username, emailAddress, password);
        } else {
            createCitizenUserInIdam(username, emailAddress, password);
        }

        final String authToken = idamUtils.authenticateUser(emailAddress, password);

        final String userId = idamUtils.getUserId(authToken);

        return UserDetails.builder()
            .id(userId)
            .username(username)
            .emailAddress(emailAddress)
            .password(password)
            .authToken(authToken)
            .build();
    }

    private void createCaseWorkerCourtAdminUserInIdam(String username, String emailAddress, String password) {
        final RegisterUserRequest registerUserRequest =
            RegisterUserRequest.builder()
                .email(emailAddress)
                .forename(username)
                .password(password)
                .roles(new String[]{"caseworker-divorce", "caseworker-divorce-courtadmin", "caseworker"})
                .userGroup(UserGroup.builder().code("caseworker").build())
                .build();

        idamUtils.createUserInIdam(registerUserRequest);
    }

    private void createCitizenUserInIdam(String username, String emailAddress, String password) {
        final RegisterUserRequest registerUserRequest =
            RegisterUserRequest.builder()
                .email(emailAddress)
                .forename(username)
                .password(password)
                .build();

        idamUtils.createUserInIdam(registerUserRequest);
    }
}
