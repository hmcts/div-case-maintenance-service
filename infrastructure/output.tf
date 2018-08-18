output "vaultUri" {
    value = "${data.azurerm_key_vault.div_key_vault.vault_uri}"
}

output "vaultName" {
    value = "${local.vaultName}"
}

output "auth_idam_client_baseUrl" {
    value = "${var.idam_api_baseurl}"
}
