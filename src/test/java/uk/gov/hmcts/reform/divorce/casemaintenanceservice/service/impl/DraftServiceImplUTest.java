package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.DraftStoreClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.factory.DraftModelFactory;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.factory.EncryptionKeyFactory;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.CreateDraft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.UpdateDraft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.IdamUserService;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DraftServiceImplUTest {
    private static final String USER_ID = "1";
    private static final String ENCRYPTED_USER_ID = "encryptUserId1";
    private static final String AUTH_TOKEN = "someToken";
    private static final String BEARER_AUTH_TOKEN = "Bearer someToken";
    private static final String SERVICE_TOKEN = "serviceToken";
    private static final String DRAFT_DOCUMENT_TYPE = "divorcedraft";

    @Mock
    private AuthTokenGenerator serviceTokenGenerator;

    @Mock
    private DraftStoreClient draftStoreClient;

    @Mock
    private IdamUserService idamUserService;

    @Mock
    private EncryptionKeyFactory encryptionKeyFactory;

    @Mock
    private DraftModelFactory modelFactory;

    @InjectMocks
    private DraftServiceImpl classUnderTest;

    @Test
    public void givenNoDataInDraftStore_whenGetAllDrafts_thenReturnNull() {
        mockGetDraftsAndReturn(null, null);
        assertNull(classUnderTest.getAllDrafts(AUTH_TOKEN));
    }

    @Test
    public void givenDraftsExists_whenGetAllDrafts_thenReturnDrafts() {
        final DraftList draftList = new DraftList(Arrays.asList(
            createDraft("1"),
            createDraft("2")),
            null);

        mockGetDraftsAndReturn(null, draftList);

        DraftList actual = classUnderTest.getAllDrafts(AUTH_TOKEN);

        assertEquals(draftList, actual);
    }

    @Test
    public void givenDraftsDoesNotExist_whenSaveDraft_thenCreateNewDraft() {
        mockGetDraftsAndReturn(null, null);

        final Map<String, Object> data = Collections.emptyMap();
        final CreateDraft createDraft = new CreateDraft(data, null, 2, true);

        when(modelFactory.createDraft(data)).thenReturn(createDraft);
        when(draftStoreClient.createSingleDraft(createDraft, BEARER_AUTH_TOKEN, SERVICE_TOKEN, ENCRYPTED_USER_ID))
            .thenReturn(null);

        classUnderTest.saveDraft(AUTH_TOKEN, data);

        verify(draftStoreClient).createSingleDraft(createDraft, BEARER_AUTH_TOKEN, SERVICE_TOKEN, ENCRYPTED_USER_ID);
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
        final UpdateDraft updateDraft = new UpdateDraft(data, null, false);

        when(modelFactory.updateDraft(data)).thenReturn(updateDraft);
        when(modelFactory.isDivorceDraft(draft)).thenReturn(true);
        when(draftStoreClient
            .updateSingleDraft(draftId, updateDraft, BEARER_AUTH_TOKEN, SERVICE_TOKEN, ENCRYPTED_USER_ID))
            .thenReturn(null);

        classUnderTest.saveDraft(AUTH_TOKEN, data);

        verify(draftStoreClient)
            .updateSingleDraft(draftId, updateDraft, BEARER_AUTH_TOKEN, SERVICE_TOKEN, ENCRYPTED_USER_ID);
    }

    @Test
    public void givenDraftDoesNotExist_whenDeleteDraft_thenDoNothing() {
        mockGetDraftsAndReturn(null, null);

        classUnderTest.deleteDraft(AUTH_TOKEN);

        verify(draftStoreClient, times(0))
            .deleteSingleDraft(anyString(), anyString(), anyString());
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

        when(modelFactory.isDivorceDraft(draft)).thenReturn(true);
        when(draftStoreClient.deleteSingleDraft(draftId, BEARER_AUTH_TOKEN, SERVICE_TOKEN)).thenReturn(null);

        classUnderTest.deleteDraft(AUTH_TOKEN);

        verify(draftStoreClient).deleteSingleDraft(draftId, BEARER_AUTH_TOKEN, SERVICE_TOKEN);
    }

    @Test
    public void givenNoDrafts_whenGetDraft_thenReturnNull() {
        mockGetDraftsAndReturn(null, null);

        assertNull(classUnderTest.getDraft(AUTH_TOKEN));
    }

    @Test
    public void givenDraftListIsEmpty_whenGetDraft_thenReturnNull() {
        mockGetDraftsAndReturn(null,
            new DraftList(Collections.emptyList(), new DraftList.PagingCursors(null)));

        assertNull(classUnderTest.getDraft(AUTH_TOKEN));
    }

    @Test
    public void givenNoDivorceDrafts_whenGetDraft_thenReturnNull() {
        final DraftList draftList = new DraftList(Arrays.asList(
            new Draft("1", null, "somerandomtype", true),
            new Draft("2", null, "somerandomtype", true)),
            new DraftList.PagingCursors(null));

        mockGetDraftsAndReturn(null, draftList);

        assertNull(classUnderTest.getDraft(AUTH_TOKEN));
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

        Draft actual = classUnderTest.getDraft(AUTH_TOKEN);

        assertEquals(draft, actual);
    }

    @Test
    public void givenDataAvailableOnSubsequentPage_whenDeleteDraft_thenReturnTheDraft() {
        final Draft draft = createDraft("3");
        final String after = "1";

        final DraftList firstPage = new DraftList(Arrays.asList(
            new Draft("1", null, "somerandomtype", true),
            new Draft("2", null, "somerandomtype", false)),
            new DraftList.PagingCursors(after));

        final DraftList secondPage = new DraftList(Arrays.asList(
            draft,
            createDraft("4")),
            new DraftList.PagingCursors(null));

        mockGetDraftsAndReturn(null, firstPage);
        mockGetDraftsAndReturn(after, secondPage);

        when(modelFactory.isDivorceDraft(draft)).thenReturn(true);

        Draft actual = classUnderTest.getDraft(AUTH_TOKEN);

        assertEquals(draft, actual);

        verify(draftStoreClient).getAllDrafts(BEARER_AUTH_TOKEN, SERVICE_TOKEN, ENCRYPTED_USER_ID);
        verify(draftStoreClient).getAllDrafts(after, BEARER_AUTH_TOKEN, SERVICE_TOKEN, ENCRYPTED_USER_ID);
    }

    private void mockGetDraftsAndReturn(String after, DraftList draftList) {
        when(idamUserService.retrieveUserDetails(BEARER_AUTH_TOKEN)).thenReturn(createUserDetails());
        when(serviceTokenGenerator.generate()).thenReturn(SERVICE_TOKEN);
        when(encryptionKeyFactory.createEncryptionKey(USER_ID)).thenReturn(ENCRYPTED_USER_ID);

        if (after == null) {
            when(draftStoreClient.getAllDrafts(BEARER_AUTH_TOKEN, SERVICE_TOKEN, ENCRYPTED_USER_ID))
                .thenReturn(draftList);
        } else {
            when(draftStoreClient.getAllDrafts(after, BEARER_AUTH_TOKEN, SERVICE_TOKEN, ENCRYPTED_USER_ID))
                .thenReturn(draftList);
        }
    }

    private UserDetails createUserDetails() {
        return UserDetails.builder().id(USER_ID).build();
    }

    private Draft createDraft(String id) {
        return new Draft(id, null, DRAFT_DOCUMENT_TYPE, true);
    }
}
