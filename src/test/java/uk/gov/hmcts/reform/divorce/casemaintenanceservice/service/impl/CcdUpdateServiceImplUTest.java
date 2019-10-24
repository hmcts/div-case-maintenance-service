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
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_AUTHORISATION;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_BEARER_AUTHORISATION;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_CASE_TYPE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_JURISDICTION_ID;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_TOKEN;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.BULK_CASE_TYPE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.CASEWORKER_ROLE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.CASE_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.CITIZEN_ROLE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.CREATE_EVENT_ID;

@RunWith(MockitoJUnitRunner.class)
public class CcdUpdateServiceImplUTest {
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
        ReflectionTestUtils.setField(classUnderTest, "jurisdictionId", TEST_JURISDICTION_ID);
        ReflectionTestUtils.setField(classUnderTest, "caseType", TEST_CASE_TYPE);
        ReflectionTestUtils.setField(classUnderTest, "bulkCaseType", BULK_CASE_TYPE);
        ReflectionTestUtils.setField(classUnderTest, "createEventId", CREATE_EVENT_ID);
    }

    @Test
    public void whenUpdate_thenProceedAsExpected() {
        final String caseId = "caseId";
        final String userId = "someUserId";
        final Object caseData = new Object();

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CASE_EVENT_ID)
            .token(TEST_TOKEN)
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

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi.startEventForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, caseId, CASE_EVENT_ID)).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, caseId,true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.update(caseId, caseData, CASE_EVENT_ID, TEST_AUTHORISATION);

        assertEquals(actual, expected);

        verify(coreCaseDataApi).startEventForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, caseId, CASE_EVENT_ID);
        verify(coreCaseDataApi).submitEventForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, caseId,true, caseDataContent);
    }

    @Test
    public void whenUpdateWithCaseworker_thenProceedAsExpected() {
        final String caseId = "caseId";
        final String userId = "someUserId";
        final Object caseData = new Object();

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CASE_EVENT_ID)
            .token(TEST_TOKEN)
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

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi.startEventForCaseWorker(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, caseId, CASE_EVENT_ID)).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCaseWorker(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, caseId,true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.update(caseId, caseData, CASE_EVENT_ID, TEST_AUTHORISATION);

        assertEquals(actual, expected);

        verify(coreCaseDataApi).startEventForCaseWorker(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, caseId, CASE_EVENT_ID);
        verify(coreCaseDataApi).submitEventForCaseWorker(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, caseId,true, caseDataContent);
    }

    @Test
    public void whenUpdateWithCaseworkerCitizen_thenProceedAsExpected() {
        final String caseId = "caseId";
        final String userId = "someUserId";
        final Object caseData = new Object();

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CASE_EVENT_ID)
            .token(TEST_TOKEN)
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

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi.startEventForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, caseId, CASE_EVENT_ID)).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, caseId,true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.update(caseId, caseData, CASE_EVENT_ID, TEST_AUTHORISATION);

        assertEquals(actual, expected);

        verify(coreCaseDataApi).startEventForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, caseId, CASE_EVENT_ID);
        verify(coreCaseDataApi).submitEventForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, caseId,true, caseDataContent);
    }

    @Test
    public void whenUpdateBulkCase_thenProceedAsExpected() {
        final String caseId = "caseId";
        final String userId = "someUserId";
        final Object caseData = new Object();

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CASE_EVENT_ID)
            .token(TEST_TOKEN)
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

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi.startEventForCaseWorker(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            BULK_CASE_TYPE, caseId, CASE_EVENT_ID)).thenReturn(startEventResponse);
        when(coreCaseDataApi.submitEventForCaseWorker(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            BULK_CASE_TYPE, caseId,true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.updateBulkCase(caseId, caseData, CASE_EVENT_ID, TEST_AUTHORISATION);

        assertEquals(actual, expected);

        verify(coreCaseDataApi).startEventForCaseWorker(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            BULK_CASE_TYPE, caseId, CASE_EVENT_ID);
        verify(coreCaseDataApi).submitEventForCaseWorker(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, userId, TEST_JURISDICTION_ID,
            BULK_CASE_TYPE, caseId,true, caseDataContent);
    }
}
