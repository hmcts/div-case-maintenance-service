package uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.factory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.CreateDraft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.UpdateDraft;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DraftModelFactoryUTest {
    private static final Map<String, Object> DATA = Collections.emptyMap();
    private static final String DRAFT_TYPE_NON_DIVORCE = "NonDivorceFormat";

    @Value("${draft.store.api.document.type.divorceFormat}")
    private String draftTypeDivorceFormat;

    @Value("${draft.store.api.document.type.ccdFormat}")
    private String draftTypeCcdFormat;

    @Value("${draft.store.api.max.age}")
    private int maxAge;

    @Mock
    private Draft draft;

    @Autowired
    private DraftModelFactory underTest;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void givenDivorceFormat_whenCreateDraft_thenCreateDraftInDivorceFormat() {
        CreateDraft createDraft = underTest.createDraft(DATA, true);

        assertEquals(draftTypeDivorceFormat, createDraft.getType());
        assertEquals(maxAge, createDraft.getMaxAge());
    }

    @Test
    public void givenCcdFormat_whenCreateDraft_thenCreateDraftInCcdFormat() {
        CreateDraft createDraft = underTest.createDraft(DATA, false);

        assertEquals(draftTypeCcdFormat, createDraft.getType());
        assertEquals(maxAge, createDraft.getMaxAge());
    }

    @Test
    public void givenDivorceFormat_whenUpdateDraft_thenCreateInDivorceFormat() {
        UpdateDraft updateDraft = underTest.updateDraft(DATA, true);

        assertEquals(draftTypeDivorceFormat, updateDraft.getType());
    }

    @Test
    public void givenCcdFormat_whenUpdateDraft_thenCreateInCcdFormat() {
        UpdateDraft updateDraft = underTest.updateDraft(DATA, false);

        assertEquals(draftTypeCcdFormat, updateDraft.getType());
    }

    @Test
    public void givenNonDivorceFormat_whenIsDivorceDraft_thenReturnFalse() {
        given(draft.getType()).willReturn(DRAFT_TYPE_NON_DIVORCE);

        assertFalse(underTest.isDivorceDraft(draft));
    }

    @Test
    public void givenDivorceFormat_whenIsDivorceDraft_thenReturnTrue() {
        given(draft.getType()).willReturn(draftTypeDivorceFormat);

        assertTrue(underTest.isDivorceDraft(draft));
    }

    @Test
    public void givenCcdFormat_whenIsDivorceDraft_thenReturnTrue() {
        given(draft.getType()).willReturn(draftTypeCcdFormat);

        assertTrue(underTest.isDivorceDraft(draft));
    }

    @Test
    public void givenCcdFormat_whenIsDivorceDraftInCcdFormat_thenReturnTrue() {
        given(draft.getType()).willReturn(draftTypeCcdFormat);

        assertTrue(underTest.isDivorceDraftInCcdFormat(draft));
    }

    @Test
    public void givenNotInCcdFormat_whenIsDivorceDraftInCcdFormat_thenReturnTrue() {
        given(draft.getType()).willReturn(draftTypeDivorceFormat);

        assertFalse(underTest.isDivorceDraftInCcdFormat(draft));
    }
}
