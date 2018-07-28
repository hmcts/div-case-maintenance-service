package uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.DraftStoreClient;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.domain.model.UserDetails;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.factory.DraftModelFactory;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.factory.EncryptionKeyFactory;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.Draft;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.draftstore.model.DraftList;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.DraftService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.service.IdamUserService;
import uk.gov.hmcts.reform.divorce.casemaintenanceservice.util.AuthUtil;

import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class DraftServiceImpl implements DraftService {

    @Autowired
    private AuthTokenGenerator serviceTokenGenerator;

    @Autowired
    private DraftStoreClient draftStoreClient;

    @Autowired
    private IdamUserService idamUserService;

    @Autowired
    private EncryptionKeyFactory encryptionKeyFactory;

    @Autowired
    private DraftModelFactory modelFactory;

    @Override
    public DraftList getAllDrafts(String userToken) {
        return draftStoreClient.getAllDrafts(getBearerToken(userToken), serviceTokenGenerator.generate(),
            getSecret(userToken));
    }

    private DraftList getAllDrafts(String userToken, String after) {
        return draftStoreClient.getAllDrafts(after, getBearerToken(userToken), serviceTokenGenerator.generate(),
            getSecret(userToken));
    }

    @Override
    public void saveDraft(String userToken, Map<String, Object> data) {
        Draft draft = getDraft(userToken);

        if (draft == null) {
            log.debug("Creating a new divorce session draft");
            draftStoreClient.createSingleDraft(
                modelFactory.createDraft(data),
                getBearerToken(userToken),
                serviceTokenGenerator.generate(),
                getSecret(userToken));
        } else {
            log.debug("Updating the existing divorce session draft");
            draftStoreClient.updateSingleDraft(
                draft.getId(),
                modelFactory.updateDraft(data),
                getBearerToken(userToken),
                serviceTokenGenerator.generate(),
                getSecret(userToken));
        }
    }

    @Override
    public void deleteDraft(String authorisation) {
        log.debug("Deleting the divorce session draft");
        Draft draft = getDraft(authorisation);

        if (draft != null) {
            draftStoreClient.deleteSingleDraft(draft.getId(), getBearerToken(authorisation),
                serviceTokenGenerator.generate());
        }
    }

    @Override
    public Draft getDraft(String userToken) {
        DraftList draftList = getAllDrafts(userToken);

        return findDivorceDraft(userToken, draftList).orElse(null);
    }

    private Optional<Draft> findDivorceDraft(String userToken, DraftList draftList) {
        if (draftList != null && !draftList.getData().isEmpty()) {
            Optional<Draft> divorceDraft = draftList.getData().stream()
                .filter(draft -> modelFactory.isDivorceDraft(draft))
                .findFirst();

            if (divorceDraft.isPresent()) {
                log.debug("Divorce session draft found");
                return divorceDraft;
            } else {
                if (draftList.getPaging().getAfter() != null) {
                    log.debug("Divorce session draft could not be found on the current page with drafts. "
                        + "Going to next page");
                    return findDivorceDraft(userToken, getAllDrafts(userToken, draftList.getPaging().getAfter()));
                }
            }
        }
        log.debug("Divorce session draft could not be found");
        return Optional.empty();
    }

    private String getSecret(String userToken) {
        UserDetails userDetails = idamUserService.retrieveUserDetails(getBearerToken(userToken));

        return encryptionKeyFactory.createEncryptionKey(userDetails.getId());
    }

    private String getBearerToken(String userToken) {
        return AuthUtil.getBearToken(userToken);
    }
}
