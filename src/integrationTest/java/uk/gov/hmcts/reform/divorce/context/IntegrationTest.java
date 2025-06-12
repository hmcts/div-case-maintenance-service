package uk.gov.hmcts.reform.divorce.context;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.IdamTestSupport;
import uk.gov.hmcts.reform.divorce.util.RetryRule;

import javax.annotation.PostConstruct;

@Slf4j
@RunWith(SerenityRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
public abstract class IntegrationTest {

    @Value("${case.maintenance.service.base.uri}")
    protected String serverUrl;

    @Autowired
    protected IdamTestSupport idamTestSupport;

    @Rule
    public RetryRule retryRule = new RetryRule(3);

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration;

    protected IntegrationTest() {
        this.springMethodIntegration = new SpringIntegrationMethodRule();
    }

    @PostConstruct
    public void init() {
        RestAssured.useRelaxedHTTPSValidation();
    }

    protected UserDetails getUserDetails() {
        return idamTestSupport.createAnonymousCitizenUser();
    }

    protected UserDetails getSolicitorUser() {
        return idamTestSupport.getSolicitorUser();
    }

    protected String getUserToken() {
        return getUserDetails().getAuthToken();
    }

    protected UserDetails getCaseWorkerUser() {
        return idamTestSupport.getCaseworkerUser();
    }

    protected String getCaseWorkerToken() {
        return getCaseWorkerUser().getAuthToken();
    }
}
