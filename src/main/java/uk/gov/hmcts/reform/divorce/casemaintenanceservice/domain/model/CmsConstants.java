package uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CmsConstants {
    public static final String YEAR_DATE_FORMAT = "yyyy-MM-dd";
    public static final String CTSC_SERVICE_CENTRE = "serviceCentre";
    public static final String CREATOR_ROLE = "[CREATOR]";
    public static final String RESP_SOL_ROLE = "[RESPSOLICITOR]";
    public static final String PET_SOL_ROLE = "[PETSOLICITOR]";
    public static final String YES_VALUE = "Yes";
    public static final String NO_VALUE = "No";
    public static final String CASE_EVENT_ID = "eventId";
    public static final String D_8_REASON_FOR_DIVORCE = "D8ReasonForDivorce";
    public static final String D_8_DIVORCE_WHO = "D8DivorceWho";
    public static final String D_8_HELP_WITH_FEES_NEED_HELP = "D8HelpWithFeesNeedHelp";
    public static final String D_8_CONNECTIONS = "D8Connections";
}
