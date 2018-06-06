package uk.gov.hmcts.reform.divorce.casemanagementservice.controller;

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
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.ccd.client.model.CaseDataContent;
import uk.gov.hmcts.reform.ccd.client.model.CaseDetails;
import uk.gov.hmcts.reform.divorce.casemanagementservice.service.CcdSubmissionService;
import uk.gov.hmcts.reform.divorce.casemanagementservice.service.CcdUpdateService;

import javax.ws.rs.core.MediaType;

@RestController
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
    })
    public ResponseEntity<CaseDetails> submitCase(
        @RequestBody @ApiParam(value = "Case Date", required = true) CaseDataContent caseDataContent,
        @RequestHeader("Authorization")
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String jwt) {
        return ResponseEntity.ok(ccdSubmissionService.submitCase(caseDataContent, jwt));
    }

    @PostMapping(path = "/version/1/updateCase/{caseId}", consumes = MediaType.APPLICATION_JSON,
        produces = MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Updates case details")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "A request to update the case details was sent to CCD. The body payload "
            + "will return the latest version of the case after the update.", response = CaseDetails.class),
    })
    public ResponseEntity<CaseDetails> updateCase(
        @RequestBody
        @PathVariable("caseId") @ApiParam("Unique identifier of the session that was submitted to CCD") String caseId,
        @ApiParam("The update event that requires the resubmission to CCD") CaseDataContent caseDataContent,
        @RequestHeader("Authorization")
        @ApiParam(value = "JWT authorisation token issued by IDAM", required = true) final String jwt) {
        return ResponseEntity.ok(ccdUpdateService.update(caseId, caseDataContent, jwt));
    }
}
