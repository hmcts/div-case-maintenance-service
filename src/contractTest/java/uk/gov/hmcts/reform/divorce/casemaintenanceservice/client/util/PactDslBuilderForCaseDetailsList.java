package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util;


import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslJsonRootValue;
import io.pactfoundation.consumer.dsl.LambdaDslObject;

import static au.com.dius.pact.consumer.dsl.PactDslJsonRootValue.stringMatcher;
import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonArray;
import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

public final class PactDslBuilderForCaseDetailsList {

    public static final  String  REGEX_DATE = "^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$";
    private static final String  ALPHABETIC_REGEX = "[/^[A-Za-z_]+$/]+";

    public static DslPart buildStartEventReponse(String eventId , String token , String emailAddress, boolean withExecutors, boolean withPayments) {
        return newJsonBody((o) -> {
            o.stringType("event_id", eventId)
                .stringType("token", token)
                .object("case_details", (cd) -> {
                    cd.numberType("id", 2000);
                    cd.stringMatcher("jurisdiction",  ALPHABETIC_REGEX,"DIVORCE");
                    cd.stringMatcher("callback_response_status", ALPHABETIC_REGEX,  "DONE");
                    cd.stringMatcher("case_type", ALPHABETIC_REGEX,  "GRANT_OF_REPRESENTATION");
                    cd.object("data", (dataMap) -> {
                        getCaseData(emailAddress, withExecutors, withPayments, cd, dataMap);
                    });
                });
        }).build();
    }

    private static void getCaseData(final String emailAddress, final boolean withExecutors, final boolean withPayments,
                                    final LambdaDslObject cd, final LambdaDslObject dataMap) {
        dataMap
            .stringMatcher("createdDate", REGEX_DATE, "2020-11-11")
            .stringMatcher("D8ScreenHasMarriageBroken", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ScreenHasRespondentAddress", "(Yes|No|YES|NO)", "Yes")
            .stringMatcher("D8ScreenHasMarriageCert", "(Yes|No|YES|NO)", "Yes")
            .stringMatcher("D8ScreenHasPrinter", "(Yes|No|YES|NO)", "Yes")
            .stringType("D8DivorceWho", "husband")
            .stringMatcher("D8MarriageIsSameSexCouple", "(Yes|No|YES|NO)", "NO")
            .stringMatcher("D8MarriageDate", REGEX_DATE, "2018-05-17")
            .stringMatcher("D8PetitionerNameDifferentToMarriageCert", REGEX_DATE, "2018-05-17")
            .stringValue("D8PetitionerEmail", "simulate-delivered@notifications.service.gov.uk")
            .stringValue("D8PetitionerPhoneNumber", "0123456789")
            .stringType("D8PetitionerFirstName", "John")
            .stringType("D8PetitionerLastName", "Smith")
            .stringType("D8DerivedPetitionerCurrentFullName", "John Smith")
            .stringType("D8PetitionerLastName", "Smith")
            .minArrayLike("D8PetitionerNameChangedHow",1,
                stringMatcher("marriageCertificate|certificateTODO", "marriageCertificate"),1)
            .stringType("D8PetitionerContactDetailsConfidential", "share")
            .object("D8PetitionerHomeAddress", (address) ->
                address.stringType("AddressLine1", "Winterfell")
                    .stringType("AddressLine2", "Westeros")
                    .stringType("AddressLine3", "Westeros")
                    .stringType("Country", "UK")
                    .stringType("County", "Westeros")
                    .stringType("PostTown", "London")
                    .stringType("PostCode", "SW17 0QT")
            )
            .stringType("D8DerivedPetitionerHomeAddress","\"82 Landor Road\nLondon\nSW9 9PE")
            .minArrayLike("D8PetitionerCorrespondenceAddress",1, (s) -> {
                s.stringType("PostCode","AB24 232");
            })
            .stringType("D8PetitionerCorrespondenceUseHomeAddress", "NO")
            .stringType("D8DerivedPetitionerCorrespondenceAddr", "84 Landor Road\nLondon\nSW9 9PE")
            .stringType("D8PetitionerCorrespondenceUseHomeAddress", "NO")
            .stringType("D8RespondentFirstName", "Jane")
            .stringType("D8RespondentLastName", "Jamed")
            .stringType("D8DerivedRespondentCurrentName", "Jane Jamed")
            .stringType("D8DerivedRespondentSolicitorDetails", "Justin\nCase\n90 Landor Road\nLondon\nSW9 9PE")
            .minArrayLike("D8RespondentHomeAddress",1, (s) -> {
                s.stringType("PostCode","SM1 2JE");
            })
            .stringType("D8RespondentCorrespondenceUseHomeAddress", "Solicitor")

            .stringType("D8DerivedRespondentHomeAddress", "88 Landor Road\nLondon\nSW9 9PE")
            .stringType("D8RespondentCorrespondenceAddress", "88 Landor Road\nLondon\nSW9 9PE")
            .stringType("D8RespondentCorrespondenceUseHomeAddress", "Solicitor")
            .stringType("D8DerivedRespondentCorrespondenceAddr", "82 Landor Road\\nLondon\\nSW9 9PE")
            .stringMatcher("D8RespondentLivesAtLastAddress", "(Yes|No|YES|NO)", "NO")
            .stringMatcher("D8LivingArrangementsLastLivedTogether", "(Yes|No|YES|NO)", "NO")
            .stringMatcher("D8LivingArrangementsLiveTogether", "(Yes|No|YES|NO)", "NO")
            .minArrayLike("D8LivingArrangementsLastLivedTogethAddr",1, (addr) -> {
                addr.stringType("PostCode","SM1 2JE");
            })
            .stringMatcher("D8LegalProceedings", "(Yes|No|YES|NO)", "YES")
            .minArrayLike("D8LegalProceedingsRelated",1, (s) -> {
                s.stringType("children");
            })
            .stringType("D8LegalProceedingsDetails", "The legal proceeding details")
            .stringType("D8ReasonForDivorce", "unreasonable-behaviour")
            .stringType("D8DerivedStatementOfCase", "My wife is having an affair this week.")
            .stringType("D8ReasonForDivorceBehaviourDetails", "My wife is having an affair this week.")
            .stringMatcher("D8ReasonForDivorceAdulteryIsNamed", "(Yes|No|YES|NO)", "NO")
            .stringMatcher("D8FinancialOrder", "(Yes|No|YES|NO)", "YES")
            .minArrayLike("D8FinancialOrderFor",1,
                stringMatcher("petitioner|children", "petitioner"),2)
            .stringMatcher("D8HelpWithFeesNeedHelp", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8HelpWithFeesAppliedForFees", "(Yes|No|YES|NO)", "YES")
            .stringType("D8HelpWithFeesReferenceNumber",  "HWF-123-456")
            .stringMatcher("D8DivorceCostsClaim", "(Yes|No|YES|NO)", "YES")
            .minArrayLike("D8DivorceClaimFrom",1, (s) -> {
                s.stringType("respondent");
            })
            .stringMatcher("D8JurisdictionConfidentLegal", "(Yes|No|YES|NO)", "YES")
            .minArrayLike("D8JurisdictionConnection",2, (s) -> {
                s.stringType("A");
                s.stringType("C");
            })
            .stringMatcher("D8JurisdictionPetitionerResidence", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8JurisdictionRespondentResidence", "(Yes|No|YES|NO)", "YES")
            // D8DocumentsUploaded
            .minArrayLike("d8DocsUploaded", 1, 1,
                d8DocsUploaded ->
                    d8DocsUploaded.stringType("id","null")
                    .object("value", (v) ->
                        v.stringType("DocumentEmailContent",null)
                         .stringType("DocumentComment", "")
                         .stringType("DocumentFileName", "govuklogo.png")
                         .stringMatcher("DocumentDateAdded", REGEX_DATE, "2017-12-11")
                         .stringType("DocumentType", "other")
                         .object("document_link", (link) ->
                            link.stringType("document_url","http://dm-store-aat."
                               + "service.core-compute-aat.internal/documents/7f63ca9b-c361-49ab-aa8c-8fbdb6bc2936"))
                ) // document_link object
            ) // minArray
            .stringType("D8DivorceUnit",  "eastMidlands")
            .stringMatcher("D8ReasonForDivorceShowAdultery", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ReasonForDivorceShowUnreasonableBehavi", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ReasonForDivorceShowTwoYearsSeparation", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ReasonForDivorceShowDesertion", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ReasonForDivorceLimitReasons", "(Yes|No|YES|NO)", "NO")
            .stringMatcher("D8ReasonForDivorceEnableAdultery", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ClaimsCostsAppliedForFees", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ReasonForDivorceClaimingAdultery", "(Yes|No|YES|NO)", "NO")
            .stringMatcher("D8MarriageCanDivorce", "(Yes|No|YES|NO)", "YES")
            .stringType("D8MarriagePetitionerName", "John Doe")
            .stringType("D8MarriageRespondentName", "Jenny Benny")
            .stringType("D8DerivedRespondentSolicitorAddr", "90 Landor Road\nLondon\nSW9 9PE")
            .stringType("D8DerivedLivingArrangementsLastLivedAddr", "Flat A-B\n86 Landor Road\nLondon\nSW9 9PE")
            .minArrayLike("D8Connections",7, (s) -> {
                s.stringType("A","The Petitioner and the Respondent are habitually resident in England and Wales");
                s.stringType("B","null");
                s.stringType("C", "The Respondent is habitually resident in England and Wales");
                s.stringType("D","null");
                s.stringType("E","null");
                s.stringType("F","null");
                s.stringType("G","null");
            })
           .stringMatcher("D8ReasonForDivorceHasMarriage", "(Yes|No|YES|NO)", "YES")
           .stringMatcher("D8ReasonForDivorceShowFiveYearsSeparatio", "(Yes|No|YES|NO)", "YES")
           .stringMatcher("D8ReasonForDivorceClaiming5YearSeparatio", "(Yes|No|YES|NO)", "NO")
           .stringType("D8Cohort", "onlineSubmissionPrivateBeta")
           .stringType("D8InferredPetitionerGender", "female")
           .stringType("D8InferredRespondentGender", "male")
           .stringType("case_type", "DIVORCE")
            .stringType("state", "Draft");
    }

    private static void getNewCaseData(final String emailAddress, final boolean withExecutors, final boolean withPayments,
                                    final LambdaDslObject cd, final LambdaDslObject dataMap) {
        dataMap
            .stringMatcher("createdDate", REGEX_DATE, "2020-11-11")
            .stringMatcher("D8ScreenHasMarriageBroken", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ScreenHasRespondentAddress", "(Yes|No|YES|NO)", "Yes")
            .stringMatcher("D8ScreenHasMarriageCert", "(Yes|No|YES|NO)", "Yes")
            .stringMatcher("D8ScreenHasPrinter", "(Yes|No|YES|NO)", "Yes")
            .stringType("D8DivorceWho", "husband")
            .stringMatcher("D8MarriageIsSameSexCouple", "(Yes|No|YES|NO)", "NO")
            .stringMatcher("D8MarriageDate", REGEX_DATE, "2018-05-17")
            .stringMatcher("D8PetitionerNameDifferentToMarriageCert", REGEX_DATE, "2018-05-17")
            .stringValue("D8PetitionerEmail", "simulate-delivered@notifications.service.gov.uk")
            .stringValue("D8PetitionerPhoneNumber", "0123456789")
            .stringType("D8PetitionerFirstName", "John")
            .stringType("D8PetitionerLastName", "Smith")
            .stringType("D8DerivedPetitionerCurrentFullName", "John Smith")
            .stringType("D8PetitionerLastName", "Smith")
            .minArrayLike("D8PetitionerNameChangedHow",1,
                stringMatcher("marriageCertificate|certificateTODO", "marriageCertificate"),1)
            .stringType("D8PetitionerContactDetailsConfidential", "share")
            .object("D8PetitionerHomeAddress", (address) ->
                address.stringType("AddressLine1", "Winterfell")
                    .stringType("AddressLine2", "Westeros")
                    .stringType("AddressLine3", "Westeros")
                    .stringType("Country", "UK")
                    .stringType("County", "Westeros")
                    .stringType("PostTown", "London")
                    .stringType("PostCode", "SW17 0QT")
            )
            .stringType("D8DerivedPetitionerHomeAddress","\"82 Landor Road\nLondon\nSW9 9PE")
            .minArrayLike("D8PetitionerCorrespondenceAddress",1, (s) -> {
                s.stringType("PostCode","AB24 232");
            })
            .stringType("D8PetitionerCorrespondenceUseHomeAddress", "NO")
            .stringType("D8DerivedPetitionerCorrespondenceAddr", "84 Landor Road\nLondon\nSW9 9PE")
            .stringType("D8PetitionerCorrespondenceUseHomeAddress", "NO")
            .stringType("D8RespondentFirstName", "Jane")
            .stringType("D8RespondentLastName", "Jamed")
            .stringType("D8DerivedRespondentCurrentName", "Jane Jamed")
            .stringType("D8DerivedRespondentSolicitorDetails", "Justin\nCase\n90 Landor Road\nLondon\nSW9 9PE")
            .minArrayLike("D8RespondentHomeAddress",1, (s) -> {
                s.stringType("PostCode","SM1 2JE");
            })
            .stringType("D8RespondentCorrespondenceUseHomeAddress", "Solicitor")

            .stringType("D8DerivedRespondentHomeAddress", "88 Landor Road\nLondon\nSW9 9PE")
            .stringType("D8RespondentCorrespondenceAddress", "88 Landor Road\nLondon\nSW9 9PE")
            .stringType("D8RespondentCorrespondenceUseHomeAddress", "Solicitor")
            .stringType("D8DerivedRespondentCorrespondenceAddr", "82 Landor Road\\nLondon\\nSW9 9PE")
            .stringMatcher("D8RespondentLivesAtLastAddress", "(Yes|No|YES|NO)", "NO")
            .stringMatcher("D8LivingArrangementsLastLivedTogether", "(Yes|No|YES|NO)", "NO")
            .stringMatcher("D8LivingArrangementsLiveTogether", "(Yes|No|YES|NO)", "NO")
            .minArrayLike("D8LivingArrangementsLastLivedTogethAddr",1, (addr) -> {
                addr.stringType("PostCode","SM1 2JE");
            })
            .stringMatcher("D8LegalProceedings", "(Yes|No|YES|NO)", "YES")
            .minArrayLike("D8LegalProceedingsRelated",1, (s) -> {
                s.stringType("children");
            })
            .stringType("D8LegalProceedingsDetails", "The legal proceeding details")
            .stringType("D8ReasonForDivorce", "unreasonable-behaviour")
            .stringType("D8DerivedStatementOfCase", "My wife is having an affair this week.")
            .stringType("D8ReasonForDivorceBehaviourDetails", "My wife is having an affair this week.")
            .stringMatcher("D8ReasonForDivorceAdulteryIsNamed", "(Yes|No|YES|NO)", "NO")
            .stringMatcher("D8FinancialOrder", "(Yes|No|YES|NO)", "YES")
            .minArrayLike("D8FinancialOrderFor",1,
                stringMatcher("petitioner|children", "petitioner"),2)
            .stringMatcher("D8HelpWithFeesNeedHelp", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8HelpWithFeesAppliedForFees", "(Yes|No|YES|NO)", "YES")
            .stringType("D8HelpWithFeesReferenceNumber",  "HWF-123-456")
            .stringMatcher("D8DivorceCostsClaim", "(Yes|No|YES|NO)", "YES")
            .minArrayLike("D8DivorceClaimFrom",1, (s) -> {
                s.stringType("respondent");
            })
            .stringMatcher("D8JurisdictionConfidentLegal", "(Yes|No|YES|NO)", "YES")
            .minArrayLike("D8JurisdictionConnection",2, (s) -> {
                s.stringType("A");
                s.stringType("C");
            })
            .stringMatcher("D8JurisdictionPetitionerResidence", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8JurisdictionRespondentResidence", "(Yes|No|YES|NO)", "YES")
            // D8DocumentsUploaded
            .minArrayLike("d8DocsUploaded", 1, 1,
                d8DocsUploaded ->
                    d8DocsUploaded.stringType("id","null")
                        .object("value", (v) ->
                            v.stringType("DocumentEmailContent",null)
                                .stringType("DocumentComment", "")
                                .stringType("DocumentFileName", "govuklogo.png")
                                .stringMatcher("DocumentDateAdded", REGEX_DATE, "2017-12-11")
                                .stringType("DocumentType", "other")
                                .object("document_link", (link) ->
                                    link.stringType("document_url","http://dm-store-aat."
                                        + "service.core-compute-aat.internal/documents/7f63ca9b-c361-49ab-aa8c-8fbdb6bc2936"))
                        ) // document_link object
            ) // minArray
            .stringType("D8DivorceUnit",  "eastMidlands")
            .stringMatcher("D8ReasonForDivorceShowAdultery", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ReasonForDivorceShowUnreasonableBehavi", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ReasonForDivorceShowTwoYearsSeparation", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ReasonForDivorceShowDesertion", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ReasonForDivorceLimitReasons", "(Yes|No|YES|NO)", "NO")
            .stringMatcher("D8ReasonForDivorceEnableAdultery", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ClaimsCostsAppliedForFees", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ReasonForDivorceClaimingAdultery", "(Yes|No|YES|NO)", "NO")
            .stringMatcher("D8MarriageCanDivorce", "(Yes|No|YES|NO)", "YES")
            .stringType("D8MarriagePetitionerName", "John Doe")
            .stringType("D8MarriageRespondentName", "Jenny Benny")
            .stringType("D8DerivedRespondentSolicitorAddr", "90 Landor Road\nLondon\nSW9 9PE")
            .stringType("D8DerivedLivingArrangementsLastLivedAddr", "Flat A-B\n86 Landor Road\nLondon\nSW9 9PE")
            .minArrayLike("D8Connections",7, (s) -> {
                s.stringType("A","The Petitioner and the Respondent are habitually resident in England and Wales");
                s.stringType("B","null");
                s.stringType("C", "The Respondent is habitually resident in England and Wales");
                s.stringType("D","null");
                s.stringType("E","null");
                s.stringType("F","null");
                s.stringType("G","null");
            })
            .stringMatcher("D8ReasonForDivorceHasMarriage", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ReasonForDivorceShowFiveYearsSeparatio", "(Yes|No|YES|NO)", "YES")
            .stringMatcher("D8ReasonForDivorceClaiming5YearSeparatio", "(Yes|No|YES|NO)", "NO")
            .stringType("D8Cohort", "onlineSubmissionPrivateBeta")
            .stringType("D8InferredPetitionerGender", "female")
            .stringType("D8InferredRespondentGender", "male")
            .stringType("case_type", "DIVORCE")
            .stringType("state", "Draft");
    }


    public static DslPart buildCaseDetailsDsl(Long caseId, String emailAddress, boolean withExecutors, boolean withPayments) {
        return newJsonBody((o) -> {
            o.numberType("id", caseId)
                .stringType("jurisdiction", "DIVORCE")
                .stringMatcher("state", "Draft|PaAppCreated|CaseCreated", "CaseCreated")
                .stringValue("case_type", "AwaitingDecreeNisi")
                .object("data", (dataMap) -> {
                    getCaseData(emailAddress, withExecutors, withPayments, o , dataMap);
                });
        }).build();
    }


    //TODO : this returns a Array of Array , rather than an Array . Needs to be fixed.
    public static DslPart buildNewListOfCaseDetailsDsl(Long caseId, String emailAddress, boolean withExecutors, boolean withPayments) {
        PactDslJsonBody body = new PactDslJsonBody();
        //PactDslJsonArray arr = new PactDslJsonArray();

        body
            //.minArrayLike("cd",2)
            .minArrayLike(2)
            .stringValue("case_type","DIVORCE")
            .stringValue("state","Draft")
            .minArrayLike("D8FinancialOrderFor",1,
                 stringMatcher("petitioner|children", "petitioner"),2)

                .object("data")
                    .stringMatcher("createdDate", REGEX_DATE, "2020-11-11")
                    .stringMatcher("D8ScreenHasMarriageBroken", "(Yes|No|YES|NO)", "YES")
                    .stringValue("D8ScreenHasMarriageBroken","YES")
                    .minArrayLike("D8PetitionerNameChangedHow",1,
                        stringMatcher("marriageCertificate|certificateTODO", "marriageCertificate"),1)
                    .minArrayLike("D8FinancialOrderFor",1,
                        stringMatcher("petitioner|children", "petitioner"),2)
                    .stringMatcher("D8HelpWithFeesNeedHelp", "(Yes|No|YES|NO)", "YES")
                    .stringMatcher("D8HelpWithFeesAppliedForFees", "(Yes|No|YES|NO)", "YES")
                    .stringType("D8HelpWithFeesReferenceNumber",  "HWF-123-456")
                    .stringMatcher("D8DivorceCostsClaim", "(Yes|No|YES|NO)", "YES")
                    .object("D8LivingArrangementsLastLivedTogethAddr")
                        .stringType("PostCode","AB22 222")
                    .closeObject()
                .closeObject()

            .closeArray();
        return body;
    }

    public static DslPart buildSearchResultDsl(Long caseId, String emailAddress, boolean withExecutors, boolean withPayments) {
        return newJsonBody((o) -> {
            o.numberType("total",123)
                           .minArrayLike("cases", 2, (cd) -> {
                               cd.numberType("id", 200);
                               cd.stringType("jurisdiction", "divorce");
                               cd.stringType("callback_response_status",  "DONE");
                               cd.stringType("case_type",  "DIVORCE");
                               cd.object("data", (dataMap) -> {
                                   getCaseData(emailAddress, withExecutors, withPayments, cd, dataMap);
                               });
                           });
        }).build();
    }


    //  public static DslPart buildListOfCaseDetailsDsl(Long caseId, String emailAddress, boolean withExecutors, boolean withPayments) {
    //        return PactDslJsonArray
    //            .arrayEachLike(2)
    //                .stringValue("case_type", "AwaitingDecreeNisi")
    //                .stringValue("jurisdiction","divorce")
    //                .object("case_data")
    //                    .numberType("outsideUKGrantCopies", 6)
    //                    .stringValue("applicationType", "Personal")
    //                    .stringMatcher("applicationSubmittedDate", REGEX_DATE, "2018-05-17")
    //                    .stringType("primaryApplicantForenames", "Jon")
    //                    .stringType("primaryApplicantSurname", "Snow")
    //                    .stringMatcher("primaryApplicantAddressFound",
    //                        "Yes|No", "Yes")
    //                    .stringMatcher("primaryApplicantPhoneNumber", "[0-9]+", "123455678")
    //                    .stringMatcher("primaryApplicantRelationshipToDeceased",
    //                        "partner|child|sibling|partner|parent|adoptedChild|other", "adoptedChild")
    //                    .stringMatcher("primaryApplicantAdoptionInEnglandOrWales", "(Yes|No)", "Yes")
    //                    .stringValue("primaryApplicantEmailAddress", "someEmail....")
    //                    .object("primaryApplicantAddress")
    //                        .stringType("AddressLine1", "Pret a Manger")
    //                        .stringType("AddressLine2", "St. Georges Hospital")
    //                        .stringType("PostTown", "London")
    //                        .stringType("PostCode", "SW17 0QT")
    //                    .closeObject()
    //                    .object("")
    //                        .stringType("deceasedForenames", "Ned")
    //                        .stringType("deceasedSurname", "Stark")
    //                        .stringMatcher("deceasedDateOfBirth", REGEX_DATE, "1930-01-01")
    //                        .stringMatcher("deceasedDateOfDeath", REGEX_DATE, "2018-01-01")
    //                    .closeObject()
    //                    .object("deceasedAddress")
    //                        .stringType("AddressLine1", "Winterfell")
    //                        .stringType("AddressLine2", "Westeros")
    //                        .stringType("PostTown", "London")
    //                        .stringType("PostCode", "SW17 0QT")
    //                    .closeObject()
    //                    //TODO Not able to add String types after this , only .object ... closeObject() types.
    //                    .closeObject();
    //    }

}
