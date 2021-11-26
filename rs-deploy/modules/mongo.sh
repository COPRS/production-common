#!/bin/bash

if [ "${WERUM_IGNORE_MONGO}" == true ];
then
    echo "Ignoring mongo module"
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

function mongo_print_result () {
    lcl_item="$1";
    lcl_result="$2";
    lcl_action="$3";
    if   [ $(echo "${lcl_result}" |egrep "#2#|#s1pro" | wc -l) -eq 1 ]; then RESULT="${GREEN}OK${NC}";
    else                                                                     RESULT="${RED}KO${NC}";
    fi;
    echo -e "${lcl_action} of ${lcl_item} | ${RESULT} (${lcl_result})";
}

#############################################
##            INIT
#############################################
function mongo_init () {
echo -e "${PURPLE}
#################################
#  MONGO INIT ...
#################################${NC}";
mongo_init_db;
mongo_check;
}

function mongo_init_db () {
echo "Setting up mongo cluster..."
# This script needs to be executed before the actual database is initialized. It is rather a hack than a real
# clean way to setup mongo. Will be tackled in V1.
rs_init_mongo.sh

echo "Initialize mongo db..."
MONGO_POD=$(kubectl -n ${MONGO_NAMESPACE} get pod | grep ${MONGO_POD_PREFIX} | grep Running | head -1 | awk '{print $1}');
MONGO_PRIMARY=$(kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_POD} -- mongo "${MONGO_ROOT_CONNECTION_STRING}" --eval 'rs.status();' | grep PRIMARY -B 4 | grep name | awk -F ':' '{print $2}' | awk -F '"' '{print $2}' | awk -F '.' '{print $1}');
echo "xxx:$MONGO_PRIMARY"
if [ "${MONGO_PRIMARY}" != "" ];
then
cat <<EOF | kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_PRIMARY} -- mongo
var db_path = "${MONGO_ROOT_CONNECTION_STRING}";
var db = connect(db_path);
use ${MONGO_DB};
db.createUser( { user: "${MONGO_USERNAME}", pwd: "${MONGO_PASSWORD}", roles: [ "readWrite", "dbAdmin" ] } )
db.sequence.insert({_id: "mqiMessage",seq: 0});
db.sequence.insert({_id: "appDataJob",seq: 0});
db.mqiMessage.createIndex({"id":1})
db.mqiMessage.createIndex({"readingPod":1, "state":1, "topic":1})
db.mqiMessage.createIndex({"readingPod":1, "state":1, "category":1})
db.mqiMessage.createIndex({"topic":1, "partition":1, "group":1, "state":1})
db.mqiMessage.createIndex({"topic":1, "partition":1, "group":1, "offset":1})
db.mqiMessage.createIndex({"lastAckDate":1, "group":1, "state":1});
db.appDataJob.createIndex({"_id":1, "generation.taskTable":1})
db.appDataJob.createIndex({"state":1, "generation.state":1, "generation.taskTable":1})
db.inboxEntry.createIndex({"processingPod":1, "pickupURL":1, "stationName":1})
EOF
MONGO_CHECK_DB=$(kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_PRIMARY} -- mongo "${MONGO_CONNECTION_STRING}/${MONGO_DB}" --eval 'db.sequence.find();' | egrep "mqi|appDataJob" | wc -l);
else
MONGO_CHECK_DB="0";
fi;
mongo_print_result "DB_INIT" "#${MONGO_CHECK_DB}#" "CREATION"
}

#############################################
##            CLEAN
#############################################
function mongo_clean () {
echo -e "${PURPLE}
#################################
#  MONGO CLEAN ...
#################################${NC}";
mongo_clean_soft;
mongo_check;
}

function mongo_clean_soft () {
MONGO_POD=$(kubectl -n ${MONGO_NAMESPACE} get pod | grep ${MONGO_POD_PREFIX} | grep Running | head -1 | awk '{print $1}');
MONGO_PRIMARY=$(kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_POD} -- mongo "${MONGO_ROOT_CONNECTION_STRING}" --eval 'rs.status();' | grep PRIMARY -B 4 | grep name | awk -F ':' '{print $2}' | awk -F '"' '{print $2}' | awk -F '.' '{print $1}');
if [ "${MONGO_PRIMARY}" != "" ];
then
cat <<EOF | kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_PRIMARY} -- mongo
var db_path = "${MONGO_ROOT_CONNECTION_STRING}";
var db = connect(db_path);
use ${MONGO_DB};
db.dropUser("${MONGO_USERNAME}");
db.dropDatabase();
EOF
fi
}

#############################################
##            CHECK
#############################################
function mongo_check () {
echo -e "${NC}
#  MONGO CHECK ...${NC}";
COLOR_PRIMARY="${RED}";
COLOR_SEQUENCE="${RED}";
COLOR_DBS="${RED}";
COLOR_COL="${RED}";
MONGO_POD=$(kubectl -n ${MONGO_NAMESPACE} get pod | grep ${MONGO_POD_PREFIX} | grep Running | head -1 | awk '{print $1}');
MONGO_PRIMARY=$(kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_POD} -- mongo "${MONGO_ROOT_CONNECTION_STRING}" --eval 'rs.status();' | grep PRIMARY -B 4 | grep name | awk -F ':' '{print $2}' | awk -F '"' '{print $2}' | awk -F '.' '{print $1}')
if [ "${MONGO_PRIMARY}" != "" ];
then
    COLOR_PRIMARY="${GREEN}";
    MONGO_CHECK_DB_MQI=$(kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_PRIMARY} -- mongo "${MONGO_CONNECTION_STRING}/${MONGO_DB}" --eval 'db.sequence.find();' | egrep "mqi");
    MONGO_CHECK_DB_JOB=$(kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_PRIMARY} -- mongo "${MONGO_CONNECTION_STRING}/${MONGO_DB}" --eval 'db.sequence.find();' | egrep "appDataJob");
    MONGO_DB_LIST=$(kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_PRIMARY} -- mongo "${MONGO_ROOT_CONNECTION_STRING}" --eval 'db.adminCommand( { listDatabases: 1 } );' | grep "${MONGO_DB}");
    MONGO_COL_LIST=$(kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_PRIMARY} -- mongo "${MONGO_CONNECTION_STRING}/${MONGO_DB}" --eval 'db.getCollectionNames();' | grep sequence | tr '\r' ' ');
    echo "lala: $MONGO_CHECK_DB_MQI"

    if [ "${MONGO_CHECK_DB_MQI}" != "" ] && [ "${MONGO_CHECK_DB_JOB}" != "" ];
    then
        COLOR_SEQUENCE="${GREEN}";
    fi;
    if [ $(echo "${MONGO_DB_LIST}" | grep "${MONGO_DB}" | wc -l) -eq 1 ];
    then
        COLOR_DBS="${GREEN}";
    fi;
    if [ $(echo "${MONGO_COL_LIST}" | grep sequence | wc -l) -eq 1 ] ;
    then
        COLOR_COL="${GREEN}";
    fi;
fi;
echo -e "${COLOR_PRIMARY} PRIMARY ${NC}  = ${MONGO_PRIMARY}
${COLOR_DBS} DBS ${NC} = ${MONGO_DB_LIST}
${COLOR_COL} COLLECTION ${NC} = ${MONGO_COL_LIST}
${COLOR_SEQUENCE} MQI  ${NC} = ${MONGO_CHECK_DB_MQI}
${COLOR_SEQUENCE} JOB  ${NC} = ${MONGO_CHECK_DB_JOB}";
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


MONGO_ROOT_USERNAME=$(kubectl -n ${NAMESPACE_PROCESSING} get secret ${MONGODB_SECRET_NAME} -o jsonpath='{.data.ROOT_USERNAME}' | base64 --decode)
MONGO_ROOT_PASSWORD=$(kubectl -n ${NAMESPACE_PROCESSING} get secret ${MONGODB_SECRET_NAME} -o jsonpath='{.data.ROOT_PASSWORD}' | base64 --decode)
MONGO_ROOT_CONNECTION_STRING="mongodb://${MONGO_ROOT_USERNAME}:${MONGO_ROOT_PASSWORD}@localhost:${MONGO_PORT}"

MONGO_USERNAME=$(kubectl -n ${NAMESPACE_PROCESSING} get secret ${MONGODB_SECRET_NAME} -o jsonpath='{.data.USERNAME}' | base64 --decode)
MONGO_PASSWORD=$(kubectl -n ${NAMESPACE_PROCESSING} get secret ${MONGODB_SECRET_NAME} -o jsonpath='{.data.PASSWORD}' | base64 --decode)
MONGO_CONNECTION_STRING="mongodb://${MONGO_USERNAME}:${MONGO_PASSWORD}@localhost:${MONGO_PORT}"

# FSI: Local workaround, don't use connection string at all.
MONGO_ROOT_CONNECTION_STRING=""

HELP="
mongo.sh CONF_FILE ACTION
CONF_FILE=path
ACTION=init|clean|check
";

case "${ACTION}" in
    "init")  mongo_init  ;;
    "clean") mongo_clean ;;
    "check") mongo_check ;;
    *) echo "${HELP}"    ;;
esac;

#############################################
##            END
#############################################
