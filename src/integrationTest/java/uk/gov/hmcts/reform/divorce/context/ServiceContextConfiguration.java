package uk.gov.hmcts.reform.divorce.context;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JSR310Module;
import feign.Feign;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.codec.Decoder;
import feign.jackson.JacksonEncoder;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder;
import org.springframework.cloud.openfeign.support.SpringDecoder;
import org.springframework.cloud.openfeign.support.SpringMvcContract;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.ServiceAuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.divorce.idam.utils.IdamUtils;
import uk.gov.hmcts.reform.divorce.idam.utils.StrategicIdamUtils;
import uk.gov.hmcts.reform.divorce.idam.utils.TacticalIdamUtils;
import uk.gov.hmcts.reform.divorce.support.IdamTestSupport;
import uk.gov.hmcts.reform.divorce.support.client.CcdClientSupport;

import java.util.List;

@Lazy
@Configuration
@ContextConfiguration(classes = {ServiceContextConfiguration.class})
@PropertySource({"classpath:application.properties"})
@PropertySource({"classpath:application-${env}.properties"})
public class ServiceContextConfiguration {

    @Value("${idam.strategic.enabled}")
    private boolean useStragicIdam;

    @Bean
    public IdamUtils getIdamUtil() {
        return useStragicIdam ? new StrategicIdamUtils() : new TacticalIdamUtils();
    }

    @Bean
    public IdamTestSupport getIdamTestSupport() {
        return new IdamTestSupport();
    }

    @Bean("ccdSubmissionTokenGenerator")
    public AuthTokenGenerator ccdSubmissionAuthTokenGenerator(
        @Value("${auth.provider.service.client.key}") final String secret,
        @Value("${auth.provider.ccdsubmission.microservice}") final String microService,
        @Value("${idam.s2s-auth.url}") final String s2sUrl
    ) {
        final ServiceAuthorisationApi serviceAuthorisationApi = Feign.builder()
            .encoder(new JacksonEncoder())
            .contract(new SpringMvcContract())
            .target(ServiceAuthorisationApi.class, s2sUrl);

        return new ServiceAuthTokenGenerator(secret, microService, serviceAuthorisationApi);
    }

    @Bean
    public CcdClientSupport getCcdClientSupport() {
        return new CcdClientSupport();
    }

    @Bean
    public CoreCaseDataApi getCoreCaseDataApi(
        @Value("${core_case_data.api.url}") final String coreCaseDataApiUrl) {
        return Feign.builder()
            .requestInterceptor(requestInterceptor())
            .encoder(new JacksonEncoder())
            .decoder(feignDecoder())
            .contract(new SpringMvcContract())
            .target(CoreCaseDataApi.class, coreCaseDataApiUrl);
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            if (template.request().httpMethod() == Request.HttpMethod.POST) {
                template.header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString());
            }
        };
    }

    @Bean
    public Decoder feignDecoder() {
        MappingJackson2HttpMessageConverter jacksonConverter =
            new MappingJackson2HttpMessageConverter(customObjectMapper());
        jacksonConverter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON));

        ObjectFactory<HttpMessageConverters> objectFactory = () -> new HttpMessageConverters(jacksonConverter);
        return new ResponseEntityDecoder(new SpringDecoder(objectFactory));
    }

    private ObjectMapper customObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JSR310Module());
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        return objectMapper;
    }
}
