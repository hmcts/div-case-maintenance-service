package uk.gov.hmcts.reform.divorce.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.hmcts.reform.divorce.idam.utils.IdamUtils;
import uk.gov.hmcts.reform.divorce.model.PinResponse;
import uk.gov.hmcts.reform.divorce.model.RegisterUserRequest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.model.UserGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class IdamTestSupport {
    private static final String CASE_WORKER_USERNAME = "CASE_WORKER_USER_NAME";
    private static final String CASE_WORKER_PASSWORD = "caseWorkerPassword123";
    private static final String CASEWORKER_ROLE = "caseworker";
    private static final String CASEWORKER_CITIZEN_ROLE = "caseworker-citizen";
    private static final String CITIZEN_ROLE = "citizen";

    private UserDetails defaultCaseWorkerUser;

    @Value("${idam.caseworker.password}")
    private String caseWorkerPassword;

    @Autowired
    private IdamUtils idamUtils;

    public UserDetails createRespondentUser(String username, String pin) {
        final UserDetails respondentUser = createNewUser(username,
            "genericPassword123", CITIZEN_ROLE);

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
                defaultCaseWorkerUser = createNewUser(
                    CASE_WORKER_USERNAME,
                    CASE_WORKER_PASSWORD,
                    CASEWORKER_CITIZEN_ROLE
                );
            }

            return defaultCaseWorkerUser;
        }
    }

    public UserDetails createPureCaseWorkerUser() {
        synchronized (this) {
            final String username = "simulate-delivered" + UUID.randomUUID();
            final String password = caseWorkerPassword;

            return createNewUser(username, password, CASEWORKER_ROLE);
        }
    }

    public UserDetails createAnonymousCitizenUser() {
        synchronized (this) {
            final String username = "simulate-delivered" + UUID.randomUUID();
            final String password = "genericPassword123";

            return createNewUser(username, password, CITIZEN_ROLE);
        }
    }

    private UserDetails createNewUser(String username, String password, String roleType) {
        final String emailAddress =  username + "@notifications.service.gov.uk";

        if (CASEWORKER_CITIZEN_ROLE.equals(roleType)) {
            createCaseWorkerCourtAdminUserInIdam(username, emailAddress, password, true);
        } else if (CASEWORKER_ROLE.equals(roleType)) {
            createCaseWorkerCourtAdminUserInIdam(username, emailAddress, password, false);
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

    private void createCaseWorkerCourtAdminUserInIdam(String username, String emailAddress, String password,
                                                      Boolean citizenRole) {
        List<UserGroup> roles = new ArrayList<>(Arrays.asList(
            UserGroup.builder().code("caseworker-divorce-courtadmin").build(),
            UserGroup.builder().code("caseworker-divorce").build(),
            UserGroup.builder().code("caseworker").build()
        ));

        if (citizenRole) {
            roles.add(UserGroup.builder().code("citizen").build());
        }

        final RegisterUserRequest registerUserRequest =
            RegisterUserRequest.builder()
                .email(emailAddress)
                .forename(username)
                .password(password)
                .roles(roles.toArray(new UserGroup[roles.size()]))
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
                .roles(new UserGroup[]{ UserGroup.builder().code("citizen").build() })
                .userGroup(UserGroup.builder().code("divorce-private-beta").build())
                .build();

        idamUtils.createUserInIdam(registerUserRequest);
    }
}
