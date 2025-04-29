package uk.gov.hmcts.reform.divorce.petition;

import io.restassured.response.Response;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import static org.junit.Assert.assertEquals;

public class CcdRetrieveAosCaseTest extends PetitionSupport {

    private static final String TEST_AOS_RESPONDED_EVENT = "testAosStarted";

    @Value("${case.maintenance.aos-case.context-path}")
    private String retrieveAosCaseContextPath;

    @Test
    @Ignore
    public void givenMultipleAosStartedAndNoAosCompletedCaseInCcd_whenRetrieveAosCase_thenReturnMultipleChoice()
            throws Exception {
        final UserDetails userDetails = getUserDetails();

        createACaseUpdateStateAndReturnTheCase(userDetails, TEST_AOS_RESPONDED_EVENT).prettyPrint();
        createACaseUpdateStateAndReturnTheCase(userDetails, TEST_AOS_RESPONDED_EVENT).prettyPrint();

        Response cmsResponse = retrieveCase(userDetails.getAuthToken());

        assertEquals(HttpStatus.MULTIPLE_CHOICES.value(), cmsResponse.getStatusCode());
    }

    private Response createACaseUpdateStateAndReturnTheCase(UserDetails userDetails, String eventName)
            throws Exception {
        Long caseId = getCaseIdFromSubmittingANewCase(userDetails);

        return updateCase((String) null, caseId, eventName, userDetails.getAuthToken());
    }

    @Override
    protected String getRetrieveCaseRequestUrl() {
        return serverUrl + retrieveAosCaseContextPath;
    }
}
