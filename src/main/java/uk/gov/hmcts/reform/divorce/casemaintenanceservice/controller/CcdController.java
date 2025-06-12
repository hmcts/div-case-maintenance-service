package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.ccd.client.model.SearchResult;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdAccessService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdRetrievalService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdSubmissionService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.CcdUpdateService;

import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(path = "casemaintenance/version/1")
public class CcdController {
    @Autowired
    private CcdSubmissionService ccdSubmissionService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private CcdRetrievalService ccdRetrievalService;


    @PostMapping(path = "/submit", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Submits a divorce session to CCD")
    @ApiResponses(value = @ApiResponse(responseCode = "200",
        description =
            "Case Data was submitted to CCD. The body payload returns the complete case back",
        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CaseDetails.class))})

    )
    public ResponseEntity<CaseDetails> submitCase(
        @RequestBody @Parameter(description = "Case Data", required = true) Map<String, Object> data,
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt) {
        return ResponseEntity.ok(ccdSubmissionService.submitCase(data, jwt));
    }

    @PostMapping(path = "/solicitor-submit", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Submits given case data to CCD")
    @ApiResponses(value = @ApiResponse(responseCode = "200",
        description = "Case Data was submitted to CCD. The body payload returns the complete case back",
        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CaseDetails.class))})

    )
    public ResponseEntity<CaseDetails> submitCaseForSolicitor(
        @RequestBody @Parameter(description = "Case Data", required = true) Map<String, Object> data,
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt) {
        return ResponseEntity.ok(ccdSubmissionService.submitCaseForSolicitor(data, jwt));
    }

    @PostMapping(path = "/bulk/submit", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Parameter(description = "Submits a divorce session to CCD")
    @ApiResponses(value = @ApiResponse(responseCode = "200",
        description = "Case Data was submitted to CCD. The body payload returns the complete case back",
        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CaseDetails.class))})

    )
    public ResponseEntity<CaseDetails> submitBulkCase(
        @RequestBody @Parameter(description = "Bulk case data", required = true) Map<String, Object> data,
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt) {
        return ResponseEntity.ok(ccdSubmissionService.submitBulkCase(data, jwt));
    }

    @PostMapping(path = "/updateCase/{caseId}/{eventId}", consumes = APPLICATION_JSON_VALUE, produces
            = APPLICATION_JSON_VALUE)
    @Operation(description = "Updates case details")
    @ApiResponses(value = @ApiResponse(responseCode = "200",
        description = "A request to update the case details was sent to CCD. The body payload "
            + "will return the latest version of the case after the update.",
        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CaseDetails.class))})

    )
    public ResponseEntity<CaseDetails> updateCase(
        @PathVariable("caseId") @Parameter(description = "Unique identifier of the session that was submitted to CCD")
        String caseId,
        @RequestBody
        @Parameter(description = "The update event that requires the resubmission to CCD", required = true) Object data,
        @PathVariable("eventId") @Parameter(description = "Update Event Type Id", required = true) String eventId,
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt) {
        return ResponseEntity.ok(ccdUpdateService.update(caseId, data, eventId, jwt));
    }

    @PostMapping(path = "/bulk/updateCase/{caseId}/{eventId}", consumes = APPLICATION_JSON_VALUE, produces
            = APPLICATION_JSON_VALUE)
    @Operation(description = "Updates bulk case details")
    @ApiResponses(value = @ApiResponse(responseCode = "200",
        description = "A request to update the bulk case details was sent to CCD. The body payload "
            + "will return the latest version of the case after the update.",
        content = {@Content(mediaType = "application/json", schema = @Schema(implementation = CaseDetails.class))})

    )
    public ResponseEntity<CaseDetails> updateBulkCase(
        @PathVariable("caseId") @Parameter(description = "Unique identifier of the bulk case that was submitted to CCD")
        String caseId,
        @RequestBody
        @Parameter(description = "The update event that requires the resubmission to CCD", required = true) Object data,
        @PathVariable("eventId") @Parameter(description = "Update Event Type Id", required = true) String eventId,
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt) {
        return ResponseEntity.ok(ccdUpdateService.updateBulkCase(caseId, data, eventId, jwt));
    }

    @PostMapping(path = "/link-respondent/{caseId}/{letterHolderId}")
    @Operation(description = "Updates case details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returned when case with id and letter "
            + "holder id exists and the access is granted to the respondent user"),
        @ApiResponse(responseCode = "404", description = "Returned when case with id not found"),
        @ApiResponse(responseCode = "400", description = "Returned when data is missing from request or case"),
        @ApiResponse(responseCode = "401", description = "Returned when case letter holder ID does not match "
            + "corresponding case data or case is already linked"),
        }
    )
    public ResponseEntity<Void> linkRespondent(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token of the respondent", required = true) final String authToken,
        @PathVariable("caseId") @Parameter(description = "Unique identifier of the session that was submitted to CCD")
        String caseId,
        @PathVariable("letterHolderId")
        @Parameter(description = "Letter holder id from the pin user", required = true) String letterHolderId) {

        ccdAccessService.linkRespondent(authToken, caseId, letterHolderId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/link-respondent/{caseId}")
    @Operation(description = "Removes user permission on a case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returned when case with id exists and the access is removed"
                + " to the respondent user"),
        @ApiResponse(responseCode = "404", description = "Returned when case with id not found"),
        }
    )
    public ResponseEntity<Void> unlinkRespondent(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token of the respondent", required = true) final String authToken,
        @PathVariable("caseId") @Parameter(description = "Unique identifier of the session that was submitted to CCD")
        String caseId) {

        ccdAccessService.unlinkRespondent(authToken, caseId);

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/search", produces = APPLICATION_JSON_VALUE)
    @Operation(description = "Retrieve CCD case by CaseId")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Returns list of cases based on search criteria"),
        @ApiResponse(responseCode = "404", description = "Returns case not found or not authorised to view")
    })
    public ResponseEntity<SearchResult> search(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM", required = true) final String jwt,
        @RequestBody @Parameter(description = "query", required = true) String query
    ) {
        return ResponseEntity.ok(ccdRetrievalService.searchCase(jwt, query));
    }

    @PutMapping(path = "/add-petitioner-solicitor-role/{caseId}", produces = APPLICATION_JSON_VALUE)
    @Operation(description = "Assign the role of [PETSOLICITOR] for user and case")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role of [PETSOLICITOR] was added to the given case"),
        @ApiResponse(responseCode = "404", description = "Case not found with given ID")
    })
    public ResponseEntity<Void> addPetitionerSolicitorRole(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @Parameter(description = "JWT authorisation token issued by IDAM for solicitor user", required = true)
        final String jwt,
        @PathVariable @Parameter(description = "caseId", required = true) String caseId
    ) {
        ccdAccessService.addPetitionerSolicitorRole(jwt, caseId);
        return ResponseEntity.ok().build();
    }
}
