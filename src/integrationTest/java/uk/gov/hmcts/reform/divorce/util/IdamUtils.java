package uk.gov.hmcts.reform.divorce.util;

import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Value;

import java.util.Base64;

public class IdamUtils {

    @Value("${auth.idam.client.baseUrl}")
    private String idamUserBaseUrl;

    public void createUserInIdam(String username, String emailAddress,  String password) {
        String payload = "{\"email\":\"" + emailAddress + "\", \"forename\":\"" + username
            + "\",\"surname\":\"User\",\"password\":\"" + password + "\"}";

        RestAssured.given()
            .header("Content-Type", "application/json")
            .body(payload)
            .post(idamCreateUrl());
    }

    private String idamCreateUrl() {
        return idamUserBaseUrl + "/testing-support/accounts";
    }

    private String loginUrl() {
        return idamUserBaseUrl + "/oauth2/authorize?response_type=token&client_id=divorce&redirect_uri="
                            + "https://www.preprod.ccd.reform.hmcts.net/oauth2redirect";
    }

    public String generateUserTokenWithNoRoles(String emailAddress, String password) {
        String userLoginDetails = String.join(":", emailAddress, password);
        final String authHeader = "Basic " + new String(Base64.getEncoder().encode((userLoginDetails).getBytes()));

        final String token = RestAssured.given()
                .header("Authorization", authHeader)
                .post(loginUrl())
                .body()
                .path("access-token");

        return "Bearer " + token;
    }
}
