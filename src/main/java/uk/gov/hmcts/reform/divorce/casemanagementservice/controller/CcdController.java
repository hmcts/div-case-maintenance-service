package uk.gov.hmcts.reform.divorce.casemanagementservice.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemanagementservice.service.CcdSubmissionService;
import uk.gov.hmcts.reform.divorce.casemanagementservice.service.CcdUpdateService;

import javax.ws.rs.core.MediaType;

@RestController
@RequestMapping(path = "casemanagement")
@Api(value = "Case Management Services", consumes = "application/json", produces = "application/json")
public class CcdController {
    @Autowired
    private CcdSubmissionService ccdSubmissionService;

    @Autowired
    private CcdUpdateService ccdUpdateService;

    @PostMapping(path = "/version/1/submit", consumes = MediaType.APPLICATION_JSON,
        produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Submits a divorce session to CCD")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Case Data was submitted to CCD. The body payload returns the complete "
            + "case back", response = CaseDetails.class),
        }
    )
    public ResponseEntity<CaseDetails> submitCase(
        @RequestBody @ApiParam(value = "Case Data", required = true) Object data,
        @RequestHeader("Authorization")
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String jwt) {
        return ResponseEntity.ok(ccdSubmissionService.submitCase(data, jwt));
    }

    @PostMapping(path = "/version/1/updateCase/{caseId}/{eventId}", consumes = MediaType.APPLICATION_JSON,
        produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates case details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A request to update the case details was sent to CCD. The body payload "
            + "will return the latest version of the case after the update.", response = CaseDetails.class),
        }
    )
    public ResponseEntity<CaseDetails> updateCase(
        @RequestBody
        @PathVariable("caseId") @ApiParam("Unique identifier of the session that was submitted to CCD") String caseId,
        @PathVariable("eventId") @ApiParam(value = "Update Event Type Id", required = true) String eventId,
        @ApiParam("The update event that requires the resubmission to CCD") Object data,
        @RequestHeader("Authorization")
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String jwt) {
        return ResponseEntity.ok(ccdUpdateService.update(caseId, data, eventId, jwt));
    }
}
