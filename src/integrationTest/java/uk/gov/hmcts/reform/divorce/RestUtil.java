package uk.gov.hmcts.reform.divorce;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;

import java.util.Map;

class RestUtil {

    static Response postToRestService(String url, Map<String, Object> headers, String requestBody) {
        if (requestBody != null) {
            return SerenityRest.given()
                .headers(headers)
                .body(requestBody)
                .when()
                .post(url)
                .andReturn();
        } else {
            return SerenityRest.given()
                .headers(headers)
                .when()
                .post(url)
                .andReturn();
        }
    }

    static Response getFromRestService(String url, Map<String, Object> headers, Map<String, Object> params) {
        if (params != null) {
            return SerenityRest.given()
                .headers(headers)
                .params(params)
                .when()
                .get(url)
                .andReturn();
        } else {
            return SerenityRest.given()
                .headers(headers)
                .when()
                .get(url)
                .andReturn();
        }
    }
}
