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

import static org.junit.Assert.assertEquals;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.CO_RESP_LETTER_HOLDER_ID_FIELD;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_EMAIL_ADDRESS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.RESP_LETTER_HOLDER_ID_FIELD;

public class LinkRespondentTest extends PetitionSupport {
    private static final String PAYLOAD_CONTEXT_PATH = "ccd-submission-payload/";
    private static final String RESPONDENT_EMAIL_ADDRESS = "RespEmailAddress";
    private static final String START_AOS_EVENT_ID = "startAos";
    private static final String TEST_AOS_AWAITING_EVENT_ID = "testAosAwaiting";
    private static final String AWAITING_PAYMENT_NO_STATE_CHANGE_EVENT_ID = "paymentReferenceGenerated";
    private static final String RESPONDENT_EMAIL = "aos@respondent.div";

    private static final String INVALID_USER_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJqdGkiOiIwOTg3NjU0M"
        + "yIsInN1YiI6IjEwMCIsImlhdCI6MTUwODk0MDU3MywiZXhwIjoxNTE5MzAzNDI3LCJkYXRhIjoiY2l0aXplbiIsInR5cGUiOiJBQ0NFU1MiL"
        + "CJpZCI6IjEwMCIsImZvcmVuYW1lIjoiSm9obiIsInN1cm5hbWUiOiJEb2UiLCJkZWZhdWx0LXNlcnZpY2UiOiJEaXZvcmNlIiwibG9hIjoxL"
        + "CJkZWZhdWx0LXVybCI6Imh0dHBzOi8vd3d3Lmdvdi51ayIsImdyb3VwIjoiZGl2b3JjZSJ9.lkNr1vpAP5_Gu97TQa0cRtHu8I-QESzu8kMX"
        + "CJOQrVU";

    @Value("${case.maintenance.link-respondent.context-path}")
    private String linkRespondentContextPath;

    @Value("${case.maintenance.aos-case.context-path}")
    private String retrieveAosCaseContextPath;

    @Autowired
    private CcdClientSupport ccdClientSupport;

    @Test
    public void givenJWTTokenIsNull_whenLinkRespondent_thenReturnUnauthorised() {
        Response cmsResponse = linkRespondent(null, "someCaseId", "someLetterHolderId");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenNoCase_whenLinkRespondent_thenReturnUnauthorised() {
        Response cmsResponse = linkRespondent(getUserToken(), "someCaseId", "someLetterHolderId");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), cmsResponse.getStatusCode());
    }

    @Test
    public void givenNoLetterHolderId_whenLinkRespondent_thenReturnUnauthorized() {
        Map caseData = ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "addresses.json", Map.class);

        Long caseId = ccdClientSupport.submitCaseForCitizen(caseData, getUserDetails()).getId();

        Response cmsResponse = linkRespondent(getUserToken(), caseId.toString(), "someLetterHolderId");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), cmsResponse.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenLetterHolderDoNotMatch_whenLinkRespondent_thenReturnUnauthorized() {
        Map<String, Object> caseData = ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "addresses.json", Map.class);

        Long caseId = ccdClientSupport.submitCaseForCitizen(caseData, getUserDetails()).getId();

        Map<String, Object> updateCaseData = new HashMap<>();
        updateCaseData.put(RESP_LETTER_HOLDER_ID_FIELD, "nonMatchingLetterHolderId");

        updateCase(updateCaseData, caseId, AWAITING_PAYMENT_NO_STATE_CHANGE_EVENT_ID,
            getCaseWorkerUser().getAuthToken());

        Response cmsResponse = linkRespondent(getUserToken(), caseId.toString(), "someLetterHolderId");

        assertEquals(HttpStatus.UNAUTHORIZED.value(), cmsResponse.getStatusCode());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenCaseAlreadyLinked_whenLinkRespondent_thenReturnUnauthorized() {
        final String respondentFirstName = "respondent-" + UUID.randomUUID().toString();

        final PinResponse pinResponse = idamTestSupport.createPinUser(respondentFirstName);

        Map<String, Object> caseData = ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "linked-case.json", Map.class);

        Long caseId = ccdClientSupport.submitCaseForCitizen(caseData, getUserDetails()).getId();

        Map<String, Object> updateCaseData = new HashMap<>();
        updateCaseData.put(RESP_LETTER_HOLDER_ID_FIELD, pinResponse.getUserId());
        updateCaseData.put(RESP_EMAIL_ADDRESS, RESPONDENT_EMAIL);

        updateCase(updateCaseData, caseId, AWAITING_PAYMENT_NO_STATE_CHANGE_EVENT_ID,
            getCaseWorkerUser().getAuthToken());

        Response cmsResponse = linkRespondent(getUserToken(), caseId.toString(), pinResponse.getUserId());

        assertEquals(HttpStatus.UNAUTHORIZED.value(), cmsResponse.getStatusCode());
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    @Test
    public void givenPetitionerAttemptsLinking_whenLinkRespondent_thenReturnUnauthorized() {
        final String respondentFirstName = "respondent-" + UUID.randomUUID().toString();

        final PinResponse pinResponse = idamTestSupport.createPinUser(respondentFirstName);

        Map<String, Object> caseData = ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "linked-case.json", Map.class);

        UserDetails petitioner = getUserDetails();
        Long caseId = ccdClientSupport.submitCaseForCitizen(caseData, petitioner).getId();

        Map<String, Object> updateCaseData = new HashMap<>();
        updateCaseData.put(RESP_LETTER_HOLDER_ID_FIELD, pinResponse.getUserId());

        updateCase(updateCaseData, caseId, AWAITING_PAYMENT_NO_STATE_CHANGE_EVENT_ID,
            getCaseWorkerUser().getAuthToken());

        Response cmsResponse = linkRespondent(petitioner.getAuthToken(), caseId.toString(), pinResponse.getUserId());

        assertEquals(HttpStatus.UNAUTHORIZED.value(), cmsResponse.getStatusCode());
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    @Test
    public void givenInvalidUserToken_whenLinkRespondent_thenReturnForbidden() {
        final String respondentFirstName = "respondent-" + UUID.randomUUID().toString();

        final PinResponse pinResponse = idamTestSupport.createPinUser(respondentFirstName);

        Map<String, Object> caseData = ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "addresses.json", Map.class);

        UserDetails petitionerUser = getUserDetails();

        Long caseId = ccdClientSupport.submitCaseForCitizen(caseData, petitionerUser).getId();

        Map<String, Object> updateCaseData = new HashMap<>();
        updateCaseData.put(RESP_LETTER_HOLDER_ID_FIELD, pinResponse.getUserId());

        updateCase(updateCaseData, caseId, AWAITING_PAYMENT_NO_STATE_CHANGE_EVENT_ID,
            getCaseWorkerUser().getAuthToken());

        updateCase((String) null, caseId, TEST_AOS_AWAITING_EVENT_ID, petitionerUser.getAuthToken());

        Response cmsResponse = linkRespondent(INVALID_USER_TOKEN, caseId.toString(), pinResponse.getUserId());

        assertEquals(HttpStatus.FORBIDDEN.value(), cmsResponse.getStatusCode());
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    @Test
    public void givenLetterHolderIdMatches_whenLinkRespondent_thenGrantAccessToCase() {

        final String respondentFirstName = "respondent-" + UUID.randomUUID().toString();

        final PinResponse pinResponse = idamTestSupport.createPinUser(respondentFirstName);


        Map<String, Object> caseData = ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "addresses.json", Map.class);

        UserDetails petitionerUser = getUserDetails();

        Long caseId = ccdClientSupport.submitCaseForCitizen(caseData, petitionerUser).getId();

        Map<String, Object> updateCaseData = new HashMap<>();
        updateCaseData.put(RESP_LETTER_HOLDER_ID_FIELD, pinResponse.getUserId());

        updateCase(updateCaseData, caseId, AWAITING_PAYMENT_NO_STATE_CHANGE_EVENT_ID,
            getCaseWorkerUser().getAuthToken());

        updateCase((String) null, caseId, TEST_AOS_AWAITING_EVENT_ID, petitionerUser.getAuthToken());

        UserDetails upliftedUser = idamTestSupport.createRespondentUser(respondentFirstName, pinResponse.getPin());
        linkRespondent(upliftedUser.getAuthToken(), caseId.toString(), pinResponse.getUserId());

        updateCase(ImmutableMap.of(RESPONDENT_EMAIL_ADDRESS, upliftedUser.getEmailAddress()),
            caseId, START_AOS_EVENT_ID, getCaseWorkerUser().getAuthToken());

        Response response = retrieveCase(upliftedUser.getAuthToken());

        assertEquals(caseId, response.path("id"));
    }

    @SuppressWarnings({"unchecked", "Duplicates"})
    @Test
    public void givenLetterHolderIdMatches_whenLinkCoRespondent_thenGrantAccessToCase() {
        final String respondentFirstName = "respondent-" + UUID.randomUUID().toString();

        final PinResponse pinResponse = idamTestSupport.createPinUser(respondentFirstName);

        UserDetails upliftedUser = idamTestSupport.createRespondentUser(respondentFirstName, pinResponse.getPin());

        Map<String, Object> caseData = ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "addresses.json", Map.class);
        caseData.put(CO_RESP_LETTER_HOLDER_ID_FIELD, pinResponse.getUserId());

        UserDetails petitionerUser = getUserDetails();

        Long caseId = ccdClientSupport.submitCaseForCitizen(caseData, petitionerUser).getId();

        updateCase((String) null, caseId, TEST_AOS_AWAITING_EVENT_ID, petitionerUser.getAuthToken());

        linkRespondent(upliftedUser.getAuthToken(), caseId.toString(), pinResponse.getUserId());

        updateCase(ImmutableMap.of(RESPONDENT_EMAIL_ADDRESS, upliftedUser.getEmailAddress()),
            caseId, START_AOS_EVENT_ID, getCaseWorkerUser().getAuthToken());

        Response response = retrieveCase(upliftedUser.getAuthToken());

        assertEquals(caseId, response.path("id"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void givenLinkedCoRespondent_whenReLinkingCoRespondent_thenProcessAsNormal() {

        final String respondentFirstName = "respondent-" + UUID.randomUUID().toString();

        final PinResponse pinResponse = idamTestSupport.createPinUser(respondentFirstName);

        Map<String, Object> caseData = ResourceLoader.loadJsonToObject(PAYLOAD_CONTEXT_PATH + "addresses.json", Map.class);
        caseData.put(CO_RESP_LETTER_HOLDER_ID_FIELD, pinResponse.getUserId());

        UserDetails petitionerUser = getUserDetails();

        Long caseId = ccdClientSupport.submitCaseForCitizen(caseData, petitionerUser).getId();
        updateCase((String) null, caseId, TEST_AOS_AWAITING_EVENT_ID, petitionerUser.getAuthToken());

        UserDetails upliftedUser = idamTestSupport.createRespondentUser(respondentFirstName, pinResponse.getPin());

        Response linkResponse = linkRespondent(upliftedUser.getAuthToken(), caseId.toString(), pinResponse.getUserId());

        assertEquals(HttpStatus.OK.value(),linkResponse.getStatusCode());

        updateCase(ImmutableMap.of(RESPONDENT_EMAIL_ADDRESS, upliftedUser.getEmailAddress()),
            caseId, START_AOS_EVENT_ID, getCaseWorkerUser().getAuthToken());

        Response response = retrieveCase(upliftedUser.getAuthToken());

        assertEquals(caseId, response.path("id"));

        linkResponse = linkRespondent(upliftedUser.getAuthToken(), caseId.toString(), pinResponse.getUserId());

        assertEquals(HttpStatus.OK.value(),linkResponse.getStatusCode());
    }


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
