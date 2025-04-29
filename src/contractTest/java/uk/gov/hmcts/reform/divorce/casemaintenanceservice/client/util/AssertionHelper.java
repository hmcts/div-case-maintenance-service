package uk.gov.hmcts.reform.divorce.casemaintenanceservice.client.util;

import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public final class AssertionHelper {

    private AssertionHelper() {
    }

    public static void assertCaseDetails(final CaseDetails caseDetails) {
        assertNotNull(caseDetails);

        Map<String,Object> caseDataMap = caseDetails.getData();

        assertThat(caseDataMap.get("D8ScreenHasMarriageBroken"),is("YES"));
        assertThat(caseDataMap.get("D8PetitionerFirstName"),is("John"));
        assertThat(caseDataMap.get("D8PetitionerLastName"),is("Smith"));

        assertThat(caseDataMap.get("D8LegalProceedingsDetails"),is("The legal proceeding details"));
        assertThat(caseDataMap.get("D8ReasonForDivorceShowFiveYearsSeparatio"),is("YES"));

        //D8PetitionerNameChangedHow
        List<String> nameChangedHow = (ArrayList<String>) caseDataMap.get("D8PetitionerNameChangedHow");
        assertEquals(nameChangedHow.get(0), "marriageCertificate");

        //D8PetitionerHomeAddress
        Map<String,String> homeAddressMap = (Map<String,String>)caseDataMap.get("D8PetitionerHomeAddress");
        assertEquals(homeAddressMap.get("PostCode"), "SW17 0QT");

        // D8FinancialOrderFor
        List<String> financialOrder = (ArrayList<String>) caseDataMap.get("D8FinancialOrderFor");
        assertEquals(financialOrder.get(0), "petitioner");

    }
}
