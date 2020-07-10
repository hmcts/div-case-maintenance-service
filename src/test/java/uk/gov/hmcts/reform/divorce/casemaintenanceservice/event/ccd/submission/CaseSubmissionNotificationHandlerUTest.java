package uk.gov.hmcts.reform.divorce.casemaintenanceservice.event.ccd.submission;

import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.ApplicationEventPublisher;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_AUTH_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class CaseSubmissionNotificationHandlerUTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private CaseSubmissionNotificationHandler classUnderTest;

    @Test
    public void givenNoCaseDetailsReturned_whenNotifyCaseSubmission_thenDoNotCallPublish() throws Throwable {
        final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        when(joinPoint.proceed()).thenReturn(null);

        classUnderTest.notifyCaseSubmission(joinPoint);

        verifyNoInteractions(applicationEventPublisher);
    }

    @Test
    public void givenCaseDetailsReturned_whenNotifyCaseSubmission_thenProceedAsExpected() throws Throwable {
        final ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);

        final CaseDetails caseDetails = CaseDetails.builder().build();
        final Object source = new Object();
        final CaseSubmittedEvent caseSubmittedEvent = new CaseSubmittedEvent(source, caseDetails, TEST_AUTH_TOKEN);

        when(joinPoint.proceed()).thenReturn(caseDetails);
        when(joinPoint.getThis()).thenReturn(source);
        when(joinPoint.getArgs()).thenReturn(new Object[]{TEST_AUTH_TOKEN});

        Object actual = classUnderTest.notifyCaseSubmission(joinPoint);

        assertEquals(caseDetails, actual);

        verify(applicationEventPublisher).publishEvent(argThat(new CaseSubmittedEventMatcher(caseSubmittedEvent)));
    }

    private class CaseSubmittedEventMatcher implements ArgumentMatcher<CaseSubmittedEvent> {
        private final CaseSubmittedEvent expected;

        CaseSubmittedEventMatcher(CaseSubmittedEvent expected) {
            this.expected = expected;
        }

        @Override
        public boolean matches(CaseSubmittedEvent actual) {
            return actual.getAuthToken().equals(expected.getAuthToken())
                && actual.getCaseDetails() == expected.getCaseDetails();
        }
    }
}
