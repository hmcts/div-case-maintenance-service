package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util;

import static io.pactfoundation.consumer.dsl.LambdaDsl.newJsonBody;

import au.com.dius.pact.consumer.dsl.DslPart;
import io.pactfoundation.consumer.dsl.LambdaDslObject;

/**
 *
 * Note : // use StringType in matcher to make it LESS STRICT,
 *       // use StringMatcher for cases where we want to
 *
 * PLEASE READ THE TODO IMPORTANT COMMENTS below ...
 *
 */
public final class PactDslBuilderForCaseDetailsList {

    public static final String REGEX_DATE = "^((19|2[0-9])[0-9]{2})-(0[1-9]|1[012])-(0[1-9]|[12][0-9]|3[01])$";
    private static final String  ALPHABETIC_REGEX = "[/^[A-Za-z]+$/]+";

    /**
     * TODO Important :
     *
     * Please can this method be checked if it returns a List<Casedetail> of size > 1 . I could nt get to
     * return an array of size 2 either using the DslPact or the PactDslJsonArray
     *
     */
    public static DslPart buildCaseDetailList(Long CASE_ID, String emailAddress, boolean withExecutors, boolean withPayments) {
        return newJsonBody((o) -> {
            o.stringType("id", CASE_ID.toString())
                .stringMatcher("state", "Draft|PaAppCreated|CaseCreated", "CaseCreated")
                .stringValue("case_type", "AwaitingDecreeNisi")
                .object("data", (cd) -> {
                    cd.numberType("outsideUKGrantCopies", 6)
                        .stringValue("applicationType", "Personal")
                        .stringMatcher("applicationSubmittedDate", REGEX_DATE, "2018-05-17")
                        .stringType("primaryApplicantForenames", "Jon")
                        .stringType("primaryApplicantSurname", "Snow")
                        .stringMatcher("primaryApplicantAddressFound",
                            "Yes|No", "Yes")
                        .stringMatcher("primaryApplicantPhoneNumber", "[0-9]+", "123455678")
                        .stringMatcher("primaryApplicantRelationshipToDeceased", "partner|child|sibling|partner|parent|adoptedChild|other", "adoptedChild")
                        .stringMatcher("primaryApplicantAdoptionInEnglandOrWales", "(Yes|No)", "Yes")
                        .stringValue("primaryApplicantEmailAddress",emailAddress )
                        .object("primaryApplicantAddress", (address) ->
                            address.stringType("AddressLine1", "Pret a Manger")
                                .stringType("AddressLine2", "St. Georges Hospital")
                                .stringType("PostTown", "London")
                                .stringType("PostCode", "SW17 0QT")
                        )
                        .stringType("deceasedForenames", "Ned")
                        .stringType("deceasedSurname", "Stark")
                        .stringMatcher("deceasedDateOfBirth", REGEX_DATE, "1930-01-01")
                        .stringMatcher("deceasedDateOfDeath", REGEX_DATE, "2018-01-01")
                        .object("deceasedAddress", (address) ->
                            address.stringType("AddressLine1", "Winterfell")
                                .stringType("AddressLine2", "Westeros")
                                .stringType("PostTown", "London")
                                .stringType("PostCode", "SW17 0QT")
                        )
                        .stringMatcher("deceasedAddressFound", "Yes|No", "Yes")
                        .stringMatcher("deceasedAnyOtherNames", "Yes|No", "Yes")
                        .minArrayLike("deceasedAliasNameList", 0, 2,
                            alias -> alias
                                .object("value", (value) ->
                                    value.stringType("Forenames", "King")
                                        .stringType("LastName", "North")
                                ))
                        .stringMatcher("deceasedMartialStatus", "marriedCivilPartnership|divorcedCivilPartnership|widowed|judicially|neverMarried")
                        .stringMatcher("deceasedMarriedAfterWillOrCodicilDate", "Yes|No", "No")
                        .stringMatcher("deceasedDivorcedInEnglandOrWales", "Yes|No", "No")
                        .stringMatcher("deceasedSpouseNotApplyingReason", "renunciated|mentallyIncapable|other")
                        .stringMatcher("deceasedOtherChildren", "Yes|No", "Yes")
                        .stringMatcher("childrenOverEighteenSurvived", "Yes|No", "Yes")
                        .stringMatcher("childrenDied", "Yes|No", "No")
                        .stringMatcher("grandChildrenSurvivedUnderEighteen", "Yes|No", "No")
                        .stringMatcher("deceasedAnyChildren", "Yes|No", "No")
                        .stringMatcher("deceasedHasAssetsOutsideUK", "Yes|No", "Yes")
                        .stringMatcher("deceasedAnyChildren", "Yes|No", "No")
                        .stringMatcher("ihtFormId", "IHT205|IHT207|IHT400421", "IHT205")
                        .stringMatcher("ihtFormCompletedOnline", "Yes|No", "No")
                        .stringType("assetsOverseasNetValue", "10050")
                        .stringType("ihtGrossValue", "100000")
                        .stringType("ihtNetValue", "100000")
                        .stringType("ihtReferenceNumber", "GOT123456")
                        .stringMatcher("declarationCheckbox", "Yes|No", "Yes")
                        .numberType("outsideUKGrantCopies", 6)
                        .numberType("extraCopiesOfGrant", 3)
                        .stringType("uploadDocumentUrl", "http://document-management/document/12345")
                        .stringMatcher("registryLocation", "Oxford|Manchester|Birmingham|Leeds|Liverpool|Brighton|Cardiff|London|Winchester|Newcastle|ctsc", "Oxford")
                        .object("declaration", (declaration) ->
                            declaration.stringType("accept", "I confirm that I will administer the estate of the person who died according to law, and that my application is truthful.")
                                .stringType("confirm", "We confirm that we will administer the estate of Suki Hammond, according to law. We will:")
                                .stringType("confirmItem1", "collect the whole estate")
                                .stringType("confirmItem2", "keep full details (an inventory) of the estate")
                                .stringType("confirmItem3", "keep a full account of how the estate has been administered")
                                .stringType("requests", "If the probate registry (court) asks us to do so, we will:")
                                .stringType("requestsItem1", "provide the full details of the estate and how it has been administered")
                                .stringType("requestsItem2", "return the grant of probate to the court")
                                .stringType("understand", "We understand that:")
                                .stringType("understandItem1", "our application will be rejected if we do not answer any questions about the information we have given")
                                .stringType("understandItem2", "criminal proceedings for fraud may be brought against us if we are found to have been deliberately untruthful or dishonest"))
                        .object("legalStatement", (legalStatement) ->
                            legalStatement.stringType("applicant", "We, Jon Snow of place make the following statement:")
                                .stringType("deceased", "Ned Stark was born on 6 October 1902 and died on 22 December 1983, domiciled in England and Wales.")
                                .stringType("deceasedEstateLand", "To the best of our knowledge, information and belief, there was no land vested in Suki Hammond which was settled previously to the death (and not by the will) of Suki Hammond and which remained settled land notwithstanding such death.")
                                .stringType("deceasedEstateValue", "The gross value for the estate amounts to &pound;2222 and the net value for the estate amounts to &pound;222.")
                                .stringType("deceasedOtherNames", "They were also known as King North.")
                                .minArrayLike("executorsApplying", 0, 2,
                                    executorApplying -> executorApplying
                                        .object("value", (value) ->
                                            value.stringType("name", "Jon Snow, an executor named in the will or codicils, is applying for probate.")
                                                .stringType("sign", "Jon Snow will sign and send to the probate registry what they believe to be the true and original last will and testament and any codicils of Ned Stark.")
                                        )
                                )
                                .minArrayLike("executorsNotApplying", 0, 1,
                                    executorNotApplying -> executorNotApplying
                                        .object("value", (value) ->
                                            value.stringType("executor", "Burton Leonard, an executor named in the will or codicils, is not making this application because they died before Suki Hammond died.")
                                        )
                                )
                                .stringType( "intro", "This statement is based on the information you&rsquo;ve given in your application. It will be stored as a public record.")
                        )
                        .numberType( "numberOfApplicants", 2)
                        .numberType( "numberOfExecutors", 3)
                        .stringMatcher("softStop", "Yes|No", "No")
                        .stringType("totalFee", "0")
                        .stringMatcher("willHasCodicils", "Yes|No", "Yes")
                        .stringMatcher("willLatestCodicilHasDate", "Yes|No", "No")
                        .numberType( "willNumberOfCodicils", 1);

                    if (withExecutors) {
                        cd.minArrayLike("executorsApplying", 0, 2,
                            executorApplying -> executorApplying
                                .object("value", (value) ->
                                    value.stringType("applyingExecutorName", "Jon Snow")
                                        .stringMatcher("applyingExecutorPhoneNumber", "[0-9]+", "07981898999")
                                        .stringMatcher("applyingExecutorAgreed", "Yes|No", "Yes")
                                        .stringType("applyingExecutorEmail", "address@email.com")
                                        .stringType("applyingExecutorInvitationId", "54321")
                                        .stringType("applyingExecutorLeadName", "Graham Garderner")
                                        .object("applyingExecutorAddress", (address) ->
                                            address.stringType("AddressLine1", "Winterfell")
                                                .stringType("AddressLine2", "Westeros")
                                                .stringType("PostTown", "London")
                                                .stringType("PostCode", "SW17 0QT")
                                        ).stringType("applyingExecutorOtherNames", "Graham Poll")
                                        .stringType("applyingExecutorOtherNamesReason", "Divorce")
                                ))
                            .minArrayLike("executorsNotApplying", 0, 1,
                                executorNotApplying -> executorNotApplying
                                    .object("value", (value) ->
                                        value.stringType("notApplyingExecutorName", "Burton Leonard")
                                            .stringType("notApplyingExecutorReason", "DiedBefore"))
                            );

                    }
                    if (withPayments) {
                        cd.minArrayLike("payments", 0, 1,
                            payment -> payment
                                // .stringType("id", "950243f2-c713-4f1b-990d-83eb508bab91")
                                .object("value", (value) ->
                                    value.stringMatcher("amount", "[0-9]+", "2000")
                                        .stringType("method", "card")
                                        .stringMatcher("status", "Success|Failed|Initiated|not_required", "Success")
                                        .stringType("reference", "RC-1599-4876-0252-6208")
                                ));

                    }

                });

        }).build();
    }


    public static DslPart buildStartEventReponse(String eventId , String token , String emailAddress, boolean withExecutors, boolean withPayments) {
        return newJsonBody((o) -> {
                o.stringType("event_id", eventId)
                 .stringType("token", token)
                 .object("case_details", (cd) ->{
                    cd.numberType("id", 2000);
                    cd.stringMatcher("jurisdiction",  ALPHABETIC_REGEX,"PROBATE");
                    cd.stringMatcher("callback_response_status", ALPHABETIC_REGEX,  "DONE");
                    cd.stringMatcher("case_type", ALPHABETIC_REGEX,  "PROBATE");
                    // below is the  Map<Object,Object> data property  of  CaseDetails object.
                    cd.object("data", (dataMap) -> {
                        getCaseData(emailAddress, withExecutors, withPayments, cd, dataMap);
                    });
                });
        }).build();
    }

    private static void getCaseData(final String emailAddress, final boolean withExecutors, final boolean withPayments, final LambdaDslObject cd, final LambdaDslObject dataMap) {
        dataMap.numberType("outsideUKGrantCopies", 6)
            .stringValue("applicationType", "Personal")
            .stringMatcher("applicationSubmittedDate", REGEX_DATE, "2018-05-17")
            .stringType("primaryApplicantForenames", "Jon")
            .stringType("primaryApplicantSurname", "Snow")
            .stringMatcher("primaryApplicantAddressFound",
                "Yes|No", "Yes")
            .stringMatcher("primaryApplicantPhoneNumber", "[0-9]+", "123455678")
            .stringMatcher("primaryApplicantRelationshipToDeceased", "partner|child|sibling|partner|parent|adoptedChild|other", "adoptedChild")
            .stringMatcher("primaryApplicantAdoptionInEnglandOrWales", "(Yes|No)", "Yes")
            .stringValue("primaryApplicantEmailAddress", emailAddress)
            .object("primaryApplicantAddress", (address) ->
                address.stringType("AddressLine1", "Pret a Manger")
                    .stringType("AddressLine2", "St. Georges Hospital")
                    .stringType("PostTown", "London")
                    .stringType("PostCode", "SW17 0QT")
            )
            .stringType("deceasedForenames", "Ned")
            .stringType("deceasedSurname", "Stark")
            .stringMatcher("deceasedDateOfBirth", REGEX_DATE, "1930-01-01")
            .stringMatcher("deceasedDateOfDeath", REGEX_DATE, "2018-01-01")
            .object("deceasedAddress", (address) ->
                address.stringType("AddressLine1", "Winterfell")
                    .stringType("AddressLine2", "Westeros")
                    .stringType("PostTown", "London")
                    .stringType("PostCode", "SW17 0QT")
            )
            .stringMatcher("deceasedAddressFound", "Yes|No", "Yes")
            .stringMatcher("deceasedAnyOtherNames", "Yes|No", "Yes")
            .minArrayLike("deceasedAliasNameList", 0, 2,
                alias -> alias
                    .object("value", (value) ->
                        value.stringType("Forenames", "King")
                            .stringType("LastName", "North")
                    ))
            .stringMatcher("deceasedMartialStatus", "marriedCivilPartnership|divorcedCivilPartnership|widowed|judicially|neverMarried")
            .stringMatcher("deceasedMarriedAfterWillOrCodicilDate", "Yes|No", "No")
            .stringMatcher("deceasedDivorcedInEnglandOrWales", "Yes|No", "No")
            .stringMatcher("deceasedSpouseNotApplyingReason", "renunciated|mentallyIncapable|other")
            .stringMatcher("deceasedOtherChildren", "Yes|No", "Yes")
            .stringMatcher("childrenOverEighteenSurvived", "Yes|No", "Yes")
            .stringMatcher("childrenDied", "Yes|No", "No")
            .stringMatcher("grandChildrenSurvivedUnderEighteen", "Yes|No", "No")
            .stringMatcher("deceasedAnyChildren", "Yes|No", "No")
            .stringMatcher("deceasedHasAssetsOutsideUK", "Yes|No", "Yes")
            .stringMatcher("deceasedAnyChildren", "Yes|No", "No")
            .stringMatcher("ihtFormId", "IHT205|IHT207|IHT400421", "IHT205")
            .stringMatcher("ihtFormCompletedOnline", "Yes|No", "No")
            .stringType("assetsOverseasNetValue", "10050")
            .stringType("ihtGrossValue", "100000")
            .stringType("ihtNetValue", "100000")
            .stringType("ihtReferenceNumber", "GOT123456")
            .stringMatcher("declarationCheckbox", "Yes|No", "Yes")
            .numberType("outsideUKGrantCopies", 6)
            .numberType("extraCopiesOfGrant", 3)
            .stringType("uploadDocumentUrl", "http://document-management/document/12345")
            .stringMatcher("registryLocation", "Oxford|Manchester|Birmingham|Leeds|Liverpool|Brighton|Cardiff|London|Winchester|Newcastle|ctsc", "Oxford")
            .object("declaration", (declaration) ->
                declaration.stringType("accept", "I confirm that I will administer the estate of the person who died according to law, and that my application is truthful.")
                    .stringType("confirm", "We confirm that we will administer the estate of Suki Hammond, according to law. We will:")
                    .stringType("confirmItem1", "collect the whole estate")
                    .stringType("confirmItem2", "keep full details (an inventory) of the estate")
                    .stringType("confirmItem3", "keep a full account of how the estate has been administered")
                    .stringType("requests", "If the probate registry (court) asks us to do so, we will:")
                    .stringType("requestsItem1", "provide the full details of the estate and how it has been administered")
                    .stringType("requestsItem2", "return the grant of probate to the court")
                    .stringType("understand", "We understand that:")
                    .stringType("understandItem1", "our application will be rejected if we do not answer any questions about the information we have given")
                    .stringType("understandItem2", "criminal proceedings for fraud may be brought against us if we are found to have been deliberately untruthful or dishonest"))
            .object("legalStatement", (legalStatement) ->
                legalStatement.stringType("applicant", "We, Jon Snow of place make the following statement:")
                    .stringType("deceased", "Ned Stark was born on 6 October 1902 and died on 22 December 1983, domiciled in England and Wales.")
                    .stringType("deceasedEstateLand", "To the best of our knowledge, information and belief, there was no land vested in Suki Hammond which was settled previously to the death (and not by the will) of Suki Hammond and which remained settled land notwithstanding such death.")
                    .stringType("deceasedEstateValue", "The gross value for the estate amounts to &pound;2222 and the net value for the estate amounts to &pound;222.")
                    .stringType("deceasedOtherNames", "They were also known as King North.")
                    .minArrayLike("executorsApplying", 0, 2,
                        executorApplying -> executorApplying
                            .object("value", (value) ->
                                value.stringType("name", "Jon Snow, an executor named in the will or codicils, is applying for probate.")
                                    .stringType("sign", "Jon Snow will sign and send to the probate registry what they believe to be the true and original last will and testament and any codicils of Ned Stark.")
                            )
                    )
                    .minArrayLike("executorsNotApplying", 0, 1,
                        executorNotApplying -> executorNotApplying
                            .object("value", (value) ->
                                value.stringType("executor", "Burton Leonard, an executor named in the will or codicils, is not making this application because they died before Suki Hammond died.")
                            )
                    )
                    .stringType("intro", "This statement is based on the information you&rsquo;ve given in your application. It will be stored as a public record.")
            )
            .numberType("numberOfApplicants", 2)
            .numberType("numberOfExecutors", 3)
            .stringMatcher("softStop", "Yes|No", "No")
            .stringType("totalFee", "0")
            .stringMatcher("willHasCodicils", "Yes|No", "Yes")
            .stringMatcher("willLatestCodicilHasDate", "Yes|No", "No")
            .numberType("willNumberOfCodicils", 1);

        if (withExecutors) {
            cd.minArrayLike("executorsApplying", 0, 2,
                executorApplying -> executorApplying
                    .object("value", (value) ->
                        value.stringType("applyingExecutorName", "Jon Snow")
                            .stringMatcher("applyingExecutorPhoneNumber", "[0-9]+", "07981898999")
                            .stringMatcher("applyingExecutorAgreed", "Yes|No", "Yes")
                            .stringType("applyingExecutorEmail", "address@email.com")
                            .stringType("applyingExecutorInvitationId", "54321")
                            .stringType("applyingExecutorLeadName", "Graham Garderner")
                            .object("applyingExecutorAddress", (address) ->
                                address.stringType("AddressLine1", "Winterfell")
                                    .stringType("AddressLine2", "Westeros")
                                    .stringType("PostTown", "London")
                                    .stringType("PostCode", "SW17 0QT")
                            ).stringType("applyingExecutorOtherNames", "Graham Poll")
                            .stringType("applyingExecutorOtherNamesReason", "Divorce")
                    ))
                .minArrayLike("executorsNotApplying", 0, 1,
                    executorNotApplying -> executorNotApplying
                        .object("value", (value) ->
                            value.stringType("notApplyingExecutorName", "Burton Leonard")
                                .stringType("notApplyingExecutorReason", "DiedBefore"))
                );

        }
        if (withPayments) {
            cd.minArrayLike("payments", 0, 1,
                payment -> payment
                    // .stringType("id", "950243f2-c713-4f1b-990d-83eb508bab91")
                    .object("value", (value) ->
                        value.stringMatcher("amount", "[0-9]+", "2000")
                            .stringType("method", "card")
                            .stringMatcher("status", "Success|Failed|Initiated|not_required", "Success")
                            .stringType("reference", "RC-1599-4876-0252-6208")
                    ));

        }
    }



    public static DslPart buildCaseDetailsDsl(Long CASE_ID, String emailAddress, boolean withExecutors, boolean withPayments) {
        return newJsonBody((o) -> {
            o.numberType("id", 2000L)
                .stringType("jurisdiction", "DIVORCE")
                .stringMatcher("state", "Draft|PaAppCreated|CaseCreated", "CaseCreated")
                .stringValue("case_type", "AwaitingDecreeNisi")
                .object("data", (cd) -> {
                    cd.numberType("outsideUKGrantCopies", 6)
                        .stringValue("applicationType", "Personal")
                        .stringMatcher("applicationSubmittedDate", REGEX_DATE, "2018-05-17")
                        .stringType("primaryApplicantForenames", "Jon")
                        .stringType("primaryApplicantSurname", "Snow")
                        .stringMatcher("primaryApplicantAddressFound",
                            "Yes|No", "Yes")
                        .stringMatcher("primaryApplicantPhoneNumber", "[0-9]+", "123455678")
                        .stringMatcher("primaryApplicantRelationshipToDeceased", "partner|child|sibling|partner|parent|adoptedChild|other", "adoptedChild")
                        .stringMatcher("primaryApplicantAdoptionInEnglandOrWales", "(Yes|No)", "Yes")
                        .stringValue("primaryApplicantEmailAddress", emailAddress)
                        .object("primaryApplicantAddress", (address) ->
                            address.stringType("AddressLine1", "Pret a Manger")
                                .stringType("AddressLine2", "St. Georges Hospital")
                                .stringType("PostTown", "London")
                                .stringType("PostCode", "SW17 0QT")
                        )
                        .stringType("deceasedForenames", "Ned")
                        .stringType("deceasedSurname", "Stark")
                        .stringMatcher("deceasedDateOfBirth", REGEX_DATE, "1930-01-01")
                        .stringMatcher("deceasedDateOfDeath", REGEX_DATE, "2018-01-01")
                        .object("deceasedAddress", (address) ->
                            address.stringType("AddressLine1", "Winterfell")
                                .stringType("AddressLine2", "Westeros")
                                .stringType("PostTown", "London")
                                .stringType("PostCode", "SW17 0QT")
                        )
                        .stringMatcher("deceasedAddressFound", "Yes|No", "Yes")
                        .stringMatcher("deceasedAnyOtherNames", "Yes|No", "Yes")
                        .minArrayLike("deceasedAliasNameList", 0, 2,
                            alias -> alias
                                .object("value", (value) ->
                                    value.stringType("Forenames", "King")
                                        .stringType("LastName", "North")
                                ))
                        .stringMatcher("deceasedMartialStatus", "marriedCivilPartnership|divorcedCivilPartnership|widowed|judicially|neverMarried")
                        .stringMatcher("deceasedMarriedAfterWillOrCodicilDate", "Yes|No", "No")
                        .stringMatcher("deceasedDivorcedInEnglandOrWales", "Yes|No", "No")
                        .stringMatcher("deceasedSpouseNotApplyingReason", "renunciated|mentallyIncapable|other")
                        .stringMatcher("deceasedOtherChildren", "Yes|No", "Yes")
                        .stringMatcher("childrenOverEighteenSurvived", "Yes|No", "Yes")
                        .stringMatcher("childrenDied", "Yes|No", "No")
                        .stringMatcher("grandChildrenSurvivedUnderEighteen", "Yes|No", "No")
                        .stringMatcher("deceasedAnyChildren", "Yes|No", "No")
                        .stringMatcher("deceasedHasAssetsOutsideUK", "Yes|No", "Yes")
                        .stringMatcher("deceasedAnyChildren", "Yes|No", "No")
                        .stringMatcher("ihtFormId", "IHT205|IHT207|IHT400421", "IHT205")
                        .stringMatcher("ihtFormCompletedOnline", "Yes|No", "No")
                        .stringType("assetsOverseasNetValue", "10050")
                        .stringType("ihtGrossValue", "100000")
                        .stringType("ihtNetValue", "100000")
                        .stringType("ihtReferenceNumber", "GOT123456")
                        .stringMatcher("declarationCheckbox", "Yes|No", "Yes")
                        .numberType("outsideUKGrantCopies", 6)
                        .numberType("extraCopiesOfGrant", 3)
                        .stringType("uploadDocumentUrl", "http://document-management/document/12345")
                        .stringMatcher("registryLocation", "Oxford|Manchester|Birmingham|Leeds|Liverpool|Brighton|Cardiff|London|Winchester|Newcastle|ctsc", "Oxford")
                        .object("declaration", (declaration) ->
                            declaration.stringType("accept", "I confirm that I will administer the estate of the person who died according to law, and that my application is truthful.")
                                .stringType("confirm", "We confirm that we will administer the estate of Suki Hammond, according to law. We will:")
                                .stringType("confirmItem1", "collect the whole estate")
                                .stringType("confirmItem2", "keep full details (an inventory) of the estate")
                                .stringType("confirmItem3", "keep a full account of how the estate has been administered")
                                .stringType("requests", "If the probate registry (court) asks us to do so, we will:")
                                .stringType("requestsItem1", "provide the full details of the estate and how it has been administered")
                                .stringType("requestsItem2", "return the grant of probate to the court")
                                .stringType("understand", "We understand that:")
                                .stringType("understandItem1", "our application will be rejected if we do not answer any questions about the information we have given")
                                .stringType("understandItem2", "criminal proceedings for fraud may be brought against us if we are found to have been deliberately untruthful or dishonest"))
                        .object("legalStatement", (legalStatement) ->
                            legalStatement.stringType("applicant", "We, Jon Snow of place make the following statement:")
                                .stringType("deceased", "Ned Stark was born on 6 October 1902 and died on 22 December 1983, domiciled in England and Wales.")
                                .stringType("deceasedEstateLand", "To the best of our knowledge, information and belief, there was no land vested in Suki Hammond which was settled previously to the death (and not by the will) of Suki Hammond and which remained settled land notwithstanding such death.")
                                .stringType("deceasedEstateValue", "The gross value for the estate amounts to &pound;2222 and the net value for the estate amounts to &pound;222.")
                                .stringType("deceasedOtherNames", "They were also known as King North.")
                                .minArrayLike("executorsApplying", 0, 2,
                                    executorApplying -> executorApplying
                                        .object("value", (value) ->
                                            value.stringType("name", "Jon Snow, an executor named in the will or codicils, is applying for probate.")
                                                .stringType("sign", "Jon Snow will sign and send to the probate registry what they believe to be the true and original last will and testament and any codicils of Ned Stark.")
                                        )
                                )
                                .minArrayLike("executorsNotApplying", 0, 1,
                                    executorNotApplying -> executorNotApplying
                                        .object("value", (value) ->
                                            value.stringType("executor", "Burton Leonard, an executor named in the will or codicils, is not making this application because they died before Suki Hammond died.")
                                        )
                                )
                                .stringType("intro", "This statement is based on the information you&rsquo;ve given in your application. It will be stored as a public record.")
                        )
                        .numberType("numberOfApplicants", 2)
                        .numberType("numberOfExecutors", 3)
                        .stringMatcher("softStop", "Yes|No", "No")
                        .stringType("totalFee", "0")
                        .stringMatcher("willHasCodicils", "Yes|No", "Yes")
                        .stringMatcher("willLatestCodicilHasDate", "Yes|No", "No")
                        .numberType("willNumberOfCodicils", 1);

                    if (withExecutors) {
                        cd.minArrayLike("executorsApplying", 0, 2,
                            executorApplying -> executorApplying
                                .object("value", (value) ->
                                    value.stringType("applyingExecutorName", "Jon Snow")
                                        .stringMatcher("applyingExecutorPhoneNumber", "[0-9]+", "07981898999")
                                        .stringMatcher("applyingExecutorAgreed", "Yes|No", "Yes")
                                        .stringType("applyingExecutorEmail", "address@email.com")
                                        .stringType("applyingExecutorInvitationId", "54321")
                                        .stringType("applyingExecutorLeadName", "Graham Garderner")
                                        .object("applyingExecutorAddress", (address) ->
                                            address.stringType("AddressLine1", "Winterfell")
                                                .stringType("AddressLine2", "Westeros")
                                                .stringType("PostTown", "London")
                                                .stringType("PostCode", "SW17 0QT")
                                        ).stringType("applyingExecutorOtherNames", "Graham Poll")
                                        .stringType("applyingExecutorOtherNamesReason", "Divorce")
                                ))
                            .minArrayLike("executorsNotApplying", 0, 1,
                                executorNotApplying -> executorNotApplying
                                    .object("value", (value) ->
                                        value.stringType("notApplyingExecutorName", "Burton Leonard")
                                            .stringType("notApplyingExecutorReason", "DiedBefore"))
                            );

                    }
                    if (withPayments) {
                        cd.minArrayLike("payments", 0, 1,
                            payment -> payment
                                // .stringType("id", "950243f2-c713-4f1b-990d-83eb508bab91")
                                .object("value", (value) ->
                                    value.stringMatcher("amount", "[0-9]+", "2000")
                                        .stringType("method", "card")
                                        .stringMatcher("status", "Success|Failed|Initiated|not_required", "Success")
                                        .stringType("reference", "RC-1599-4876-0252-6208")
                                ));

                    }

                })
            ;

        }).build();
    }

    //    public static DslPart buildCaseDetailsListArrayStartEventReponse(String eventId , String token , String emailAddress, boolean withExecutors, boolean withPayments) {
//        return newJsonBody((o) -> {
//            o.   stringType("event_id", eventId)
//                .stringType("token", token)
//                .stringMatcher("state", "Draft|PaAppCreated|CaseCreated", "CaseCreated")
//                .stringValue("case_type", "AwaitingDecreeNisi")
//                .object("case_details", (cd) ->{
//                    cd.numberValue("id", 2000L);
//                    cd.stringMatcher("jurisdiction",  ALPHABETIC_REGEX,"DIVORCE");
//                    cd.stringMatcher("callback_response_status", ALPHABETIC_REGEX,  "DONE");
//                    cd.stringMatcher("case_type", ALPHABETIC_REGEX,  "DIVORCE");
//                    // the below is the  Map<Object,Object> data property  of the  CaseDetails object.
//                    cd.object("data", (dataMap) -> {
//                        dataMap.numberType("outsideUKGrantCopies", 6)
//                            .stringValue("applicationType", "Personal")
//                            .stringMatcher("applicationSubmittedDate", REGEX_DATE, "2018-05-17")
//                            .stringType("primaryApplicantForenames", "Jon")
//                            .stringType("primaryApplicantSurname", "Snow")
//                            .stringMatcher("primaryApplicantAddressFound",
//                                "Yes|No", "Yes")
//                            .stringMatcher("primaryApplicantPhoneNumber", "[0-9]+", "123455678")
//                            .stringMatcher("primaryApplicantRelationshipToDeceased", "partner|child|sibling|partner|parent|adoptedChild|other", "adoptedChild")
//                            .stringMatcher("primaryApplicantAdoptionInEnglandOrWales", "(Yes|No)", "Yes")
//                            .stringValue("primaryApplicantEmailAddress",emailAddress )
//                            .object("primaryApplicantAddress", (address) ->
//                                address.stringType("AddressLine1", "Pret a Manger")
//                                    .stringType("AddressLine2", "St. Georges Hospital")
//                                    .stringType("PostTown", "London")
//                                    .stringType("PostCode", "SW17 0QT")
//                            )
//                            .stringType("deceasedForenames", "Ned")
//                            .stringType("deceasedSurname", "Stark")
//                            .stringMatcher("deceasedDateOfBirth", REGEX_DATE, "1930-01-01")
//                            .stringMatcher("deceasedDateOfDeath", REGEX_DATE, "2018-01-01")
//                            .object("deceasedAddress", (address) ->
//                                address.stringType("AddressLine1", "Winterfell")
//                                    .stringType("AddressLine2", "Westeros")
//                                    .stringType("PostTown", "London")
//                                    .stringType("PostCode", "SW17 0QT")
//                            )
//                            .stringMatcher("deceasedAddressFound", "Yes|No", "Yes")
//                            .stringMatcher("deceasedAnyOtherNames", "Yes|No", "Yes")
//                            .minArrayLike("deceasedAliasNameList", 0, 2,
//                                alias -> alias
//                                    .object("value", (value) ->
//                                        value.stringType("Forenames", "King")
//                                            .stringType("LastName", "North")
//                                    ))
//                            .stringMatcher("deceasedMartialStatus", "marriedCivilPartnership|divorcedCivilPartnership|widowed|judicially|neverMarried")
//                            .stringMatcher("deceasedMarriedAfterWillOrCodicilDate", "Yes|No", "No")
//                            .stringMatcher("deceasedDivorcedInEnglandOrWales", "Yes|No", "No")
//                            .stringMatcher("deceasedSpouseNotApplyingReason", "renunciated|mentallyIncapable|other")
//                            .stringMatcher("deceasedOtherChildren", "Yes|No", "Yes")
//                            .stringMatcher("childrenOverEighteenSurvived", "Yes|No", "Yes")
//                            .stringMatcher("childrenDied", "Yes|No", "No")
//                            .stringMatcher("grandChildrenSurvivedUnderEighteen", "Yes|No", "No")
//                            .stringMatcher("deceasedAnyChildren", "Yes|No", "No")
//                            .stringMatcher("deceasedHasAssetsOutsideUK", "Yes|No", "Yes")
//                            .stringMatcher("deceasedAnyChildren", "Yes|No", "No")
//                            .stringMatcher("ihtFormId", "IHT205|IHT207|IHT400421", "IHT205")
//                            .stringMatcher("ihtFormCompletedOnline", "Yes|No", "No")
//                            .stringType("assetsOverseasNetValue", "10050")
//                            .stringType("ihtGrossValue", "100000")
//                            .stringType("ihtNetValue", "100000")
//                            .stringType("ihtReferenceNumber", "GOT123456")
//                            .stringMatcher("declarationCheckbox", "Yes|No", "Yes")
//                            .numberType("outsideUKGrantCopies", 6)
//                            .numberType("extraCopiesOfGrant", 3)
//                            .stringType("uploadDocumentUrl", "http://document-management/document/12345")
//                            .stringMatcher("registryLocation", "Oxford|Manchester|Birmingham|Leeds|Liverpool|Brighton|Cardiff|London|Winchester|Newcastle|ctsc", "Oxford")
//                            .object("declaration", (declaration) ->
//                                declaration.stringType("accept", "I confirm that I will administer the estate of the person who died according to law, and that my application is truthful.")
//                                    .stringType("confirm", "We confirm that we will administer the estate of Suki Hammond, according to law. We will:")
//                                    .stringType("confirmItem1", "collect the whole estate")
//                                    .stringType("confirmItem2", "keep full details (an inventory) of the estate")
//                                    .stringType("confirmItem3", "keep a full account of how the estate has been administered")
//                                    .stringType("requests", "If the probate registry (court) asks us to do so, we will:")
//                                    .stringType("requestsItem1", "provide the full details of the estate and how it has been administered")
//                                    .stringType("requestsItem2", "return the grant of probate to the court")
//                                    .stringType("understand", "We understand that:")
//                                    .stringType("understandItem1", "our application will be rejected if we do not answer any questions about the information we have given")
//                                    .stringType("understandItem2", "criminal proceedings for fraud may be brought against us if we are found to have been deliberately untruthful or dishonest"))
//                            .object("legalStatement", (legalStatement) ->
//                                legalStatement.stringType("applicant", "We, Jon Snow of place make the following statement:")
//                                    .stringType("deceased", "Ned Stark was born on 6 October 1902 and died on 22 December 1983, domiciled in England and Wales.")
//                                    .stringType("deceasedEstateLand", "To the best of our knowledge, information and belief, there was no land vested in Suki Hammond which was settled previously to the death (and not by the will) of Suki Hammond and which remained settled land notwithstanding such death.")
//                                    .stringType("deceasedEstateValue", "The gross value for the estate amounts to &pound;2222 and the net value for the estate amounts to &pound;222.")
//                                    .stringType("deceasedOtherNames", "They were also known as King North.")
//                                    .minArrayLike("executorsApplying", 0, 2,
//                                        executorApplying -> executorApplying
//                                            .object("value", (value) ->
//                                                value.stringType("name", "Jon Snow, an executor named in the will or codicils, is applying for probate.")
//                                                    .stringType("sign", "Jon Snow will sign and send to the probate registry what they believe to be the true and original last will and testament and any codicils of Ned Stark.")
//                                            )
//                                    )
//                                    .minArrayLike("executorsNotApplying", 0, 1,
//                                        executorNotApplying -> executorNotApplying
//                                            .object("value", (value) ->
//                                                value.stringType("executor", "Burton Leonard, an executor named in the will or codicils, is not making this application because they died before Suki Hammond died.")
//                                            )
//                                    )
//                                    .stringType( "intro", "This statement is based on the information you&rsquo;ve given in your application. It will be stored as a public record.")
//                            )
//                            .numberType( "numberOfApplicants", 2)
//                            .numberType( "numberOfExecutors", 3)
//                            .stringMatcher("softStop", "Yes|No", "No")
//                            .stringType("totalFee", "0")
//                            .stringMatcher("willHasCodicils", "Yes|No", "Yes")
//                            .stringMatcher("willLatestCodicilHasDate", "Yes|No", "No")
//                            .numberType( "willNumberOfCodicils", 1);
//
//                        if (withExecutors) {
//                            cd.minArrayLike("executorsApplying", 0, 2,
//                                executorApplying -> executorApplying
//                                    .object("value", (value) ->
//                                        value.stringType("applyingExecutorName", "Jon Snow")
//                                            .stringMatcher("applyingExecutorPhoneNumber", "[0-9]+", "07981898999")
//                                            .stringMatcher("applyingExecutorAgreed", "Yes|No", "Yes")
//                                            .stringType("applyingExecutorEmail", "address@email.com")
//                                            .stringType("applyingExecutorInvitationId", "54321")
//                                            .stringType("applyingExecutorLeadName", "Graham Garderner")
//                                            .object("applyingExecutorAddress", (address) ->
//                                                address.stringType("AddressLine1", "Winterfell")
//                                                    .stringType("AddressLine2", "Westeros")
//                                                    .stringType("PostTown", "London")
//                                                    .stringType("PostCode", "SW17 0QT")
//                                            ).stringType("applyingExecutorOtherNames", "Graham Poll")
//                                            .stringType("applyingExecutorOtherNamesReason", "Divorce")
//                                    ))
//                                .minArrayLike("executorsNotApplying", 0, 1,
//                                    executorNotApplying -> executorNotApplying
//                                        .object("value", (value) ->
//                                            value.stringType("notApplyingExecutorName", "Burton Leonard")
//                                                .stringType("notApplyingExecutorReason", "DiedBefore"))
//                                );
//
//                        }
//                        if (withPayments) {
//                            cd.minArrayLike("payments", 0, 1,
//                                payment -> payment
//                                    // .stringType("id", "950243f2-c713-4f1b-990d-83eb508bab91")
//                                    .object("value", (value) ->
//                                        value.stringMatcher("amount", "[0-9]+", "2000")
//                                            .stringType("method", "card")
//                                            .stringMatcher("status", "Success|Failed|Initiated|not_required", "Success")
//                                            .stringType("reference", "RC-1599-4876-0252-6208")
//                                    ));
//
//                        }
//                    });
//                });
//        }).build();
//    }



}
