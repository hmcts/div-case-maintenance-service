package uk.gov.hmcts.reform.divorce.casemaintenanceservice.functionaltest;

import com.fasterxml.jackson.databind.ObjectMapper;

class ObjectMapperTestUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static <T> T convertStringToObject(String data, Class<T> type) {
        try {
            return OBJECT_MAPPER.readValue(data, type);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String convertObjectToJsonString(final Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
