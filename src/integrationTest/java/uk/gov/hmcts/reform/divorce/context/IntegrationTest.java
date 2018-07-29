package uk.gov.hmcts.reform.divorce.context;

import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.util.IdamUtils;

import java.util.UUID;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
public abstract class IntegrationTest {
    @Value("${case.maintenance.service.base.uri}")
    protected String serverUrl;

    @Autowired
    private IdamUtils idamTestSupportUtil;

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration;

    protected IntegrationTest() {
        this.springMethodIntegration = new SpringIntegrationMethodRule();
    }

    protected synchronized UserDetails getUserDetails() {
        final String username = "simulate-delivered" + UUID.randomUUID();
        final String emailAddress =  username + "@notifications.service.gov.uk";
        final String password = UUID.randomUUID().toString();

        idamTestSupportUtil.createUserInIdam(username, emailAddress, password);
        final String authToken = idamTestSupportUtil.generateUserTokenWithNoRoles(emailAddress, password);

        return UserDetails.builder()
            .username(username)
            .emailAddress(emailAddress)
            .password(password)
            .authToken(authToken)
            .build();
    }

    protected String getUserToken() {
        return getUserDetails().getAuthToken();
    }
}
