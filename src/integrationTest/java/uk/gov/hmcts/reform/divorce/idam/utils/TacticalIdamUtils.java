package uk.gov.hmcts.reform.divorce.idam.utils;

import net.serenitybdd.rest.SerenityRest;
import org.apache.http.entity.ContentType;
import org.springframework.http.HttpHeaders;

import java.util.Base64;

public class TacticalIdamUtils extends IdamUtils {
    private static final String PIN_PREFIX = "Pin ";

    @Override
    public String authenticatePinUser(String pin) {
        String authHeader = PIN_PREFIX + new String(Base64.getEncoder().encode(pin.getBytes()));
        return getAuthToken(authHeader);
    }

    @Override
    public void upliftUser(String emailAddress, String password, String authToken) {
        final String authHeader = getBasicAuthHeader(emailAddress, password);

        SerenityRest.given()
            .header(HttpHeaders.AUTHORIZATION, authHeader)
            .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
            .queryParam("upliftToken", authToken)
            .queryParam("response_type", CODE)
            .queryParam("client_id", CLIENT_ID)
            .queryParam("redirect_uri", idamRedirectUrl)
            .post(idamUserBaseUrl + "/oauth2/authorize")
            .andReturn();
    }
}
