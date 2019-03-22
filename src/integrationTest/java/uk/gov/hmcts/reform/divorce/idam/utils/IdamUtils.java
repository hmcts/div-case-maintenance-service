package uk.gov.hmcts.reform.divorce.idam.utils;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import uk.gov.hmcts.reform.divorce.model.GeneratePinRequest;
import uk.gov.hmcts.reform.divorce.model.PinResponse;
import uk.gov.hmcts.reform.divorce.model.RegisterUserRequest;
import uk.gov.hmcts.reform.divorce.util.ResourceLoader;

import java.util.Base64;

public abstract class IdamUtils {
    private static final String TOKEN = "token";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    static final String CLIENT_ID = "divorce";
    static final String CODE = "code";


    @Value("${auth.idam.client.baseUrl}")
    String idamUserBaseUrl;

    @Value("${auth.idam.client.redirect-url}")
    String idamRedirectUrl;

    @Value("${auth.idam.client.secret}")
    private String idamClientSecret;

    public final void createUserInIdam(RegisterUserRequest registerUserRequest) {
        SerenityRest.given()
            .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
            .body(ResourceLoader.objectToJson(registerUserRequest))
            .post(idamCreateUrl());
    }

    private Response retrieveUserDetails(String authToken) {
        return SerenityRest.given()
            .header(HttpHeaders.AUTHORIZATION, authToken)
            .get(idamUserBaseUrl +  "/details")
            .andReturn();
    }

    public final String getUserId(String authorisation) {
        return retrieveUserDetails(authorisation).jsonPath().get("id").toString();
    }

    public final String authenticateUser(String emailAddress, String password) {
        final String authHeader = getBasicAuthHeader(emailAddress, password);
        return "Bearer " + getAuthToken(authHeader);
    }

    final String getAuthToken(String authHeader) {
        return getAuthTokenByCode(getAuthCode(authHeader));
    }

    private String getAuthCode(String authHeader) {
        return SerenityRest.given()
            .header(HttpHeaders.AUTHORIZATION, authHeader)
            .queryParam("response_type", CODE)
            .queryParam("client_id", CLIENT_ID)
            .queryParam("redirect_uri", idamRedirectUrl)
            .post(idamUserBaseUrl + "/oauth2/authorize")
            .body()
            .jsonPath().get("code");
    }

    final String getAuthTokenByCode(String code) {
        return SerenityRest.given()
            .queryParam("code", code)
            .queryParam("grant_type", AUTHORIZATION_CODE)
            .queryParam("redirect_uri", idamRedirectUrl)
            .queryParam("client_id", CLIENT_ID)
            .queryParam("client_secret", idamClientSecret)
            .post(idamUserBaseUrl + "/oauth2/token")
            .body()
            .jsonPath().get("access_" + TOKEN);
    }

    public final PinResponse generatePin(String firstName, String lastName, String authToken) {
        final GeneratePinRequest generatePinRequest =
            GeneratePinRequest.builder()
                .firstName(firstName)
                .lastName(lastName)
                .build();

        Response pinResponse =  SerenityRest.given()
            .header(HttpHeaders.AUTHORIZATION, authToken)
            .header(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.toString())
            .body(ResourceLoader.objectToJson(generatePinRequest))
            .post(idamUserBaseUrl + "/pin")
            .andReturn();

        return PinResponse.builder()
            .pin(pinResponse.jsonPath().get("pin").toString())
            .userId(pinResponse.jsonPath().get("userId").toString())
            .build();
    }

    final String getBasicAuthHeader(String emailAddress, String password) {
        String userLoginDetails = String.join(":", emailAddress, password);
        return "Basic " + new String(Base64.getEncoder().encode(userLoginDetails.getBytes()));
    }

    public abstract void upliftUser(String emailAddress, String password, String authToken);

    public abstract String authenticatePinUser(String pin);

    private String idamCreateUrl() {
        return idamUserBaseUrl + "/testing-support/accounts";
    }
}
