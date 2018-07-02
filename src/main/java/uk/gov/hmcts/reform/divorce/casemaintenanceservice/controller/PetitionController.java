package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.PetitionService;

import javax.ws.rs.core.MediaType;

@RestController
@RequestMapping(path = "casemaintenance")
@Api(value = "Case Maintenance Services", consumes = "application/json", produces = "application/json")
public class PetitionController {

    @Autowired
    private PetitionService petitionService;

    @GetMapping(path = "/version/1/retrievePetition", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieves a divorce case draft")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A Petition exists. The petition is in the response body"),
        @ApiResponse(code = 300, message = "Multiple Petition found")
    })
    public ResponseEntity<Object> retrievePetition(
        @RequestHeader("Authorization")
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String jwt,
        @RequestParam(value = "checkCcd", required = false)
        @ApiParam(value = "Boolean flag enabling CCD check for petition") final boolean checkCcd) {

        try {
            return ResponseEntity.ok(petitionService.retrievePetition(jwt, checkCcd));
        } catch (DuplicateCaseException e) {
            return ResponseEntity.status(HttpStatus.MULTIPLE_CHOICES).build();
        }
    }

}
