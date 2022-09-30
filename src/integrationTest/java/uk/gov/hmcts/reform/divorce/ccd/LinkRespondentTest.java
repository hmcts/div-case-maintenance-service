package uk.gov.hmcts.reform.divorce.ccd;

import com.google.common.collect.ImmutableMap;
import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.PinResponse;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;
import uk.gov.hmcts.reform.divorce.support.client.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_LETTER_HOLDER_ID_FIELD;

public class LinkRespondentTest extends PetitionSupport {
    private static final String PAYLOAD_CONTEXT_PATH = "ccd-submission-payload/";
    private static final String RESPONDENT_EMAIL_ADDRESS = "RespEmailAddress";
    private static final String START_AOS_EVENT_ID = "startAos";
    private static final String TEST_AOS_AWAITING_EVENT_ID = "testAosAwaiting";
    private static final String AWAITING_PAYMENT_NO_STATE_CHANGE_EVENT_ID = "paymentReferenceGenerated";
    private static final String RESPONDENT_EMAIL = "aos@respondent.div";

    @Value("${case.maintenance.link-respondent.context-path}")
    private String linkRespondentContextPath;

    @Value("${case.maintenance.aos-case.context-path}")
    private String retrieveAosCaseContextPath;

    @Autowired
    private CcdClientSupport ccdClientSupport;

    private Response linkRespondent(String authToken, String caseId, String letterHolderId) {
        return RestUtil.postToRestService(
            serverUrl + linkRespondentContextPath + "/" + caseId + "/" + letterHolderId,
            Collections.singletonMap(HttpHeaders.AUTHORIZATION, authToken),
            null);
    }

    @Override
    protected String getRetrieveCaseRequestUrl() {
        return serverUrl + retrieveAosCaseContextPath;
    }
}
