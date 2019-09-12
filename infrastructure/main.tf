provider "azurerm" {
  version = "1.19.0"
}

locals {
  aseName   = "core-compute-${var.env}"
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"

  idam_s2s_url              = "http://rpe-service-auth-provider-${local.local_env}.service.core-compute-${local.local_env}.internal"
  ccd_casedatastore_baseurl = "http://ccd-data-store-api-${local.local_env}.service.core-compute-${local.local_env}.internal"
  case_formatter_baseurl    = "http://div-cfs-${local.local_env}.service.core-compute-${local.local_env}.internal"
  draft_store_api_baseurl   = "http://draft-store-service-${local.local_env}.service.core-compute-${local.local_env}.internal"
  petitioner_fe_baseurl     = "https://div-pfe-${local.local_env}.service.core-compute-${local.local_env}.internal"

  previewVaultName    = "${var.raw_product}-aat"
  nonPreviewVaultName = "${var.raw_product}-${var.env}"
  vaultName           = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"
  asp_name            = "${var.env == "prod" ? "div-cms-prod" : "${var.raw_product}-${var.env}"}"
  asp_rg              = "${var.env == "prod" ? "div-cms-prod" : "${var.raw_product}-${var.env}"}"
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = "${var.location}"

  tags = "${var.common_tags}"
}

resource "azurerm_application_insights" "appinsights" {
  name                = "${var.product}-${var.component}-appinsights-${var.env}"
  location            = "${var.appinsights_location}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  application_type    = "Web"

  tags = "${var.common_tags}"
}

module "div-cms" {
  source                          = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  enable_ase                      = "${var.enable_ase}"
  resource_group_name             = "${azurerm_resource_group.rg.name}"
  product                         = "${var.product}-${var.component}"
  location                        = "${var.location}"
  env                             = "${var.env}"
  ilbIp                           = "${var.ilbIp}"
  subscription                    = "${var.subscription}"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  is_frontend                     = false
  capacity                        = "${var.capacity}"
  common_tags                     = "${var.common_tags}"
  asp_name                        = "${local.asp_name}"
  asp_rg                          = "${local.asp_rg}"
  instance_size                   = "${var.instance_size}"

  app_settings = {
    REFORM_SERVICE_NAME                                   = "${var.component}"
    REFORM_TEAM                                           = "${var.product}"
    REFORM_ENVIRONMENT                                    = "${var.env}"
    AUTH_PROVIDER_SERVICE_CLIENT_BASEURL                  = "${local.idam_s2s_url}"
    AUTH_PROVIDER_SERVICE_CLIENT_MICROSERVICE             = "${var.auth_provider_service_client_microservice}"
    AUTH_PROVIDER_SERVICE_CLIENT_KEY                      = "${data.azurerm_key_vault_secret.ccd-submission-s2s-auth-secret.value}"
    AUTH_PROVIDER_SERVICE_CLIENT_TOKENTIMETOLIVEINSECONDS = "${var.auth_provider_service_client_tokentimetoliveinseconds}"
    CASE_DATA_STORE_BASEURL                               = "${local.ccd_casedatastore_baseurl}"
    IDAM_API_BASEURL                                      = "${var.idam_api_baseurl}"
    CASE_FORMATTER_SERVICE_API_BASEURL                    = "${local.case_formatter_baseurl}"
    DRAFT_STORE_API_BASEURL                               = "${local.draft_store_api_baseurl}"
    DRAFT_STORE_API_ENCRYPTION_KEY_VALUE                  = "${data.azurerm_key_vault_secret.draft-store-api-encryption-key.value}"
    AUTH2_CLIENT_SECRET                                   = "${data.azurerm_key_vault_secret.idam-secret.value}"
    IDAM_CASEWORKER_USERNAME                              = "${data.azurerm_key_vault_secret.idam-caseworker-username.value}"
    IDAM_CASEWORKER_PASSWORD                              = "${data.azurerm_key_vault_secret.idam-caseworker-password.value}"
    IDAM_API_REDIRECT_URL                                 = "${local.petitioner_fe_baseurl}/authenticated"
    MANAGEMENT_ENDPOINT_HEALTH_CACHE_TIMETOLIVE           = "${var.health_check_ttl}"
    DOCUMENTATION_SWAGGER_ENABLED                         = "${var.documentation_swagger_enabled}"
  }
}

data "azurerm_key_vault" "div_key_vault" {
  name                = "${local.vaultName}"
  resource_group_name = "${local.vaultName}"
}

data "azurerm_key_vault_secret" "ccd-submission-s2s-auth-secret" {
  name      = "ccd-submission-s2s-auth-secret"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "draft-store-api-encryption-key" {
  name      = "draft-store-api-encryption-key"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam-secret" {
  name      = "idam-secret"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam-caseworker-username" {
  name      = "idam-caseworker-username"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

data "azurerm_key_vault_secret" "idam-caseworker-password" {
  name      = "idam-caseworker-password"
  vault_uri = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}
