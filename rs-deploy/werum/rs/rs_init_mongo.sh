#!/bin/bash

function mongo_init_rs () {
#MONGO_SVC="s1pro-mongodb-svc"

#echo $MONGO_POD_PREFIX
#echo $MONGO_SVC
#echo $MONGO_NAMESPACE

# TODO: No idea from where the wrong namespace is took, override
MONGO_NAMESPACE="processing"

function mongo_print_result () {     lcl_item="$1";     lcl_result="$2";     lcl_action="$3";     if   [ $(echo "${lcl_result}" |egrep "#2#|#s1pro" | wc -l) -eq 1 ]; then RESULT="${GREEN}OK${NC}";     else                                                                     RESULT="${RED}KO${NC}";     fi;     echo -e "${lcl_action} of ${lcl_item} | ${RESULT} (${lcl_result})"; }

INIT_RS='rs.initiate({_id :  "MainRepSet", members: [
{ _id: 0, host: "#HOST_0#" },
{ _id: 1, host: "#HOST_1#" },
{ _id: 2, host: "#HOST_2#" }
] })';
#INIT_RS=$(echo "${INIT_RS}" | sed "s/#HOST_0#/${MONGO_POD_PREFIX}-0.${MONGO_SVC}.${MONGO_NAMESPACE}.svc.cluster.local:27017/g");
#INIT_RS=$(echo "${INIT_RS}" | sed "s/#HOST_1#/${MONGO_POD_PREFIX}-1.${MONGO_SVC}.${MONGO_NAMESPACE}.svc.cluster.local:27017/g");
#INIT_RS=$(echo "${INIT_RS}" | sed "s/#HOST_2#/${MONGO_POD_PREFIX}-2.${MONGO_SVC}.${MONGO_NAMESPACE}.svc.cluster.local:27017/g");

INIT_RS=$(echo "${INIT_RS}" | sed "s/#HOST_0#/s1pro-mongodb-0.s1pro-mongodb-svc.processing.svc.cluster.local:27017/g");
INIT_RS=$(echo "${INIT_RS}" | sed "s/#HOST_1#/s1pro-mongodb-1.s1pro-mongodb-svc.processing.svc.cluster.local:27017/g");
INIT_RS=$(echo "${INIT_RS}" | sed "s/#HOST_2#/s1pro-mongodb-2.s1pro-mongodb-svc.processing.svc.cluster.local:27017/g");

echo -e "### Wait all mongo pods are running ...";
while [ $(kubectl -n ${MONGO_NAMESPACE} get pod | grep mongo | grep Running | grep "1/1" | wc -l) -ne 3 ];
do
    TMP=$(kubectl -n ${MONGO_NAMESPACE} get pod | grep mongo | tail -1);
    echo "tmp: $TMP"
    echo
    echo -ne "${TMP} \r";
    sleep 5;
done;
kubectl -n ${MONGO_NAMESPACE} wait --timeout=-1s --for condition=ready pods -l app=s1pro-mongodb

MONGO_POD=$(kubectl -n ${MONGO_NAMESPACE} get pod | grep ${MONGO_POD_PREFIX} | grep Running | head -1 | awk '{print $1}');
MONGO_PRIMARY=$(kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_POD} -- mongo --eval 'rs.status();' | grep PRIMARY -B 4 | head -1 | awk -F ':' '{print $2}' | awk -F '"' '{print $2}' | awk -F '.' '{print $1}');
MONGO_RETRY=0
MONGO_MAX_RETRIES=25
while [[ "${MONGO_PRIMARY}" == "" && ${MONGO_RETRY} -le ${MONGO_MAX_RETRIES} ]];
do
    if [ ${MONGO_RETRY} -gt 0 ]; then
       echo "Retry #${MONGO_RETRY} to setup MongoDB replication"
    fi
    MONGO_RETRY=$((MONGO_RETRY+1))
    #kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_POD} -- mongo ${MONGO_DB} --eval "${INIT_RS}"
    echo kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_POD} -- mongo ${MONGO_DB} --eval "${INIT_RS}"
    R=$(kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_POD} -- mongo ${MONGO_DB} --eval "${INIT_RS}");
    echo $R
    sleep 30;
    MONGO_PRIMARY=$(kubectl -n ${MONGO_NAMESPACE} exec -ti ${MONGO_POD} -- mongo --eval 'rs.status();' | grep PRIMARY -B 4 | head -1 | awk -F ':' '{print $2}' | awk -F '"' '{print $2}' | awk -F '.' '{print $1}');
    mongo_print_result "RS_INIT" "#${MONGO_PRIMARY}#" "CREATION"
    sleep 20;
done;
}

echo "RS Init Mongo DB"
mongo_init_rs
echo "Done."

