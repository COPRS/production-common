#!/bin/bash

##########################
#   PRINT                #
##########################
RED='\033[0;31m'
ORANGE='\033[0;33m'
GREEN='\033[0;32m'
PURPLE='\033[0;35m'
NC='\033[0m'

SCRIPT_PATH=$(dirname $(realpath $0))

##########################
#   LOAD CONFIGURATION   #
##########################
function load_conf () {
source "${CONFIGURATION_FILE}";
}

function pvc_clean () {
# PVC
echo -e "${PURPLE}
#################################
#  K8S PVC CLEANING ...
#################################${NC}";
if [ "${WERUM_IGNORE_INGESTION_PV}" == "true" ]
then
  kubectl -n "${NAMESPACE_PROCESSING}" delete pvc $(kubectl -n "${NAMESPACE_PROCESSING}" get pvc | awk '{print $1}' | egrep -v "NAME" | xargs);
  kubectl -n "${NAMESPACE_PROCESSING}" get pvc;
else
  kubectl -n "${NAMESPACE_PROCESSING}" delete pvc $(kubectl -n "${NAMESPACE_PROCESSING}" get pvc | awk '{print $1}' | egrep -v "NAME|ingestion|webdav" | xargs);
  kubectl -n "${NAMESPACE_PROCESSING}" get pvc;
fi
}

##########################
#    PROCESSING INIT     #
##########################
function init () {
load_conf;

lcl_CONF="${1}";

# create namespace
kubectl create ns ${NAMESPACE_PROCESSING}

# External parties
$SCRIPT_PATH/modules/secrets.sh "${lcl_CONF}" init;
$SCRIPT_PATH/modules/s3.sh      "${lcl_CONF}" init;
$SCRIPT_PATH/modules/es.sh      "${lcl_CONF}" init;
$SCRIPT_PATH/modules/kafka.sh   "${lcl_CONF}" init;
$SCRIPT_PATH/modules/mongo.sh   "${lcl_CONF}" init;
$SCRIPT_PATH/modules/deploy.sh  "${lcl_CONF}" init;
}
 
##########################
#    PROCESSING RESET    #
##########################
function reset () {
load_conf;

local lcl_CONF="${1}";

# Scale down
$SCRIPT_PATH/modules/deploy.sh "${lcl_CONF}" down;

# PVC clean
#pvc_clean;
 
# S3 Reset
$SCRIPT_PATH/modules/s3.sh    "${lcl_CONF}" reset;

# ES Reset
$SCRIPT_PATH/modules/es.sh    "${lcl_CONF}" clean ;
$SCRIPT_PATH/modules/es.sh    "${lcl_CONF}" init ;

# Kafka Reset
$SCRIPT_PATH/modules/kafka.sh "${lcl_CONF}" reset ;
$SCRIPT_PATH/modules/kafka.sh "${lcl_CONF}" check ;

# Mongo Reset
$SCRIPT_PATH/modules/mongo.sh "${lcl_CONF}" clean ;
$SCRIPT_PATH/modules/mongo.sh "${lcl_CONF}" init ;

# Scale up
$SCRIPT_PATH/modules/deploy.sh "${lcl_CONF}" upgrade;
}

##########################
#    PROCESSING CLEAN    #
##########################
function clean () {
load_conf;

lcl_CONF="${1}";

$SCRIPT_PATH/modules/deploy.sh "${lcl_CONF}" clean;

# External parties
$SCRIPT_PATH/modules/s3.sh      "${lcl_CONF}" clean;
$SCRIPT_PATH/modules/es.sh      "${lcl_CONF}" clean;
$SCRIPT_PATH/modules/mongo.sh   "${lcl_CONF}" clean;
$SCRIPT_PATH/modules/kafka.sh   "${lcl_CONF}" clean;
$SCRIPT_PATH/modules/secrets.sh "${lcl_CONF}" clean;

# PVC
#pvc_clean;
}

##########################
#    PROCESSING CHECK    #
##########################
function check () {
load_conf;

local lcl_CONF="${1}";

# External parties
echo -e "${PURPLE}
#################################
#  S3 / ELASTICSEARCH
#  MONGO / KAFKA CHECK ...
#################################${NC}";
$SCRIPT_PATH/modules/s3.sh      "${lcl_CONF}" check;
$SCRIPT_PATH/modules/es.sh      "${lcl_CONF}" check;
$SCRIPT_PATH/modules/mongo.sh   "${lcl_CONF}" check;
$SCRIPT_PATH/modules/kafka.sh   "${lcl_CONF}" check;
$SCRIPT_PATH/modules/secrets.sh "${lcl_CONF}" check;
$SCRIPT_PATH/modules/deploy.sh  "${lcl_CONF}" check;
}
 
##########################
#         CORE           #
##########################
CONFIGURATION_FILE=$(realpath "$1");
ACTION="${2}";

HELP="
./wrapper.sh CONF_FILE ACTION
CONF_FILE=configuration/XXXX
ACTION=init|reset|clean|check
";

case "${ACTION}" in
    "init")  init  "${CONFIGURATION_FILE}" ;;
    "reset") reset "${CONFIGURATION_FILE}" ;;
    "clean") clean "${CONFIGURATION_FILE}" ;;
    "check") check "${CONFIGURATION_FILE}" ;;
    *)       echo  "${HELP}"               ;;
esac;

#############################################
##            END
#############################################
