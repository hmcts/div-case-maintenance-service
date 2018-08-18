package uk.gov.hmcts.reform.divorce.idam.utils;

import io.restassured.RestAssured;
import io.restassured.config.RedirectConfig;
import io.restassured.response.Response;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

public class StrategicIdamUtils extends IdamUtils {

    @Override
    public void upliftUser(String emailAddress, String password, String authToken) {
        Response response = RestAssured.given()
            .config(RestAssured.config().redirect(RedirectConfig.redirectConfig().followRedirects(false)))
            .queryParam("userName", emailAddress)
            .queryParam("password", password)
            .queryParam("jwt", authToken)
            .queryParam("clientId", CLIENT_ID)
            .queryParam("redirectUri", idamRedirectUrl)
            .post(idamUserBaseUrl +  "/login/uplift")
            .andReturn();

        getAuthTokenByCode(getCodeFromRedirect(response));
    }

    @Override
    public String authenticatePinUser(String pin) {
        Response response = RestAssured.given()
            .config(RestAssured.config().redirect(RedirectConfig.redirectConfig().followRedirects(false)))
            .header("pin", pin)
            .queryParam("client_id", CLIENT_ID)
            .queryParam("redirect_uri", idamRedirectUrl)
            .get(idamUserBaseUrl +  "/pin")
            .andReturn();

        return getAuthTokenByCode(getCodeFromRedirect(response));
    }

    private String getCodeFromRedirect(Response response) {
        String location = response.headers().getList("Location").stream().findFirst()
            .orElseThrow(IllegalArgumentException::new).getValue();

        UriComponents build = UriComponentsBuilder.fromUriString(location).build();
        return build.getQueryParams().getFirst("code");
    }
}
