package uk.gov.hmcts.reform.divorce.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.divorce.idam.utils.IdamUtils;
import uk.gov.hmcts.reform.divorce.model.PinResponse;
import uk.gov.hmcts.reform.divorce.model.RegisterUserRequest;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.model.UserGroup;

import java.util.UUID;
import java.util.function.Supplier;

@Slf4j
public class IdamTestSupport {
    private static final String CASE_WORKER_USERNAME = "TEST_CASE_WORKER_USER";
    private static final String SOLICITOR_USER_NAME = "TEST_SOLICITOR";
    private static final String EMAIL_DOMAIN = "@mailinator.com";
    private static final String GENERIC_PASSWORD = "genericPassword123";

    private UserDetails defaultCaseWorkerUser;
    private UserDetails defaultSolicitorUser;

    @Autowired
    private IdamUtils idamUtils;

    public UserDetails createRespondentUser(String username, String pin) {
        return wrapInRetry(() -> {
            final UserDetails respondentUser = createNewUser(username, GENERIC_PASSWORD);

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

    public UserDetails getCaseworkerUser() {
        return wrapInRetry(() -> {
            synchronized (this) {
                if (defaultCaseWorkerUser == null) {
                    String emailAddress = CASE_WORKER_USERNAME + EMAIL_DOMAIN;
                    defaultCaseWorkerUser = getUser(CASE_WORKER_USERNAME, emailAddress, GENERIC_PASSWORD);
                }

                return defaultCaseWorkerUser;
            }
        });
    }

    public UserDetails getSolicitorUser() {
        return wrapInRetry(() -> {
            synchronized (this) {
                if (defaultSolicitorUser == null) {
                    String emailAddress = SOLICITOR_USER_NAME + EMAIL_DOMAIN;
                    defaultSolicitorUser = getUser(SOLICITOR_USER_NAME, emailAddress, GENERIC_PASSWORD);
                }

                return defaultSolicitorUser;
            }
        });
    }

    public UserDetails createAnonymousCitizenUser() {
        return wrapInRetry(() -> {
            synchronized (this) {
                final String username = "simulate-delivered" + UUID.randomUUID();
                final String password = GENERIC_PASSWORD;

                return createNewUser(username, password);
            }
        });
    }

    private UserDetails createNewUser(String username, String password) {
        final String emailAddress =  username + "@mailinator.com";

        createCitizenUserInIdam(username, emailAddress, password);

        return getUser(username, emailAddress, password);
    }

    private UserDetails getUser(String username, String emailAddress, String password) {
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
