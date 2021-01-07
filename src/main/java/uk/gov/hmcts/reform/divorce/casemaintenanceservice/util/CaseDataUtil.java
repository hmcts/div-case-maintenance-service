package uk.gov.hmcts.reform.divorce.casemaintenanceservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CcdCaseProperties;

import java.util.Map;
import java.util.Optional;

import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CmsConstants.NO_VALUE;

@SuppressWarnings("squid:S1118")
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CaseDataUtil {

    public static String getOptionalPropertyValueAsString(Map<String, Object> propertiesMap, String key, String defaultValue) {
        return Optional.ofNullable(propertiesMap.get(key))
            .map(String.class::cast)
            .orElse(defaultValue);
    }


    public static boolean isDnRefused(Map<String, Object> caseData) {
        String isDnGranted = getOptionalPropertyValueAsString(caseData, CcdCaseProperties.DECREE_NISI_GRANTED, "");
        return NO_VALUE.equalsIgnoreCase(isDnGranted);
    }
}
