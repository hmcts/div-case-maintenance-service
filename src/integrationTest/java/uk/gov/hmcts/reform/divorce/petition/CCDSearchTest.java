package uk.gov.hmcts.reform.divorce.petition;

import io.restassured.response.Response;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.divorce.model.UserDetails;
import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

import static org.junit.Assert.assertEquals;

public class CCDSearchTest  extends PetitionSupport {

    @Test
    public void whenQueryAllCases_ReturnNumberOfCasesExpected(){
        final UserDetails userDetails = getCaseWorkerUser();
        String queryString = "{ \"from\": 0, \"size\": 5, \"query\": { \"match_all\": {} } }";

        Response cmsResponse = searchCases(userDetails.getAuthToken(), queryString);

        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());

        SearchResult searchResult = cmsResponse.as(SearchResult.class);
        assertEquals(5, searchResult.getCases().size());

    }
}
