#!/bin/sh
#############################################
##            PRIVATE DATA
#############################################
CLEAR_DATA=$(gpg --batch --passphrase-file ~/gpg_pass_werum -d $(dirname ${BASH_SOURCE})/private.gpg);
$CLEAR_DATA

#############################################
##            MISCELEANOUS
#############################################
export NAMESPACE_PROCESSING="processing";

export DOCKER_REGISTRY_SECRET_NAME="harbor-tools"
export DOCKER_REGISTRY_ADRESS="registry.tools.s1pdgs.eu"
#export DOCKER_REGISTRY_USER="git_ivv";
export DOCKER_REGISTRY_USER="ops_harbor";

export HELM_REPO_NAME="local"
export HELM_REPO_URL=""
#export HELM_REPO_NAME="werum_harbor"
#export HELM_REPO_URL="https://registry.tools.s1pdgs.eu/chartrepo/werum"
export HELM_REPO_USER="git_ivv";

export OPENSTACK_SECRET_NAME="openstack";
export OPENSTACK_USER=$(    sudo cat /mnt/s1pdgs/k8s/cloud-config | grep 'username'        | head -n 1 | cut -d '=' -f 2 | sed 's/"//g' | sed 's/ //g');
export OPENSTACK_PASS=$(    sudo cat /mnt/s1pdgs/k8s/cloud-config | grep 'password'        | head -n 1 | cut -d '=' -f 2 | sed 's/"//g' | sed 's/ //g');

export S3_SECRET_NAME="obs";
export S3_ACCESS_KEY=$(     cat ~/.s3cfg                | grep access_key                    | cut -d '=' -f 2 | sed 's/ //g');
export S3_SECRET_KEY=$(     cat ~/.s3cfg                | grep secret_key                    | cut -d '=' -f 2 | sed 's/ //g');

export MONGODB_SECRET_NAME="mongodb";
export MONGODB_ROOT_USER="root";
export MONGODB_ROOT_PASS=$(kubectl get secret -n infra mongodb -o json | jq -r '.data."mongodb-root-password"' | base64 -d);
export MONGODB_USER="s1pdgs";

export AUXIP_SECRET_NAME="auxip";
export AUXIP_USER="s1pdgs1";

export EDIP_PEDC_SECRET_NAME="edip-pedc";
export EDIP_PEDC_USER="esaclient01";

export EDIP_BEDC_SECRET_NAME="edip-bedc";
export EDIP_BEDC_USER="esaclient01";

export QCSS_SECRET_NAME="qcss";
export QCSS_USER="s1pdgs";
export QCSS_PASS="XXXXX"

export XBIP_01_SECRET_NAME="xbip-cgs01";
export XBIP_02_SECRET_NAME="xbip-cgs02";
export XBIP_03_SECRET_NAME="xbip-cgs03";
export XBIP_04_SECRET_NAME="xbip-cgs04";
export XBIP_05_SECRET_NAME="xbip-cgs05";
export XBIP_10_SECRET_NAME="xbip-cgs10";
export XBIP_01_USER="s1pdgs";
export XBIP_02_USER="sentinel1";
export XBIP_03_USER="s1pdgs_intaddp";
export XBIP_04_USER="esacopas";
export XBIP_10_USER="xbip_airbus";

export AMALFI_SECRET_NAME="amalfi";
export AMALFI_DB_URL="postgresql-quality-pgpool.processing.svc.cluster.local";
export AMALFI_DB_USER="amalfi";

export KONGPLUGIN_JSON=$(kubectl -n infra get kongplugin kong-oidc-plugin -o json | jq -r '.metadata.annotations."kubectl.kubernetes.io/last-applied-configuration"' | sed 's/\\"/"/g')

export KEYCLOAK_OIDC_CLIENT_NAME=$(       echo ${KONGPLUGIN_JSON} | jq -r '.config.client_id');
export KEYCLOAK_OIDC_CLIENT_SECRET=$(     echo ${KONGPLUGIN_JSON} | jq -r '.config.client_secret');
export KEYCLOAK_OIDC_DISCOVERY_URL=$(     echo ${KONGPLUGIN_JSON} | jq -r '.config.discovery');
export KEYCLOAK_OIDC_SESSION_SECRET=$(    echo ${KONGPLUGIN_JSON} | jq -r '.config.session_secret');
export KEYCLOAK_OIDC_IPA_GROUP_ALLOWED=$( echo ${KONGPLUGIN_JSON} | jq -r '.config.groups_authorized_paths[0].group_name');

export KEYCLOAK_OIDC_PERMISSIONS="  - group_authorized_paths:
    - /odata
    group_name: operations
  - group_authorized_paths:
    - /odata
    group_name: centreexpert
  - group_authorized_paths:
    - /odata
    group_name: op_manager
  - group_authorized_paths:
    - /
    group_name: sysadmin
  - group_authorized_paths:
    - /odata
    group_name: customer
  - group_authorized_paths:
    - /odata
    group_name: b2b"

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
export LOG4J_CONFIG="log/log4j2.yml"       

#############################################
##            Werum
#############################################
export WAITING_FROM_INGESTION_IN_SECONDS="1800"

export WERUM_USE_MOCK_WEBDAV="false"
export WERUM_USE_MOCK_DISSEMINATION="true"

export WERUM_IGNORE_INGESTION_PV="false";

export WERUM_IGNORE_SECRETS="false";
export WERUM_IGNORE_DEPLOY="false";

export WERUM_IGNORE_S3="true";
export WERUM_IGNORE_ES="false";
export WERUM_IGNORE_MONGO="false";
export WERUM_IGNORE_KAFKA="true";

export WERUM_FAST_KAFKA_INIT="false";
export WERUM_SKIP_KAFKA_CLEAN="false";
 
#############################################
##            END
#############################################
