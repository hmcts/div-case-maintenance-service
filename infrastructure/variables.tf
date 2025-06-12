variable "raw_product" {
  default = "div"
}

variable "env" {}

variable "product" {}

variable "idam_api_baseurl" {}

variable "capacity" {
  default = "1"
}

variable "instance_size" {
    default = "I2"
}

variable "health_check_ttl" {
    default = "4000"
}

variable "vault_env" {}

variable "common_tags" {
    type = map(string)
}

variable "documentation_swagger_enabled" {
  default = "false"
}
