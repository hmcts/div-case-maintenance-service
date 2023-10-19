terraform {
    backend "azurerm" {}

    required_providers {
        azurerm = {
            source  = "hashicorp/azurerm"
            version = "3.76.0"
        }
        random = {
            source = "hashicorp/random"
        }
    }
}
