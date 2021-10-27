#!/bin/bash

if [ "${WERUM_IGNORE_DEPLOY}" == true ]
then
    echo "Ignoring deploy module"
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

function update_helm_repo() {
  if [ "${OMIT_REPO_UPDATE:-false}" == true ]; 
  then
    echo "Omitting helm repository update"
    return
  fi
  # if helm repo is not present, add it
  if [ $(helm repo list | grep ${HELM_REPO_NAME} | wc -l) -eq 0 ];
  then
    if [ $(echo "${HELM_REPO_URL}" | grep "http" | wc -l) -eq 1 ];
    then
      echo "Adding helm repository ... ${HELM_REPO_NAME} (${HELM_REPO_URL})"
      helm repo add ${HELM_REPO_NAME} ${HELM_REPO_URL} --username=${HELM_REPO_USER} --password=${HELM_REPO_PASS};
    fi
  else
    echo "Updating helm repository ${HELM_REPO_NAME} (at ${HELM_REPO_URL})"
    helm repo update
  fi;
}

function remove_helm_repo() {
  echo "Removing helm repository ... ${HELM_REPO_NAME}"
  helm repo remove ${HELM_REPO_NAME}
}

function deploy_init () {
  echo -e "${PURPLE}
#################################
#  HELM PROCESSING INIT / UPGRADE ...
#################################${NC}";

  # update_helm_repo # No helm chart used in V1 scenario. Detection for il is not trivial. So it is deactivated.
  # echo "Using ${HELM_REPO_NAME} (at ${HELM_REPO_URL}) to deploy services ..."

  # Parse all the COMPONENTS to deploy
  for application in $(echo "${HELM_LIST}" | awk '{print $1":"$2":"$3":"$4":"$5}' | grep "YES" | sort | egrep "${FILTER}");
  do
    HELM_ORDER=$(         echo "${application}" | cut -d ":" -f1);
    HELM_TODO=$(          echo "${application}" | cut -d ":" -f2);
    HELM_NAME=$(          echo "${application}" | cut -d ":" -f3);
    HELM_CHART_VERSION=$( echo "${application}" | cut -d ":" -f4);
    HELM_CONF_LOCAL=$(    echo "${application}" | cut -d ":" -f5);
    HELM_NAME_FOR_REPLICA=$(echo "${HELM_NAME}" | sed 's/-/_/g' | sed 's/s1pro_//g');

    COMMON_HELM_ARGS="--namespace ${NAMESPACE_PROCESSING} -f ${HELM_CONF_GLOBAL} --version ${HELM_CHART_VERSION} --set processing.namespace=${NAMESPACE_PROCESSING}"

    ##########################################
    # INSTALL
    if [ "${2}" == "install" ]; then
        MODE="Installing";
        if "${LOCAL}"; then
            HELM_DIR_LOCAL=$(dirname ${HELM_CONF_LOCAL});
            HELM_ARGS="install ${HELM_NAME} ${HELM_DIR_LOCAL} ${COMMON_HELM_ARGS} -f ${HELM_CONF_REPLICAS} -f ${HELM_CONF_LOCAL}";
        else
           HELM_ARGS="install ${HELM_NAME} ${HELM_REPO_NAME}/${HELM_NAME} ${COMMON_HELM_ARGS} -f ${HELM_CONF_REPLICAS} --devel";
        fi;

    # SCALE DOWN
    elif [ "${2}" == "down" ]; then
        MODE="Scaling down";
        HELM_ARGS="upgrade ${HELM_NAME} ${HELM_REPO_NAME}/${HELM_NAME} ${COMMON_HELM_ARGS} --set replicaCount.${HELM_NAME_FOR_REPLICA}=0";

    # SCALE UP
    elif [ "${2}" == "upgrade" ]; then
        MODE="Upgrading";
        HELM_ARGS="upgrade ${HELM_NAME} ${HELM_REPO_NAME}/${HELM_NAME} ${COMMON_HELM_ARGS} -f ${HELM_CONF_REPLICAS}";    

    else
      MODE="N/A";
    fi;

    ##########################################
    echo "${MODE} ${HELM_NAME}..."    
    if [ "${MODE}" == "N/A" ];
    then
    	echo "Unknown Action !!";
    	
    elif [ "${HELM_NAME}" == "s1pro-s1pdgs-base" ];
    then
        echo "INFO : ${HELM_NAME} has specific parameters";
	helm ${HELM_ARGS} \
--set log4j_config="${LOG4J_CONFIG:-log/log4j2.yml}" \
--set mock.webdav="${WERUM_USE_MOCK_WEBDAV}" \
--set os.endpoint="${OS_ENDPOINT}" \
--set os.domainid="${OS_DOMAIN_ID}" \
--set os.projectid="${OS_PROJECT_ID}" \
--set os.username="${OS_USERNAME}" \
--set os.password="${OS_PASSWORD}" \
--set os.az="${OS_AZ}" \
--set os.network="${OS_NETWORK}" \
--set os.security_group="${OS_SECURITY_GROUP}" \
--set os.floating_network="${OS_FLOATING_NETWORK}" \
--set k8s.master_url="${K8S_MASTER}" \
--set k8s.namespace="${K8S_NAMESPACE}" \
--set k8s.username="${K8S_USERNAME}" \
--set k8s.client_key="${K8S_CLIENT_KEY}" \
--set k8s.client_cert_data="${K8S_CLIENT_CERT_DATA}" \
--set s3.endpoint="${S3_ENDPOINT}" \
--set s3.region="${S3_REGION}" \
--set s3.disable_chunked_encoding="${S3_DISABLE_CHUNKED_ENCODING}" \
--set s3.bucket_aux="${S3_BUCKET_AUX}" \
--set s3.bucket_sessions="${S3_BUCKET_SESSIONS}" \
--set s3.bucket_l0_segments="${S3_BUCKET_L0_SEGMENTS}" \
--set s3.bucket_l0_slices="${S3_BUCKET_L0_SLICES}" \
--set s3.bucket_l0_acns="${S3_BUCKET_L0_ACNS}" \
--set s3.bucket_l1_slices="${S3_BUCKET_L1_SLICES}" \
--set s3.bucket_l1_acns="${S3_BUCKET_L1_ACNS}" \
--set s3.bucket_l2_slices="${S3_BUCKET_L2_SLICES}" \
--set s3.bucket_l2_acns="${S3_BUCKET_L2_ACNS}" \
--set s3.bucket_l0_blanks="${S3_BUCKET_L0_BLANKS}" \
--set s3.bucket_spp="${S3_BUCKET_SPP}" \
--set s3.bucket_spp_mbu="${S3_BUCKET_SPP_MBU}" \
--set s3.bucket_invalid="${S3_BUCKET_INVALID}" \
--set s3.bucket_ghost="${S3_BUCKET_GHOST}" \
--set s3.bucket_debug="${S3_BUCKET_DEBUG}" \
--set s3.bucket_failed_workdir="${S3_BUCKET_FAILED_WORKDIR}" \
--set s3.bucket_session_retransfer="${S3_BUCKET_SESSION_RETRANSFER}" \
--set s3.bucket_plans_and_reports="${S3_BUCKET_PLANS_AND_REPORTS}" \
--set s3.bucket_zip_aux="${S3_BUCKET_ZIP_AUX}" \
--set s3.bucket_zip_l0_segments="${S3_BUCKET_ZIP_L0_SEGMENTS}" \
--set s3.bucket_zip_l0_slices="${S3_BUCKET_ZIP_L0_SLICES}" \
--set s3.bucket_zip_l0_acns="${S3_BUCKET_ZIP_L0_ACNS}" \
--set s3.bucket_zip_l1_slices="${S3_BUCKET_ZIP_L1_SLICES}" \
--set s3.bucket_zip_l1_acns="${S3_BUCKET_ZIP_L1_ACNS}" \
--set s3.bucket_zip_l2_slices="${S3_BUCKET_ZIP_L2_SLICES}" \
--set s3.bucket_zip_l2_acns="${S3_BUCKET_ZIP_L2_ACNS}" \
--set s3.bucket_zip_l0_blanks="${S3_BUCKET_ZIP_L0_BLANKS}" \
--set s3.bucket_zip_spp="${S3_BUCKET_ZIP_SPP}" \
--set s3.bucket_zip_plans_and_reports="${S3_BUCKET_ZIP_PLANS_AND_REPORTS}" \
--set es.svc="${ES_SVC}" \
--set es.port="${ES_PORT}" \
--set es.cluster_name="${ES_CLUSTER_NAME}" \
--set processing.namespace="${NAMESPACE_PROCESSING}" \
--set ingestion.waiting_from_ingestion_in_seconds="$WAITING_FROM_INGESTION_IN_SECONDS" \
--set kafka.url="${KAFKA_URL}" \
--set kafka.topic_errors="${KAFKA_TOPIC_ERROR}" \
--set mongodb.svc="${MONGO_SVC}" \
--set mongodb.host="${MONGO_HOST//,/\\,}" \
--set mongodb.port="${MONGO_PORT}" \
--set mongodb.db="${MONGO_DB}" \
--set auxip.auth_type="${AUXIP_AUTH_METHOD:-disable}" \
--set auxip.oauth_auth_url="${AUXIP_OAUTH_AUTH_URL:-}" \
--set kafkaTopicList="${TOPIC_LIST}" \
> /dev/null;
    else
      if [ $(echo ${HELM_NAME} | grep -c "mock-webdav")        -ne 0 ] && [[ "${WERUM_USE_MOCK_WEBDAV}"        != "true" ]]; then echo "skipping ..."; continue; fi;
      if [ $(echo ${HELM_NAME} | grep -c "mock-dissemination") -ne 0 ] && [[ "${WERUM_USE_MOCK_DISSEMINATION}" != "true" ]]; then echo "skipping ..."; continue; fi;
	    
      #echo "DEBUG: helm ${HELM_ARGS}"
      helm ${HELM_ARGS} > /dev/null
    fi;  
  done;

echo "Wait 5 seconds ... before checking"
sleep 5;
deploy_check
}

function deploy_clean() {
# Processing chain
echo -e "${PURPLE}
#################################
#  HELM PROCESSING CLEANING ...
#################################${NC}";
echo "Using ${HELM_REPO_NAME} (at ${HELM_REPO_URL}) to undeploy services ..."

for application in $(echo "${HELM_LIST}" | awk '{print $1":"$2":"$3":"$4":"$5}' | sort -r | egrep "${FILTER}");
do
    HELM_ORDER=$(         echo "${application}" | cut -d ":" -f1);
    HELM_TODO=$(          echo "${application}" | cut -d ":" -f2);
    HELM_NAME=$(          echo "${application}" | cut -d ":" -f3);
    HELM_CHART_VERSION=$( echo "${application}" | cut -d ":" -f4);
    HELM_CONF=$(          echo "${application}" | cut -d ":" -f5);
    echo "Undeploying ... ${HELM_ORDER} : ${HELM_TODO} : ${HELM_NAME} : ${HELM_CHART_VERSION} : ${HELM_CONF}";
    
    HELM_CHECK=$(helm ls -n "${NAMESPACE_PROCESSING}" | egrep -i "DEPLOYED|FAILED" | grep "${HELM_NAME}" | awk '{print $1}' | wc -l);
    if [ "${HELM_CHECK}" -ge 1 ];
    then
        echo "try to delete ${HELM_NAME}"
        helm delete -n "${NAMESPACE_PROCESSING}" "${HELM_NAME}" 2>&1;
    fi;

    if [[ $? -eq 1 ]];
    then
        echo "An error occured while cleaning ${HELM_NAME}";
    fi;

done;

echo "Wait 5 seconds ... before checking"
sleep 5;
deploy_check
}

function deploy_check() {
# Processing chain
echo -e "${PURPLE}
#################################
#  HELM PROCESSING CHECK ...
#################################${NC}";
RESULT="";
L="";
echo "Using ${HELM_REPO_NAME} (at ${HELM_REPO_URL}) to check services ..."

for application in $(echo "${HELM_LIST}" | awk '{print $1":"$2":"$3":"$4":"$5}' | sort | egrep "${FILTER}");
do
    HELM_ORDER=$(         echo "${application}" | cut -d ":" -f1);
    HELM_TODO=$(          echo "${application}" | cut -d ":" -f2);
    HELM_NAME=$(          echo "${application}" | cut -d ":" -f3);
    HELM_CHART_VERSION=$( echo "${application}" | cut -d ":" -f4);
    HELM_CONF=$(          echo "${application}" | cut -d ":" -f5);
    echo "Checking ... ${HELM_ORDER} : ${HELM_TODO} : ${HELM_NAME} : ${HELM_CHART_VERSION} : ${HELM_CONF}";

    HELM_CHECK=$(helm ls -n "${NAMESPACE_PROCESSING}" | grep -i DEPLOYED | grep "${HELM_NAME}-${HELM_CHART_VERSION}");
    HELM_CHECK_D=$(echo "${HELM_CHECK}" | grep "${HELM_NAME}" |                                wc -l);
    HELM_CHECK_V=$(echo "${HELM_CHECK}" | grep "${HELM_NAME}" | grep "${HELM_CHART_VERSION}" | wc -l);
    
    HELM_CHECK=$(echo "${HELM_CHECK}" | awk '{print $1":"$8":"$9}');
    if [ "${HELM_CHECK_D}" -ge 1 ] && [ "${HELM_CHECK_V}" -ge 1 ];
    then
        R="${HELM_NAME} | ${HELM_CHART_VERSION} | ${GREEN} OK ${NC} | ";
    elif [ "${HELM_CHECK_D}" -ge 1 ] && [ "${HELM_CHECK_V}" -eq 0 ];
    then
        R="${HELM_NAME} | ${HELM_CHART_VERSION} | ${ORANGE} NOK ${NC} | ${HELM_CHECK}";
    else
        R="${HELM_NAME} | ${HELM_CHART_VERSION} | ${RED} KO ${NC} | ${HELM_CHECK}";
    fi;

    L="${L}
${HELM_CHECK}";
    RESULT="${RESULT}
${R}";
done;
echo -e "${RESULT}" | column -t -s '|';
#echo -e "${L}";
}

#############################################
##            CORE
#############################################

if [ $# -lt 2 ]; then
  echo "Synopsis: CONFIGURATION ACTION [FILTER] [LOCAL]"; exit 1
fi

CONF="${1}";
ACTION="${2}";
FILTER="${3}";
LOCAL="${4:-false}"

source "${CONF}";

HELP="
deploy.sh CONF_FILE ACTION FILTER
CONF_FILE=path
ACTION=init|upgrade|down|clean|check
FILTER=Regex to define which components shall be installed (default: all)
";

case "${ACTION}" in
    "init")     deploy_init     "${FILTER}" "install";;
    "upgrade")  deploy_init     "${FILTER}" "upgrade";;
    "down")     deploy_init     "${FILTER}" "down"   ;; 
    "reset")    deploy_clean    "${FILTER}" \
             && deploy_init     "${FILTER}" "install";;
    "clean")    deploy_clean    "${FILTER}" ;;
    "check")    deploy_check    "${FILTER}" ;;

    *) echo "${HELP}" ;;
esac;

#############################################
##            END
#############################################
