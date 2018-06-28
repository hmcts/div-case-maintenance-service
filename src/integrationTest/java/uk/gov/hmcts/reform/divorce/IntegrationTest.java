package uk.gov.hmcts.reform.divorce;

import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
public abstract class IntegrationTest {
    @Value("${case.maintenance.service.base.uri}")
    String serverUrl;

    @Autowired
    private IdamUtils idamTestSupportUtil;

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration;

    IntegrationTest() {
        this.springMethodIntegration = new SpringIntegrationMethodRule();
    }


    synchronized String getUserToken() {
        String username = "simulate-delivered" + UUID.randomUUID() + "@notifications.service.gov.uk";
        String password = UUID.randomUUID().toString();

        idamTestSupportUtil.createUserInIdam(username, password);
        return idamTestSupportUtil.generateUserTokenWithNoRoles(username, password);
    }
}
