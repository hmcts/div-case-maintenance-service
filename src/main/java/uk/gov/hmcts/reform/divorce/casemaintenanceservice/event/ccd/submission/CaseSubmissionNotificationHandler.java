package uk.gov.hmcts.reform.divorce.casemaintenanceservice.event.ccd.submission;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

@Aspect
@Component
public class CaseSubmissionNotificationHandler {
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    @Around("@annotation(NotifyCaseSubmission)")
    public Object notifyCaseSubmission(ProceedingJoinPoint joinPoint) throws Throwable {

        Object caseDetails = joinPoint.proceed();

        if (caseDetails != null) {
            applicationEventPublisher.publishEvent(new CaseSubmittedEvent(joinPoint.getThis(), (CaseDetails)caseDetails,
                (String)joinPoint.getArgs()[joinPoint.getArgs().length - 1]));
        }

        return caseDetails;
    }
}
