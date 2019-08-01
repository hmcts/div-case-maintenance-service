package uk.gov.hmcts.reform.divorce.support;

import lombok.extern.slf4j.Slf4j;
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
import java.util.function.Supplier;

@Slf4j
public class IdamTestSupport {
    private static final String CASEWORKER_ROLE = "caseworker";
    private static final String CITIZEN_ROLE = "citizen";
    private static final String GENERIC_PASSWORD = "genericPassword123";

    private UserDetails defaultCaseWorkerUser;

    @Autowired
    private IdamUtils idamUtils;

    public UserDetails createRespondentUser(String username, String pin) {
        return wrapInRetry(() -> {
            final UserDetails respondentUser = createNewUser(username, GENERIC_PASSWORD, CITIZEN_ROLE);

            final String pinAuthToken = idamUtils.authenticatePinUser(pin);

            idamUtils.upliftUser(respondentUser.getEmailAddress(),
                respondentUser.getPassword(),
                pinAuthToken);

            String upliftedUserToken = idamUtils.authenticateUser(respondentUser.getEmailAddress(),
                respondentUser.getPassword());

            respondentUser.setAuthToken(upliftedUserToken);

            return respondentUser;
        });
    }

    public PinResponse createPinUser(String firstName) {
        return idamUtils.generatePin(firstName, "",  createAnonymousCitizenUser().getAuthToken());
    }

    public UserDetails createAnonymousCaseWorkerUser() {
        return wrapInRetry(() -> {
            synchronized (this) {
                if (defaultCaseWorkerUser == null) {
                    final String username = "simulate-delivered" + UUID.randomUUID();
                    final String password = GENERIC_PASSWORD;

                    defaultCaseWorkerUser = createNewUser(username, password, CASEWORKER_ROLE);
                }

                return defaultCaseWorkerUser;
            }
        });
    }

    public UserDetails createAnonymousCitizenUser() {
        return wrapInRetry(() -> {
            synchronized (this) {
                final String username = "simulate-delivered" + UUID.randomUUID();
                final String password = GENERIC_PASSWORD;

                return createNewUser(username, password, CITIZEN_ROLE);
            }
        });
    }

    private UserDetails createNewUser(String username, String password, String roleType) {
        final String emailAddress =  username + "@mailinator.com";

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

        try {
            //give the user some time to warm up..
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.debug("IDAM waiting thread was interrupted");
        }
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

        try {
            //give the user some time to warm up..
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.debug("IDAM waiting thread was interrupted");
        }
    }

    private UserDetails wrapInRetry(Supplier<UserDetails> supplier) {
        //tactical solution as sometimes the newly created user is somehow corrupted and won't generate a code..
        int count = 0;
        int maxTries = 5;
        while (true) {
            try {
                return supplier.get();
            } catch (Exception e) {
                if (++count == maxTries) {
                    log.error("Exhausted the number of maximum retry attempts..", e);
                    throw e;
                }
                try {
                    //some backoff time
                    Thread.sleep(200);
                } catch (InterruptedException ex) {
                    log.error("Error during sleep", ex);
                }
                log.trace("Encountered an error creating a user/token - retrying", e);
            }
        }
    }
}
