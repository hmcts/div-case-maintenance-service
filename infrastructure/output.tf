output "vaultName" {
    value = "${module.key-vault.key_vault_name}"
}

output "vaultUri" {
    value = "${module.key-vault.key_vault_uri}"
}

output "idam_s2s_url" {
    value = "http://${var.idam_s2s_url_prefix}-${local.local_env}.service.core-compute-${local.local_env}.internal"
}

output "test_environment" {
    value = "${local.local_env}"
}
