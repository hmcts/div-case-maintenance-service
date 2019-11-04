package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.DraftStoreClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.factory.DraftModelFactory;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.factory.EncryptionKeyFactory;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.CreateDraft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.UpdateDraft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.UserService;
import uk.gov.hmcts.reform.idam.client.models.User;
import uk.gov.hmcts.reform.idam.client.models.UserDetails;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_AUTHORISATION;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_BEARER_AUTHORISATION;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_DRAFT_DOCUMENT_TYPE_CCD_FORMAT;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_DRAFT_DOC_TYPE_DIVORCE_FORMAT;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.TestConstants.TEST_SERVICE_TOKEN;

@RunWith(MockitoJUnitRunner.class)
public class DraftServiceImplUTest {
    private static final String USER_ID = "1";
    private static final String ENCRYPTED_USER_ID = "encryptUserId1";
    private static final boolean DIVORCE_FORMAT = false;

    @Mock
    private AuthTokenGenerator serviceTokenGenerator;

    @Mock
    private DraftStoreClient draftStoreClient;

    @Mock
    private UserService userService;

    @Mock
    private EncryptionKeyFactory encryptionKeyFactory;

    @Mock
    private DraftModelFactory modelFactory;

    @InjectMocks
    private DraftServiceImpl classUnderTest;

    @Test
    public void givenNoDataInDraftStore_whenGetAllDrafts_thenReturnNull() {
        mockGetDraftsAndReturn(null, null);
        assertNull(classUnderTest.getAllDrafts(TEST_AUTHORISATION));
    }

    @Test
    public void givenDraftsExists_whenGetAllDrafts_thenReturnDrafts() {
        final DraftList draftList = new DraftList(Arrays.asList(
            createDraft("1"),
            createDraft("2")),
            null);

        mockGetDraftsAndReturn(null, draftList);

        DraftList actual = classUnderTest.getAllDrafts(TEST_AUTHORISATION);

        assertEquals(draftList, actual);
    }

    @Test
    public void givenDraftsDoesNotExist_whenSaveDraft_thenCreateNewDraft() {
        mockGetDraftsAndReturn(null, null);

        final Map<String, Object> data = Collections.emptyMap();
        final CreateDraft createDraft = new CreateDraft(data, null, 2);

        when(modelFactory.createDraft(data, DIVORCE_FORMAT)).thenReturn(createDraft);
        doNothing().when(draftStoreClient)
            .createSingleDraft(createDraft, TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, ENCRYPTED_USER_ID);

        classUnderTest.saveDraft(TEST_AUTHORISATION, data, DIVORCE_FORMAT);

        verify(draftStoreClient).createSingleDraft(createDraft, TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, ENCRYPTED_USER_ID);
    }

    @Test
    public void givenDraftsExist_whenSaveDraft_thenUpdateExistingDraft() {
        final String draftId = "1";
        final Draft draft = createDraft(draftId);

        final DraftList draftList = new DraftList(Arrays.asList(
            draft,
            createDraft("2")),
            new DraftList.PagingCursors(null));

        mockGetDraftsAndReturn(null, draftList);

        final Map<String, Object> data = Collections.emptyMap();
        final UpdateDraft updateDraft = new UpdateDraft(data, null);

        when(modelFactory.updateDraft(data, DIVORCE_FORMAT)).thenReturn(updateDraft);
        when(modelFactory.isDivorceDraft(draft)).thenReturn(true);
        doNothing().when(draftStoreClient)
            .updateSingleDraft(draftId, updateDraft, TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, ENCRYPTED_USER_ID);

        classUnderTest.saveDraft(TEST_AUTHORISATION, data, DIVORCE_FORMAT);

        verify(draftStoreClient)
            .updateSingleDraft(draftId, updateDraft, TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, ENCRYPTED_USER_ID);
    }

    @Test
    public void whenCreateDraft_thenProceedAsExpected() {
        final Map<String, Object> data = Collections.emptyMap();
        final CreateDraft createDraft = new CreateDraft(data, null, 2);

        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(createUserDetails());
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(encryptionKeyFactory.createEncryptionKey(USER_ID)).thenReturn(ENCRYPTED_USER_ID);
        when(modelFactory.createDraft(data, DIVORCE_FORMAT)).thenReturn(createDraft);
        doNothing().when(draftStoreClient)
            .createSingleDraft(createDraft, TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, ENCRYPTED_USER_ID);

        classUnderTest.createDraft(TEST_AUTHORISATION, data, DIVORCE_FORMAT);

        verify(draftStoreClient).createSingleDraft(createDraft, TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, ENCRYPTED_USER_ID);
    }

    @Test
    public void givenDraftExists_whenDeleteDraft_thenDeleteTheFirstDraft() {
        final String draftId = "1";
        final Draft draft = createDraft(draftId);

        final DraftList draftList = new DraftList(Arrays.asList(
            draft,
            createDraft("2")),
            new DraftList.PagingCursors(null));

        mockGetDraftsAndReturn(null, draftList);

        doNothing().when(draftStoreClient).deleteAllDrafts(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN);

        classUnderTest.deleteDraft(TEST_AUTHORISATION);

        verify(draftStoreClient).deleteAllDrafts(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN);
    }

    @Test
    public void givenNoDrafts_whenGetDraft_thenReturnNull() {
        mockGetDraftsAndReturn(null, null);

        assertNull(classUnderTest.getDraft(TEST_AUTHORISATION));
    }

    @Test
    public void givenDraftListIsEmpty_whenGetDraft_thenReturnNull() {
        mockGetDraftsAndReturn(null,
            new DraftList(Collections.emptyList(), new DraftList.PagingCursors(null)));

        assertNull(classUnderTest.getDraft(TEST_AUTHORISATION));
    }

    @Test
    public void givenNoDivorceDrafts_whenGetDraft_thenReturnNull() {
        final DraftList draftList = new DraftList(Arrays.asList(
            new Draft("1", null, "somerandomtype"),
            new Draft("2", null, "somerandomtype")),
            new DraftList.PagingCursors(null));

        mockGetDraftsAndReturn(null, draftList);

        assertNull(classUnderTest.getDraft(TEST_AUTHORISATION));
    }

    @Test
    public void whenIsInCcdFormat_thenProceedAsExpected() {
        Draft draft = mock(Draft.class);

        when(modelFactory.isDivorceDraftInCcdFormat(draft)).thenReturn(true);

        assertTrue(classUnderTest.isInCcdFormat(draft));
    }

    @Test
    public void givenDataAvailableOnFirstPage_whenDeleteDraft_thenReturnTheDraft() {
        final Draft draft = createDraft("1");

        final DraftList draftList = new DraftList(Arrays.asList(
            draft,
            createDraft("2")),
            new DraftList.PagingCursors(null));

        mockGetDraftsAndReturn(null, draftList);
        when(modelFactory.isDivorceDraft(draft)).thenReturn(true);

        Draft actual = classUnderTest.getDraft(TEST_AUTHORISATION);

        assertEquals(draft, actual);
    }

    @Test
    public void givenDataAvailableOnSubsequentPage_whenDeleteDraft_thenReturnTheDraft() {
        final Draft draft = createDraft("3", TEST_DRAFT_DOC_TYPE_DIVORCE_FORMAT);
        final String after = "1";

        final DraftList firstPage = new DraftList(Arrays.asList(
            new Draft("1", null, "somerandomtype"),
            new Draft("2", null, "somerandomtype")),
            new DraftList.PagingCursors(after));

        final DraftList secondPage = new DraftList(Arrays.asList(
            draft,
            createDraft("4")),
            new DraftList.PagingCursors(null));

        mockGetDraftsAndReturn(null, firstPage);
        mockGetDraftsAndReturn(after, secondPage);

        when(modelFactory.isDivorceDraft(draft)).thenReturn(true);

        Draft actual = classUnderTest.getDraft(TEST_AUTHORISATION);

        assertEquals(draft, actual);

        verify(draftStoreClient).getAllDrafts(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, ENCRYPTED_USER_ID);
        verify(draftStoreClient).getAllDrafts(after, TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, ENCRYPTED_USER_ID);
    }

    private void mockGetDraftsAndReturn(String after, DraftList draftList) {
        when(userService.retrieveUser(TEST_BEARER_AUTHORISATION)).thenReturn(createUserDetails());
        when(serviceTokenGenerator.generate()).thenReturn(TEST_SERVICE_TOKEN);
        when(encryptionKeyFactory.createEncryptionKey(USER_ID)).thenReturn(ENCRYPTED_USER_ID);

        if (after == null) {
            when(draftStoreClient.getAllDrafts(TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, ENCRYPTED_USER_ID))
                .thenReturn(draftList);
        } else {
            when(draftStoreClient.getAllDrafts(after, TEST_BEARER_AUTHORISATION, TEST_SERVICE_TOKEN, ENCRYPTED_USER_ID))
                .thenReturn(draftList);
        }
    }

    private User createUserDetails() {
        return new User("auth", UserDetails.builder().id(USER_ID).build());
    }

    private Draft createDraft(String id) {
        return createDraft(id, TEST_DRAFT_DOCUMENT_TYPE_CCD_FORMAT);
    }

    private Draft createDraft(String id, String documentType) {
        return new Draft(id, null, documentType);
    }
}
