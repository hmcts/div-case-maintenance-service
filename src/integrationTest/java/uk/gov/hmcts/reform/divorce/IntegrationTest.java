package uk.gov.hmcts.reform.divorce;

import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
public abstract class IntegrationTest {

    private static final String CITIZEN_USER_NAME = "CaseWorkerTest";
    private static final String CITIZEN_USER_PASSWORD = "password";

    @Autowired
    private AuthTokenGenerator authTokenGenerator;

    @Autowired
    private IdamUtils idamTestSupportUtil;

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration;

    private static String userToken = null;

    public IntegrationTest() {
        this.springMethodIntegration = new SpringIntegrationMethodRule();
    }

    private synchronized String getUserToken() {
        if (userToken == null) {
            idamTestSupportUtil.createDivorceCaseworkerUserInIdam(CITIZEN_USER_NAME, CITIZEN_USER_PASSWORD);

            userToken = idamTestSupportUtil.generateUserTokenWithNoRoles(CITIZEN_USER_NAME, CITIZEN_USER_PASSWORD);
        }

        return userToken;
    }
}
