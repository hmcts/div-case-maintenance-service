package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.ccd.client.CoreCaseDataApi;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.Event;
import uk.gov.hmcts.reform.ccd.client.model.StartEventResponse;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CcdUpdateServiceImplUTest {
    private static final String JURISDICTION_ID = "someJurisdictionId";
    private static final String CASE_TYPE = "someCaseType";
    private static final String BULK_CASE_TYPE = "bulkCaseType";
    private static final String CREATE_EVENT_ID = "createEventId";
    private static final String CASEWORKER_ROLE = "caseworker";
    private static final String CITIZEN_ROLE = "citizen";

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY =
        (String)ReflectionTestUtils.getField(CcdSubmissionServiceImpl.class,
            "DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY");
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION =
        (String)ReflectionTestUtils.getField(CcdSubmissionServiceImpl.class,
            "DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION");

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private UserService userService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CcdUpdateServiceImpl classUnderTest;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(classUnderTest, "jurisdictionId", JURISDICTION_ID);
        ReflectionTestUtils.setField(classUnderTest, "caseType", CASE_TYPE);
        ReflectionTestUtils.setField(classUnderTest, "bulkCaseType", BULK_CASE_TYPE);
        ReflectionTestUtils.setField(classUnderTest, "createEventId", CREATE_EVENT_ID);
    }

    @Test
    public void whenUpdate_thenProceedAsExpected() {
        final String caseId = "caseId";
        final String userId = "someUserId";
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";
        final Object caseData = new Object();

        final String eventId = "eventId";
        final String token = "token";
        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(eventId)
            .token(token)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(caseData)
            .build();

        final User userDetails = new User("auth", UserDetails.builder().id(userId).build());
        final CaseDetails expected = CaseDetails.builder().build();

        when(userService.retrieveUser(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi.startEventForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId, eventId)).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId,true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.update(caseId, caseData, eventId, authorisation);

        assertEquals(actual, expected);

        verify(coreCaseDataApi).startEventForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId, eventId);
        verify(coreCaseDataApi).submitEventForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId,true, caseDataContent);
    }

    @Test
    public void whenUpdateWithCaseworker_thenProceedAsExpected() {
        final String caseId = "caseId";
        final String userId = "someUserId";
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";
        final Object caseData = new Object();

        final String eventId = "eventId";
        final String token = "token";
        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(eventId)
            .token(token)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(caseData)
            .build();

        final User userDetails = new User(
            "auth",
            UserDetails.builder().id(userId).roles(Collections.singletonList(CASEWORKER_ROLE)).build()
        );
        final CaseDetails expected = CaseDetails.builder().build();

        when(userService.retrieveUser(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi.startEventForCaseWorker(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId, eventId)).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCaseWorker(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId,true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.update(caseId, caseData, eventId, authorisation);

        assertEquals(actual, expected);

        verify(coreCaseDataApi).startEventForCaseWorker(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId, eventId);
        verify(coreCaseDataApi).submitEventForCaseWorker(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId,true, caseDataContent);
    }

    @Test
    public void whenUpdateWithCaseworkerCitizen_thenProceedAsExpected() {
        final String caseId = "caseId";
        final String userId = "someUserId";
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";
        final Object caseData = new Object();

        final String eventId = "eventId";
        final String token = "token";
        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(eventId)
            .token(token)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(caseData)
            .build();

        List<String> userRoles = Arrays.asList(CASEWORKER_ROLE, CITIZEN_ROLE);

        final User userDetails = new User("auth",UserDetails.builder().id(userId).roles(userRoles).build());
        final CaseDetails expected = CaseDetails.builder().build();

        when(userService.retrieveUser(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi.startEventForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId, eventId)).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId,true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.update(caseId, caseData, eventId, authorisation);

        assertEquals(actual, expected);

        verify(coreCaseDataApi).startEventForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId, eventId);
        verify(coreCaseDataApi).submitEventForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId,true, caseDataContent);
    }

    @Test
    public void whenUpdateBulkCase_thenProceedAsExpected() {
        final String caseId = "caseId";
        final String userId = "someUserId";
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";
        final Object caseData = new Object();

        final String eventId = "eventId";
        final String token = "token";
        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(eventId)
            .token(token)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(caseData)
            .build();

        final User userDetails = new User("auth",UserDetails.builder().id(userId).build());
        final CaseDetails expected = CaseDetails.builder().build();

        when(userService.retrieveUser(bearerAuthorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi.startEventForCaseWorker(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            BULK_CASE_TYPE, caseId, eventId)).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCaseWorker(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            BULK_CASE_TYPE, caseId,true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.updateBulkCase(caseId, caseData, eventId, authorisation);

        assertEquals(actual, expected);

        verify(coreCaseDataApi).startEventForCaseWorker(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            BULK_CASE_TYPE, caseId, eventId);
        verify(coreCaseDataApi).submitEventForCaseWorker(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            BULK_CASE_TYPE, caseId,true, caseDataContent);
    }
}
