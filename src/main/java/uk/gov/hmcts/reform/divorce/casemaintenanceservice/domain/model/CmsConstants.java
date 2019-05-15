package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CmsConstants {
    public static final String YEAR_DATE_FORMAT = "yyyy-MM-dd";
    public static final String CTSC_SERVICE_CENTRE = "serviceCentre";
    public static final String CREATOR_ROLE = "[CREATOR]";
    public static final String RESP_SOL_ROLE = "[RESPSOLICITOR]";
}
