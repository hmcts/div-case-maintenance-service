package uk.gov.hmcts.reform.divorce.context;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit.spring.integration.SpringIntegrationMethodRule;
import org.assertj.core.util.Strings;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.IdamTestSupport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import javax.annotation.PostConstruct;

@Slf4j
@RunWith(SerenityRunner.class)
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
public abstract class IntegrationTest {

    @Value("${case.maintenance.service.base.uri}")
    protected String serverUrl;

    @Value("${http.proxy:#{null}}")
    protected String httpProxy;

    @Autowired
    protected IdamTestSupport idamTestSupport;

    @Rule
    public SpringIntegrationMethodRule springMethodIntegration;

    protected IntegrationTest() {
        this.springMethodIntegration = new SpringIntegrationMethodRule();
    }

    @PostConstruct
    public void init() {
        if (!Strings.isNullOrEmpty(httpProxy)) {
            try {
                URL proxy = new URL(httpProxy);
                // check proxy connectivity
                if (!InetAddress.getByName(proxy.getHost()).isReachable(2000)) {
                    throw new IOException();
                }
                System.setProperty("http.proxyHost", proxy.getHost());
                System.setProperty("http.proxyPort", Integer.toString(proxy.getPort()));
                System.setProperty("https.proxyHost", proxy.getHost());
                System.setProperty("https.proxyPort", Integer.toString(proxy.getPort()));
            } catch (IOException e) {
                log.error("Error setting up proxy - are you connected to the VPN?", e);
                throw new RuntimeException("Error setting up proxy", e);
            }
        }
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
