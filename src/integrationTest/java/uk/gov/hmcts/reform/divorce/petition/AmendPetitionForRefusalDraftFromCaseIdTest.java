package uk.gov.hmcts.reform.divorce.petition;

import uk.gov.hmcts.reform.divorce.support.PetitionSupport;

public class AmendPetitionForRefusalDraftFromCaseIdTest extends PetitionSupport {
    private static final String AMEND_CASE_EVENT_ID = "amendCase";

//    @Test
//    public void givenSingleCaseInCcd_whenAmendPetitionDraftFromCaseId_thenReturnTheDraft() throws Exception {
//        final UserDetails userDetails = getUserDetails();
//
//        Long caseId = getCaseIdFromCompletedCase(userDetails);
//
//        updateCase(ImmutableMap.of(REFUSAL_ORDER_REJECTION_REASONS, Collections.singletonList("other")),
//            caseId, AMEND_CASE_EVENT_ID, getCaseWorkerUser().getAuthToken());
//
//        Response cmsResponse = putAmendedPetitionDraftForRefusalFromCaseId(userDetails.getAuthToken(), caseId);
//
//        assertEquals(HttpStatus.OK.value(), cmsResponse.getStatusCode());
//        assertEquals(String.valueOf(caseId), cmsResponse.path("previousCaseId"));
//    }
}
