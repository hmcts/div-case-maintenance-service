package uk.gov.hmcts.reform.divorce.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.idam.utils.IdamUtils;
import uk.gov.hmcts.reform.divorce.model.PinResponse;
import uk.gov.hmcts.reform.divorce.model.RegisterUserRequest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.model.UserGroup;

import java.util.UUID;

@Slf4j
public class IdamTestSupport {
    private static final String GENERIC_PASSWORD = "genericPassword123";
    private static final UserGroup CITIZENS_USER_GROUP = UserGroup.builder().code("citizens").build();
    private static final UserGroup CITIZEN = UserGroup.builder().code("citizen").build();
    private static final UserGroup CASEWORKER_USER_GROUP = UserGroup.builder().code("caseworker").build();
    private static final UserGroup CASEWORKER = UserGroup.builder().code("caseworker").build();
    private static final UserGroup CASEWORKER_DIVORCE = UserGroup.builder().code("caseworker-divorce").build();
    private static final UserGroup CASEWORKER_ADMIN = UserGroup.builder().code("caseworker-divorce-courtadmin").build();
    private static final UserGroup CASEWORKER_ADMIN_BETA = UserGroup.builder().code("caseworker-divorce-courtadmin_beta").build();
    private static final UserGroup CASEWORKER_SOLICITOR = UserGroup.builder().code("caseworker-divorce-solicitor").build();

    private UserDetails defaultCaseWorkerUser;

    @Autowired
    private IdamUtils idamUtils;

    public UserDetails createRespondentUser(String username, String pin) {
        final UserDetails respondentUser = createNewUser(username, CITIZENS_USER_GROUP, CITIZEN);

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
                defaultCaseWorkerUser = createNewUser(username, CASEWORKER_USER_GROUP,
                    CASEWORKER,
                    CASEWORKER_DIVORCE,
                    CASEWORKER_ADMIN,
                    CASEWORKER_ADMIN_BETA
                );
            }

            return defaultCaseWorkerUser;
        }
    }

    public UserDetails createAnonymousCitizenUser() {
        synchronized (this) {
            final String username = "simulate-delivered" + UUID.randomUUID();
            return createNewUser(username, CITIZENS_USER_GROUP, CITIZEN);
        }
    }

    public UserDetails createAnonymousSolicitorUser() {
        synchronized (this) {
            final String username = "simulate-delivered" + UUID.randomUUID();
            return createNewUser(username, CASEWORKER_USER_GROUP, CASEWORKER, CASEWORKER_DIVORCE, CASEWORKER_SOLICITOR);
        }
    }

    private UserDetails createNewUser(String username, UserGroup userGroup, UserGroup... roles) {
        final String emailAddress =  username + "@notifications.service.gov.uk";
        createUserInIdam(username, emailAddress, userGroup, roles);

        final String authToken = idamUtils.authenticateUser(emailAddress, GENERIC_PASSWORD);
        final String userId = idamUtils.getUserId(authToken);

        return UserDetails.builder()
            .id(userId)
            .username(username)
            .emailAddress(emailAddress)
            .password(GENERIC_PASSWORD)
            .authToken(authToken)
            .build();
    }

    private void createUserInIdam(String username, String emailAddress, UserGroup userGroup, UserGroup... roles) {
        final RegisterUserRequest registerUserRequest =
            RegisterUserRequest.builder()
                .email(emailAddress)
                .forename(username)
                .password(GENERIC_PASSWORD)
                .roles(roles)
                .userGroup(userGroup)
                .build();

        idamUtils.createUserInIdam(registerUserRequest);

        try {
            //give the user some time to warm up..
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.debug("IDAM waiting thread was interrupted");
        }
    }
}
