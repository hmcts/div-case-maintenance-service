package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

import java.util.Map;
import java.util.Optional;
import javax.validation.constraints.NotNull;

import static uk.gov.hmcts.reform.divorce.casemaintenanceservice.domain.model.CaseRetrievalStateMap.PETITIONER_CASE_STATE_GROUPING;

@RestController
@RequestMapping(path = "casemaintenance/version/1")
@Slf4j
public class PetitionController {

    @Autowired
    private PetitionService petitionService;

    @GetMapping(path = "/retrieveCase", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Retrieves a divorce case from CCD or Draft store")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A Petition exists. The petition is in the response body"),
        @ApiResponse(responseCode = "204", description = "When there are no petition exists"),
        @ApiResponse(responseCode = "300", description = "Multiple Petition found")
        })
    public ResponseEntity<CaseDetails> retrievePetition(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt) {

        try {
            CaseDetails caseDetails = petitionService.retrievePetition(jwt, PETITIONER_CASE_STATE_GROUPING);

            return caseDetails == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(caseDetails);
        } catch (DuplicateCaseException e) {
            log.warn(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.MULTIPLE_CHOICES).build();
        }
    }

    @GetMapping(path = "/retrieveAosCase", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Retrieves a divorce case from CCD of Draft store")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A Petition exists. The petition is in the response body"),
        @ApiResponse(responseCode = "204", description = "When there are no petition exists"),
        @ApiResponse(responseCode = "300", description = "Multiple Petition found")
        })
    public ResponseEntity<CaseDetails> retrieveCaseForRespondent(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt) {

        try {
            CaseDetails caseDetails = petitionService.retrievePetitionForAos(jwt);

            return caseDetails == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(caseDetails);
        } catch (DuplicateCaseException e) {
            log.warn(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.MULTIPLE_CHOICES).build();
        }
    }

    @GetMapping(path = "/case", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Retrieves a divorce case from CCD")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "A Case exists. The case is in the response body"),
        @ApiResponse(responseCode = "404", description = "When no case exists"),
        @ApiResponse(responseCode = "300", description = "Multiple Cases found")
        })
    public ResponseEntity<CaseDetails> retrieveCase(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt) {
        try {
            CaseDetails caseDetails = petitionService.retrievePetition(jwt);
            return caseDetails == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(caseDetails);
        } catch (DuplicateCaseException e) {
            log.warn(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.MULTIPLE_CHOICES).build();
        }
    }

    @PutMapping(path = "/amended-petition-draft", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Creates a new draft petition for an amend petition workflow")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description =
            "A draft amendment case was created based on the users previously rejected petition"),
        @ApiResponse(responseCode = "404", description = "When no case exists"),
        @ApiResponse(responseCode = "300", description = "Multiple cases found")})
    public ResponseEntity<Map<String, Object>> createAmendedPetitionDraft(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt) {
        try {
            Map<String, Object> newCaseDraftData = petitionService.createAmendedPetitionDraft(jwt);

            if (newCaseDraftData == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(newCaseDraftData);
        } catch (DuplicateCaseException e) {
            log.warn(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.MULTIPLE_CHOICES).build();
        }
    }

    @PutMapping(path = "/amended-petition-draft-refusal", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description
            = "Creates a new draft petition for an amend petition workflow due to Refusal Order Rejection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description =
            "A draft amendment case was created based on the users previously rejected petition"),
        @ApiResponse(responseCode = "404", description = "When no case exists"),
        @ApiResponse(responseCode = "300", description = "Multiple cases found")})
    public ResponseEntity<Map<String, Object>> createAmendedPetitionDraftRefusal(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt) {
        try {
            Map<String, Object> newCaseDraftData = petitionService.createAmendedPetitionDraftRefusal(jwt);

            if (newCaseDraftData == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(newCaseDraftData);
        } catch (DuplicateCaseException e) {
            log.warn(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.MULTIPLE_CHOICES).build();
        }
    }

    @PutMapping(path = "/amended-petition-draft-refusal/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description
            = "Creates a new draft petition for an amend petition workflow due to Refusal Order Rejection")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description =
            "A draft amendment case was created based on the users previously rejected petition"),
        @ApiResponse(responseCode = "404", description = "When no case exists"),
        @ApiResponse(responseCode = "300", description = "Multiple cases found")})
    public ResponseEntity<Map<String, Object>> createAmendedPetitionDraftRefusalFromCaseId(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt,
        @PathVariable @Parameter(description = "caseId", required = true) String caseId
    ) {
        try {
            log.info("Creating a new CCD amended petition (for DN refusal) for Case id: {}", caseId);
            Map<String, Object> newCase = petitionService.createAmendedPetitionDraftRefusalFromCaseId(jwt, caseId);

            if (newCase == null) {
                log.info("Amended case is null for Case id: {}", caseId);
                return ResponseEntity.notFound().build();
            }
            log.info("Amended case returned successfully for Case id: {}", caseId);
            return ResponseEntity.ok(newCase);
        } catch (DuplicateCaseException e) {
            log.warn(e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.MULTIPLE_CHOICES).build();
        }
    }

    @PutMapping(path = "/drafts", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Saves or updates a draft to draft store")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Draft saved")})
    public ResponseEntity<Void> saveDraft(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt,
        @RequestBody
        @Parameter(description = "The case draft", required = true)
        @NotNull final Map<String, Object> data,
        @RequestParam(value = "divorceFormat", required = false)
        @Parameter(description = "Boolean flag indicting the data is in divorce format") final Boolean divorceFormat) {
        log.debug("Received request to save a draft");
        petitionService.saveDraft(jwt, data, Optional.ofNullable(divorceFormat).orElse(false));
        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/drafts", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Create a new draft in draft store")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Draft saved")})
    public ResponseEntity<Void> createDraft(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt,
        @RequestBody
        @Parameter(description = "The  case draft", required = true)
        @NotNull final Map<String, Object> data,
        @RequestParam(value = "divorceFormat", required = false)
        @Parameter(description = "Boolean flag indicting the data is in divorce format") final Boolean divorceFormat) {
        log.debug("Received request to create a draft");
        petitionService.createDraft(jwt, data, Optional.ofNullable(divorceFormat).orElse(false));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/drafts")
    @Operation(description = "Deletes a divorce case draft")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "The divorce draft has been deleted successfully")})
    public ResponseEntity<Void> deleteDraft(@RequestHeader("Authorization")
                                            @Parameter(description = "JWT authorisation token issued by IDAM",
                                                required = true) final String jwt) {
        log.debug("Received request to delete a divorce session draft");
        petitionService.deleteDraft(jwt);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/drafts", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Retrieve All the Drafts for a given user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returns all the saved drafts for a given user")})
    public ResponseEntity<DraftList> retrieveAllDrafts(@RequestHeader(HttpHeaders.AUTHORIZATION)
                                                       @Parameter(description
                                                           = "JWT authorisation token issued by IDAM",
                                                               required = true)final String jwt) {
        return ResponseEntity.ok(petitionService.getAllDrafts(jwt));
    }

    @GetMapping(path = "/case/{caseId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(description = "Retrieve CCD case by CaseId")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returns case with CaseId"),
        @ApiResponse(responseCode = "404", description = "Returns case not found or not authorised to view")
        })
    public ResponseEntity<CaseDetails> retrieveCaseById(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt,
        @PathVariable("caseId")
        @Parameter(description = "Unique identifier of the session that was submitted to CCD") String caseId
    ) {
        CaseDetails retrievedCase = Optional.ofNullable(
            petitionService.retrievePetitionByCaseId(jwt, caseId)
        ).orElse(CaseDetails.builder().build());

        return retrievedCase.getId() == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(retrievedCase);
    }
}
