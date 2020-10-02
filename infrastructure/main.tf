provider "azurerm" {
    features {}
}

locals {
  aseName   = "core-compute-${var.env}"
  local_env = (var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env

  idam_s2s_url              = "http://rpe-service-auth-provider-${local.local_env}.service.core-compute-${local.local_env}.internal"
  ccd_casedatastore_baseurl = "http://ccd-data-store-api-${local.local_env}.service.core-compute-${local.local_env}.internal"
  case_formatter_baseurl    = "http://div-cfs-${local.local_env}.service.core-compute-${local.local_env}.internal"
  draft_store_api_baseurl   = "http://draft-store-service-${local.local_env}.service.core-compute-${local.local_env}.internal"
  petitioner_fe_baseurl     = "https://div-pfe-${local.local_env}.service.core-compute-${local.local_env}.internal"

  previewVaultName    = "${var.raw_product}-aat"
  nonPreviewVaultName = "${var.raw_product}-${var.env}"
  vaultName           = (var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName
  asp_name            = var.env == "prod" ? "div-cms-prod" : "${var.raw_product}-${var.env}"
  asp_rg              = var.env == "prod" ? "div-cms-prod" : "${var.raw_product}-${var.env}"
}

data "azurerm_key_vault" "div_key_vault" {
  name                = local.vaultName
  resource_group_name = local.vaultName
}

data "azurerm_key_vault_secret" "ccd-submission-s2s-auth-secret" {
  name      = "ccd-submission-s2s-auth-secret"
  key_vault_id = data.azurerm_key_vault.div_key_vault.id

}

data "azurerm_key_vault_secret" "draft-store-api-encryption-key" {
  name      = "draft-store-api-encryption-key"
  key_vault_id = data.azurerm_key_vault.div_key_vault.id
}

data "azurerm_key_vault_secret" "idam-secret" {
  name      = "idam-secret"
  key_vault_id = data.azurerm_key_vault.div_key_vault.id
}

data "azurerm_key_vault_secret" "idam-caseworker-username" {
  name      = "idam-caseworker-username"
  key_vault_id = data.azurerm_key_vault.div_key_vault.id
}

data "azurerm_key_vault_secret" "idam-caseworker-password" {
  name      = "idam-caseworker-password"
  key_vault_id = data.azurerm_key_vault.div_key_vault.id
}
