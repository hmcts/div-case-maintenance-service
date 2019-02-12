package uk.gov.hmcts.reform.divorce.context;

import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.IdamTestSupport;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
public abstract class IntegrationTest {
    @Value("${case.maintenance.service.base.uri}")
    protected String serverUrl;

    @Autowired
    protected IdamTestSupport idamTestSupport;

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration;

    protected IntegrationTest() {
        this.springMethodIntegration = new SpringIntegrationMethodRule();
    }

    protected UserDetails getUserDetails() {
        return idamTestSupport.createAnonymousCitizenUser();
    }

    protected String getUserToken() {
        return getUserDetails().getAuthToken();
    }

    protected UserDetails getCaseWorkerUser() {
        return idamTestSupport.createAnonymousCaseWorkerUser();
    }

    protected String getPureCaseWorkerToken() {
        return idamTestSupport.createPureCaseWorkerUser().getAuthToken();
    }
}
