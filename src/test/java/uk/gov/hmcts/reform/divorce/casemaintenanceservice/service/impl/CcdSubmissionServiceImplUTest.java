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
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_AUTHORISATION;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_BEARER_AUTHORISATION;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_CASE_TYPE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_JURISDICTION_ID;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_SERVICE_TOKEN;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.BULK_CASE_TYPE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.CREATE_BULK_CASE_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.CREATE_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.CREATE_HWF_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.D_8_HELP_WITH_FEES_NEED_HELP;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.NO_VALUE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.SOLICITOR_CREATE_EVENT_ID;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.YES_VALUE;

@RunWith(MockitoJUnitRunner.class)
public class CcdSubmissionServiceImplUTest {

    private static final String DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY =
        (String) ReflectionTestUtils.getField(CcdSubmissionServiceImpl.class,
            "DIVORCE_CASE_SUBMISSION_EVENT_SUMMARY");
    private static final String DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION =
        (String) ReflectionTestUtils.getField(CcdSubmissionServiceImpl.class,
            "DIVORCE_CASE_SUBMISSION_EVENT_DESCRIPTION");

    private static final String DIVORCE_BULK_CASE_SUBMISSION_EVENT_SUMMARY = "Divorce Bulk case submission event";
    private static final String DIVORCE_BULK_CASE_SUBMISSION_EVENT_DESCRIPTION = "Submitting divorce bulk Case";

    private static final String USER_ID = "someUserId";

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
        ReflectionTestUtils.setField(classUnderTest, "jurisdictionId", TEST_JURISDICTION_ID);
        ReflectionTestUtils.setField(classUnderTest, "caseType", TEST_CASE_TYPE);
        ReflectionTestUtils.setField(classUnderTest, "createHwfEventId", CREATE_HWF_EVENT_ID);
        ReflectionTestUtils.setField(classUnderTest, "createEventId", CREATE_EVENT_ID);
        ReflectionTestUtils.setField(classUnderTest, "bulkCaseType", BULK_CASE_TYPE);
        ReflectionTestUtils.setField(classUnderTest, "createBulkCaseEventId", CREATE_BULK_CASE_EVENT_ID);
        ReflectionTestUtils.setField(classUnderTest, "solicitorCreateEventId", SOLICITOR_CREATE_EVENT_ID);
    }

    @Test
    public void whenUpdate_thenProceedAsExpected() {
        final Map<String, Object> caseData = ImmutableMap.of(D_8_HELP_WITH_FEES_NEED_HELP, NO_VALUE);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CREATE_EVENT_ID)
            .token(TEST_AUTH_TOKEN)
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

        final User userDetails = new User(TEST_AUTH_TOKEN, UserDetails.builder().id(USER_ID).build());
        final CaseDetails expected = CaseDetails.builder().build();

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi.startForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, CREATE_EVENT_ID)).thenReturn(startEventResponse);

        when(coreCaseDataApi.submitForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.submitCase(caseData, TEST_AUTHORISATION);

        assertThat(actual).isEqualTo(expected);

        verify(coreCaseDataApi).startForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, CREATE_EVENT_ID);
        verify(coreCaseDataApi).submitForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, true, caseDataContent);
    }

    @Test
    public void givenCaseWithHelpWithFees_whenSubmit_thenCrateWithWHFEventTriggered() {

        final Map<String, Object> caseData = ImmutableMap.of(D_8_HELP_WITH_FEES_NEED_HELP, YES_VALUE);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CREATE_HWF_EVENT_ID)
            .token(TEST_AUTH_TOKEN)
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

        final User userDetails = new User(TEST_AUTH_TOKEN, UserDetails.builder().id(USER_ID).build());
        final CaseDetails expected = CaseDetails.builder().build();

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi.startForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, CREATE_HWF_EVENT_ID)).thenReturn(startEventResponse);

        when(coreCaseDataApi.submitForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.submitCase(caseData, TEST_AUTHORISATION);

        assertThat(actual).isEqualTo(expected);

        verify(coreCaseDataApi).startForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, CREATE_HWF_EVENT_ID);
        verify(coreCaseDataApi).submitForCitizen(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, true, caseDataContent);
    }

    @Test
    public void whenSubmitBulkCase_thenProceedAsExpected() {
        final Map<String, Object> caseData = ImmutableMap.of(D_8_HELP_WITH_FEES_NEED_HELP, NO_VALUE);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(CREATE_BULK_CASE_EVENT_ID)
            .token(TEST_AUTH_TOKEN)
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

        final User userDetails = new User(TEST_AUTH_TOKEN, UserDetails.builder().id(USER_ID).build());
        final CaseDetails expected = CaseDetails.builder().build();

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi.startForCaseworker(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID,
            BULK_CASE_TYPE, CREATE_BULK_CASE_EVENT_ID)).thenReturn(startEventResponse);

        when(coreCaseDataApi.submitForCaseworker(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID,
            BULK_CASE_TYPE, true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.submitBulkCase(caseData, TEST_AUTHORISATION);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void whenSubmitCaseForSolicitor_thenProceedAsExpected() {
        final Map<String, Object> caseData = ImmutableMap.of(D8_PETITIONER_EMAIL, TEST_USER_EMAIL);

        final StartEventResponse startEventResponse = StartEventResponse.builder()
            .eventId(SOLICITOR_CREATE_EVENT_ID)
            .token(TEST_AUTH_TOKEN)
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

        final User userDetails = new User(TEST_AUTH_TOKEN, UserDetails.builder().id(USER_ID).build());
        final CaseDetails expected = CaseDetails.builder().build();

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(coreCaseDataApi.startForCaseworker(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, SOLICITOR_CREATE_EVENT_ID)).thenReturn(startEventResponse);

        when(coreCaseDataApi.submitForCaseworker(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, USER_ID, TEST_JURISDICTION_ID,
            TEST_CASE_TYPE, true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.submitCaseForSolicitor(caseData, TEST_AUTHORISATION);

        assertThat(actual).isEqualTo(expected);
    }
}
