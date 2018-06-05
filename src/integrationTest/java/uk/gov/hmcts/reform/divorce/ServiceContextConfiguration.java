package uk.gov.hmcts.reform.divorce;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;

@Lazy
@Configuration
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.divorce"})
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
@ImportAutoConfiguration({RibbonAutoConfiguration.class,HttpMessageConvertersAutoConfiguration.class,
    FeignRibbonClientAutoConfiguration.class, FeignAutoConfiguration.class})
@EnableFeignClients(basePackageClasses = ServiceAuthorisationApi.class)
@ComponentScan(basePackages = {"uk.gov.hmcts.reform.divorce.divorce"})
@PropertySource({"classpath:application.properties"})
public class ServiceContextConfiguration {
    @Bean
    public IdamUtils getIdamUtil() {
        return new IdamUtils();
    }


}
