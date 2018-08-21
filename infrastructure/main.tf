locals {
    ase_name                  = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
    local_env                 = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"

    idam_s2s_url              = "http://rpe-service-auth-provider-${local.local_env}.service.core-compute-${local.local_env}.internal"
    ccd_casedatastore_baseurl = "http://ccd-data-store-api-${local.local_env}.service.core-compute-${local.local_env}.internal"
    case_formatter_baseurl    = "http://div-cfs-${local.local_env}.service.core-compute-${local.local_env}.internal"
    draft_store_api_baseurl   = "http://draft-store-service-${local.local_env}.service.core-compute-${local.local_env}.internal"
}

module "div-cms" {
    source                          = "git@github.com:hmcts/moj-module-webapp.git?ref=master"
    product                         = "${var.product}-${var.component}"
    location                        = "${var.location}"
    env                             = "${var.env}"
    ilbIp                           = "${var.ilbIp}"
    subscription                    = "${var.subscription}"
    appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
    is_frontend                     = false
    capacity                        = "${var.capacity}"
    common_tags                     = "${var.common_tags}"

    app_settings = {
        REFORM_SERVICE_NAME                                   = "${var.component}"
        REFORM_TEAM                                           = "${var.product}"
        REFORM_ENVIRONMENT                                    = "${var.env}"
        AUTH_PROVIDER_SERVICE_CLIENT_BASEURL                  = "${local.idam_s2s_url}"
        AUTH_PROVIDER_SERVICE_CLIENT_MICROSERVICE             = "${var.auth_provider_service_client_microservice}"
        AUTH_PROVIDER_SERVICE_CLIENT_KEY                      = "${data.vault_generic_secret.ccd-submission-s2s-auth-secret.data["value"]}"
        AUTH_PROVIDER_SERVICE_CLIENT_TOKENTIMETOLIVEINSECONDS = "${var.auth_provider_service_client_tokentimetoliveinseconds}"
        CASE_DATA_STORE_BASEURL                               = "${local.ccd_casedatastore_baseurl}"
        IDAM_API_BASEURL                                      = "${var.idam_api_baseurl}"
        CASE_FORMATTER_SERVICE_API_BASEURL                    = "${local.case_formatter_baseurl}"
        DRAFT_STORE_API_BASEURL                               = "${local.draft_store_api_baseurl}"
        DRAFT_STORE_API_ENCRYPTION_KEY                        = "${data.vault_generic_secret.draft-store-api-encryption-key.data["value"]}"
    }
}

provider "vault" {
    address = "https://vault.reform.hmcts.net:6200"
}

data "vault_generic_secret" "ccd-submission-s2s-auth-secret" {
    path = "secret/${var.vault_env}/ccidam/service-auth-provider/api/microservice-keys/divorceCcdSubmission"
}

data "vault_generic_secret" "draft-store-api-encryption-key" {
    path = "secret/${var.vault_env}/divorce/draft/encryption_key"
}
