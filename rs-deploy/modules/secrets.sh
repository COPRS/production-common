#!/bin/bash

if [ "${WERUM_IGNORE_SECRETS}" == true ];
then
    echo "Ignoring secrets module"
    exit
fi

#############################################
##            PRINT
#############################################
RED='\033[0;31m'
ORANGE='\033[0;33m'
GREEN='\033[0;32m'
PURPLE='\033[0;35m'
NC='\033[0m'

function secrets_init () {
# Secrets
echo -e "${PURPLE}
#################################
#  K8S SECRET INIT ...
#################################${NC}";
kubectl -n "${NAMESPACE_PROCESSING}" create secret docker-registry "${DOCKER_REGISTRY_SECRET_NAME}"   --docker-server="${DOCKER_REGISTRY_ADRESS}"  --docker-username="${DOCKER_REGISTRY_USER}" --docker-password="${DOCKER_REGISTRY_PASS}";
kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${OPENSTACK_SECRET_NAME}"         --from-literal=USER="${OPENSTACK_USER}"   --from-literal=PASS="${OPENSTACK_PASS}";
kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${S3_SECRET_NAME}"                --from-literal=USER_ID="${S3_ACCESS_KEY}" --from-literal=USER_SECRET="${S3_SECRET_KEY}";
kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${MONGODB_SECRET_NAME}"           --from-literal=ROOT_USERNAME="${MONGODB_ROOT_USER}" --from-literal=ROOT_PASSWORD="${MONGODB_ROOT_PASS}" --from-literal=USERNAME="${MONGODB_USER}" --from-literal=PASSWORD="${MONGODB_PASS}";

kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${AUXIP_SECRET_NAME}"             --from-literal=USER="${AUXIP_USER}"    --from-literal=PASS="${AUXIP_PASS}" --from-literal=OAUTHCLIENTID="${AUXIP_OAUTHCLIENTID}" --from-literal=OAUTHCLIENTSECRET="${AUXIP_OAUTHCLIENTSECRET}";
kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${EDIP_PEDC_SECRET_NAME}"         --from-literal=USER="${EDIP_PEDC_USER}"  --from-literal=PASS="${EDIP_PEDC_PASS}";
kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${EDIP_BEDC_SECRET_NAME}"         --from-literal=USER="${EDIP_BEDC_USER}"  --from-literal=PASS="${EDIP_BEDC_PASS}";
kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${QCSS_SECRET_NAME}"              --from-literal=USER="${QCSS_USER}"       --from-literal=PASS="${QCSS_PASS}";
kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${XBIP_01_SECRET_NAME}"           --from-literal=USER="${XBIP_01_USER}"  --from-literal=PASS="${XBIP_01_PASS}";
kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${XBIP_02_SECRET_NAME}"           --from-literal=USER="${XBIP_02_USER}"  --from-literal=PASS="${XBIP_02_PASS}";
kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${XBIP_03_SECRET_NAME}"           --from-literal=USER="${XBIP_03_USER}"  --from-literal=PASS="${XBIP_03_PASS}";
kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${XBIP_04_SECRET_NAME}"           --from-literal=USER="${XBIP_04_USER}"  --from-literal=PASS="${XBIP_04_PASS}";
kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${XBIP_05_SECRET_NAME}"           --from-literal=USER="${XBIP_05_USER}"  --from-literal=PASS="${XBIP_05_PASS}";
kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${XBIP_10_SECRET_NAME}"           --from-literal=USER="${XBIP_10_USER}"  --from-literal=PASS="${XBIP_10_PASS}";
kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${XBIP_S3_SECRET_NAME}"           --from-literal=USER="${XBIP_S3_USER}"  --from-literal=PASS="${XBIP_S3_PASS}";

kubectl -n "${NAMESPACE_PROCESSING}" create secret generic         "${AMALFI_SECRET_NAME}"            --from-literal=AMALFI_DB_URL="${AMALFI_DB_URL}"  --from-literal=AMALFI_DB_USER="${AMALFI_DB_USER}" --from-literal=AMALFI_DB_PASS="${AMALFI_DB_PASS}";
}

function secrets_clean () {
# Secrets
echo -e "${PURPLE}
#################################
#  K8S SECRET CLEANING ...
#################################${NC}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${DOCKER_REGISTRY_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${OPENSTACK_SECRET_NAME}" ;
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${S3_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${MONGODB_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${AUXIP_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${EDIP_PEDC_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${EDIP_BEDC_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${QCSS_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${XBIP_01_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${XBIP_02_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${XBIP_03_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${XBIP_04_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${XBIP_05_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${XBIP_10_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${XBIP_S3_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" delete secret "${AMALFI_SECRET_NAME}";
}

function secrets_check() {
# K8S Secrets
echo -e "${PURPLE}
#################################
#  K8S SECRETS CHECK ...
#################################${NC}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${DOCKER_REGISTRY_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${OPENSTACK_SECRET_NAME}" ;
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${S3_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${MONGODB_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${AUXIP_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${EDIP_PEDC_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${EDIP_BEDC_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${QCSS_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${XBIP_01_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${XBIP_02_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${XBIP_03_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${XBIP_04_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${XBIP_05_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${XBIP_10_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${XBIP_S3_SECRET_NAME}";
kubectl -n "${NAMESPACE_PROCESSING}" get secret "${AMALFI_SECRET_NAME}";
}

#############################################
##            CORE
#############################################

if [ $# -ne 2 ]; then
  echo "Synopsis: CONFIGURATION ACTION"; exit 1
fi

CONF="${1}";
ACTION="${2}";

source "${CONF}";

HELP="
secrets.sh CONF_FILE ACTION
CONF_FILE=path
ACTION=init|clean|check
";

case "${ACTION}" in
    "init")  secrets_init  ;;
    "clean") secrets_clean ;;
    "check") secrets_check ;;
    *) echo "${HELP}" ;;
esac;

#############################################
##            END
#############################################
