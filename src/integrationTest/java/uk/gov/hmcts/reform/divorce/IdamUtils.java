package uk.gov.hmcts.reform.divorce;

import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
class IdamUtils {

    @Value("${auth.idam.client.baseUrl}")
    private String idamUserBaseUrl;

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
