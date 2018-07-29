package uk.gov.hmcts.reform.divorce.util;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;

import java.util.Map;

public class RestUtil {

    public static Response postToRestService(String url, Map<String, Object> headers, String requestBody) {
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

    public static Response putToRestService(String url, Map<String, Object> headers, String requestBody,
                                     Map<String, Object> params) {
        if (requestBody != null) {
            return SerenityRest.given()
                .headers(headers)
                .params(params)
                .body(requestBody)
                .when()
                .put(url)
                .andReturn();
        } else {
            return SerenityRest.given()
                .headers(headers)
                .when()
                .put(url)
                .andReturn();
        }
    }

    public static Response deleteOnRestService(String url, Map<String, Object> headers) {
        return SerenityRest.given()
            .headers(headers)
            .when()
            .delete(url)
            .andReturn();
    }

    public static Response getFromRestService(String url, Map<String, Object> headers, Map<String, Object> params) {
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
