package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import uk.gov.hmcts.reform.divorce.casemaintenanceservice.CaseMaintenanceServiceApplication;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes= CaseMaintenanceServiceApplication.class)
@AutoConfigureMockMvc
public class RootControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    public void getShouldReturn200() throws Exception {

        // given
        MockHttpServletRequestBuilder getRequest = MockMvcRequestBuilders.get("/");

        // when
        ResultActions performedGet = mvc.perform(getRequest);

        // then
        performedGet.andExpect(status().isOk()).andReturn();
    }
}
