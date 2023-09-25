package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util;


import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.LambdaDslObject;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;

import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonArray;
import static au.com.dius.pact.consumer.dsl.LambdaDsl.newJsonBody;
import static au.com.dius.pact.consumer.dsl.PactDslJsonRootValue.stringMatcher;

public final class PactDslBuilderForCaseDetailsList {

    public static final String REGEX_DATE = "^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$";
    private static final String ALPHABETIC_REGEX = "[/^[A-Za-z_]+$/]+";

    public static DslPart buildStartEventReponse(String eventId) {
        return newJsonBody((o) -> {
            o.stringType("event_id", eventId)
                .stringType("token", null)
                .object("case_details", (cd) -> {
                    cd.numberType("id", 2000);
                    cd.stringMatcher("jurisdiction", ALPHABETIC_REGEX, "DIVORCE");
                    cd.stringType("callback_response_status",null);
                    //cd.stringMatcher("case_type", ALPHABETIC_REGEX, "DIVORCE");
                    cd.object("case_data", PactDslBuilderForCaseDetailsList::getCaseData);
                });
        }).build();
    }

    public static DslPart buildStartEventResponseWithEmptyCaseDetails(String eventId) {
        return newJsonBody((o) -> {
            o.stringType("event_id", eventId)
                .stringType("token", null)
                .object("case_details", (cd) -> {
                    cd.numberType("id", null);
                    cd.stringMatcher("jurisdiction", ALPHABETIC_REGEX, "DIVORCE");
                    cd.stringType("callback_response_status",null);
                    cd.stringMatcher("case_type_id", ALPHABETIC_REGEX, "DIVORCE");
                    cd.object("case_data", data -> {
                    });
                });
        }).build();
    }

    private static void getCaseData(final LambdaDslObject dataMap) {
        dataMap
            .stringType("createdDate", "")
            .stringMatcher("D8ScreenHasMarriageBroken", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ScreenHasRespondentAddress", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ScreenHasMarriageCert", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ScreenHasPrinter", "Yes|No|YES|NO", "YES")
            .stringType("D8DivorceWho", "husband")
            .stringMatcher("D8MarriageIsSameSexCouple", "Yes|No|YES|NO", "NO")
            .stringMatcher("D8MarriageDate", REGEX_DATE, "2018-05-17")
            .stringMatcher("D8PetitionerNameDifferentToMarriageCert", "Yes|No|YES|NO", "NO")
            .stringValue("D8PetitionerEmail", "simulate-delivered@notifications.service.gov.uk")
            .stringValue("D8PetitionerPhoneNumber", "01234567890")
            .stringType("D8PetitionerFirstName", "John")
            .stringType("D8PetitionerLastName", "Smith")
            .stringType("D8DerivedPetitionerCurrentFullName", "John Smith")
            .stringType("D8PetitionerLastName", "Smith")
            .minArrayLike("D8PetitionerNameChangedHow", 1,
                stringMatcher("marriageCertificate|certificateTODO", "marriageCertificate"), 1)
            .stringType("D8PetitionerContactDetailsConfidential", "share")
            .object("D8PetitionerHomeAddress", (address) ->
                address.stringType("AddressLine1", "")
                    .stringType("AddressLine2", "")
                    .stringType("AddressLine3", "")
                    .stringType("Country", "")
                    .stringType("County", "")
                    .stringType("PostTown", "")
                    .stringType("PostCode", "SW17 0QT")
            )
            .stringType("D8DerivedPetitionerHomeAddress", "\"82 Landor Road\nLondon\nSW9 9PE")
            .object("D8PetitionerCorrespondenceAddress", (addr) -> {
                addr.stringType("AddressLine1", "");
                addr.stringType("AddressLine2", "");
                addr.stringType("AddressLine3", "");
                addr.stringType("Country", "");
                addr.stringType("PostCode", "SM1 2JE");
                addr.stringType("PostTown", "");
            })
            .stringType("D8PetitionerCorrespondenceUseHomeAddress", "NO")
            .stringType("D8DerivedPetitionerCorrespondenceAddr", "84 Landor Road\nLondon\nSW9 9PE")
            .stringType("D8PetitionerCorrespondenceUseHomeAddress", "NO")
            .stringType("D8RespondentFirstName", "Jane")
            .stringType("D8RespondentLastName", "Jamed")
            .stringType("D8DerivedRespondentCurrentName", "Jane Jamed")
            .stringType("D8DerivedRespondentSolicitorDetails", "Justin\nCase\n90 Landor Road\nLondon\nSW9 9PE")
            .object("D8RespondentHomeAddress", (addr) -> {
                addr.stringType("AddressLine1", "");
                addr.stringType("AddressLine2", "");
                addr.stringType("AddressLine3", "");
                addr.stringType("Country", "");
                addr.stringType("PostCode", "SM1 2JE");
                addr.stringType("PostTown", "");
            })
            .stringType("D8RespondentCorrespondenceUseHomeAddress", "Solicitor")

            .stringType("D8DerivedRespondentHomeAddress", "88 Landor Road\nLondon\nSW9 9PE")
            .object("D8RespondentCorrespondenceAddress", (addr) -> {
                addr.stringType("AddressLine1", "");
                addr.stringType("AddressLine2", "");
                addr.stringType("AddressLine3", "");
                addr.stringType("Country", "");
                addr.stringType("PostCode", "SM1 2JE");
                addr.stringType("PostTown", "");
            })
            .stringType("D8RespondentCorrespondenceUseHomeAddress", "Solicitor")
            .stringType("D8DerivedRespondentCorrespondenceAddr", "82 Landor Road\nLondon\nSW9 9PE")
            .stringMatcher("D8RespondentLivesAtLastAddress", "Yes|No|YES|NO", "NO")
            .stringMatcher("D8LivingArrangementsLastLivedTogether", "Yes|No|YES|NO", "NO")
            .stringMatcher("D8LivingArrangementsLiveTogether", "Yes|No|YES|NO", "NO")
            .object("D8LivingArrangementsLastLivedTogethAddr", (addr) -> {
                addr.stringType("AddressLine1", "");
                addr.stringType("AddressLine2", "");
                addr.stringType("AddressLine3", "");
                addr.stringType("Country", "");
                addr.stringType("PostCode", "SM1 2JE");
                addr.stringType("PostTown", "");
            })
            .stringMatcher("D8LegalProceedings", "Yes|No|YES|NO", "YES")
            .array("D8LegalProceedingsRelated", (s) -> {
                s.stringType("children");
            })
            .stringType("D8LegalProceedingsDetails", "The legal proceeding details")
            .stringType("D8ReasonForDivorce", "unreasonable-behaviour")
            .stringType("D8DerivedStatementOfCase", "My wife is having an affair this week.")
            .stringType("D8ReasonForDivorceBehaviourDetails", "My wife is having an affair this week.")
            .stringMatcher("D8FinancialOrder", "Yes|No|YES|NO", "YES")
            .minArrayLike("D8FinancialOrderFor", 1,
                stringMatcher("petitioner|children", "petitioner"), 2)
            .stringMatcher("D8HelpWithFeesNeedHelp", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8HelpWithFeesAppliedForFees", "(Yes|No|YES|NO)", "YES")
            .stringType("D8HelpWithFeesReferenceNumber", "HWF-123-456")
            .stringMatcher("D8DivorceCostsClaim", "Yes|No|YES|NO", "YES")
            .array("D8DivorceClaimFrom", (s) -> {
                s.stringType("respondent");
            })
            .stringMatcher("D8JurisdictionConfidentLegal", "Yes|No|YES|NO", "YES")
            .array("D8JurisdictionConnection", (s) -> {
                s.stringType("A");
                s.stringType("C");
            })
            .stringMatcher("D8JurisdictionPetitionerResidence", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8JurisdictionRespondentResidence", "Yes|No|YES|NO", "YES")

            // D8DocumentsUploaded
            .minArrayLike("D8DocumentsUploaded", 2, 2,
                d8DocsUploaded ->
                    d8DocsUploaded.stringType("id", "059dadb4-04c1-44f9-9828-6194bfc9893c")
                        .object("value", (v) ->
                            v.stringType("DocumentEmailContent", "")
                                .stringType("DocumentComment", "")
                                .stringType("DocumentFileName", "govuklogo.png")
                                .stringMatcher("DocumentDateAdded", REGEX_DATE, "2017-12-11")
                                .stringType("DocumentType", "other")
                                .object("DocumentLink", (link) ->
                                    link.stringType("document_url", "http://dm-store-aat."
                                        + "service.core-compute-aat.internal/documents/7f63ca9b-c361-49ab-aa8c-8fbdb6bc2936"))
                        ) // document_link object
            ) // minArray
            .stringType("D8DivorceUnit", "eastMidlands")
            .stringMatcher("D8ReasonForDivorceShowAdultery", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ReasonForDivorceShowUnreasonableBehavi", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ReasonForDivorceShowTwoYearsSeparation", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ReasonForDivorceShowDesertion", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ReasonForDivorceLimitReasons", "Yes|No|YES|NO", "NO")
            .stringMatcher("D8ReasonForDivorceEnableAdultery", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ClaimsCostsAppliedForFees", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ReasonForDivorceClaimingAdultery", "Yes|No|YES|NO", "NO")
            .stringMatcher("D8MarriageCanDivorce", "Yes|No|YES|NO", "YES")
            .stringType("D8MarriagePetitionerName", "John Doe")
            .stringType("D8MarriageRespondentName", "Jenny Benny")
            .stringType("D8DerivedRespondentSolicitorAddr", "90 Landor Road\nLondon\nSW9 9PE")
            .stringType("D8DerivedLivingArrangementsLastLivedAddr", "Flat A-B\n86 Landor Road\nLondon\nSW9 9PE")
            .object("D8Connections", (d8) -> {
                d8.stringType("A", "The Petitioner and the Respondent are habitually resident in England and Wales");
                d8.stringType("B", "");
                d8.stringType("C", "The Respondent is habitually resident in England and Wales");
                d8.stringType("D", "");
                d8.stringType("E", "");
                d8.stringType("F", "");
                d8.stringType("G", "");


            })
            .stringMatcher("D8ReasonForDivorceHasMarriage", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ReasonForDivorceShowFiveYearsSeparatio", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ReasonForDivorceClaiming5YearSeparatio", "Yes|No|YES|NO", "NO")
            .stringType("D8Cohort", "onlineSubmissionPrivateBeta")
            .stringType("D8InferredPetitionerGender", "female")
            .stringType("D8InferredRespondentGender", "male");
    }


    private static DslPart getCaseData() {
        PactDslJsonBody body = new PactDslJsonBody();
        body.stringType("createdDate", "")
            .stringMatcher("D8ScreenHasMarriageBroken", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ScreenHasRespondentAddress", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ScreenHasMarriageCert", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ScreenHasPrinter", "Yes|No|YES|NO", "YES")
            .stringType("D8DivorceWho", "husband")
            .stringMatcher("D8MarriageIsSameSexCouple", "Yes|No|YES|NO", "NO")
            .stringMatcher("D8MarriageDate", REGEX_DATE, "2018-05-17")
            .stringMatcher("D8PetitionerNameDifferentToMarriageCert", "Yes|No|YES|NO", "NO")
            .stringValue("D8PetitionerEmail", "simulate-delivered@notifications.service.gov.uk")
            .stringValue("D8PetitionerPhoneNumber", "01234567890")
            .stringType("D8PetitionerFirstName", "John")
            .stringType("D8PetitionerLastName", "Smith")
            .stringType("D8DerivedPetitionerCurrentFullName", "John Smith")
            .stringType("D8PetitionerLastName", "Smith")
            .minArrayLike("D8PetitionerNameChangedHow", 1,
                stringMatcher("marriageCertificate|certificateTODO", "marriageCertificate"), 1)
            .stringType("D8PetitionerContactDetailsConfidential", "share")
            .object("D8PetitionerHomeAddress")
            .stringType("AddressLine1", "")
            .stringType("AddressLine2", "")
            .stringType("AddressLine3", "")
            .stringType("Country", "")
            .stringType("County", "")
            .stringType("PostTown", "")
            .stringType("PostCode", "SW17 0QT")
            .closeObject();


        body.stringType("D8DerivedPetitionerHomeAddress", "\"82 Landor Road\nLondon\nSW9 9PE")
            .object("D8PetitionerCorrespondenceAddress")
            .stringType("AddressLine1", "")
            .stringType("AddressLine2", "")
            .stringType("AddressLine3", "")
            .stringType("Country", "")
            .stringType("PostCode", "SM1 2JE")
            .stringType("PostTown", "").closeObject();

        body
            .stringType("D8PetitionerCorrespondenceUseHomeAddress", "NO")
            .stringType("D8DerivedPetitionerCorrespondenceAddr", "84 Landor Road\nLondon\nSW9 9PE")
            .stringType("D8PetitionerCorrespondenceUseHomeAddress", "NO")
            .stringType("D8RespondentFirstName", "Jane")
            .stringType("D8RespondentLastName", "Jamed")
            .stringType("D8DerivedRespondentCurrentName", "Jane Jamed")
            .stringType("D8DerivedRespondentSolicitorDetails", "Justin\nCase\n90 Landor Road\nLondon\nSW9 9PE")
            .object("D8RespondentHomeAddress")
            .stringType("AddressLine1", "")
            .stringType("AddressLine2", "")
            .stringType("AddressLine3", "")
            .stringType("Country", "")
            .stringType("PostCode", "SM1 2JE")
            .stringType("PostTown", "")
            .closeObject();

        body.stringType("D8RespondentCorrespondenceUseHomeAddress", "Solicitor")

            .stringType("D8DerivedRespondentHomeAddress", "88 Landor Road\nLondon\nSW9 9PE")
            .object("D8RespondentCorrespondenceAddress")
            .stringType("AddressLine1", "")
            .stringType("AddressLine2", "")
            .stringType("AddressLine3", "")
            .stringType("Country", "")
            .stringType("PostCode", "SM1 2JE")
            .stringType("PostTown", "")
            .closeObject();
        body.stringType("D8RespondentCorrespondenceUseHomeAddress", "Solicitor")
            .stringType("D8DerivedRespondentCorrespondenceAddr", "82 Landor Road\nLondon\nSW9 9PE")
            .stringMatcher("D8RespondentLivesAtLastAddress", "Yes|No|YES|NO", "NO")
            .stringMatcher("D8LivingArrangementsLastLivedTogether", "Yes|No|YES|NO", "NO")
            .stringMatcher("D8LivingArrangementsLiveTogether", "Yes|No|YES|NO", "NO")
            .object("D8LivingArrangementsLastLivedTogethAddr")
            .stringType("AddressLine1", "")
            .stringType("AddressLine2", "")
            .stringType("AddressLine3", "")
            .stringType("Country", "")
            .stringType("PostCode", "SM1 2JE")
            .stringType("PostTown", "")
            .closeObject();
        body.stringMatcher("D8LegalProceedings", "Yes|No|YES|NO", "YES")
            .array("D8LegalProceedingsRelated")
            .stringType("children").closeArray();
        body
            .stringType("D8LegalProceedingsDetails", "The legal proceeding details")
            .stringType("D8ReasonForDivorce", "unreasonable-behaviour")
            .stringType("D8DerivedStatementOfCase", "My wife is having an affair this week.")
            .stringType("D8ReasonForDivorceBehaviourDetails", "My wife is having an affair this week.")
            .stringMatcher("D8FinancialOrder", "Yes|No|YES|NO", "YES")
            .minArrayLike("D8FinancialOrderFor", 1,
                stringMatcher("petitioner|children", "petitioner"), 2)
            .stringMatcher("D8HelpWithFeesNeedHelp", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8HelpWithFeesAppliedForFees", "(Yes|No|YES|NO)", "YES")
            .stringType("D8HelpWithFeesReferenceNumber", "HWF-123-456")
            .stringMatcher("D8DivorceCostsClaim", "Yes|No|YES|NO", "YES")
            .array("D8DivorceClaimFrom")
            .stringType("respondent").closeArray();

        body.stringMatcher("D8JurisdictionConfidentLegal", "Yes|No|YES|NO", "YES")
            .array("D8JurisdictionConnection")
            .stringType("A")
            .stringType("C").closeArray();

        body.stringMatcher("D8JurisdictionPetitionerResidence", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8JurisdictionRespondentResidence", "Yes|No|YES|NO", "YES")
            .minArrayLike("D8DocumentsUploaded", 2, 2)
            .stringType("id", "059dadb4-04c1-44f9-9828-6194bfc9893c")
            .object("value")
            .stringType("DocumentEmailContent", "")
            .stringType("DocumentComment", "")
            .stringType("DocumentFileName", "govuklogo.png")
            .stringMatcher("DocumentDateAdded", REGEX_DATE, "2017-12-11")
            .stringType("DocumentType", "other")
            .object("DocumentLink")
            .stringType("document_url", "http://dm-store-aat."
                + "service.core-compute-aat.internal/documents/7f63ca9b-c361-49ab-aa8c-8fbdb6bc2936").closeObject()
            .closeObject();

        body.stringType("D8DivorceUnit", "eastMidlands")
            .stringMatcher("D8ReasonForDivorceShowAdultery", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ReasonForDivorceShowUnreasonableBehavi", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ReasonForDivorceShowTwoYearsSeparation", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ReasonForDivorceShowDesertion", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ReasonForDivorceLimitReasons", "Yes|No|YES|NO", "NO")
            .stringMatcher("D8ReasonForDivorceEnableAdultery", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ClaimsCostsAppliedForFees", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ReasonForDivorceClaimingAdultery", "Yes|No|YES|NO", "NO")
            .stringMatcher("D8MarriageCanDivorce", "Yes|No|YES|NO", "YES")
            .stringType("D8MarriagePetitionerName", "John Doe")
            .stringType("D8MarriageRespondentName", "Jenny Benny")
            .stringType("D8DerivedRespondentSolicitorAddr", "90 Landor Road\nLondon\nSW9 9PE")
            .stringType("D8DerivedLivingArrangementsLastLivedAddr", "Flat A-B\n86 Landor Road\nLondon\nSW9 9PE")
            .object("D8Connections")
            .stringType("A", "The Petitioner and the Respondent are habitually resident in England and Wales")
            .stringType("B", "")
            .stringType("C", "The Respondent is habitually resident in England and Wales")
            .stringType("D", "")
            .stringType("E", "")
            .stringType("F", "")
            .stringType("G", "")
            .closeObject();

        body.stringMatcher("D8ReasonForDivorceHasMarriage", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ReasonForDivorceShowFiveYearsSeparatio", "Yes|No|YES|NO", "YES")
            .stringMatcher("D8ReasonForDivorceClaiming5YearSeparatio", "Yes|No|YES|NO", "NO")
            .stringType("D8Cohort", "onlineSubmissionPrivateBeta")
            .stringType("D8InferredPetitionerGender", "female")
            .stringType("D8InferredRespondentGender", "male");

        return body;
    }

    public static DslPart buildCaseDetailsDsl(Long caseId) {
        return newJsonBody((o) -> {
            o.numberType("id", caseId)
                .stringType("jurisdiction", "DIVORCE")
                .stringMatcher("state", "Draft|AwaitingPayment|Submitted", "AwaitingPayment")
                .stringValue("case_type_id", "DIVORCE")
                .object("case_data", (dataMap) -> {
                    getCaseData(dataMap);
                });
        }).build();
    }


    public static DslPart buildSearchResultDsl() {
        return newJsonBody((o) -> {
            o.numberType("total", 123)
                .minArrayLike("cases", 1, (cd) -> {
                    cd.numberType("id", 200);
                    cd.stringType("jurisdiction", "divorce");
                    cd.stringType("callback_response_status", "");
                    cd.object("case_data", (dataMap) -> {
                        getCaseData(dataMap);
                    });
                });
        }).build();
    }

    public static DslPart buildNewListOfCaseDetailsDsl(Long caseId) {
        return newJsonArray((rootArray) -> {
            rootArray.object((dataMap) ->
                dataMap.stringValue("case_type_id", "DIVORCE")
                    .object("case_data", (caseData) -> {
                        getCaseData(caseData);
                    }));
        }).build();
    }
}



