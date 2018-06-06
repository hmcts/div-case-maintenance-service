package uk.gov.hmcts.reform.divorce.casemanagementservice.service.impl;

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
import uk.gov.hmcts.reform.divorce.casemanagementservice.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemanagementservice.service.IdamUserService;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CcdUpdateServiceImplUTest {
    private static final String JURISDICTION_ID = "someJurisdictionId";
    private static final String CASE_TYPE = "someCaseType";
    private static final String CREATE_EVENT_ID = "createEventId";

    @Mock
    private CoreCaseDataApi coreCaseDataApi;

    @Mock
    private IdamUserService idamUserService;

    @Mock
    private AuthTokenGenerator authTokenGenerator;

    @InjectMocks
    private CcdUpdateServiceImpl classUnderTest;

    @Before
    public void setup(){
        ReflectionTestUtils.setField(classUnderTest, "jurisdictionId", JURISDICTION_ID);
        ReflectionTestUtils.setField(classUnderTest, "caseType", CASE_TYPE);
        ReflectionTestUtils.setField(classUnderTest, "createEventId", CREATE_EVENT_ID);
    }

    @Test
    public void whenUpdate_thenProceedAsExpected(){
        final String caseId = "caseId";
        final String userId = "someUserId";
        final String authorisation = "authorisation";
        final String bearerAuthorisation = "Bearer authorisation";
        final String serviceToken = "serviceToken";
        final CaseDataContent caseDataContent = CaseDataContent.builder().build();

        final UserDetails userDetails = UserDetails.builder().id(userId).build();
        final CaseDetails expected = CaseDetails.builder().build();

        when(idamUserService.retrieveUserDetails(authorisation)).thenReturn(userDetails);
        when(authTokenGenerator.generate()).thenReturn(serviceToken);
        when(coreCaseDataApi.submitEventForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId,true, caseDataContent)).thenReturn(expected);

        CaseDetails actual = classUnderTest.update(caseId, caseDataContent, authorisation);

        assertEquals(actual, expected);

        verify(coreCaseDataApi).startEventForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId, CREATE_EVENT_ID);
        verify(coreCaseDataApi).submitEventForCitizen(bearerAuthorisation, serviceToken, userId, JURISDICTION_ID,
            CASE_TYPE, caseId,true, caseDataContent);
    }
}
