variable "product" {
    type = "string"
}

variable "component" {
  type = "string"
}

variable "raw_product" {
    default = "div"
}

variable "env" {
  type = "string"
}

variable "client_id" {
  description = "(Required) The object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies. This is usually sourced from environment variables and not normally required to be specified."
}

variable "tenant_id" {}

variable "jenkins_AAD_objectId" {
  type        = "string"
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "appinsights_instrumentation_key" {
    description = "Instrumentation key of the App Insights instance this webapp should use. Module will create own App Insights resource if this is not provided"
    default = ""
}

variable "idam_api_baseurl" {
    type = "string"
}

variable "capacity" {
    default = "1"
}

variable "auth_provider_service_client_microservice" {
  default = "divorce_ccd_submission"
}

variable "auth_provider_service_client_tokentimetoliveinseconds" {
  default = "900"
}

variable "subscription" {}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "ilbIp" {}

variable "vault_env" {}

variable "common_tags" {
    type = "map"
}
