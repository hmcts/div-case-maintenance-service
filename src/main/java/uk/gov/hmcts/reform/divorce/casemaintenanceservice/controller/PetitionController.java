package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.exception.DuplicateCaseException;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.PetitionService;

import javax.validation.constraints.NotNull;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(path = "casemaintenance/version/1")
@Api(value = "Case Maintenance Services", consumes = "application/json", produces = "application/json")
@Slf4j
public class PetitionController {

    @Autowired
    private PetitionService petitionService;

    @GetMapping(path = "/retrievePetition", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieves a divorce case from CCD of Draft store")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A Petition exists. The petition is in the response body"),
        @ApiResponse(code = 204, message = "When there are no petition exists"),
        @ApiResponse(code = 300, message = "Multiple Petition found")
        })
    public ResponseEntity<CaseDetails> retrievePetition(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String jwt,
        @RequestParam(value = "checkCcd", required = false)
        @ApiParam(value = "Boolean flag enabling CCD check for petition") final Boolean checkCcd) {

        try {
            CaseDetails caseDetails = petitionService.retrievePetition(jwt,
                Optional.ofNullable(checkCcd).orElse(false));

            return caseDetails == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(caseDetails);
        } catch (DuplicateCaseException e) {
            return ResponseEntity.status(HttpStatus.MULTIPLE_CHOICES).build();
        }
    }

    @PutMapping(path = "/drafts", consumes = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Saves a divorce case to draft store")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Draft saved")})
    public ResponseEntity<Void> saveDraft(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String jwt,
        @RequestBody
        @ApiParam(value = "The divorce case draft", required = true)
        @NotNull final Map<String, Object> data) {
        log.debug("Received request to save a divorce session draft");
        petitionService.saveDraft(jwt, data);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/drafts")
    @ApiOperation(value = "Deletes a divorce case draft")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "The divorce case draft has been deleted successfully")})
    public ResponseEntity<Void> deleteDraft(@RequestHeader("Authorization")
                                            @ApiParam(value = "JWT authorisation token issued by IDAM",
                                                required = true) final String jwt) {
        log.debug("Received request to delete a divorce session draft");
        petitionService.deleteDraft(jwt);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/drafts", produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Retrieve All the Drafts for a given user")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns all the saved drafts for a given user")})
    public ResponseEntity<DraftList> retrieveAllDrafts(@RequestHeader(HttpHeaders.AUTHORIZATION)
                                                        @ApiParam(value = "JWT authorisation token issued by IDAM",
                                                            required = true)final String jwt) {
        return ResponseEntity.ok(petitionService.getAllDrafts(jwt));
    }
}
