package uk.gov.hmcts.reform.divorce.casemaintenanceservice.util;

import org.junit.Test;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.NO_VALUE;

public class CaseDataUtilUTest {

    @Test
    public void givenValueExistsWhenGetOptionalPropertyValueAsStringThenReturnValue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("testKey", "testValue");

        String value = CaseDataUtil.getOptionalPropertyValueAsString(caseData, "testKey", "");

        assertThat(value, equalTo("testValue"));
    }

    @Test
    public void givenValueDoesNoExistWhenGetOptionalPropertyValueAsStringThenReturnDefaultValue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put("testKey", "testValue");

        String value = CaseDataUtil.getOptionalPropertyValueAsString(caseData, "randomKey", "");

        assertThat(value, equalTo(""));
    }

    @Test
    public void givenDecreeNisiGrantedIsNoWhenisDnRefusedThenReturnTrue() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdCaseProperties.DECREE_NISI_GRANTED, NO_VALUE);

        assertThat(CaseDataUtil.isDnRefused(caseData), is(true));
    }

    @Test
    public void givenDecreeNisiGrantedIsNotNoWhenisDnRefusedThenReturnFalse() {
        Map<String, Object> caseData = new HashMap<>();
        caseData.put(CcdCaseProperties.DECREE_NISI_GRANTED, "NotNo");

        assertThat(CaseDataUtil.isDnRefused(caseData), is(false));
    }

    @Test
    public void givenDecreeNisiGrantedDoesNotExistWhenisDnRefusedThenReturnFalse() {
        assertThat(CaseDataUtil.isDnRefused(Collections.emptyMap()), is(false));
    }
}
