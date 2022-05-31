package uk.gov.hmcts.reform.divorce.util;

import io.restassured.http.ContentType;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;

import java.util.Collections;
import java.util.Map;

public class RestUtil {

    private RestUtil() {
        throw new IllegalStateException("RestUtil class");
    }

    public static Response postToRestService(String url, Map<String, Object> headers, String requestBody) {
        return postToRestService(url, headers, requestBody, Collections.emptyMap());
    }

    public static Response postToRestService(String url, Map<String, Object> headers, String requestBody,
                                             Map<String, Object> params) {
        if (requestBody != null) {
            return SerenityRest.given()
                .headers(headers)
                .contentType(ContentType.JSON)
                .queryParams(params)
                .body(requestBody)
                .when()
                .post(url)
                .andReturn();
        } else {
            return SerenityRest.given()
                .headers(headers)
                .contentType(ContentType.JSON)
                .queryParams(params)
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
                .contentType(ContentType.JSON)
                .params(params)
                .body(requestBody)
                .when()
                .put(url)
                .andReturn();
        } else {
            return SerenityRest.given()
                .headers(headers)
                .contentType(ContentType.JSON)
                .when()
                .put(url)
                .andReturn();
        }
    }

    public static Response deleteOnRestService(String url, Map<String, Object> headers) {
        return SerenityRest.given()
            .headers(headers)
            .contentType(ContentType.JSON)
            .when()
            .delete(url)
            .andReturn();
    }

    public static Response getFromRestService(String url, Map<String, Object> headers) {
        return SerenityRest.given()
            .headers(headers)
            .contentType(ContentType.JSON)
            .when()
            .get(url)
            .andReturn();
    }
}
