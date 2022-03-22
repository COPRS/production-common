#!/bin/bash

if [ "${WERUM_IGNORE_ES}" == true ];
then
    echo "Ignoring elastic search module"
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

function es_print_result () {
    lcl_item="$1";
    lcl_result="$2";
    lcl_action="$3";
    if   [ $(echo "${lcl_result}" | grep acknowledged | grep true                      | wc -l) -eq 1 ]; then RESULT="${GREEN}OK${NC}";
    elif [ $(echo "${lcl_result}" | grep error        | grep index_not_found_exception | wc -l) -eq 1 ]; then RESULT="${ORANGE}OK${NC} not_found";
    else                                                                                                      RESULT="${RED}KO${NC} (${R})";
    fi;
    echo -e "${lcl_action} of ${lcl_item} | ${RESULT}";
}

#############################################
##            INIT
#############################################
function es_init () {
RESULT="";
echo -e "${PURPLE}
#################################
#  ELASTICSEARCH INIT ...
# (Exception = ${ES_EXCEPTION})
# (Filter = ${ES_FILTER})
#################################${NC}";
for item in ${ES_INDICES_INPUT[@]};
do
    echo "Generating $item"
    echo "http://${ES_SVC}:${ES_PORT}/${item}"
    R=$(curl -vv -XPUT "http://${ES_SVC}:${ES_PORT}/${item}" -H 'Content-Type: application/json' -d '{"mappings":{"properties":{"creationTime":{"type":"date"},"insertionTime":{"type":"date"}}}}');
    D=$(es_print_result "${item}" "${R}" "CREATION");
    RESULT="${RESULT}
${D}";
done;
 
for item in ${ES_INDICES_TMP[@]};
do
    echo "Generating $item"
    R=$(curl -XPUT "http://${ES_SVC}:${ES_PORT}/${item}" -H 'Content-Type: application/json' -d '{"mappings":{"properties":{"creationTime":{"type":"date"},"insertionTime":{"type":"date"},"instrumentConfigurationId":{"type":"long"},"missionId":{"type":"text","fields":{"keyword":{"type":"keyword","ignore_above":256}}},"productFamily":{"type":"text","fields":{"keyword":{"type":"keyword","ignore_above":256}}},"productName":{"type":"text","fields":{"keyword":{"type":"keyword","ignore_above":256}}},"productType":{"type":"text","fields":{"keyword":{"type":"keyword","ignore_above":256}}},"satelliteId":{"type":"text","fields":{"keyword":{"type":"keyword","ignore_above":256}}},"site":{"type":"text","fields":{"keyword":{"type":"keyword","ignore_above":256}}},"url":{"type":"text","fields":{"keyword":{"type":"keyword","ignore_above":256}}},"validityStartTime":{"type":"date"},"validityStopTime":{"type":"date"}}}}' 2>&1);
    D=$(es_print_result ${item} "${R}" "CREATION");
    RESULT="${RESULT}
${D}";
done;
 
for item in ${ES_INDICES_SEGMENTS[@]};
do
    R=$(curl -XPUT "http://${ES_SVC}:${ES_PORT}/${item}" -H 'Content-Type: application/json' -d '{"mappings":{"properties":{"insertionTime":{"type":"date"},"segmentCoordinates":{"type":"geo_shape","tree":"geohash"}}}}' 2>&1);
    D=$(es_print_result "${item}" "${R}" "CREATION");
    RESULT="${RESULT}
${D}";
done;

for item in ${ES_INDICES_PRODUCTS[@]};
do
    R=$(curl -XPUT "http://${ES_SVC}:${ES_PORT}/${item}" -H 'Content-Type: application/json' -d '{"mappings":{"properties":{"creationTime":{"type":"date"},"startTime":{"type":"date"},"sliceCoordinates":{"type":"geo_shape","tree":"geohash"},"oqcFlag":{"type":"text"}}}}' 2>&1);
    D=$(es_print_result "${item}" "${R}" "CREATION");
    RESULT="${RESULT}
${D}";
done;

for item in ${ES_INDICES_EWSLCMASK[@]};
do
    R=$(curl -XPUT "http://${ES_SVC}:${ES_PORT}/${item}" -H 'Content-Type: application/json' -d '{"mappings":{"properties":{"geometry":{"type":"geo_shape","tree":"geohash"}}}}' 2>&1);
    D=$(es_print_result "${item}" "${R}" "CREATION");
    RESULT="${RESULT}
${D}";
done;

for item in ${ES_INDICES_LANDMASK[@]};
do
    R=$(curl -XPUT "http://${ES_SVC}:${ES_PORT}/${item}" -H 'Content-Type: application/json' -d '{"mappings":{"properties":{"geometry":{"type":"geo_shape","tree":"geohash"}}}}' 2>&1);
    D=$(es_print_result "${item}" "${R}" "CREATION");
    RESULT="${RESULT}
${D}";
done;

for item in ${ES_INDICES_OCEANMASK[@]};
do
    R=$(curl -XPUT "http://${ES_SVC}:${ES_PORT}/${item}" -H 'Content-Type: application/json' -d '{"mappings":{"properties":{"geometry":{"type":"geo_shape","tree":"geohash"}}}}' 2>&1);
    D=$(es_print_result "${item}" "${R}" "CREATION");
    RESULT="${RESULT}
${D}";
done;

for item in ${ES_INDICES_OVERPASSMASK[@]};
do
    R=$(curl -XPUT "http://${ES_SVC}:${ES_PORT}/${item}" -H 'Content-Type: application/json' -d '{"mappings":{"properties":{"geometry":{"type":"geo_shape","tree":"geohash"}}}}' 2>&1);
    D=$(es_print_result "${item}" "${R}" "CREATION");
    RESULT="${RESULT}
${D}";
done;

for item in ${ES_INDICES_PRIP[@]};
do
    R=$(curl -XPUT "http://${ES_SVC}:${ES_PORT}/${item}" -H 'Content-Type: application/json' -d '{"mappings":{"properties":{"id": {"type":"keyword"},"obsKey":{"type":"keyword"},"name":{"type":"keyword"},"productFamily":{"type":"keyword"},"contentType":{"type":"keyword"},"contentLength":{"type":"long"},"contentDateStart":{"type":"date"},"contentDateEnd":{"type":"date"},"creationDate":{"type":"date"},"evictionDate":{"type":"date"},"checksum":{"type":"nested","properties":{"algorithm":{"type":"keyword"},"value":{"type":"keyword"},"checksum_date":{"type":"date"}}},"footprint":{"type":"geo_shape","tree":"geohash"}}}}' 2>&1);
    D=$(es_print_result "${item}" "${R}" "CREATION");
    RESULT="${RESULT}
${D}";
done;

for item in ${ES_INDICES_DATA_LIFECYCLE_METADATA[@]};
do
	R=$(curl -XPUT "http://${ES_SVC}:${ES_PORT}/${item}" -H 'Content-Type: application/json' -d '{"mappings":{"properties":{"ProductName":{"type":"text","analyzer":"keyword","fields":{"keyword":{"type":"keyword","ignore_above":1024}}},"ProductFamilyInUncompressedStorage":{"type":"text","fields":{"keyword":{"type":"keyword","ignore_above":256}}},"ProductFamilyInCompressedStorage":{"type":"text","fields":{"keyword":{"type":"keyword","ignore_above":256}}},"PathInUncompressedStorage":{"type":"text","analyzer":"keyword","fields":{"keyword":{"type":"keyword","ignore_above":2048}}},"PathInCompressedStorage":{"type":"text","analyzer":"keyword","fields":{"keyword":{"type":"keyword","ignore_above":2048}}},"EvictionDateInUncompressedStorage":{"type":"date"},"EvictionDateInCompressedStorage":{"type":"date"},"LastInsertionInUncompressedStorage":{"type":"date"},"LastInsertionInCompressedStorage":{"type":"date"},"PersistentInUncompressedStorage":{"type":"boolean"},"PersistentInCompressedStorage":{"type":"boolean"},"AvailableInLta":{"type":"boolean"},"LastModified":{"type":"date"},"LastDataRequest":{"type":"date"}}}}' 2>&1);
    D=$(es_print_result "${item}" "${R}" "CREATION");
    RESULT="${RESULT}
${D}";
done;

echo -e "${RESULT}" | column -t -s '|';

es_check; 
}


#############################################
##            CLEAN
#############################################
function es_clean () {
echo -e "${PURPLE}
#################################
#  ELASTICSEARCH CLEAN ...
# (Exception = ${ES_EXCEPTION})
# (Filter = ${ES_FILTER})
#################################${NC}";
RESULT="";
ES_LIST=$(echo "${ES_ALL}" | egrep -v "${ES_EXCEPTION}");
for item in ${ES_LIST[@]};
do
    R=$(curl -X DELETE "http://${ES_SVC}:${ES_PORT}/${item}" 2>&1);
    D=$(es_print_result "${item}" "${R}" "DELETION");
    RESULT="${RESULT}
${D}";
done;

echo -e "${RESULT}" | column -t -s '|'; 

es_check;
}

#############################################
##            CHECK
#############################################
function es_check () {
echo -e "${NC}
#  ELASTICSEARCH CHECK ...
# (Exception = ${ES_EXCEPTION})
# (Filter = ${ES_FILTER})
${NC}";
RESULT="INDICE | ORDER | STATUS";
GET=$(curl -X GET "http://${ES_SVC}:${ES_PORT}/_cat/indices" 2>&1 | sort -r);
NB_INDICES=$(echo "${ES_ALL}" | wc -l);
CPT=0;
for item in ${ES_ALL[@]};
do
    CPT=$(echo "${CPT} + 1" | bc);
    CHECK=$(echo "${GET}" | grep -E " ${item} ");
    if [ $(echo "${CHECK}" | grep "open" | wc -l) -eq 1 ];
    then
        STATUS="${GREEN}OK${NC} (${CHECK})";
    else
        STATUS="${RED}KO${NC} (${CHECK})";
    fi;
    RESULT="${RESULT}
${item} | ${CPT} / ${NB_INDICES} | ${STATUS}";
done;

echo -e "${RESULT}" | column -t -s '|';
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
elasticsearch.sh CONF_FILE ACTION
CONF_FILE=path
ACTION=init|clean|check
";

case "${ACTION}" in
    "init")  es_init  ;;
    "clean") es_clean ;;
    "check") es_check ;;
    *) echo "${HELP}" ;;
esac;

#############################################
##            END
#############################################
