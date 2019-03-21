package uk.gov.hmcts.reform.divorce.support;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.idam.utils.IdamUtils;
import uk.gov.hmcts.reform.divorce.model.PinResponse;
import uk.gov.hmcts.reform.divorce.model.RegisterUserRequest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.model.UserGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class IdamTestSupport {
    private static final String CASEWORKER_ROLE = "caseworker";
    private static final String CITIZEN_ROLE = "citizen";
    private static final String GENERIC_PASSWORD = "genericPassword123";

    private UserDetails defaultCaseWorkerUser;

    @Autowired
    private IdamUtils idamUtils;

    public UserDetails createRespondentUser(String username, String pin) {
        final UserDetails respondentUser = createNewUser(username, GENERIC_PASSWORD, CITIZEN_ROLE);

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
        return idamUtils.generatePin(firstName, "",  createAnonymousCitizenUser().getAuthToken());
    }

    public UserDetails createAnonymousCaseWorkerUser() {
        synchronized (this) {
            if (defaultCaseWorkerUser == null) {
                final String username = "simulate-delivered" + UUID.randomUUID();
                final String password = GENERIC_PASSWORD;

                defaultCaseWorkerUser = createNewUser(username, password, CASEWORKER_ROLE);
            }

            return defaultCaseWorkerUser;
        }
    }

    public UserDetails createAnonymousCitizenUser() {
        synchronized (this) {
            final String username = "simulate-delivered" + UUID.randomUUID();
            final String password = GENERIC_PASSWORD;

            return createNewUser(username, password, CITIZEN_ROLE);
        }
    }

    private UserDetails createNewUser(String username, String password, String roleType) {
        final String emailAddress =  username + "@notifications.service.gov.uk";

        if (CASEWORKER_ROLE.equals(roleType)) {
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
        List<UserGroup> roles = new ArrayList<>(Arrays.asList(
            UserGroup.builder().code("caseworker-divorce-courtadmin_beta").build(),
            UserGroup.builder().code("caseworker-divorce-courtadmin").build(),
            UserGroup.builder().code("caseworker-divorce").build(),
            UserGroup.builder().code("caseworker").build()
        ));

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
                .userGroup(UserGroup.builder().code("citizens").build())
                .build();

        idamUtils.createUserInIdam(registerUserRequest);
    }
}
