variable "raw_product" {
  default = "div"
}

variable "env" {
  type = "string"
}

variable "idam_api_baseurl" {
  type = "string"
}

variable "capacity" {
  default = "1"
}

variable "vault_env" {}

variable "documentation_swagger_enabled" {
  default = "false"
}
