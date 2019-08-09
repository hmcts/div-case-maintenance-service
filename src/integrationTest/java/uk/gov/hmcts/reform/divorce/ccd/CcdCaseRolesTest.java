package uk.gov.hmcts.reform.divorce.ccd;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;
import uk.gov.hmcts.reform.divorce.support.client.CcdClientSupport;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;
import uk.gov.hmcts.reform.divorce.util.RestUtil;

import java.util.Collections;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class CcdCaseRolesTest extends PetitionSupport {

    private static final String SOL_PAYLOAD_CONTEXT_PATH = "ccd-solicitor-payload/";

    @Value("${case.maintenance.add-petitioner-solicitor-role.context-path}")
    private String addPetSolContextPath;

    @Autowired
    private CcdClientSupport ccdClientSupport;

    @SuppressWarnings("unchecked")
    @Test
    public void givenSolicitorCreatedCase_whenAssignPetitionerSolRole_thenReturnOk() {
        Map<String, Object> caseData = ResourceLoader.loadJsonToObject(SOL_PAYLOAD_CONTEXT_PATH + "base-case.json", Map.class);
        UserDetails solicitorUser = getSolicitorUser();
        CaseDetails caseDetails = ccdClientSupport.submitCaseForSolicitor(caseData, solicitorUser);
        Long caseId = caseDetails.getId();

        Response cmsResponse = addPetSolicitorRole(solicitorUser.getAuthToken(), caseId.toString());

        assertThat(cmsResponse.getStatusCode(), equalTo(HttpStatus.OK.value()));
    }

    private Response addPetSolicitorRole(String authToken, String caseId) {
        return RestUtil.putToRestService(
            serverUrl + addPetSolContextPath + "/" + caseId,
            Collections.singletonMap(HttpHeaders.AUTHORIZATION, authToken),
            null, null);
    }
}
