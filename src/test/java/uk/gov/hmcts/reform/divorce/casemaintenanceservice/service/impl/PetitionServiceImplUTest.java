package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.FormatterServiceClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseState;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivorceSessionProperties;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.event.ccd.submission.CaseSubmittedEvent;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_AUTHORISATION;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_AUTH_TOKEN;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_CASE_REF;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_DRAFT_DOC_TYPE_DIVORCE_FORMAT;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_REASON_ADULTERY;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_REASON_UNREASONABLE_BEHAVIOUR;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_RELATIONSHIP;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_USER_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseRetrievalStateMap.PETITIONER_CASE_STATE_GROUPING;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseRetrievalStateMap.RESPONDENT_CASE_STATE_GROUPING;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_CASE_REFERENCE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_DIVORCE_UNIT;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_DOCUMENTS_GENERATED;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_DOCUMENTS_UPLOADED;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_PETITIONER_EMAIL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.D8_REJECT_DOCUMENTS_UPLOADED;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.ISSUE_DATE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.PREVIOUS_ISSUE_DATE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.PREVIOUS_REASONS_DIVORCE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.PREVIOUS_REASONS_DIVORCE_REFUSAL;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.REFUSAL_ORDER_REJECTION_REASONS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.REJECTION_INSUFFICIENT_DETAILS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.REJECTION_NO_CRITERIA;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties.REJECTION_NO_JURISDICTION;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.D_8_CONNECTIONS;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.D_8_DIVORCE_WHO;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.D_8_HELP_WITH_FEES_NEED_HELP;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.D_8_REASON_FOR_DIVORCE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.YES_VALUE;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivCaseRole.PETITIONER;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivCaseRole.RESPONDENT;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE_REFUSAL;

@RunWith(MockitoJUnitRunner.class)
public class PetitionServiceImplUTest {
    private static final boolean DIVORCE_FORMAT = false;
    private static final String TEST_CASE_ID = "1234567891234567";
    private static final String TEST_FAMILY_MAN_REF = "REF12345";
    private static final String USER_FIRST_NAME = "John";
    private static final String TWO_YEAR_SEPARATION = "2yr-separation";
    private static final String DRAFT_ID = "1";

    @Captor
    private ArgumentCaptor<Object> ccdCaseDataArgumentCaptor;

    @Mock
    private CcdRetrievalService ccdRetrievalService;

    @Mock
    private DraftServiceImpl draftService;

    @Mock
    private FormatterServiceClient formatterServiceClient;

    @Mock
    private UserService userService;

    @InjectMocks
    private PetitionServiceImpl classUnderTest;

    @Test
    public void givenCcdRetrievalServiceReturnsCase_whenRetrievePetition_thenProceedAsExpected() throws DuplicateCaseException {
        final CaseDetails caseDetails = CaseDetails.builder().build();

        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING, PETITIONER)).thenReturn(caseDetails);

        CaseDetails actual = classUnderTest.retrievePetition(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING);

        assertEquals(caseDetails, actual);
        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING, PETITIONER);
    }

    @Test
    public void givenCcdRetrievalServiceReturnsAmendCase_whenRetrievePetition_thenReturnCaseAsDraft() throws DuplicateCaseException {
        final CaseDetails caseDetails = buildAdulteryCaseData();

        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING, PETITIONER)).thenReturn(caseDetails);

        Map<String, Object> expectedCaseData = new HashMap<>();
        expectedCaseData.put(PetitionServiceImpl.IS_DRAFT_KEY, true);
        expectedCaseData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        expectedCaseData.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE,
            singletonList(TEST_REASON_ADULTERY));
        CaseDetails expected = CaseDetails.builder()
            .data(expectedCaseData)
            .build();

        CaseDetails actual = classUnderTest.retrievePetition(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING);
        assertEquals(expected, actual);
        verifyCcdCaseDataToBeTransformed();
        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING, PETITIONER);
    }

    @Test
    public void givenCcdRetrievalServiceReturnsAmendCaseWithStandardDraft_whenRetrievePetition_thenReturnCaseAsDraft() throws DuplicateCaseException {
        final CaseDetails caseDetails = buildAdulteryCaseData();

        final Draft draft = new Draft("1", Collections.singletonMap("test", "value"), null);

        when(draftService.getDraft(TEST_AUTH_TOKEN)).thenReturn(draft);
        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING, PETITIONER)).thenReturn(caseDetails);

        Map<String, Object> expectedCaseData = new HashMap<>();
        expectedCaseData.put(PetitionServiceImpl.IS_DRAFT_KEY, true);
        expectedCaseData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        expectedCaseData.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE,
            singletonList(TEST_REASON_ADULTERY));
        CaseDetails expected = CaseDetails.builder()
            .data(expectedCaseData)
            .build();

        CaseDetails actual = classUnderTest.retrievePetition(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING);
        assertEquals(expected, actual);
        verifyCcdCaseDataToBeTransformed();
        verify(draftService).getDraft(TEST_AUTH_TOKEN);
        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING, PETITIONER);
    }

    @Test
    public void givenCcdRetrievalServiceReturnsAmendCaseWithAmendDraft_whenRetrievePetition_thenReturnAmendDraft() throws DuplicateCaseException {
        final CaseDetails caseDetails = CaseDetails.builder().state(CaseState.AMEND_PETITION.getValue()).build();
        Map<String, Object> amendedDraft = new HashMap<>();
        amendedDraft.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        amendedDraft.put("test", "value");
        final Draft draft = new Draft("1", amendedDraft, null);

        when(draftService.getDraft(TEST_AUTH_TOKEN)).thenReturn(draft);
        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING, PETITIONER)).thenReturn(caseDetails);

        CaseDetails actual = classUnderTest.retrievePetition(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING);

        amendedDraft.put(PetitionServiceImpl.IS_DRAFT_KEY, true);
        final CaseDetails expected = CaseDetails.builder()
            .data(amendedDraft)
            .build();
        assertEquals(expected, actual);
        verify(draftService).getDraft(TEST_AUTH_TOKEN);
        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING, PETITIONER);
    }

    @Test(expected = DuplicateCaseException.class)
    public void givenCcdRetrievalServiceThrowException_whenRetrievePetition_thenThrowException()
        throws DuplicateCaseException {
        final DuplicateCaseException duplicateCaseException = new DuplicateCaseException("Duplicate");

        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING, PETITIONER))
            .thenThrow(duplicateCaseException);

        classUnderTest.retrievePetition(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING);

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING, PETITIONER);
    }

    @Test
    public void givenNoDataInCcdOrDraft_whenRetrievePetition_thenReturnNull()
        throws DuplicateCaseException {

        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING, PETITIONER)).thenReturn(null);
        when(draftService.getDraft(TEST_AUTH_TOKEN)).thenReturn(null);

        assertNull(classUnderTest.retrievePetition(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING));

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER_CASE_STATE_GROUPING, PETITIONER);
        verify(draftService).getDraft(TEST_AUTH_TOKEN);
    }

    @Test
    public void givenCcdCaseIsNotFound_whenRetrievingPetitionForRespondent_thenReturnNull()
        throws DuplicateCaseException {
        CaseDetails actualCaseDetails = classUnderTest.retrievePetitionForAos(TEST_AUTH_TOKEN);

        assertThat(actualCaseDetails, is(nullValue()));
        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, RESPONDENT_CASE_STATE_GROUPING, RESPONDENT);
    }

    @Test
    public void givenDnDraftedCase_whenRetrievePetitionForRespondent_thenReturnCase() throws DuplicateCaseException {
        final CaseDetails caseDetails = CaseDetails.builder().state(CaseState.DN_DRAFTED.getValue()).build();

        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, RESPONDENT_CASE_STATE_GROUPING, RESPONDENT))
            .thenReturn(caseDetails);

        assertEquals(caseDetails, classUnderTest.retrievePetitionForAos(TEST_AUTH_TOKEN));

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, RESPONDENT_CASE_STATE_GROUPING, RESPONDENT);
    }

    @Test
    public void whenRetrievePetition_thenProceedAsExpected() throws DuplicateCaseException {
        final CaseDetails caseDetails = CaseDetails.builder().build();

        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(caseDetails);

        assertEquals(caseDetails, classUnderTest.retrievePetition(TEST_AUTH_TOKEN));

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
    }

    @Test
    public void whenRetrievePetitionById_thenProceedAsExpected() {
        final CaseDetails caseDetails = CaseDetails.builder().build();

        when(ccdRetrievalService.retrieveCaseById(TEST_AUTH_TOKEN, TEST_CASE_ID)).thenReturn(caseDetails);

        assertEquals(caseDetails, classUnderTest.retrievePetitionByCaseId(TEST_AUTH_TOKEN, TEST_CASE_ID));

        verify(ccdRetrievalService).retrieveCaseById(TEST_AUTH_TOKEN, TEST_CASE_ID);
    }

    @Test
    public void whenSaveDraft_thenProceedAsExpected() {
        final Map<String, Object> data = Collections.emptyMap();

        classUnderTest.saveDraft(TEST_AUTH_TOKEN, data, DIVORCE_FORMAT);

        verify(draftService).saveDraft(TEST_AUTH_TOKEN, data, DIVORCE_FORMAT);
    }

    @Test
    public void whenCreateDraft_thenProceedAsExpected() {
        final Map<String, Object> data = Collections.emptyMap();

        classUnderTest.createDraft(TEST_AUTH_TOKEN, data, DIVORCE_FORMAT);

        verify(draftService).createDraft(TEST_AUTH_TOKEN, data, DIVORCE_FORMAT);
    }

    @Test
    public void whenGetAllDrafts_thenProceedAsExpected() {
        classUnderTest.getAllDrafts(TEST_AUTH_TOKEN);

        verify(draftService).getAllDrafts(TEST_AUTH_TOKEN);
    }

    @Test
    public void whenDeleteDraft_thenProceedAsExpected() {
        classUnderTest.deleteDraft(TEST_AUTH_TOKEN);

        verify(draftService).deleteDraft(TEST_AUTH_TOKEN);
    }

    @Test
    public void whenOnApplicationEvent_thenProceedAsExpected() {
        final CaseSubmittedEvent caseSubmittedEvent = mock(CaseSubmittedEvent.class);

        when(caseSubmittedEvent.getAuthToken()).thenReturn(TEST_AUTH_TOKEN);

        classUnderTest.onApplicationEvent(caseSubmittedEvent);

        verify(draftService).deleteDraft(TEST_AUTH_TOKEN);
    }

    @Test
    public void whenCreateAmendedPetitionDraft_thenProceedAsExpected() throws DuplicateCaseException {
        Date originalCaseIssueDate = new Date();

        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(ISSUE_DATE, originalCaseIssueDate);
        caseData.put(D8_CASE_REFERENCE, TEST_CASE_ID);
        caseData.put(D8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        caseData.put(PREVIOUS_REASONS_DIVORCE, new ArrayList<>());

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData)
            .id(Long.decode(TEST_CASE_ID)).build();

        final Map<String, Object> draftData = new HashMap<>();
        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE, singletonList(TEST_REASON_ADULTERY));

        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());
        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(caseDetails);
        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);

        classUnderTest.createAmendedPetitionDraft(TEST_AUTH_TOKEN);

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
        verify(draftService).createDraft(TEST_AUTH_TOKEN, draftData, true);
        Map<String, Object> ccdCaseDataToBeTransformed = verifyCcdCaseDataToBeTransformed();
        assertThat(ccdCaseDataToBeTransformed, allOf(
            hasEntry(CcdCaseProperties.PREVIOUS_ISSUE_DATE, originalCaseIssueDate)
        ));
    }

    @Test
    public void givenCaseWasNotIssued_whenCreateAmendedPetitionDraft_thenProceedAsExpected() throws DuplicateCaseException {
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8_CASE_REFERENCE, TEST_CASE_ID);
        caseData.put(D8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        caseData.put(PREVIOUS_REASONS_DIVORCE, new ArrayList<>());

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData)
            .id(Long.decode(TEST_CASE_ID)).build();

        final Map<String, Object> draftData = new HashMap<>();
        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE, singletonList(TEST_REASON_ADULTERY));

        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());
        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(caseDetails);
        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);

        classUnderTest.createAmendedPetitionDraft(TEST_AUTH_TOKEN);

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
        verify(draftService).createDraft(TEST_AUTH_TOKEN, draftData, true);
        Map<String, Object> ccdCaseDataToBeTransformed = verifyCcdCaseDataToBeTransformed();
        assertThat(ccdCaseDataToBeTransformed, allOf(not(hasKey(CcdCaseProperties.PREVIOUS_ISSUE_DATE))));
    }

    @Test
    public void givenCaseNotProgressed_whenCreateAmendedPetitionDraft_thenReturnNull() throws DuplicateCaseException {
        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        caseData.put(PREVIOUS_REASONS_DIVORCE, new ArrayList<>());

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData)
            .id(Long.decode(TEST_CASE_ID)).build();

        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);
        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(caseDetails);

        assertNull(classUnderTest.createAmendedPetitionDraft(TEST_AUTH_TOKEN));

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
    }

    @Test
    public void givenNoUserExists_whenCreateAmendedPetitionDraft_thenReturnNull() throws DuplicateCaseException {
        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(null);

        assertNull(classUnderTest.createAmendedPetitionDraft(TEST_AUTH_TOKEN));

        verify(userService).retrieveUser(TEST_AUTH_TOKEN);
    }

    @Test
    public void whenCreateAmendedPetitionDraft_whenCaseHasPreviousReasons_thenProceedAsExpected()
        throws DuplicateCaseException {

        final Map<String, Object> caseData = new HashMap<>();
        final List<String> previousReasonsOld = new ArrayList<>();
        previousReasonsOld.add(TWO_YEAR_SEPARATION);
        caseData.put(D8_CASE_REFERENCE, TEST_CASE_REF);
        caseData.put(D8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        caseData.put(PREVIOUS_REASONS_DIVORCE, previousReasonsOld);

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData).id(Long.decode(TEST_CASE_ID)).build();
        final Map<String, Object> draftData = new HashMap<>();
        final List<String> previousReasons = new ArrayList<>();

        previousReasons.add(TWO_YEAR_SEPARATION);
        previousReasons.add(TEST_REASON_ADULTERY);
        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE, previousReasons);

        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());

        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);
        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(caseDetails);

        classUnderTest.createAmendedPetitionDraft(TEST_AUTH_TOKEN);

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
        verify(draftService).createDraft(TEST_AUTH_TOKEN, draftData, true);
    }

    @Test
    public void whenCreateAmendedPetitionDraft_whenPetitionNotFound_thenProceedAsExpected()
        throws DuplicateCaseException {
        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());

        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(null);
        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);

        classUnderTest.createAmendedPetitionDraft(TEST_AUTH_TOKEN);

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
    }

    @Test
    public void givenAmendPetitionDraft_whenRetrieveCase_thenReturnDraft() {
        Map<String, Object> documentMap = new HashMap<>();
        documentMap.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        Draft draft = buildDraft(ImmutableMap.of(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID));
        when(draftService.getDraft(TEST_AUTH_TOKEN))
            .thenReturn(draft);

        CaseDetails petition = classUnderTest.retrievePetition(TEST_AUTH_TOKEN, RESPONDENT_CASE_STATE_GROUPING);
        Draft expectedDraft = buildDraft(ImmutableMap.of(
            DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID,
            PetitionServiceImpl.IS_DRAFT_KEY, true
        ));

        assertThat(petition.getData(), equalTo(expectedDraft.getDocument()));
    }

    @Test
    public void whenCreateAmendedPetitionDraftForRefusal_thenProceedAsExpected() throws DuplicateCaseException {
        Date originalCaseIssueDate = new Date();

        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(ISSUE_DATE, originalCaseIssueDate);
        caseData.put(D8_CASE_REFERENCE, TEST_CASE_ID);
        caseData.put(D8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        caseData.put(REFUSAL_ORDER_REJECTION_REASONS, Collections.singletonList("other"));

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData)
            .id(Long.decode(TEST_CASE_ID)).build();

        final Map<String, Object> draftData = new HashMap<>();
        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(PREVIOUS_REASONS_FOR_DIVORCE_REFUSAL, singletonList(TEST_REASON_ADULTERY));

        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());
        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(caseDetails);
        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);

        classUnderTest.createAmendedPetitionDraftRefusalForDivorce(TEST_AUTH_TOKEN);

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
        verify(draftService).createDraft(TEST_AUTH_TOKEN, draftData, true);
        Map<String, Object> ccdCaseDataToBeTransformed = verifyCcdCaseDataToBeTransformed();
        assertThat(ccdCaseDataToBeTransformed, allOf(
            hasEntry(CcdCaseProperties.PREVIOUS_ISSUE_DATE, originalCaseIssueDate)
        ));
    }

    @Test
    public void whenCreateAmendedPetitionDraftForRefusalWithPreviousReasonsForDivorce_thenProceedAsExpected() throws DuplicateCaseException {
        Date originalCaseIssueDate = new Date();

        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(ISSUE_DATE, originalCaseIssueDate);
        caseData.put(D8_CASE_REFERENCE, TEST_CASE_ID);
        caseData.put(D8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        caseData.put(PREVIOUS_REASONS_DIVORCE_REFUSAL, new ArrayList<String>() {
            {
                add(TEST_REASON_UNREASONABLE_BEHAVIOUR);
            }
        });
        caseData.put(REFUSAL_ORDER_REJECTION_REASONS, Collections.singletonList("other"));

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData)
            .id(Long.decode(TEST_CASE_ID)).build();

        final Map<String, Object> draftData = new HashMap<>();
        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(PREVIOUS_REASONS_FOR_DIVORCE_REFUSAL, ImmutableList.of(
            TEST_REASON_UNREASONABLE_BEHAVIOUR, TEST_REASON_ADULTERY)
        );

        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());
        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(caseDetails);
        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);

        classUnderTest.createAmendedPetitionDraftRefusalForDivorce(TEST_AUTH_TOKEN);

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
        verify(draftService).createDraft(TEST_AUTH_TOKEN, draftData, true);
        Map<String, Object> ccdCaseDataToBeTransformed = verifyCcdCaseDataToBeTransformed();
        assertThat(ccdCaseDataToBeTransformed, allOf(
            hasEntry(CcdCaseProperties.PREVIOUS_ISSUE_DATE, originalCaseIssueDate)
        ));
    }

    @Test
    public void givenCaseWasNotIssued_whenCreateAmendedPetitionDraftForRefusal_thenProceedAsExpected() throws DuplicateCaseException {
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8_CASE_REFERENCE, TEST_CASE_ID);
        caseData.put(D8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        caseData.put(REFUSAL_ORDER_REJECTION_REASONS, Collections.singletonList("other"));

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData)
            .id(Long.decode(TEST_CASE_ID)).build();

        final Map<String, Object> draftData = new HashMap<>();
        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(PREVIOUS_REASONS_FOR_DIVORCE_REFUSAL, singletonList(TEST_REASON_ADULTERY));

        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());
        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(caseDetails);
        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);

        classUnderTest.createAmendedPetitionDraftRefusalForDivorce(TEST_AUTH_TOKEN);

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
        verify(draftService).createDraft(TEST_AUTH_TOKEN, draftData, true);
        Map<String, Object> ccdCaseDataToBeTransformed = verifyCcdCaseDataToBeTransformed();
        assertThat(ccdCaseDataToBeTransformed, allOf(not(hasKey(CcdCaseProperties.PREVIOUS_ISSUE_DATE))));
    }

    @Test
    public void givenCaseNotProgressed_whenCreateAmendedPetitionDraftForRefusal_thenReturnNull() throws DuplicateCaseException {
        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());
        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(REFUSAL_ORDER_REJECTION_REASONS, Collections.singletonList("other"));

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData)
            .id(Long.decode(TEST_CASE_ID)).build();

        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);
        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(caseDetails);

        assertNull(classUnderTest.createAmendedPetitionDraftRefusalForDivorce(TEST_AUTH_TOKEN));

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
    }

    @Test
    public void givenNoUserExists_whenCreateAmendedPetitionDraftForRefusal_thenReturnNull() throws DuplicateCaseException {
        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(null);

        assertNull(classUnderTest.createAmendedPetitionDraftRefusalForDivorce(TEST_AUTH_TOKEN));

        verify(userService).retrieveUser(TEST_AUTH_TOKEN);
    }

    @Test
    public void whenCreateAmendedPetitionDraftForRefusal_whenCaseIsRejectedForNoJurisdiction_thenProceedAsExpected()
        throws DuplicateCaseException {

        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8_CASE_REFERENCE, TEST_CASE_REF);
        caseData.put(REFUSAL_ORDER_REJECTION_REASONS, Collections.singletonList(REJECTION_NO_JURISDICTION));
        // Case Data to Keep
        caseData.put(D_8_DIVORCE_WHO, TEST_RELATIONSHIP);
        caseData.put(D_8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        // Case Data to be Removed
        caseData.put(D_8_HELP_WITH_FEES_NEED_HELP, YES_VALUE);
        caseData.put(D_8_CONNECTIONS, ImmutableList.of("A", "B"));

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData).id(Long.decode(TEST_CASE_ID)).build();
        final Map<String, Object> draftData = new HashMap<>();

        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE_REFUSAL,
            singletonList(TEST_REASON_ADULTERY));

        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());

        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);
        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(caseDetails);

        classUnderTest.createAmendedPetitionDraftRefusalForDivorce(TEST_AUTH_TOKEN);

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
        verify(draftService).createDraft(TEST_AUTH_TOKEN, draftData, true);
        Map<String, Object> ccdCaseDataToBeTransformed = verifyCcdCaseDataToBeTransformed();
        assertThat(ccdCaseDataToBeTransformed, allOf(
            hasKey(D_8_DIVORCE_WHO),
            hasKey(D_8_REASON_FOR_DIVORCE),
            not(hasKey(D_8_HELP_WITH_FEES_NEED_HELP)),
            not(hasKey(D_8_CONNECTIONS))
        ));
    }

    @Test
    public void whenCreateAmendedPetitionDraftForRefusal_whenCaseIsRejectedForNoCriteria_thenProceedAsExpected()
        throws DuplicateCaseException {

        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8_CASE_REFERENCE, TEST_CASE_REF);
        caseData.put(REFUSAL_ORDER_REJECTION_REASONS, Collections.singletonList(REJECTION_NO_CRITERIA));
        // Case Data to Keep
        caseData.put(D_8_DIVORCE_WHO, TEST_RELATIONSHIP);
        caseData.put(D_8_CONNECTIONS, ImmutableList.of("A", "B"));
        // Case Data to be Removed
        caseData.put(D_8_HELP_WITH_FEES_NEED_HELP, YES_VALUE);
        caseData.put(D_8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData).id(Long.decode(TEST_CASE_ID)).build();
        final Map<String, Object> draftData = new HashMap<>();

        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE_REFUSAL,
            singletonList(TEST_REASON_ADULTERY));

        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());

        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);
        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(caseDetails);

        classUnderTest.createAmendedPetitionDraftRefusalForDivorce(TEST_AUTH_TOKEN);

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
        verify(draftService).createDraft(TEST_AUTH_TOKEN, draftData, true);
        Map<String, Object> ccdCaseDataToBeTransformed = verifyCcdCaseDataToBeTransformed();
        assertThat(ccdCaseDataToBeTransformed, allOf(
            hasKey(D_8_DIVORCE_WHO),
            hasKey(D_8_CONNECTIONS),
            not(hasKey(D_8_HELP_WITH_FEES_NEED_HELP)),
            not(hasKey(D_8_REASON_FOR_DIVORCE))
        ));
    }

    @Test
    public void whenCreateAmendedPetitionDraftForRefusal_whenCaseIsRejectedForInsufficientDetails_thenProceedAsExpected()
        throws DuplicateCaseException {

        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8_CASE_REFERENCE, TEST_CASE_REF);
        caseData.put(REFUSAL_ORDER_REJECTION_REASONS, Collections.singletonList(REJECTION_INSUFFICIENT_DETAILS));
        // Case Data to Keep
        caseData.put(D_8_DIVORCE_WHO, TEST_RELATIONSHIP);
        caseData.put(D_8_CONNECTIONS, ImmutableList.of("A", "B"));
        // Case Data to be Removed
        caseData.put(D_8_HELP_WITH_FEES_NEED_HELP, YES_VALUE);
        caseData.put(D_8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData).id(Long.decode(TEST_CASE_ID)).build();
        final Map<String, Object> draftData = new HashMap<>();

        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE_REFUSAL,
            singletonList(TEST_REASON_ADULTERY));

        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());

        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);
        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(caseDetails);

        classUnderTest.createAmendedPetitionDraftRefusalForDivorce(TEST_AUTH_TOKEN);

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
        verify(draftService).createDraft(TEST_AUTH_TOKEN, draftData, true);
        Map<String, Object> ccdCaseDataToBeTransformed = verifyCcdCaseDataToBeTransformed();
        assertThat(ccdCaseDataToBeTransformed, allOf(
            hasKey(D_8_DIVORCE_WHO),
            hasKey(D_8_CONNECTIONS),
            not(hasKey(D_8_HELP_WITH_FEES_NEED_HELP)),
            not(hasKey(D_8_REASON_FOR_DIVORCE))
        ));
    }

    @Test
    public void whenCreateAmendedPetitionDraftForRefusal_whenCaseIsRejectedForAllReasons_thenProceedAsExpected()
        throws DuplicateCaseException {

        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8_CASE_REFERENCE, TEST_CASE_REF);
        List<String> refusalRejectionReasons = ImmutableList.of(
            REJECTION_NO_JURISDICTION, REJECTION_NO_CRITERIA, REJECTION_INSUFFICIENT_DETAILS
        );
        caseData.put(REFUSAL_ORDER_REJECTION_REASONS, refusalRejectionReasons);
        // Case Data to Keep
        caseData.put(D_8_DIVORCE_WHO, TEST_RELATIONSHIP);
        // Case Data to be Removed
        caseData.put(D_8_HELP_WITH_FEES_NEED_HELP, YES_VALUE);
        caseData.put(D_8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        caseData.put(D_8_CONNECTIONS, ImmutableList.of("A", "B"));

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData).id(Long.decode(TEST_CASE_ID)).build();
        final Map<String, Object> draftData = new HashMap<>();

        draftData.put(DivorceSessionProperties.PREVIOUS_CASE_ID, TEST_CASE_ID);
        draftData.put(DivorceSessionProperties.PREVIOUS_REASONS_FOR_DIVORCE_REFUSAL,
            singletonList(TEST_REASON_ADULTERY));

        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());

        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);
        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(caseDetails);

        classUnderTest.createAmendedPetitionDraftRefusalForDivorce(TEST_AUTH_TOKEN);

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
        verify(draftService).createDraft(TEST_AUTH_TOKEN, draftData, true);
        Map<String, Object> ccdCaseDataToBeTransformed = verifyCcdCaseDataToBeTransformed();
        assertThat(ccdCaseDataToBeTransformed, allOf(
            hasKey(D_8_DIVORCE_WHO),
            not(hasKey(D_8_HELP_WITH_FEES_NEED_HELP)),
            not(hasKey(D_8_REASON_FOR_DIVORCE)),
            not(hasKey(D_8_CONNECTIONS))
        ));
    }

    @Test
    public void whenCreateAmendedPetitionDraftForRefusal_whenPetitionNotFound_thenProceedAsExpected()
        throws DuplicateCaseException {
        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());

        when(ccdRetrievalService.retrieveCase(TEST_AUTH_TOKEN, PETITIONER)).thenReturn(null);
        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);

        classUnderTest.createAmendedPetitionDraftRefusalForDivorce(TEST_AUTH_TOKEN);

        verify(ccdRetrievalService).retrieveCase(TEST_AUTH_TOKEN, PETITIONER);
    }

    @Test
    public void whenCreateCcdAmendedPetitionDraftForRefusal_thenProceedAsExpectedAndKeepCcdFormatting()
        throws DuplicateCaseException {
        Date originalCaseIssueDate = new Date();

        final Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8_CASE_REFERENCE, TEST_FAMILY_MAN_REF);
        caseData.put(REFUSAL_ORDER_REJECTION_REASONS, Arrays.asList("noJurisdiction", "insufficentDetails"));
        // Case Data to Keep
        caseData.put(D8_PETITIONER_EMAIL, TEST_USER_EMAIL);
        // Case Data to be Removed
        caseData.put(ISSUE_DATE, originalCaseIssueDate);
        caseData.put(D8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        caseData.put(D_8_HELP_WITH_FEES_NEED_HELP, YES_VALUE);
        caseData.put(D_8_CONNECTIONS, ImmutableList.of("A", "B"));

        final CaseDetails caseDetails = CaseDetails.builder().data(caseData)
            .id(Long.decode(TEST_CASE_ID)).build();

        final User user = new User(TEST_AUTH_TOKEN, UserDetails.builder().forename(USER_FIRST_NAME).build());
        final User caseworkerUser = new User(TEST_AUTHORISATION, UserDetails.builder().forename(USER_FIRST_NAME).build());
        when(ccdRetrievalService.retrieveCaseById(TEST_AUTHORISATION, TEST_CASE_ID)).thenReturn(caseDetails);
        when(userService.retrieveUser(TEST_AUTH_TOKEN)).thenReturn(user);
        when(userService.retrieveAnonymousCaseWorkerDetails()).thenReturn(caseworkerUser);

        Map<String, Object> newCase = classUnderTest
            .createAmendedPetitionDraftRefusalForCCD(TEST_AUTH_TOKEN, TEST_CASE_ID);

        verify(ccdRetrievalService).retrieveCaseById(TEST_AUTHORISATION, TEST_CASE_ID);
        assertThat(newCase, hasEntry(PREVIOUS_ISSUE_DATE, originalCaseIssueDate));
        assertThat(newCase, hasEntry(PREVIOUS_REASONS_FOR_DIVORCE_REFUSAL, singletonList(TEST_REASON_ADULTERY)));
        assertThat(newCase, allOf(
            hasEntry(D8_DIVORCE_UNIT, CmsConstants.CTSC_SERVICE_CENTRE),
            hasEntry(D8_PETITIONER_EMAIL, TEST_USER_EMAIL),
            not(hasKey(ISSUE_DATE)),
            not(hasKey(D8_REASON_FOR_DIVORCE)),
            not(hasKey(D_8_HELP_WITH_FEES_NEED_HELP)),
            not(hasKey(D_8_CONNECTIONS))
        ));
        verifyNoInteractions(formatterServiceClient);
    }

    private Draft buildDraft(Map<String, Object> properties) {
        return new Draft(DRAFT_ID, properties, TEST_DRAFT_DOC_TYPE_DIVORCE_FORMAT);
    }

    private CaseDetails buildAdulteryCaseData() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(D8_DOCUMENTS_UPLOADED, singletonList(new Object()));
        caseData.put(D8_REJECT_DOCUMENTS_UPLOADED, singletonList(new Object()));
        caseData.put(D8_DOCUMENTS_GENERATED, singletonList(new Object()));
        caseData.put(D8_REASON_FOR_DIVORCE, TEST_REASON_ADULTERY);
        caseData.put(PREVIOUS_REASONS_DIVORCE, new ArrayList<>());
        return CaseDetails.builder()
            .id(Long.parseLong(TEST_CASE_ID))
            .state(CaseState.AMEND_PETITION.getValue())
            .data(caseData)
            .build();
    }

    private Map<String, Object> verifyCcdCaseDataToBeTransformed() {
        verify(formatterServiceClient).transformToDivorceFormat(ccdCaseDataArgumentCaptor.capture(), eq(TEST_AUTH_TOKEN));
        Map<String, Object> ccdCaseDataToBeTransformed = (Map) ccdCaseDataArgumentCaptor.getValue();
        assertThat(ccdCaseDataToBeTransformed, allOf(
            not(hasKey(D8_DOCUMENTS_UPLOADED)),
            not(hasKey(D8_REJECT_DOCUMENTS_UPLOADED)),
            not(hasKey(D8_DOCUMENTS_GENERATED))
        ));

        return ccdCaseDataToBeTransformed;
    }
}
