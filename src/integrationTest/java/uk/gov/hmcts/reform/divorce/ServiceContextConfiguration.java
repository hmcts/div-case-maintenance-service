package uk.gov.hmcts.reform.divorce;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.ribbon.FeignRibbonClientAutoConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;

@Lazy
@Configuration
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.divorce"})
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
@ImportAutoConfiguration({RibbonAutoConfiguration.class,HttpMessageConvertersAutoConfiguration.class,
    FeignRibbonClientAutoConfiguration.class, FeignAutoConfiguration.class})
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.divorce"})
@PropertySource({"classpath:application.properties"})
public class ServiceContextConfiguration {
    @Bean
    public IdamUtils getIdamUtil() {
        return new IdamUtils();
    }


}
