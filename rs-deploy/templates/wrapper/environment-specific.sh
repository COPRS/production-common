#!/bin/sh

# This file contains the environment specific configurations
# Configurations marked with an asterix (*) are mandatory or likely needs
# to be modified for your environment. The other options require best practise
# default values and usually not required to be modified.

#############################################
##            MISCELEANOUS
#############################################

# The kubernetes namespace that shall be used for the S1PRO services
export NAMESPACE_PROCESSING="processing";

## Credentials used by the microservices in order to fetch the docker images

# The name of the secret that will be generated
export DOCKER_REGISTRY_SECRET_NAME="harbor-tools"
# The URL of the docker registry
export DOCKER_REGISTRY_ADRESS="artifactory.coprs.esa-copernicus.eu"
# (*) The user used to login the registry
export DOCKER_REGISTRY_USER="xxx";              
# (*) The password used to login the registry
export DOCKER_REGISTRY_PASS="xxx";                         


## Credentials for the old S1PRO Gitlab instance. Currently used to pull in MongoDB
# NOTE: This will be obsolete once CS provided the Charts for the MIS cluster

# The name of the secret that will be generated
export GITLAB_DOCKER_REGISTRY_SECRET_NAME="gitlab"
# The URL of the docker registry
export GITLAB_DOCKER_REGISTRY_ADRESS="registry.tools.s1pdgs.eu"
# (*) The user used to login the registry
export GITLAB_DOCKER_REGISTRY_USER="xxx";
# (*) The password used to login the registry
export GITLAB_DOCKER_REGISTRY_PASS="xxx";

export HELM_REPO_NAME="werum"
#export HELM_REPO_URL="https://registry.tools.s1pdgs.eu/chartrepo/werum"
#export HELM_REPO_NAME="werum_harbor"
#export HELM_REPO_URL="https://registry.tools.s1pdgs.eu/chartrepo/werum"
#export HELM_REPO_USER="git_werum"; # Deactivated for security purposes
#export HELM_REPO_PASS="HucwP6tvGJckUsZi";

export OPENSTACK_SECRET_NAME="openstack";
export OPENSTACK_USER="NOTEXISTING"
export OPENSTACK_PASS="NOTEXISTING"
#export OPENSTACK_USER=$(    sudo cat /mnt/s1pdgs/k8s/cloud-config | grep 'username'        | head -n 1 | cut -d '=' -f 2 | sed 's/"//g' | sed 's/ //g');
#export OPENSTACK_PASS=$(    sudo cat /mnt/s1pdgs/k8s/cloud-config | grep 'password'        | head -n 1 | cut -d '=' -f 2 | sed 's/"//g' | sed 's/ //g');

export S3_SECRET_NAME="obs";
export S3_ACCESS_KEY=$(     cat ~/.s3cfg                | grep access_key                    | cut -d '=' -f 2 | sed 's/ //g');
export S3_SECRET_KEY=$(     cat ~/.s3cfg                | grep secret_key                    | cut -d '=' -f 2 | sed 's/ //g');

#export MONGODB_SECRET_NAME="mongodb";
#export MONGODB_ROOT_USER="root";
#export MONGODB_ROOT_PASS=$(kubectl get secret -n infra mongodb -o json | jq -r '.data."mongodb-root-password"' | base64 -d);
#export MONGODB_USER="s1pdgs";
#export MONGODB_PASS="scharbeutz9";

export AUXIP_SECRET_NAME="auxip";
# (*) User that shall be used to login to AUXIP
export AUXIP_USER="xxx";
# (*) Password for the user that shall be used to login to AUXIP
export AUXIP_PASS="xxx";
export AUXIP_OAUTHCLIENTID="XXXXX"
export AUXIP_OAUTHCLIENTSECRET="XXXXX"
export AUXIP_AUTH_METHOD="disable" 		# allowed values: basic oauth2 disable
export AUXIP_OAUTH_AUTH_URL="XXXXX"

export EDIP_PEDC_SECRET_NAME="edip-pedc";
# (*) User that shall be used to login to EDIP PEDC
export EDIP_PEDC_USER="xxx";
# (*) Password for the user that shall be used to login to EDIP PEDC
export EDIP_PEDC_PASS="xxx";

export EDIP_BEDC_SECRET_NAME="edip-bedc";
# (*) User that shall be used to login to EDIP BEDC
export EDIP_BEDC_USER="xxx";
# (*) Password for the user that shall be used to login to EDIP BEDC
export EDIP_BEDC_PASS="xxx";

#export QCSS_SECRET_NAME="qcss";
#export QCSS_USER="s1pdgs";
#export QCSS_PASS="PDG5!"

# used by werum internally

# (*) User that shall be used to login to XBIP
XBIP_USER="xbip_werum";
# (*) Password for the user that shall be used to login to XBIP
XBIP_PASSWORD="bWmMeluCz-1";

export XBIP_01_SECRET_NAME="xbip-cgs01";
export XBIP_02_SECRET_NAME="xbip-cgs02";
export XBIP_03_SECRET_NAME="xbip-cgs03";
export XBIP_04_SECRET_NAME="xbip-cgs04";
export XBIP_05_SECRET_NAME="xbip-cgs05";
export XBIP_10_SECRET_NAME="xbip-cgs10";
export XBIP_01_USER=${XBIP_USER};
export XBIP_01_PASS=${XBIP_PASSWORD};
export XBIP_02_USER=${XBIP_USER};
export XBIP_02_PASS=${XBIP_PASSWORD};
export XBIP_03_USER=${XBIP_USER};
export XBIP_03_PASS=${XBIP_PASSWORD};
export XBIP_04_USER=${XBIP_USER};
export XBIP_04_PASS=${XBIP_PASSWORD};
export XBIP_05_USER=${XBIP_USER};
export XBIP_05_PASS=${XBIP_PASSWORD};
export XBIP_10_USER=${XBIP_USER};
export XBIP_10_PASS=${XBIP_PASSWORD};

#export MYOCEAN_OUTBOX_1_HOSTNAME="s1pro-mock-dissemination-svc"
#export MYOCEAN_OUTBOX_1_PATH="public"
#export MYOCEAN_OUTBOX_1_PATH_EVAL="myocean"
#export MYOCEAN_OUTBOX_1_PROTOCOL="ftps"
#export MYOCEAN_OUTBOX_1_PORT="21"
#export MYOCEAN_OUTBOX_1_FTPPASV="true"
#export MYOCEAN_OUTBOX_1_USERNAME="s1pdgs"
#export MYOCEAN_OUTBOX_1_PASSWORD="PDG5!"
##export MYOCEAN_OUTBOX_1_KEYSTOREDATA=
#export MYOCEAN_OUTBOX_1_KEYSTOREPASS="#changeit"
#export MYOCEAN_OUTBOX_1_TRUSTSTOREDATA="/u3+7QAAAAIAAAABAAAAAgAIbXlvY2VhbjEAAAF3RQrFWwAFWC41MDkAAAPDMIIDvzCCAqegAwIBAgIUUbIDATMWy1AeVKX9P8A/0kaI5mEwDQYJKoZIhvcNAQELBQAwbzELMAkGA1UEBhMCREUxFTATBgNVBAgMDExvd2VyIFNheG9ueTESMBAGA1UEBwwJTHVlbmVidXJnMSEwHwYDVQQKDBhXZXJ1bSBTb2Z0d2FyZSAmIFN5c3RlbXMxEjAQBgNVBAMMCXMxcGRncy5ldTAeFw0yMDEyMDQxODQwMzhaFw0yMTEyMDQxODQwMzhaMG8xCzAJBgNVBAYTAkRFMRUwEwYDVQQIDAxMb3dlciBTYXhvbnkxEjAQBgNVBAcMCUx1ZW5lYnVyZzEhMB8GA1UECgwYV2VydW0gU29mdHdhcmUgJiBTeXN0ZW1zMRIwEAYDVQQDDAlzMXBkZ3MuZXUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDZGsBDpcjrwGCY74UsujgSZkcQsRjv9TSCHimOV/dlvbDl58iIhZUg93uwub+Ae0F77pfPClN5YDjbepkT6qWWJd06AAUDlbylGELrnqNANpIiVqK2YVrOUR2qBnVDronOrrajeTD5bBAtPpjZssc5v3swhdqSLSBKTz/5LykOSvD4IpxKkdCJBu/L+y01kUGDe3eXYbbf4T1tjo11t4wJunUkv0RV2nvnPkrdu/YtTN9X7kw2LRNaHR0rHudo68F8m3PylC9HtDetK/bZ2gO23Vy5ZLriKpDi4cycqdAIUy0mBwjd3O4nQ353bh7T5l2zuPTcvysbtp9u5SbTLLLPAgMBAAGjUzBRMB0GA1UdDgQWBBRn55CvXWTHfl4RIavXecPpcM3AsDAfBgNVHSMEGDAWgBRn55CvXWTHfl4RIavXecPpcM3AsDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQDYXwNm4CnLsBXRj995oVaRuCG87F4srET7CzCrZUEy9QJYTxreB7oQz8dsFeW9SRbb5PaWhlnDwXjTIq03GUN3TIDLRcW4uEzRL/9rWv0Nl5hzdVo6GTFJ1drUTtzjtmv5laTs4gxF36R0xxl+TeYSgo41T78Fv6NIYvcABw5zdZoydbZyt2WkbWiJx8fkX5jbn+mXK3ECprhRYb6tMPUxeMRB0ZZEDrG7A+7qyLF9fpvx6m2qF80CaXKekxf4im2R7UjzqS0+b5c02JJnBpNrUvrjAabNh0Fd3omKjEGRoB5loK6c8t3rS5sr6PCr27wPLcSNT45u0P3FnsEYGnOZ5wEFmzWTWqty+5TotBFojPkkuQQ="
#export MYOCEAN_OUTBOX_1_TRUSTSTOREPASS="changeit"

#export MYOCEAN_CLEANER_HOSTNAME="s1pro-mock-dissemination-svc"
#export MYOCEAN_CLEANER_PATH="public"
#export MYOCEAN_CLEANER_PROTOCOL="ftps"
#export MYOCEAN_CLEANER_PORT="21"
#export MYOCEAN_CLEANER_FTPPASV="true"
#export MYOCEAN_CLEANER_USERNAME="s1pdgs"
#export MYOCEAN_CLEANER_PASSWORD="PDG5!"
#export MYOCEAN_CLEANER_RETENTIONTIME_DAYS="7"
#export MYOCEAN_CLEANER_SCHEDULE='0 \\* \\* \\* \\*'
#export MYOCEAN_CLEANER_KEYSTOREDATA=
#export MYOCEAN_CLEANER_KEYSTOREPASS="changeit"
#export MYOCEAN_CLEANER_TRUSTSTOREDATA="/u3+7QAAAAIAAAABAAAAAgAIbXlvY2VhbjEAAAF3RQrFWwAFWC41MDkAAAPDMIIDvzCCAqegAwIBAgIUUbIDATMWy1AeVKX9P8A/0kaI5mEwDQYJKoZIhvcNAQELBQAwbzELMAkGA1UEBhMCREUxFTATBgNVBAgMDExvd2VyIFNheG9ueTESMBAGA1UEBwwJTHVlbmVidXJnMSEwHwYDVQQKDBhXZXJ1bSBTb2Z0d2FyZSAmIFN5c3RlbXMxEjAQBgNVBAMMCXMxcGRncy5ldTAeFw0yMDEyMDQxODQwMzhaFw0yMTEyMDQxODQwMzhaMG8xCzAJBgNVBAYTAkRFMRUwEwYDVQQIDAxMb3dlciBTYXhvbnkxEjAQBgNVBAcMCUx1ZW5lYnVyZzEhMB8GA1UECgwYV2VydW0gU29mdHdhcmUgJiBTeXN0ZW1zMRIwEAYDVQQDDAlzMXBkZ3MuZXUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDZGsBDpcjrwGCY74UsujgSZkcQsRjv9TSCHimOV/dlvbDl58iIhZUg93uwub+Ae0F77pfPClN5YDjbepkT6qWWJd06AAUDlbylGELrnqNANpIiVqK2YVrOUR2qBnVDronOrrajeTD5bBAtPpjZssc5v3swhdqSLSBKTz/5LykOSvD4IpxKkdCJBu/L+y01kUGDe3eXYbbf4T1tjo11t4wJunUkv0RV2nvnPkrdu/YtTN9X7kw2LRNaHR0rHudo68F8m3PylC9HtDetK/bZ2gO23Vy5ZLriKpDi4cycqdAIUy0mBwjd3O4nQ353bh7T5l2zuPTcvysbtp9u5SbTLLLPAgMBAAGjUzBRMB0GA1UdDgQWBBRn55CvXWTHfl4RIavXecPpcM3AsDAfBgNVHSMEGDAWgBRn55CvXWTHfl4RIavXecPpcM3AsDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQDYXwNm4CnLsBXRj995oVaRuCG87F4srET7CzCrZUEy9QJYTxreB7oQz8dsFeW9SRbb5PaWhlnDwXjTIq03GUN3TIDLRcW4uEzRL/9rWv0Nl5hzdVo6GTFJ1drUTtzjtmv5laTs4gxF36R0xxl+TeYSgo41T78Fv6NIYvcABw5zdZoydbZyt2WkbWiJx8fkX5jbn+mXK3ECprhRYb6tMPUxeMRB0ZZEDrG7A+7qyLF9fpvx6m2qF80CaXKekxf4im2R7UjzqS0+b5c02JJnBpNrUvrjAabNh0Fd3omKjEGRoB5loK6c8t3rS5sr6PCr27wPLcSNT45u0P3FnsEYGnOZ5wEFmzWTWqty+5TotBFojPkkuQQ="
#export MYOCEAN_CLEANER_TRUSTSTOREPASS="changeit"

#export MBU_OUTBOX_HOSTNAME="s1pro-mock-dissemination-svc"
#export MBU_OUTBOX_PATH="public/METEO"
#export MBU_OUTBOX_PATH_EVAL="mbu"
#export MBU_OUTBOX_PROTOCOL="ftps"
#export MBU_OUTBOX_PORT="21"
#export MBU_OUTBOX_FTPPASV="true"
#export MBU_OUTBOX_USERNAME="s1pdgs"
#export MBU_OUTBOX_PASSWORD="PDG5!"
#export MBU_OUTBOX_KEYSTOREDATA=
#export MBU_OUTBOX_KEYSTOREPASS="changeit"
#export MBU_OUTBOX_TRUSTSTOREDATA="/u3+7QAAAAIAAAABAAAAAgAIbXlvY2VhbjEAAAF3RQrFWwAFWC41MDkAAAPDMIIDvzCCAqegAwIBAgIUUbIDATMWy1AeVKX9P8A/0kaI5mEwDQYJKoZIhvcNAQELBQAwbzELMAkGA1UEBhMCREUxFTATBgNVBAgMDExvd2VyIFNheG9ueTESMBAGA1UEBwwJTHVlbmVidXJnMSEwHwYDVQQKDBhXZXJ1bSBTb2Z0d2FyZSAmIFN5c3RlbXMxEjAQBgNVBAMMCXMxcGRncy5ldTAeFw0yMDEyMDQxODQwMzhaFw0yMTEyMDQxODQwMzhaMG8xCzAJBgNVBAYTAkRFMRUwEwYDVQQIDAxMb3dlciBTYXhvbnkxEjAQBgNVBAcMCUx1ZW5lYnVyZzEhMB8GA1UECgwYV2VydW0gU29mdHdhcmUgJiBTeXN0ZW1zMRIwEAYDVQQDDAlzMXBkZ3MuZXUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDZGsBDpcjrwGCY74UsujgSZkcQsRjv9TSCHimOV/dlvbDl58iIhZUg93uwub+Ae0F77pfPClN5YDjbepkT6qWWJd06AAUDlbylGELrnqNANpIiVqK2YVrOUR2qBnVDronOrrajeTD5bBAtPpjZssc5v3swhdqSLSBKTz/5LykOSvD4IpxKkdCJBu/L+y01kUGDe3eXYbbf4T1tjo11t4wJunUkv0RV2nvnPkrdu/YtTN9X7kw2LRNaHR0rHudo68F8m3PylC9HtDetK/bZ2gO23Vy5ZLriKpDi4cycqdAIUy0mBwjd3O4nQ353bh7T5l2zuPTcvysbtp9u5SbTLLLPAgMBAAGjUzBRMB0GA1UdDgQWBBRn55CvXWTHfl4RIavXecPpcM3AsDAfBgNVHSMEGDAWgBRn55CvXWTHfl4RIavXecPpcM3AsDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQDYXwNm4CnLsBXRj995oVaRuCG87F4srET7CzCrZUEy9QJYTxreB7oQz8dsFeW9SRbb5PaWhlnDwXjTIq03GUN3TIDLRcW4uEzRL/9rWv0Nl5hzdVo6GTFJ1drUTtzjtmv5laTs4gxF36R0xxl+TeYSgo41T78Fv6NIYvcABw5zdZoydbZyt2WkbWiJx8fkX5jbn+mXK3ECprhRYb6tMPUxeMRB0ZZEDrG7A+7qyLF9fpvx6m2qF80CaXKekxf4im2R7UjzqS0+b5c02JJnBpNrUvrjAabNh0Fd3omKjEGRoB5loK6c8t3rS5sr6PCr27wPLcSNT45u0P3FnsEYGnOZ5wEFmzWTWqty+5TotBFojPkkuQQ="
#export MBU_OUTBOX_TRUSTSTOREPASS="changeit"

export AMALFI_SECRET_NAME="amalfi";

export AMALFI_DB_URL="postgresql-infra-pgpool.infra.svc.cluster.local";
### will this url change to:
### export AMALFI_DB_URL="postgresql-quality-pgpool.processing.svc.cluster.local"

export AMALFI_DB_USER="amalfi";
export AMALFI_DB_PASS="XXXXX";

#export KONGPLUGIN_JSON=$(kubectl -n infra get kongplugin kong-oidc-plugin -o json | jq -r '.metadata.annotations."kubectl.kubernetes.io/last-applied-configuration"' | sed 's/\\"/"/g')

#export KEYCLOAK_OIDC_CLIENT_NAME=$(       echo ${KONGPLUGIN_JSON} | jq -r '.config.client_id');
#export KEYCLOAK_OIDC_CLIENT_SECRET=$(     echo ${KONGPLUGIN_JSON} | jq -r '.config.client_secret');
#export KEYCLOAK_OIDC_DISCOVERY_URL=$(     echo ${KONGPLUGIN_JSON} | jq -r '.config.discovery');
#export KEYCLOAK_OIDC_SESSION_SECRET=$(    echo ${KONGPLUGIN_JSON} | jq -r '.config.session_secret');
#export KEYCLOAK_OIDC_IPA_GROUP_ALLOWED=$( echo ${KONGPLUGIN_JSON} | jq -r '.config.groups_authorized_paths[0].group_name');

# export KEYCLOAK_OIDC_PERMISSIONS="  - group_authorized_paths:
#     - /odata
#     group_name: operations
#   - group_authorized_paths:
#     - /odata
#     group_name: centreexpert
#   - group_authorized_paths:
#     - /odata
#     group_name: op_manager
#   - group_authorized_paths:
#     - /
#     group_name: sysadmin
#   - group_authorized_paths:
#     - /odata
#     group_name: customer
#   - group_authorized_paths:
#     - /odata
#     group_name: b2b"

export HELM_ARGS_54_l0_asp_ipf_execution_worker="--set resources.requests.cpu=3"

export HELM_ARGS_33_disseminator="\
    --set disseminator.outboxes.fos.protocol=\"ftps\" \
    --set disseminator.outboxes.fos.path=\"/outboxes/FOS\" \
    --set disseminator.outboxes.fos.port=21 \
    --set disseminator.outboxes.fos.user=\"s1pdgs\" \
    --set disseminator.outboxes.fos.pass=\"PDG5!\" \
    --set disseminator.outboxes.fos.key_data=\"LS0tLS1CRUdJTiBSU0EgUFJJVkFURSBLRVktLS0tLQpNSUlFcFFJQkFBS0NBUUVBd2RjSjhhMHpBUXZncUZ6a2pIOUxzcVo0d1JTb1RieU4xRGQ1d2tvSlEvazRRV3JuCnZSU3VCRDU4d0tUVFQ2Z0o1QVhhTUVRVlBkbXF1Y21TdGc0NkV5Wi96ZHZoU1ROdFF6dGRuRWFBdDNpdkUrTmIKTVlsNGR6UXhEcnNKZWdCbzBMNmJJMjhMQ01Td2dUWDNYSzJNekJ5eUFqSTRDZlhBOUN4VXVreTZIb3BzZFMwTApuWUdtUFdTcUY4U3NsZlBtQnExYUZRaHpET2NWeGlIRmJDVm90cGxXQzhRSm9BazQyZ05sN2Z0ZnBLV3I5Y3Y4CjBpaTArcWZIR3hkL09ZQmVwM3lsOEdtZXJMSjJ1aGluWW5Sd3lyWmRmUFEzY2xuUVJObXQ2d1FYVGJ2bDBRZVIKS1didEd1Vk5IYkluUWpBSHArYnN6YW9YMGhidFZ1WDhXbklyNFFJREFRQUJBb0lCQVFDN1E5WTFoMUFwQ0tLSAp4UVovdlZNU1NzV0tNenpOclFXUUtmQVZoWlVoK0JrakNmYkxzcmpUSE5ORkFYZXNFdmxMTTJReEVQVThoWmJzCldoTEVrMFlEemtQV1MwQ0FpWkJiVFhVSVR3eDNNNzVnaGx0SnBqZWRZZXlyQUlNTlBHd0dqcjR1STA4VHI0R2QKc3RkQXorR2dKM0RKc2w1MU1OaXlqRWxtbWsyTXhob3VYcE9lSDFFdFdTdXAwUVFIUWs4cjRZMUNENkhnWjQxNApJaW5vR21WWlh3MkpZcnFPZEJjaEZHWkFXVHJSYm5FOUd2VFc0RHNtVGZ4alhxdjZLTXVRYzJLaXZRNTh6ZFRYCjVPbjBQQ1Y4V1VteFM4SkhueXo1QWVGZERodkorQ2s4bWlCZm5WOEJDM0FkYkpMWEZRMmhDc1ErajFzVFRiaHIKYnQ4Yk84bUJBb0dCQVBMZ3lZNnZVazFLSENFd0k3SzJQZWQraXJSYndPMDVpK1hCei9QMlBHazllN1ZqRjJLbgpYUlkzQ21FNXhydlQ3Ni9sTDVuVTgwY09FK0xQbEpvVyt6cTl0L0l6d3J0VjVtdWt2YU13dk0yRndobWZwMmxyCnppRGRNcXZuOGpPNUwraWRQOEhNMGt0UUF1SjFSeWliNjU5OTZIbDNER1o4R3JEZW1ESE0xRW41QW9HQkFNeFEKQXorZFNETmViQjdEWFlxNHFzU3BxT2ZvSFNRR2NTcnNiTW1Ba1RIck0zTXBWUmtWOThONzZHdEw3aFF6aG1aZgp5M3ZWQVdoZHpZUEFMbjg5UklkaDRsRzN3S3ZueWplVjVPOXdhOGt0ZmRmMkp4RGpoQys1UEVVeFBTWTQ1cFJLCmF1NDRzV1RST1B6bytxMkNWcW5xbStTYVpGNGpNSXpRaENmaGthc3BBb0dCQUx5NjdDVFNHOVF2V0hoNW5lUEwKTllOams0amZHNlc3Wi9oYmlLcDhseWo0Tzk4Ulp5U2tCUE8wUlg2VGxOaHpzZmN1MHJScEE3b2RPRnF4RHpwRgp6V2N4OFhSbHdGWXVRK1UxbUo3c3ltbXlITWdvaGNDbm92OXFvMnR0eTRsaHg5YXNDdFVmd2ZILzlKM2dvRER3CktFSkJacHFzWlpobHE1L3crTGNLQVpTUkFvR0FXVVVnbGp3SG53SkRwWkZoSHNxZk9yeVNvM2xrdWlmaWJJbk8KNHFBOVVKMWU1cE9KcmlOT0ZXamZYWHBZdFJUcVJYTFh1dXlQNStVTWRlT1RyVjY4d1phQVI0cE5NZzlkNkxtaQp4UWZPNEtEeTJsaWdLeDN0MU5oUHdEL0tZeGY3ajVHUWRUUHFObzNBSTZrOFZuR2JvalJ6RndocWtFTTU1Rno3ClVWMmF3emtDZ1lFQXVQTVh1VUhyc3VBdjAyZitNa2hMUE00Yk01M3NQeGQybzZzSFllK2NsMkVpNWxscS9BUkoKaTBBTHhWMUZYZmR2WlJxU2VwbFNXQjZKSUxaSWlSREY3TW5yQnFnY0J5K3Y4aFZOa1NOSkI2L0VPQmoweWdZSAprLzVlZlVMWEF3QUp0QzYvMlljNlZHM2ZFaGRKMURQMGs4TkdXcTVIckR2L2JyVWdUWldZUDVzPQotLS0tLUVORCBSU0EgUFJJVkFURSBLRVktLS0tLQo=\" \
    --set disseminator.outboxes.fos.host=\"s1pro-mock-dissemination-svc\" \
    --set disseminator.outboxes.fos.path_evaluator=\"ISIP\" \
    --set disseminator.outboxes.fos.ftp_pasv=\"true\" \
    --set disseminator.outboxes.fos.keystore_data=\"\" \
    --set disseminator.outboxes.fos.keystore_pass=\"changeit\" \
    --set disseminator.outboxes.outboxes.fos.truststore_data=\"/u3+7QAAAAIAAAABAAAAAgAIbXlvY2VhbjEAAAF3RQrFWwAFWC41MDkAAAPDMIIDvzCCAqegAwIBAgIUUbIDATMWy1AeVKX9P8A/0kaI5mEwDQYJKoZIhvcNAQELBQAwbzELMAkGA1UEBhMCREUxFTATBgNVBAgMDExvd2VyIFNheG9ueTESMBAGA1UEBwwJTHVlbmVidXJnMSEwHwYDVQQKDBhXZXJ1bSBTb2Z0d2FyZSAmIFN5c3RlbXMxEjAQBgNVBAMMCXMxcGRncy5ldTAeFw0yMDEyMDQxODQwMzhaFw0yMTEyMDQxODQwMzhaMG8xCzAJBgNVBAYTAkRFMRUwEwYDVQQIDAxMb3dlciBTYXhvbnkxEjAQBgNVBAcMCUx1ZW5lYnVyZzEhMB8GA1UECgwYV2VydW0gU29mdHdhcmUgJiBTeXN0ZW1zMRIwEAYDVQQDDAlzMXBkZ3MuZXUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDZGsBDpcjrwGCY74UsujgSZkcQsRjv9TSCHimOV/dlvbDl58iIhZUg93uwub+Ae0F77pfPClN5YDjbepkT6qWWJd06AAUDlbylGELrnqNANpIiVqK2YVrOUR2qBnVDronOrrajeTD5bBAtPpjZssc5v3swhdqSLSBKTz/5LykOSvD4IpxKkdCJBu/L+y01kUGDe3eXYbbf4T1tjo11t4wJunUkv0RV2nvnPkrdu/YtTN9X7kw2LRNaHR0rHudo68F8m3PylC9HtDetK/bZ2gO23Vy5ZLriKpDi4cycqdAIUy0mBwjd3O4nQ353bh7T5l2zuPTcvysbtp9u5SbTLLLPAgMBAAGjUzBRMB0GA1UdDgQWBBRn55CvXWTHfl4RIavXecPpcM3AsDAfBgNVHSMEGDAWgBRn55CvXWTHfl4RIavXecPpcM3AsDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQDYXwNm4CnLsBXRj995oVaRuCG87F4srET7CzCrZUEy9QJYTxreB7oQz8dsFeW9SRbb5PaWhlnDwXjTIq03GUN3TIDLRcW4uEzRL/9rWv0Nl5hzdVo6GTFJ1drUTtzjtmv5laTs4gxF36R0xxl+TeYSgo41T78Fv6NIYvcABw5zdZoydbZyt2WkbWiJx8fkX5jbn+mXK3ECprhRYb6tMPUxeMRB0ZZEDrG7A+7qyLF9fpvx6m2qF80CaXKekxf4im2R7UjzqS0+b5c02JJnBpNrUvrjAabNh0Fd3omKjEGRoB5loK6c8t3rS5sr6PCr27wPLcSNT45u0P3FnsEYGnOZ5wEFmzWTWqty+5TotBFojPkkuQQ=\" \
    --set disseminator.outboxes.outboxes.fos.truststore_pass=\"changeit\" \
    --set disseminator.outboxes.pod.protocol=\"ftps\" \
    --set disseminator.outboxes.pod.path=\"/outboxes/POD\" \
    --set disseminator.outboxes.pod.port=21 \
    --set disseminator.outboxes.pod.user=\"s1pdgs\" \
    --set disseminator.outboxes.pod.pass=\"PDG5!\" \
    --set disseminator.outboxes.pod.host=\"s1pro-mock-dissemination-svc\" \
    --set disseminator.outboxes.pod.ftp_pasv=\"true\" \
    --set disseminator.outboxes.pod.keystore_data=\"\" \
    --set disseminator.outboxes.pod.keystore_pass=\"changeit\" \
    --set disseminator.outboxes.pod.truststore_data=\"/u3+7QAAAAIAAAABAAAAAgAIbXlvY2VhbjEAAAF3RQrFWwAFWC41MDkAAAPDMIIDvzCCAqegAwIBAgIUUbIDATMWy1AeVKX9P8A/0kaI5mEwDQYJKoZIhvcNAQELBQAwbzELMAkGA1UEBhMCREUxFTATBgNVBAgMDExvd2VyIFNheG9ueTESMBAGA1UEBwwJTHVlbmVidXJnMSEwHwYDVQQKDBhXZXJ1bSBTb2Z0d2FyZSAmIFN5c3RlbXMxEjAQBgNVBAMMCXMxcGRncy5ldTAeFw0yMDEyMDQxODQwMzhaFw0yMTEyMDQxODQwMzhaMG8xCzAJBgNVBAYTAkRFMRUwEwYDVQQIDAxMb3dlciBTYXhvbnkxEjAQBgNVBAcMCUx1ZW5lYnVyZzEhMB8GA1UECgwYV2VydW0gU29mdHdhcmUgJiBTeXN0ZW1zMRIwEAYDVQQDDAlzMXBkZ3MuZXUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDZGsBDpcjrwGCY74UsujgSZkcQsRjv9TSCHimOV/dlvbDl58iIhZUg93uwub+Ae0F77pfPClN5YDjbepkT6qWWJd06AAUDlbylGELrnqNANpIiVqK2YVrOUR2qBnVDronOrrajeTD5bBAtPpjZssc5v3swhdqSLSBKTz/5LykOSvD4IpxKkdCJBu/L+y01kUGDe3eXYbbf4T1tjo11t4wJunUkv0RV2nvnPkrdu/YtTN9X7kw2LRNaHR0rHudo68F8m3PylC9HtDetK/bZ2gO23Vy5ZLriKpDi4cycqdAIUy0mBwjd3O4nQ353bh7T5l2zuPTcvysbtp9u5SbTLLLPAgMBAAGjUzBRMB0GA1UdDgQWBBRn55CvXWTHfl4RIavXecPpcM3AsDAfBgNVHSMEGDAWgBRn55CvXWTHfl4RIavXecPpcM3AsDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQDYXwNm4CnLsBXRj995oVaRuCG87F4srET7CzCrZUEy9QJYTxreB7oQz8dsFeW9SRbb5PaWhlnDwXjTIq03GUN3TIDLRcW4uEzRL/9rWv0Nl5hzdVo6GTFJ1drUTtzjtmv5laTs4gxF36R0xxl+TeYSgo41T78Fv6NIYvcABw5zdZoydbZyt2WkbWiJx8fkX5jbn+mXK3ECprhRYb6tMPUxeMRB0ZZEDrG7A+7qyLF9fpvx6m2qF80CaXKekxf4im2R7UjzqS0+b5c02JJnBpNrUvrjAabNh0Fd3omKjEGRoB5loK6c8t3rS5sr6PCr27wPLcSNT45u0P3FnsEYGnOZ5wEFmzWTWqty+5TotBFojPkkuQQ=\" \
    --set disseminator.outboxes.pod.truststore_pass=\"changeit\""

export HELM_ARGS_34_disseminator_sup="\
    --set disseminator.outboxes.unav.protocol=\"file\" \
    --set disseminator.outboxes.unav.path=\"/rep_in/UNAV\" \
    --set disseminator.outboxes.unav.port=0 \
    --set disseminator.outboxes.unav.user=\"\" \
    --set disseminator.outboxes.unav.pass=\"\" \
    --set disseminator.outboxes.unav.host=\"\" \
    --set disseminator.outboxes.unav.ftp_pasv=\"true\" \
    --set disseminator.outboxes.unav.keystore_data=\"\" \
    --set disseminator.outboxes.unav.keystore_pass=\"changeit\" \
    --set disseminator.outboxes.unav.truststore_data=\"/u3+7QAAAAIAAAABAAAAAgAIbXlvY2VhbjEAAAF3RQrFWwAFWC41MDkAAAPDMIIDvzCCAqegAwIBAgIUUbIDATMWy1AeVKX9P8A/0kaI5mEwDQYJKoZIhvcNAQELBQAwbzELMAkGA1UEBhMCREUxFTATBgNVBAgMDExvd2VyIFNheG9ueTESMBAGA1UEBwwJTHVlbmVidXJnMSEwHwYDVQQKDBhXZXJ1bSBTb2Z0d2FyZSAmIFN5c3RlbXMxEjAQBgNVBAMMCXMxcGRncy5ldTAeFw0yMDEyMDQxODQwMzhaFw0yMTEyMDQxODQwMzhaMG8xCzAJBgNVBAYTAkRFMRUwEwYDVQQIDAxMb3dlciBTYXhvbnkxEjAQBgNVBAcMCUx1ZW5lYnVyZzEhMB8GA1UECgwYV2VydW0gU29mdHdhcmUgJiBTeXN0ZW1zMRIwEAYDVQQDDAlzMXBkZ3MuZXUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDZGsBDpcjrwGCY74UsujgSZkcQsRjv9TSCHimOV/dlvbDl58iIhZUg93uwub+Ae0F77pfPClN5YDjbepkT6qWWJd06AAUDlbylGELrnqNANpIiVqK2YVrOUR2qBnVDronOrrajeTD5bBAtPpjZssc5v3swhdqSLSBKTz/5LykOSvD4IpxKkdCJBu/L+y01kUGDe3eXYbbf4T1tjo11t4wJunUkv0RV2nvnPkrdu/YtTN9X7kw2LRNaHR0rHudo68F8m3PylC9HtDetK/bZ2gO23Vy5ZLriKpDi4cycqdAIUy0mBwjd3O4nQ353bh7T5l2zuPTcvysbtp9u5SbTLLLPAgMBAAGjUzBRMB0GA1UdDgQWBBRn55CvXWTHfl4RIavXecPpcM3AsDAfBgNVHSMEGDAWgBRn55CvXWTHfl4RIavXecPpcM3AsDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQDYXwNm4CnLsBXRj995oVaRuCG87F4srET7CzCrZUEy9QJYTxreB7oQz8dsFeW9SRbb5PaWhlnDwXjTIq03GUN3TIDLRcW4uEzRL/9rWv0Nl5hzdVo6GTFJ1drUTtzjtmv5laTs4gxF36R0xxl+TeYSgo41T78Fv6NIYvcABw5zdZoydbZyt2WkbWiJx8fkX5jbn+mXK3ECprhRYb6tMPUxeMRB0ZZEDrG7A+7qyLF9fpvx6m2qF80CaXKekxf4im2R7UjzqS0+b5c02JJnBpNrUvrjAabNh0Fd3omKjEGRoB5loK6c8t3rS5sr6PCr27wPLcSNT45u0P3FnsEYGnOZ5wEFmzWTWqty+5TotBFojPkkuQQ=\" \
    --set disseminator.outboxes.unav.truststore_pass=\"changeit\""

export HELM_ARGS_35_disseminator_mpr="\
    --set disseminator.outboxes.mp.protocol=\"file\" \
    --set disseminator.outboxes.mp.path=\"/rep_in/MP\" \
    --set disseminator.outboxes.mp.port=0 \
    --set disseminator.outboxes.mp.user=\"\" \
    --set disseminator.outboxes.mp.pass=\"\" \
    --set disseminator.outboxes.mp.host=\"\" \
    --set disseminator.outboxes.mp.ftp_pasv=\"true\" \
    --set disseminator.outboxes.mp.keystore_data=\"\" \
    --set disseminator.outboxes.mp.keystore_pass=\"changeit\" \
    --set disseminator.outboxes.mp.truststore_data=\"/u3+7QAAAAIAAAABAAAAAgAIbXlvY2VhbjEAAAF3RQrFWwAFWC41MDkAAAPDMIIDvzCCAqegAwIBAgIUUbIDATMWy1AeVKX9P8A/0kaI5mEwDQYJKoZIhvcNAQELBQAwbzELMAkGA1UEBhMCREUxFTATBgNVBAgMDExvd2VyIFNheG9ueTESMBAGA1UEBwwJTHVlbmVidXJnMSEwHwYDVQQKDBhXZXJ1bSBTb2Z0d2FyZSAmIFN5c3RlbXMxEjAQBgNVBAMMCXMxcGRncy5ldTAeFw0yMDEyMDQxODQwMzhaFw0yMTEyMDQxODQwMzhaMG8xCzAJBgNVBAYTAkRFMRUwEwYDVQQIDAxMb3dlciBTYXhvbnkxEjAQBgNVBAcMCUx1ZW5lYnVyZzEhMB8GA1UECgwYV2VydW0gU29mdHdhcmUgJiBTeXN0ZW1zMRIwEAYDVQQDDAlzMXBkZ3MuZXUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDZGsBDpcjrwGCY74UsujgSZkcQsRjv9TSCHimOV/dlvbDl58iIhZUg93uwub+Ae0F77pfPClN5YDjbepkT6qWWJd06AAUDlbylGELrnqNANpIiVqK2YVrOUR2qBnVDronOrrajeTD5bBAtPpjZssc5v3swhdqSLSBKTz/5LykOSvD4IpxKkdCJBu/L+y01kUGDe3eXYbbf4T1tjo11t4wJunUkv0RV2nvnPkrdu/YtTN9X7kw2LRNaHR0rHudo68F8m3PylC9HtDetK/bZ2gO23Vy5ZLriKpDi4cycqdAIUy0mBwjd3O4nQ353bh7T5l2zuPTcvysbtp9u5SbTLLLPAgMBAAGjUzBRMB0GA1UdDgQWBBRn55CvXWTHfl4RIavXecPpcM3AsDAfBgNVHSMEGDAWgBRn55CvXWTHfl4RIavXecPpcM3AsDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQDYXwNm4CnLsBXRj995oVaRuCG87F4srET7CzCrZUEy9QJYTxreB7oQz8dsFeW9SRbb5PaWhlnDwXjTIq03GUN3TIDLRcW4uEzRL/9rWv0Nl5hzdVo6GTFJ1drUTtzjtmv5laTs4gxF36R0xxl+TeYSgo41T78Fv6NIYvcABw5zdZoydbZyt2WkbWiJx8fkX5jbn+mXK3ECprhRYb6tMPUxeMRB0ZZEDrG7A+7qyLF9fpvx6m2qF80CaXKekxf4im2R7UjzqS0+b5c02JJnBpNrUvrjAabNh0Fd3omKjEGRoB5loK6c8t3rS5sr6PCr27wPLcSNT45u0P3FnsEYGnOZ5wEFmzWTWqty+5TotBFojPkkuQQ=\" \
    --set disseminator.outboxes.mp.truststore_pass=\"changeit\""

export HELM_ARGS_36_disseminator_errmat="\
    --set disseminator.outboxes.errmat.protocol=\"sftp\" \
    --set disseminator.outboxes.errmat.path=\"/outboxes/ERRMAT\" \
    --set disseminator.outboxes.errmat.port=22 \
    --set disseminator.outboxes.errmat.user=\"foo\" \
    --set disseminator.outboxes.errmat.pass=\"pass\" \
    --set disseminator.outboxes.errmat.host=\"s1pro-mock-dissemination-svc\" \
    --set disseminator.outboxes.errmat.ftp_pasv=\"true\" \
    --set disseminator.outboxes.errmat.keystore_data=\"\" \
    --set disseminator.outboxes.errmat.keystore_pass=\"changeit\" \
    --set disseminator.outboxes.errmat.truststore_data=\"/u3+7QAAAAIAAAABAAAAAgAIbXlvY2VhbjEAAAF3RQrFWwAFWC41MDkAAAPDMIIDvzCCAqegAwIBAgIUUbIDATMWy1AeVKX9P8A/0kaI5mEwDQYJKoZIhvcNAQELBQAwbzELMAkGA1UEBhMCREUxFTATBgNVBAgMDExvd2VyIFNheG9ueTESMBAGA1UEBwwJTHVlbmVidXJnMSEwHwYDVQQKDBhXZXJ1bSBTb2Z0d2FyZSAmIFN5c3RlbXMxEjAQBgNVBAMMCXMxcGRncy5ldTAeFw0yMDEyMDQxODQwMzhaFw0yMTEyMDQxODQwMzhaMG8xCzAJBgNVBAYTAkRFMRUwEwYDVQQIDAxMb3dlciBTYXhvbnkxEjAQBgNVBAcMCUx1ZW5lYnVyZzEhMB8GA1UECgwYV2VydW0gU29mdHdhcmUgJiBTeXN0ZW1zMRIwEAYDVQQDDAlzMXBkZ3MuZXUwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDZGsBDpcjrwGCY74UsujgSZkcQsRjv9TSCHimOV/dlvbDl58iIhZUg93uwub+Ae0F77pfPClN5YDjbepkT6qWWJd06AAUDlbylGELrnqNANpIiVqK2YVrOUR2qBnVDronOrrajeTD5bBAtPpjZssc5v3swhdqSLSBKTz/5LykOSvD4IpxKkdCJBu/L+y01kUGDe3eXYbbf4T1tjo11t4wJunUkv0RV2nvnPkrdu/YtTN9X7kw2LRNaHR0rHudo68F8m3PylC9HtDetK/bZ2gO23Vy5ZLriKpDi4cycqdAIUy0mBwjd3O4nQ353bh7T5l2zuPTcvysbtp9u5SbTLLLPAgMBAAGjUzBRMB0GA1UdDgQWBBRn55CvXWTHfl4RIavXecPpcM3AsDAfBgNVHSMEGDAWgBRn55CvXWTHfl4RIavXecPpcM3AsDAPBgNVHRMBAf8EBTADAQH/MA0GCSqGSIb3DQEBCwUAA4IBAQDYXwNm4CnLsBXRj995oVaRuCG87F4srET7CzCrZUEy9QJYTxreB7oQz8dsFeW9SRbb5PaWhlnDwXjTIq03GUN3TIDLRcW4uEzRL/9rWv0Nl5hzdVo6GTFJ1drUTtzjtmv5laTs4gxF36R0xxl+TeYSgo41T78Fv6NIYvcABw5zdZoydbZyt2WkbWiJx8fkX5jbn+mXK3ECprhRYb6tMPUxeMRB0ZZEDrG7A+7qyLF9fpvx6m2qF80CaXKekxf4im2R7UjzqS0+b5c02JJnBpNrUvrjAabNh0Fd3omKjEGRoB5loK6c8t3rS5sr6PCr27wPLcSNT45u0P3FnsEYGnOZ5wEFmzWTWqty+5TotBFojPkkuQQ=\" \
    --set disseminator.outboxes.errmat.truststore_pass=\"changeit\""

export HELM_ARGS_81_myocean_worker="\
    --set disseminator.myocean.protocol=\"${MYOCEAN_OUTBOX_1_PROTOCOL}\" \
    --set disseminator.myocean.path=\"${MYOCEAN_OUTBOX_1_PATH}\" \
    --set disseminator.myocean.port=\"${MYOCEAN_OUTBOX_1_PORT}\" \
    --set disseminator.myocean.user=\"${MYOCEAN_OUTBOX_1_USERNAME}\" \
    --set disseminator.myocean.pass=\"${MYOCEAN_OUTBOX_1_PASSWORD}\" \
    --set disseminator.myocean.host=\"${MYOCEAN_OUTBOX_1_HOSTNAME}\" \
    --set disseminator.myocean.path_evaluator=\"${MYOCEAN_OUTBOX_1_PATH_EVAL}\" \
    --set disseminator.myocean.ftp_pasv=\"${MYOCEAN_OUTBOX_1_FTPPASV}\" \
    --set disseminator.myocean.keystore_data=\"${MYOCEAN_OUTBOX_1_KEYSTOREDATA}\" \
    --set disseminator.myocean.keystore_pass=\"${MYOCEAN_OUTBOX_1_KEYSTOREPASS}\" \
    --set disseminator.myocean.truststore_data=\"${MYOCEAN_OUTBOX_1_TRUSTSTOREDATA}\" \
    --set disseminator.myocean.truststore_pass=\"${MYOCEAN_OUTBOX_1_TRUSTSTOREPASS}\""
    
export HELM_ARGS_95_mbu_dissemination_worker="\
    --set disseminator.outboxes.mbu.protocol=\"${MBU_OUTBOX_PROTOCOL}\" \
    --set disseminator.outboxes.mbu.path=\"${MBU_OUTBOX_PATH}\" \
    --set disseminator.outboxes.mbu.port=\"${MBU_OUTBOX_PORT}\" \
    --set disseminator.outboxes.mbu.user=\"${MBU_OUTBOX_USERNAME}\" \
    --set disseminator.outboxes.mbu.pass=\"${MBU_OUTBOX_PASSWORD}\" \
    --set disseminator.outboxes.mbu.host=\"${MBU_OUTBOX_HOSTNAME}\" \
    --set disseminator.outboxes.mbu.path_evaluator=\"${MBU_OUTBOX_PATH_EVAL}\" \
    --set disseminator.outboxes.mbu.ftp_pasv=\"${MBU_OUTBOX_FTPPASV}\" \
    --set disseminator.outboxes.mbu.keystore_data=\"${MBU_OUTBOX_KEYSTOREDATA}\" \
    --set disseminator.outboxes.mbu.keystore_pass=\"${MBU_OUTBOX_KEYSTOREPASS}\" \
    --set disseminator.outboxes.mbu.truststore_data=\"${MBU_OUTBOX_TRUSTSTOREDATA}\" \
    --set disseminator.outboxes.mbu.truststore_pass=\"${MBU_OUTBOX_TRUSTSTOREPASS}\""
    



#############################################
##            OPENSTACK
#############################################
export OS_ENDPOINT="https://iam.eu-west-0.prod-cloud-ocb.orange-business.com/v3";
export OS_DOMAIN_ID="XXXXX";
export OS_PROJECT_ID="XXXXX";
export OS_USERNAME="XXXXX";
export OS_PASSWORD="XXXXX";
export OS_AZ="eu-west-0a";
export OS_NETWORK="XXXXX";
export OS_SECURITY_GROUP="XXXXX";
export OS_FLOATING_NETWORK="XXXXXXXXXXX";

#############################################
##            K8S
#############################################
export K8S_MASTER=$(cat ~/.kube/config | grep server: | awk '{print $2}');
export K8S_NAMESPACE="default";
export K8S_USERNAME="kubernetes-admin";
export K8S_CLIENT_KEY=$(cat ~/.kube/config | grep client-key-data: | awk '{print $2}')
export K8S_CLIENT_CERT_DATA=$(cat ~/.kube/config | grep client-certificate-data: | awk '{print $2}');

#############################################
##            Debug
#############################################
export LOG4J_CONFIG="log/log4j2_debug.yml"

#############################################
##            Werum
#############################################

export WAITING_FROM_INGESTION_IN_SECONDS="1800"

export WERUM_USE_MOCK_WEBDAV=${WERUM_USE_MOCK_WEBDAV:-true}
export WERUM_USE_MOCK_DISSEMINATION=${WERUM_USE_MOCK_DISSEMINATION:-true}

export WERUM_IGNORE_INGESTION_PV=${WERUM_IGNORE_INGESTION_PV:-true}

export WERUM_IGNORE_SECRETS=${WERUM_IGNORE_SECRETS:-false}
export WERUM_IGNORE_DEPLOY=${WERUM_IGNORE_DEPLOY:-false}

export WERUM_IGNORE_S3=${WERUM_IGNORE_S3:-false}
export WERUM_IGNORE_ES=${WERUM_IGNORE_ES:-false}
export WERUM_IGNORE_MONGO=${WERUM_IGNORE_MONGO:-false}
export WERUM_IGNORE_KAFKA=${WERUM_IGNORE_KAFKA:-false}

export WERUM_FAST_KAFKA_INIT=${WERUM_FAST_KAFKA_INIT:-true}
export WERUM_SKIP_KAFKA_CLEAN=${WERUM_SKIP_KAFKA_CLEAN:-true}
 
#############################################
##            END
#############################################
