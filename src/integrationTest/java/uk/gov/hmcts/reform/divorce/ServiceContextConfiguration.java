package uk.gov.hmcts.reform.divorce;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@Lazy
@Configuration
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.divorce.divorce", "uk.gov.hmcts.auth.provider.service"})
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
@ImportAutoConfiguration({RibbonAutoConfiguration.class,HttpMessageConvertersAutoConfiguration.class,
    FeignRibbonClientAutoConfiguration.class, FeignAutoConfiguration.class})
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
@PropertySource({"classpath:application.properties"})
@PropertySource({"classpath:application-${env}.properties"})
public class ServiceContextConfiguration {

    @Bean
    public AuthTokenGenerator serviceAuthTokenGenerator(
        @Value("${idam.auth.secret}") final String secret,
        @Value("${idam.auth.microservice}") final String microService,
        final ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(secret, microService, serviceAuthorisationApi);
    }

    @Bean
    public IDAMUtils getIDAMUtil() {
        return new IDAMUtils();
    }
}
