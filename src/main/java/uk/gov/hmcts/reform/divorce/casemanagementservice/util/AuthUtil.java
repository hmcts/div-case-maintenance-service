package uk.gov.hmcts.reform.divorce.casemanagementservice.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthUtil {

    private static final String BEARER = "Bearer ";

    public static String getBearToken(String token) {
        if (StringUtils.isBlank(token)) {
            return token;
        }

        return token.startsWith(BEARER) ? token : BEARER.concat(token);
    }
}
