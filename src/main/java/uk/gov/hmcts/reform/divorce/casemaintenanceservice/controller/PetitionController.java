package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;

import javax.ws.rs.core.MediaType;

@RestController
@RequestMapping(path = "casemaintenance")
@Api(value = "Case Maintenance Services", consumes = "application/json", produces = "application/json")
public class PetitionController {

    @Autowired
    private CcdRetrievalService ccdRetrievalService;

    @GetMapping(path = "/version/1/retrievePetition", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieves a divorce case draft")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A Petition exists. The petition is in the response body"),
        @ApiResponse(code = 300, message = "Multiple Petition found"),
        @ApiResponse(code = 404, message = "Petition does not exist")
    })
    public ResponseEntity<Object> retrievePetition(
        @RequestHeader("Authorization")
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String jwt,
        @RequestParam("checkCcd")
        @ApiParam(value = "Boolean flag enabling CCD check for petition") final boolean checkCcd) {


        return ResponseEntity.ok(ccdRetrievalService.retrievePetition(jwt));
    }

}
