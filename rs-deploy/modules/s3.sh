#!/bin/bash

if [ "${WERUM_IGNORE_S3}" == true ]
then
    echo "Ignoring S3 module"
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

function s3_print_result () {
    lcl_item="s3://$1";
    lcl_code="$2";
    lcl_result="$3";
    lcl_action="$4";
    if   [ ${lcl_code} -eq 0 ];
    then
        RESULT="${GREEN}OK${NC} | (code = ${lcl_code} - result = ${lcl_result})";
    else
        RESULT="${RED}KO${NC} | (code = ${lcl_code} - result = ${lcl_result})";
    fi;
    echo -e "${lcl_action} of ${lcl_item} | ${RESULT}";
}

#############################################
##            INIT
#############################################
function s3_init () {
echo -e "${PURPLE}
#################################
#  S3 INIT ...
#################################${NC}";
S3_LIST=$(echo "${BUCKETS_ALL[@]}");
RESULT="";
for item in ${S3_LIST[@]};
do
    R=$(s3cmd --config=${S3_CONFIGURATION_FILE_PATH} mb s3://"${item}");
    D=$(s3_print_result ${item} $? "${R}" "CREATION");
    RESULT="${RESULT}
${D}";
done;
echo -e "${RESULT}" | column -t -s '|';

s3_check;
}

#############################################
##            RESET
#############################################
function s3_reset () {
echo -e "${PURPLE}
#################################
# S3 RESET ...
# (Exception = ${S3_EXCEPTION_RESET})
#################################${NC}";
S3_LIST=$(echo "${BUCKETS_ALL[@]}");
RESULT="";
for item in ${S3_LIST[@]};
do
    local lcl_EXCEPTION=$(echo "${S3_EXCEPTION_RESET}" | grep "${item}:" | awk -F ':' '{print $2}');
    if [ "${lcl_EXCEPTION}" != "" ];
    then
        cmd="s3cmd --config=${S3_CONFIGURATION_FILE_PATH} ls --recursive \"s3://${item}\" | awk '{print \$4}' | egrep -v \"${lcl_EXCEPTION}\"";
        #R=$(s3cmd --config=${S3_CONFIGURATION_FILE_PATH} rm $(s3cmd --config=${S3_CONFIGURATION_FILE_PATH} ls --recursive "s3://${item}" | awk '{print $4}' | egrep -v "${lcl_EXCEPTION}" | xargs) > /dev/null 2>&1);
    else
        cmd="s3cmd --config=${S3_CONFIGURATION_FILE_PATH} ls --recursive \"s3://${item}\" | awk '{print \$4}'";
        #R=$(s3cmd --config=${S3_CONFIGURATION_FILE_PATH} rm $(s3cmd --config=${S3_CONFIGURATION_FILE_PATH} ls --recursive "s3://${item}" | awk '{print $4}'                               | xargs) > /dev/null 2>&1);
    fi;
    # Compare number of objects to delete into bucket, before and after deletion
    # Loop until objects to delete are deleted, or until no more deletion is possible
    length_old=0;
    length_new=$(eval ${cmd} | wc -l);
    while [[ ${length_new} != ${length_old} && ${length_new} != 0 ]];
    do
        R=$(s3cmd --config=${S3_CONFIGURATION_FILE_PATH} rm $(eval ${cmd} | head -1000 | xargs) > /dev/null 2>&1);
        D=$(s3_print_result ${item} $? "${R}" "RESET");
        RESULT="${RESULT}
${D}";
        length_old=${length_new};
        length_new=$(eval ${cmd} | wc -l);
        echo "${item}: remaining number of objects to deleted is ${length_new}";
    done;
done;
echo -e "${RESULT}" | column -t -s '|';

s3_check;
}

#############################################
##            CLEAN
#############################################
function s3_clean () {
echo -e "${PURPLE}
#################################
#  S3 CLEAN ...
# (Exception = ${S3_EXCEPTION_CLEAN})
#################################${NC}";
S3_LIST=$(echo "${BUCKETS_ALL[@]}" | egrep -v "${S3_EXCEPTION_CLEAN}");
RESULT="";
for item in ${S3_LIST[@]};
do
    R=$(s3cmd --config=${S3_CONFIGURATION_FILE_PATH} ${S3_CLEAN} "s3://${item}" > /dev/null 2>&1);
    D=$(s3_print_result ${item} $? "${R}" "DELETION");
    RESULT="${RESULT}
${D}";
done;
echo -e "${RESULT}" | column -t -s '|';

s3_check;
}

#############################################
##            CHECK
#############################################
function s3_check () {
echo -e "${NC}
#  S3 CHECK ...${NC}";

RESULT="BUCKET | ORDER | COUNT | LOCATION";
S3_LIST=$(echo "${BUCKETS_ALL[@]}");
NB_BUCKET=$(echo "${S3_LIST}}" | wc -l);
CPT=0;
for item in ${S3_LIST[@]};
do
    CPT=$(echo "${CPT} + 1" | bc);
    CHECK=$(s3cmd --config=${S3_CONFIGURATION_FILE_PATH} info "s3://${item}" 2>&1);
    LOCATION=$(echo "${CHECK}" | head -2 | egrep -i "location" | awk '{print $2}');
    ERROR=$(   echo "${CHECK}" | head -2 | egrep -i "error"    | head -1);
    if [ $(echo "${ERROR}" | grep -i error | wc -l) -eq 0 ]; then COUNT=$(s3cmd --config=${S3_CONFIGURATION_FILE_PATH} ls "s3://${item}/" | wc -l); else COUNT="N/A"; fi;
    COLOR="${RED}";
    if [ $(echo "${LOCATION}" | grep -i "eu-west" | wc -l) -eq 1 ]; then D="${GREEN}${LOCATION}${NC}"; 
    else                                                                 D="${RED}${ERROR}${NC}";      fi;
    RESULT="${RESULT}
${item} | ${CPT} / ${NB_BUCKET} | ${COUNT} | ${D}";
done;
echo -e "${RESULT}" | column -t -s '|'
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
s3.sh CONF_FILE ACTION
CONF_FILE=path
ACTION=init|clean|check
";

case "${ACTION}" in
    "init")  s3_init  ;;
    "reset") s3_reset ;;
    "clean") s3_clean ;;
    "check") s3_check ;;
    *) echo "${HELP}" ;;
esac;

#############################################
##            END
#############################################
