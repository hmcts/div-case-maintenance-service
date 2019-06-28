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

import static uk.gov.hmcts.reform.divorce.support.UserRoleType.CASEWORKER_ROLE;
import static uk.gov.hmcts.reform.divorce.support.UserRoleType.CITIZEN_ROLE;
import static uk.gov.hmcts.reform.divorce.support.UserRoleType.SOLICITOR_ROLE;

@Slf4j
public class IdamTestSupport {
    private static final String GENERIC_PASSWORD = "genericPassword123";

    private UserDetails defaultCaseWorkerUser;

    @Autowired
    private IdamUtils idamUtils;

    public UserDetails createRespondentUser(String username, String pin) {
        final UserDetails respondentUser = createNewUser(username, CITIZEN_ROLE);

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
                defaultCaseWorkerUser = createNewUser(username, CASEWORKER_ROLE);
            }

            return defaultCaseWorkerUser;
        }
    }

    public UserDetails createAnonymousCitizenUser() {
        synchronized (this) {
            final String username = "simulate-delivered" + UUID.randomUUID();
            return createNewUser(username, CITIZEN_ROLE);
        }
    }

    public UserDetails createAnonymousSolicitorUser() {
        synchronized (this) {
            final String username = "simulate-delivered" + UUID.randomUUID();
            return createNewUser(username, SOLICITOR_ROLE);
        }
    }

    private UserDetails createNewUser(String username, UserRoleType roleType) {
        final String emailAddress =  username + "@notifications.service.gov.uk";

        if (roleType == CASEWORKER_ROLE || roleType == SOLICITOR_ROLE) {
            createCaseWorkerUserInIdam(username, emailAddress, roleType);
        } else if (roleType == CITIZEN_ROLE) {
            createUserInIdam(username, emailAddress);
        }

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

    private void createCaseWorkerUserInIdam(String username, String emailAddress, UserRoleType roleType) {
        List<UserGroup> roles = new ArrayList<>();
        roles.add(UserGroup.builder().code("caseworker").build());
        roles.add(UserGroup.builder().code("caseworker-divorce").build());

        if (roleType == CASEWORKER_ROLE) {
            roles.addAll(Arrays.asList(
                UserGroup.builder().code("caseworker-divorce-courtadmin_beta").build(),
                UserGroup.builder().code("caseworker-divorce-courtadmin").build()
            ));
        } else if (roleType == SOLICITOR_ROLE) {
            roles.add(UserGroup.builder().code("caseworker-divorce-solicitor").build());
        }

        final RegisterUserRequest registerUserRequest =
            RegisterUserRequest.builder()
                .email(emailAddress)
                .forename(username)
                .password(GENERIC_PASSWORD)
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

    private void createUserInIdam(String username, String emailAddress) {
        final RegisterUserRequest registerUserRequest =
            RegisterUserRequest.builder()
                .email(emailAddress)
                .forename(username)
                .password(GENERIC_PASSWORD)
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
}
