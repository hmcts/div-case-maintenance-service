package uk.gov.hmcts.reform.divorce.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.divorce.util.IdamUtils;

@Lazy
@Configuration
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
@PropertySource({"classpath:application.properties"})
public class ServiceContextConfiguration {
    @Bean
    public IdamUtils getIdamUtil() {
        return new IdamUtils();
    }
}
