variable "raw_product" {
  default = "div"
}

variable "env" {}

variable "product" {}

variable "idam_api_baseurl" {}

variable "capacity" {
  default = "1"
}

variable "vault_env" {}

variable "common_tags" {
    type = map(string)
}

variable "documentation_swagger_enabled" {
  default = "false"
}
