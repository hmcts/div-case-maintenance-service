package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import com.google.common.collect.ImmutableMap;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CcdSubmissionServiceImplUTest {
    private static final String JURISDICTION_ID = "someJurisdictionId";
    private static final String CASE_TYPE = "someCaseType";
    private static final String CREATE_EVENT_ID = "createEventId";
    private static final String CREATE_HWF_EVENT_ID = "createHwfEventId";
    private static final String BULK_CASE_TYPE = "bulkCaseType";
    private static final String CREATE_BULK_CASE_EVENT_ID = "createBulkCaseEventId";
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY =
        (String) ReflectionTestUtils.getField(CcdSubmissionServiceImpl.class,
            "DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY");
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION =
        (String) ReflectionTestUtils.getField(CcdSubmissionServiceImpl.class,
            "DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION");
    private static final String HELP_WITH_FEES_FIELD = "D8HelpWithFeesNeedHelp";

    private static final String DIVORCE_BULK_CASE_SUBMISSION_EVENT_SUMMARY = "Divorce Bulk case submission event";
    private static final String DIVORCE_BULK_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting divorce bulk Case";

    private static final String USER_ID = "someUserId";
    private static final String AUTHORISATION = "authorisation";
    private static final String BEARER_AUTHORISATION = "Bearer authorisation";
    private static final String SERVICE_TOKEN = "serviceToken";
    public static final String TOKEN = "token";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private UserService userService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CcdSubmissionServiceImpl classUnderTest;

    @Before
    public void setup() {
        ReflectionTestUtils.setField(classUnderTest, "jurisdictionId", JURISDICTION_ID);
        ReflectionTestUtils.setField(classUnderTest, "caseType", CASE_TYPE);
        ReflectionTestUtils.setField(classUnderTest, "createHwfEventId", CREATE_HWF_EVENT_ID);
        ReflectionTestUtils.setField(classUnderTest, "createEventId", CREATE_EVENT_ID);
        ReflectionTestUtils.setField(classUnderTest, "bulkCaseType", BULK_CASE_TYPE);
        ReflectionTestUtils.setField(classUnderTest, "createBulkCaseEventId", CREATE_BULK_CASE_EVENT_ID);
    }

    @Test
    public void whenUpdate_thenProceedAsExpected() {
        final Map<String, Object> caseData = ImmutableMap.of(HELP_WITH_FEES_FIELD, "NO");

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CREATE_EVENT_ID)
            .token(TOKEN)
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

        final User userDetails = new User(TOKEN, UserDetails.builder().id(USER_ID).build());
        final CaseDetails expected = CaseDetails.builder().build();

        when(userService.retrieveUser(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi.startForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID,
            CASE_TYPE, CREATE_EVENT_ID)).thenReturn(startEventResponse);

        when(coreCaseDataApi.submitForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID,
            CASE_TYPE, true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.submitCase(caseData, AUTHORISATION);

        assertThat(actual).isEqualTo(expected);

        verify(coreCaseDataApi).startForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID,
            CASE_TYPE, CREATE_EVENT_ID);
        verify(coreCaseDataApi).submitForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID,
            CASE_TYPE, true, caseDataContent);
    }

    @Test
    public void givenCaseWithHelpWithFees_whenSubmit_thenCrateWithWHFEventTriggered() {

        final Map<String, Object> caseData = ImmutableMap.of(HELP_WITH_FEES_FIELD, "YES");

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CREATE_HWF_EVENT_ID)
            .token(TOKEN)
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

        final User userDetails = new User(TOKEN, UserDetails.builder().id(USER_ID).build());
        final CaseDetails expected = CaseDetails.builder().build();

        when(userService.retrieveUser(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi.startForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID,
            CASE_TYPE, CREATE_HWF_EVENT_ID)).thenReturn(startEventResponse);

        when(coreCaseDataApi.submitForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID,
            CASE_TYPE, true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.submitCase(caseData, AUTHORISATION);

        assertThat(actual).isEqualTo(expected);

        verify(coreCaseDataApi).startForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID,
            CASE_TYPE, CREATE_HWF_EVENT_ID);
        verify(coreCaseDataApi).submitForCitizen(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID,
            CASE_TYPE, true, caseDataContent);
    }

    @Test
    public void whenSubmitBulkCase_thenProceedAsExpected() {
        final Map<String, Object> caseData = ImmutableMap.of(HELP_WITH_FEES_FIELD, "NO");

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CREATE_BULK_CASE_EVENT_ID)
            .token(TOKEN)
            .build();

        final CaseDataContent caseDataContent = CaseDataContent.builder()
            .eventToken(startEventResponse.getToken())
            .event(
                Event.builder()
                    .id(startEventResponse.getEventId())
                    .summary(DIVORCE_BULK_CASE_SUBMISSION_EVENT_SUMMARY)
                    .description(DIVORCE_BULK_CASE_SUBMISSION_EVENT_DESCRIPTION)
                    .build()
            ).data(caseData)
            .build();

        final User userDetails = new User(TOKEN, UserDetails.builder().id(USER_ID).build());
        final CaseDetails expected = CaseDetails.builder().build();

        when(userService.retrieveUser(BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(coreCaseDataApi.startForCaseworker(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID,
            BULK_CASE_TYPE, CREATE_BULK_CASE_EVENT_ID)).thenReturn(startEventResponse);

        when(coreCaseDataApi.submitForCaseworker(BEARER_AUTHORISATION, SERVICE_TOKEN, USER_ID, JURISDICTION_ID,
            BULK_CASE_TYPE, true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.submitBulkCase(caseData, AUTHORISATION);

        assertThat(actual).isEqualTo(expected);
    }
}
