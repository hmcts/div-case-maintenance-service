package uk.gov.hmcts.reform.divorce.casemaintenanceservice.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

@RestController
@RequestMapping(path = "casemaintenance/version/1")
@Api(value = "Case Maintenance Services", consumes = "application/json", produces = "application/json")
public class CcdController {
    @Autowired
    private CcdSubmissionService ccdSubmissionService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @Autowired
    private CcdAccessService ccdAccessService;

    @Autowired
    private CcdRetrievalService ccdRetrievalService;

    @PostMapping(path = "/submit", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Submits a divorce session to CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case Data was submitted to CCD. The body payload returns the complete "
            + "case back", response = CaseDetails.class)
        }
    )
    public ResponseEntity<CaseDetails> submitCase(
        @RequestBody @ApiParam(value = "Case Data", required = true) Map<String, Object> data,
        @RequestHeader("Authorization")
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String jwt) {
        return ResponseEntity.ok(ccdSubmissionService.submitCase(data, jwt));
    }

    @PostMapping(path = "/updateCase/{caseId}/{eventId}", consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Updates case details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A request to update the case details was sent to CCD. The body payload "
            + "will return the latest version of the case after the update.", response = CaseDetails.class)
        }
    )
    public ResponseEntity<CaseDetails> updateCase(
        @PathVariable("caseId") @ApiParam("Unique identifier of the session that was submitted to CCD") String caseId,
        @RequestBody
        @ApiParam(value = "The update event that requires the resubmission to CCD", required = true) Object data,
        @PathVariable("eventId") @ApiParam(value = "Update Event Type Id", required = true) String eventId,
        @RequestHeader("Authorization")
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String jwt) {
        return ResponseEntity.ok(ccdUpdateService.update(caseId, data, eventId, jwt));
    }

    @PostMapping(path = "/link-respondent/{caseId}/{letterHolderId}")
    @ApiOperation(value = "Updates case details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returned when case with id and letter holder id exists and the access is granted to the respondent user"),
        @ApiResponse(code = 404, message = "Returned when case with id not found"),
        @ApiResponse(code = 400, message = "Returned when data is missing from request or case"),
        @ApiResponse(code = 401, message = "Returned when case letter holder ID does not match corresponding case data or case is already linked"),
        }
    )
    public ResponseEntity<Void> linkRespondent(
        @RequestHeader("Authorization")
        @ApiParam(value = "JWT authorisation token of the respondent", required = true) final String authToken,
        @PathVariable("caseId") @ApiParam("Unique identifier of the session that was submitted to CCD") String caseId,
        @PathVariable("letterHolderId")
        @ApiParam(value = "Letter holder id from the pin user", required = true) String letterHolderId) {

        ccdAccessService.linkRespondent(authToken, caseId, letterHolderId);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping(path = "/link-respondent/{caseId}")
    @ApiOperation(value = "Removes user permission on a case")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returned when case with id exists and the access is "
            + "removed to the respondent user"),
        @ApiResponse(code = 404, message = "Returned when case with id not found"),
        }
    )
    public ResponseEntity<Void> unlinkRespondent(
        @RequestHeader("Authorization")
        @ApiParam(value = "JWT authorisation token of the respondent", required = true) final String authToken,
        @PathVariable("caseId") @ApiParam("Unique identifier of the session that was submitted to CCD") String caseId) {

        ccdAccessService.unlinkRespondent(authToken, caseId);

        return ResponseEntity.ok().build();
    }

    @PostMapping(path = "/search", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Retrieve CCD case by CaseId")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Returns list of cases based on search criteria"),
        @ApiResponse(code = 404, message = "Returns case not found or not authorised to view")
        })
    public ResponseEntity<SearchResult> search(
        @RequestHeader(HttpHeaders.AUTHORIZATION)
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String jwt,
        @RequestBody @ApiParam(value = "query", required = true) String query
    ) {
        return ResponseEntity.ok(ccdRetrievalService.searchCase(jwt, query));
    }
}
