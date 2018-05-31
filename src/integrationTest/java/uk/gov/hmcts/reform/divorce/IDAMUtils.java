package uk.gov.hmcts.reform.divorce;

import com.nimbusds.jwt.JWTParser;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Base64;
import java.util.Map;

@Service
class IDAMUtils {

    @Value("${auth.idam.client.baseUrl}")
    private String idamUserBaseUrl;


    public String getUserId(String encodedJwt) {
        String jwt = encodedJwt.replaceFirst("Bearer ", "");
        Map<String, Object> claims;
        try {
            claims = JWTParser.parse(jwt).getJWTClaimsSet().getClaims();

        } catch (ParseException e) {
            throw new IllegalStateException("Cannot find user from authorization token ", e);
        }
        return (String) claims.get("id");
    }

    void createUserInIdam(String username, String password) {
        String s = "{\"email\":\"" + username + "@test.com\", \"forename\":\"" + username +
            "\",\"surname\":\"User\",\"password\":\"" + password + "\"}";

        RestAssured.given()
                .header("Content-Type", "application/json")
                .body(s)
                .post(idamCreateUrl());
    }

    void createDivorceCaseworkerUserInIdam(String username, String password) {
        String body = "{\"email\":\"" + username + "@test.com" + "\", "
                + "\"forename\":" + "\"" + username + "\"," + "\"surname\":\"User\",\"password\":\"" + password + "\", "
                + "\"roles\":[\"caseworker-divorce\"], \"userGroup\":{\"code\":\"caseworker\"}}";
        RestAssured.given()
                .header("Content-Type", "application/json")
                .body(body)
                .post(idamCreateUrl());
    }

    private String idamCreateUrl() {
        return idamUserBaseUrl + "/testing-support/accounts";
    }

    private String loginUrl() {
        return idamUserBaseUrl + "/oauth2/authorize?response_type=token&client_id=divorce&redirect_uri="
                            + "https://www.preprod.ccd.reform.hmcts.net/oauth2redirect";
    }

    String generateUserTokenWithNoRoles(String username, String password) {
        String userLoginDetails = String.join(":", username + "@test.com", password);
        final String authHeader = "Basic " + new String(Base64.getEncoder().encode((userLoginDetails).getBytes()));

        final String token = RestAssured.given()
                .header("Authorization", authHeader)
                .post(loginUrl())
                .body()
                .path("access-token");

        return "Bearer " + token;
    }
}
