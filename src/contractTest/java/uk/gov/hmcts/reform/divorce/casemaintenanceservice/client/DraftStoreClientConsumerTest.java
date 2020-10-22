package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client;

import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactFolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.fluent.Executor;
import org.json.JSONException;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.ResourceUtils;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;
import static org.junit.Assert.assertEquals;

@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@PactTestFor(providerName = "draftStore_draft", port = "8891")
@PactFolder("pacts")
@SpringBootTest({
    "draft.store.api.baseurl : localhost:8891"
})
public class DraftStoreClientConsumerTest {

    @Autowired
    private DraftStoreClient draftStoreClient;
    ObjectMapper objectMapper = new ObjectMapper();

    public static final String SOME_AUTHORIZATION_TOKEN = "Bearer UserAuthToken";
    public static final String SOME_SERVICE_AUTHORIZATION_TOKEN = "ServiceToken";
    public static final String SOME_SECRET = "Secret";
    private static final String TOKEN = "someToken";

    public static final String SERVICE_AUTHORIZATION = "ServiceAuthorization";
    public static final String REGEX_DATE = "^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$";

    @BeforeEach
    public void setUpEachTest() throws InterruptedException {
        Thread.sleep(2000);
    }

    @After
    void teardown() {
        Executor.closeIdleConnections();
    }

    @Pact(provider = "draftStore_draft", consumer = "divorce_caseMaintenanceService")
    RequestResponsePact getAllDraftsForLoggedInUser(PactDslWithProvider builder) throws IOException {
        // @formatter:off
        Map<String, Object> draftMap = this.getDraftAsMap("base-case.json");
        return builder
            .given("A draft exists for a logged in user", draftMap)
            .uponReceiving("a request to retrieve those drafts")
            .path("/drafts")
            .method("GET")
            .headers(HttpHeaders.AUTHORIZATION, SOME_AUTHORIZATION_TOKEN, SERVICE_AUTHORIZATION,
                SOME_SERVICE_AUTHORIZATION_TOKEN)
            .willRespondWith()
            .status(200)
            .body(buildDraftListsPactDsl())
            .toPact();
    }

    private DslPart buildDraftListsPactDsl() {
        return newJsonBody((o) -> {
            o.minArrayLike("data", 0, 1, d -> d
                .stringType("id",
                    "123432")
                .stringValue("type", "divorce")
                .object("document", doc -> doc
                    .stringType("id", "1547073120300616")
                    .date("createdDate", "")
                    .stringMatcher("D8ScreenHasMarriageBroken", "YES|NO", "YES")
                    .stringMatcher("D8ScreenHasRespondentAddress", "YES|NO", "YES")
                    .stringMatcher("D8ScreenHasMarriageCert", "YES|NO", "YES")
                    .stringMatcher("D8ScreenHasPrinter", "YES|NO", "YES")
                    .stringMatcher("D8DivorceWho", "husband|wife")
                    .stringMatcher("D8MarriageDate", REGEX_DATE, "2001-02-02")
                    .stringMatcher("D8PetitionerNameDifferentToMarriageCert", "YES|NO", "YES")
                    .stringType("D8PetitionerEmail", "simulate-delivered@notifications.service.gov.uk")
                    .stringType("D8PetitionerPhoneNumber", "01234567890")
                    .stringType("D8PetitionerFirstName", "John")
                    .stringType("D8PetitionerLastName", "Smith")
                    .stringType("D8DerivedPetitionerCurrentFullName", "John Smith")
                    .stringType("D8PetitionerLastName", "Smith")
                    .array("D8PetitionerNameChangedHow", a -> a.stringType("marriageCertificate"))
                    .stringType("D8PetitionerContactDetailsConfidential", "share")
                    .object("D8PetitionerHomeAddress", ha -> ha
                        .stringType("AddressLine1", "82 Landor Road")
                        .stringType("AddressLine2", "London")
                        .stringType("PostCode", "SW9 9PE")
                    )
                    .stringType("D8DerivedPetitionerHomeAddress", "82 Landor Road\nLondon\nSW9 9PE")
                    .object("D8PetitionerCorrespondenceAddress", ca -> ca
                        .stringType("AddressLine1", "82 Landor Road")
                        .stringType("AddressLine2", "London")
                        .stringType("PostCode", "SW9 9PE")
                    )
                    .stringType("D8DerivedPetitionerCorrespondenceAddr", "82 Landor Road\nLondon\nSW9 9PE")
                    .stringMatcher("D8PetitionerCorrespondenceUseHomeAddress", "YES|NO", "YES")
                    .stringType("D8RespondentFirstName", "Jane")
                    .stringType("D8RespondentFirstName", "Jamed")
                    .stringType("D8DerivedRespondentCurrentName", "Jamed")
                    .stringType("D8DerivedRespondentSolicitorDetails", "Justin\nCase\n90 Landor Road\nLondon\nSW9 9PE")
                    .object("D8RespondentHomeAddress", ra -> ra
                        .stringType("AddressLine1", "82 Landor Road")
                        .stringType("AddressLine2", "London")
                        .stringType("PostCode", "SW9 9PE")
                    )
                    .stringType("D8DerivedRespondentHomeAddress", "Justin\nCase\n90 Landor Road\nLondon\nSW9 9PE")
                    .object("D8RespondentCorrespondenceAddress", rca -> rca
                        .stringType("AddressLine1", "82 Landor Road")
                        .stringType("AddressLine2", "London")
                        .stringType("PostCode", "SW9 9PE")
                    )
                    .stringType("D8DerivedRespondentCorrespondenceAddr", "Justin\nCase\n90 Landor Road\nLondon\nSW9 9PE")
                    .stringType("D8RespondentCorrespondenceUseHomeAddress", "Solicitor")
                    .stringMatcher("D8RespondentLivesAtLastAddress", "YES|NO", "YES")
                    .stringMatcher("D8LivingArrangementsLastLivedTogether", "YES|NO", "YES")
                    .stringMatcher("D8LivingArrangementsLiveTogether", "YES|NO", "YES")
                    .object("D8LivingArrangementsLastLivedTogethAddr", lta -> lta
                        .stringType("AddressLine1", "82 Landor Road")
                        .stringType("AddressLine2", "London")
                        .stringType("PostCode", "SW9 9PE")
                    )
                    .stringMatcher("D8LegalProceedings", "YES|NO", "YES")
                    .array("D8LegalProceedingsRelated", a -> a.stringType("children"))
                    .stringType("D8LegalProceedingsDetails", "The legal proceeding details")
                    .stringType("D8ReasonForDivorce", "unreasonable-behaviour")
                    .stringType("D8DerivedStatementOfCase", "My wife is having an affair this week.")

                    .stringType("D8ReasonForDivorceBehaviourDetails", "My wife is having an affair this week.")
                    .stringMatcher("D8ReasonForDivorceAdulteryIsNamed", "YES|NO", "NO")
                    .stringMatcher("D8FinancialOrder", "YES|NO", "NO")
                    .stringType("D8DerivedStatementOfCase", "My wife is having an affair this week.")
                    .array("D8FinancialOrderFor", a -> a.stringType("petitioner").stringType("children"))
                    .stringMatcher("D8HelpWithFeesNeedHelp", "YES|NO", "NO")
                    .stringMatcher("D8HelpWithFeesAppliedForFees", "YES|NO", "NO")
                    .stringType("D8HelpWithFeesReferenceNumber", "HWF-123-456")
                    .stringMatcher("D8DivorceCostsClaim", "YES|NO", "YES")
                    .array("D8DivorceClaimFrom", a -> a.stringType("respondent"))
                    .stringMatcher("D8JurisdictionConfidentLegal", "YES|NO", "YES")
                    .array("D8JurisdictionConnection", a -> a.stringType("A").stringType("C"))
                    .stringMatcher("D8JurisdictionPetitionerResidence", "YES|NO", "YES")
                    .stringMatcher("D8JurisdictionRespondentResidence", "YES|NO", "YES")
                    .minArrayLike("D8DocumentsUploaded", 0, 1, du -> du
                        .object("value", docO -> docO
                            .stringType("DocumentEmailContent")
                            .stringType("DocumentComment")
                            .stringType("DocumentFileName", "govuklogo.png")
                            .stringMatcher("DocumentDateAdded", REGEX_DATE, "2001-02-02")
                            .stringType("DocumentType", "other")
                            .object("DocumentLink", dl -> dl
                                .stringType("document_url",
                                    "http://dm-store-aat.service.core-compute-aat.internal/documents/7f63ca9b-c361-49ab-aa8c-8fbdb6bc2936")
                            )
                        )
                    )
                    .stringType("D8DivorceUnit", "eastMidlands")
                    .stringMatcher("D8ReasonForDivorceShowAdultery", "YES|NO", "YES")
                    .stringMatcher("D8ReasonForDivorceShowUnreasonableBehavi", "YES|NO", "YES")
                    .stringMatcher("D8ReasonForDivorceShowTwoYearsSeparation", "YES|NO", "YES")
                    .stringMatcher("D8ReasonForDivorceShowDesertion", "YES|NO", "YES")
                    .stringMatcher("D8ReasonForDivorceLimitReasons", "YES|NO", "YES")
                    .stringMatcher("D8ReasonForDivorceEnableAdultery", "YES|NO", "YES")
                    .stringMatcher("D8ClaimsCostsAppliedForFees", "YES|NO", "YES")
                    .stringMatcher("D8ReasonForDivorceClaimingAdultery", "YES|NO", "YES")
                    .stringMatcher("D8MarriageCanDivorce", "YES|NO", "YES")
                    .stringType("D8MarriagePetitionerName", "John Doe")
                    .stringType("D8MarriageRespondentName", "Jenny Benny")
                    .stringType("D8DerivedRespondentSolicitorAddr", "90 Landor Road\nLondon\nSW9 9PE")
                    .stringType("D8DerivedLivingArrangementsLastLivedAddr", "Flat A-B\n86 Landor Road\nLondon\nSW9 9PE")
                    .minArrayLike("D8Connections", 0, 1, dc -> dc
                        .stringType("A", "The Petitioner and the Respondent are habitually resident in England and Wales")
                    )
                    .stringMatcher("D8ReasonForDivorceHasMarriage", "YES|NO", "YES")
                    .stringMatcher("D8ReasonForDivorceShowFiveYearsSeparatio", "YES|NO", "YES")
                    .stringMatcher("D8ReasonForDivorceClaiming5YearSeparatio", "YES|NO", "YES")
                    .stringType("D8Cohort", "onlineSubmissionPrivateBeta")
                    .stringMatcher("D8InferredPetitionerGender", "male|female", "female")
                    .stringMatcher("D8InferredRespondentGender", "male|female", "male")
                )
            );

        }).build();
    }

    @Test
    @PactTestFor(pactMethod = "getAllDraftsForLoggedInUser")
    public void verifyGetAllDraftsForLoggedInUserPact() throws IOException, JSONException {
        DraftList response = draftStoreClient.getAllDrafts(SOME_AUTHORIZATION_TOKEN,
            SOME_SERVICE_AUTHORIZATION_TOKEN, SOME_SECRET);
        assertEquals(response.getData().get(0).getDocument().get("id"), "1547073120300616");

    }

    protected Map getDraftAsMap(String fileName) throws JSONException, IOException {
        File file = getFile(fileName);
        Map draft = objectMapper.readValue(file, Map.class);
        return draft;
    }

    private File getFile(String fileName) throws FileNotFoundException {
        return ResourceUtils.getFile(this.getClass().getResource("/json/" + fileName));
    }
}
