locals {
    ase_name                  = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
    local_env                 = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"

    idam_s2s_url              = "http://rpe-service-auth-provider-${local.local_env}.service.core-compute-${local.local_env}.internal"
    ccd_casedatastore_baseurl = "http://ccd-data-store-api-${local.local_env}.service.core-compute-${local.local_env}.internal"
    case_formatter_baseurl    = "http://div-cfs-${local.local_env}.service.core-compute-${local.local_env}.internal"
    draft_store_api_baseurl   = "http://draft-store-service-${local.local_env}.service.core-compute-${local.local_env}.internal"
    petitioner_fe_baseurl     = "http://div-pfe-${local.local_env}.service.core-compute-${local.local_env}.internal"

    previewVaultName          = "${var.raw_product}-aat"
    nonPreviewVaultName       = "${var.raw_product}-${var.env}"
    vaultName                 = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"
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
        AUTH_PROVIDER_SERVICE_CLIENT_KEY                      = "${data.azurerm_key_vault_secret.ccd_submission_s2s_auth_secret.value}"
        AUTH_PROVIDER_SERVICE_CLIENT_TOKENTIMETOLIVEINSECONDS = "${var.auth_provider_service_client_tokentimetoliveinseconds}"
        CASE_DATA_STORE_BASEURL                               = "${local.ccd_casedatastore_baseurl}"
        IDAM_API_BASEURL                                      = "${var.idam_api_baseurl}"
        CASE_FORMATTER_SERVICE_API_BASEURL                    = "${local.case_formatter_baseurl}"
        DRAFT_STORE_API_BASEURL                               = "${local.draft_store_api_baseurl}"
        DRAFT_STORE_API_ENCRYPTION_KEY                        = "${data.azurerm_key_vault_secret.draft_store_api_encryption_key.value}"
        AUTH2_CLIENT_SECRET                                   = "${data.azurerm_key_vault_secret.idam_secret.value}"
        IDAM_CASEWORKER_USERNAME                              = "${data.azurerm_key_vault_secret.idam_caseworker_username.value}"
        IDAM_CASEWORKER_PASSWORD                              = "${data.azurerm_key_vault_secret.idam_caseworker_password.value}"
        IDAM_API_REDIRECT_URL                                 = "${local.petitioner_fe_baseurl}/authenticated"
    }
}

data "azurerm_key_vault" "div_key_vault" {
    name = "${local.vaultName}"
    resource_group_name = "${local.vaultName}"
}

data "azurerm_key_vault_secret" "ccd_submission_s2s_auth_secret" {
    name = "ccd-submission-s2s-auth-secret"
    vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "draft_store_api_encryption_key" {
    name = "draft-store-api-encryption-key"
    vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam_secret" {
    name = "idam-secret"
    vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam_caseworker_username" {
    name = "idam-caseworker-username"
    vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam_caseworker_password" {
    name = "idam-caseworker-password"
    vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}
