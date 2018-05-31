package uk.gov.hmcts.reform.divorce.casemanagementservice.functionaltest;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperTestUtil {

    public static byte[] convertObjectToJsonBytes(final Object object) {
        try {
            return new ObjectMapper().writeValueAsBytes(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertObjectToJsonString(final Object object) {
        try {
            return new ObjectMapper().writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
