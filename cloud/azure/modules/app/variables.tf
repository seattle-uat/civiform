variable "application_name" {
  type        = string
  description = "Azure Web App Name"
}

variable "aws_region" {
  type        = string
  description = "Region for the aws account, if using"
  default     = "us-east-1"
}

variable "docker_username" {
  type        = string
  description = "Docker username"
}

variable "docker_repository_name" {
  type        = string
  description = "Name of container image"
}

variable "image_tag_name" {
  type        = string
  description = "Tag for container image"
  default     = "latest"
}

variable "location_name" {
  type        = string
  description = "Name of the location for the resource group"
  default     = "eastus"
}

variable "vnet_address_space" {
  type        = list(string)
  description = "List of vnet address_spaces"
  default = [
    "10.0.0.0/16"
  ]
}

variable "subnet_address_prefixes" {
  type        = list(string)
  description = "List of the apps subnet address prefixes (must be distinct from the postgress subnet)"
  default = [
    "10.0.2.0/24"
  ]
}

variable "bastion_address_prefixes" {
  type        = list(string)
  description = "Prefixes for the bastion instance (must be distinct from other subnets)"
  default = [
    "10.0.6.0/24"
  ]
}

variable "app_sku" {
  type        = map(string)
  description = "SKU tier/size/capacity information"
  default = {
    tier     = "Standard",
    size     = "S1",
    capacity = "2"
  }
}

variable "resource_group_name" {
  type    = string
  default = "civiform-resourcegroup"
}

variable "postgres_admin_login" {
  type        = string
  description = "Postgres admin login"
}

variable "postgres_sku_name" {
  type        = string
  description = "The sku name for postgres server"
  default     = "GP_Gen5_2"
}
variable "postgres_storage_mb" {
  type        = number
  description = "The mb of storage for postgres instance"
  default     = 5120
}

variable "postgres_backup_retention_days" {
  type        = number
  description = "Number of days to retain postgres backup"
  default     = 7
}

variable "postgres_subnet_address_prefixes" {
  type        = list(string)
  description = "A list of the subnet address prefixes for postgres (must be distinct from the app's subnet addresses)"
  default = [
    "10.0.4.0/24"
  ]
}

variable "log_sku" {
  type        = string
  description = "The SKU for the sever logs"
  default     = "PerGB2018"
}

variable "log_retention" {
  type        = number
  description = "The number of days the logs will be retained for"
  default     = 30
}

variable "aws_access_key_id" {
  type        = string
  description = "The access key you need to send things via "
}

variable "ses_sender_email" {
  type        = string
  description = "Email address of who is sending the email, passed to the app"
}

variable "staging_hostname" {
  type        = string
  description = "hostname for staging (used by the code to allow dev features)"
  default     = "staging-azure.civiform.dev"
}

variable "custom_hostname" {
  type        = string
  description = "custom hostname for the app to map the dns (used also for CORS)"
}
variable "key_vault_name" {
  type        = string
  description = "Name of key vault where secrets are stored."
}

variable "key_vault_resource_group" {
  type = string
}
variable "adfs_client_id" {
  type        = string
  description = "Active Directory Federation Service client id"
}

variable "adfs_discovery_uri" {
  type        = string
  description = "Active Directory Federation Service url that handles adfs login"
}

variable "adfs_admin_group" {
  type        = string
  description = "Active Directory Federation Service group name"
}

variable "staging_program_admin_notification_mailing_list" {
  type        = string
  description = "Admin notification mailing list for staging"
  default     = ""
}

variable "staging_ti_notification_mailing_list" {
  type        = string
  description = "Intermediarry notification mailing list for staging"
  default     = ""
}

variable "staging_applicant_notification_mailing_list" {
  type        = string
  description = "Applicant notification mailing list for staging"
  default     = ""
}
