package uk.gov.hmcts.reform.divorce.ccd;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.CcdUpdateSupport;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class CcdUpdateTest extends CcdUpdateSupport {

    @Test
    public void shouldReturnCaseIdWhenUpdatingDataAfterInitialSubmit() throws Exception {
        UserDetails userDetails = getUserDetails();

        Long caseId = getCaseIdFromSubmittingANewCase(userDetails);

        Response cmsResponse = updateCase("update-addresses.json", caseId, EVENT_ID, userDetails.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(caseId, cmsResponse.getBody().path("id"));
    }

    @Test
    public void shouldReturnCaseIdWhenUpdatingDataAfterInitialSubmitWithCaseWorker() throws Exception {
        UserDetails userDetails = getUserDetails();
        String caseWorkerToken = getCaseWorkerToken();

        Long caseId = getCaseIdFromSubmittingANewCase(userDetails);

        Response cmsResponse = updateCase("update-addresses.json", caseId, EVENT_ID, caseWorkerToken);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(caseId, cmsResponse.getBody().path("id"));
    }

    @Test
    public void shouldReturnCaseIdWhenUpdatingPaymentAfterUpdatingWithPaymentReference() throws Exception {
        UserDetails userDetails = getUserDetails();

        Long caseId = getCaseIdFromSubmittingANewCase(userDetails);

        Response cmsResponse = updateCase("payment-made.json", caseId, EVENT_ID, userDetails.getAuthToken());

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
        assertEquals(caseId, cmsResponse.getBody().path("id"));
    }

    @Test
    public void shouldReturnErrorForNonExistingCaseId() throws Exception {
        String userToken = getUserToken();

        Response cmsResponse = updateCase("payment-made.json", -1L, EVENT_ID, userToken);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), cmsResponse.getStatusCode());
        assertThat(cmsResponse.asString(), containsString("Case reference is not valid"));
    }

    @Test
    public void shouldReturnErrorForInvalidEventId() throws Exception {
        UserDetails userDetails = getUserDetails();

        Long caseId = getCaseIdFromSubmittingANewCase(userDetails);

        Response cmsResponse = updateCase("payment-made.json", caseId, "InvalidEvenId", userDetails.getAuthToken());

        assertEquals(HttpStatus.NOT_FOUND.value(), cmsResponse.getStatusCode());
        assertThat(cmsResponse.asString(),
            containsString("Cannot find event InvalidEvenId for case type DIVORCE"));
    }

    @Test
    public void shouldReturnErrorUpdatingWithCaseSameEventId() throws Exception {
        UserDetails userDetails = getUserDetails();

        Long caseId = getCaseIdFromSubmittingANewCase(userDetails);

        updateCase("payment-made.json", caseId, EVENT_ID, userDetails.getAuthToken());

        Response cmsResponse = updateCase("payment-made.json", caseId, EVENT_ID, userDetails.getAuthToken());

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), cmsResponse.getStatusCode());
        assertThat(cmsResponse.asString(),
            containsString("The case status did not qualify for the event"));
    }
}
