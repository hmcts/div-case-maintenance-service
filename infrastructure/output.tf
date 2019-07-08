output "vaultUri" {
    value = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

output "vaultName" {
    value = "${local.vaultName}"
}

output "test_environment" {
    value = "${local.local_env}"
}

output "idam_api_baseurl" {
    value = "${var.idam_api_baseurl}"
}

output "idam_s2s_url" {
    value = "${local.idam_s2s_url}"
}

output "auth_idam_client_redirect-url" {
    value = "${local.petitioner_fe_baseurl}/authenticated"
}

output "ccd_casedatastore_baseurl" {
    value = "${local.ccd_casedatastore_baseurl}"
}

output "documentation_swagger_enabled" {
    value = "${var.documentation_swagger_enabled}"
}
