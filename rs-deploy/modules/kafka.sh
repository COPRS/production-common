#!/bin/bash

if [ "${WERUM_IGNORE_KAFKA}" == true ]
then
    echo "Ignoring kafka module"
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

#############################################
##            INIT
#############################################
function kafka_init () {
local PROFILE_START=$(date +%s)
echo -e "${PURPLE}
#################################
#  KAFKA INIT ...
#################################${NC}";

if [[ "${WERUM_FAST_KAFKA_INIT}" == "true" ]]
then
    kafka_init_purge
fi

NB_PROC=6
SCRIPT_PATH=tmp/kafka_init
mkdir -p ${SCRIPT_PATH}
rm -f ${SCRIPT_PATH}/kafka_init_script*.sh ${SCRIPT_PATH}/kafka_init_topic*.out ${SCRIPT_PATH}/kafka_init_topic*.res
for (( PROC_NUM = $NB_PROC - 1; PROC_NUM >= 0; PROC_NUM-- )); do
    echo "#!/bin/bash" >${SCRIPT_PATH}/kafka_init_script${PROC_NUM}.sh
    chmod +x ${SCRIPT_PATH}/kafka_init_script${PROC_NUM}.sh
done

NB_TOPIC=$(echo "${TOPIC_LIST_DETAILS}" | wc -l)
CPT=0
PROC_NUM=0
export KAFKA_POD=$(kubectl -n ${KAFKA_NAMESPACE} get pod | grep ${KAFKA_POD_PREFIX} | grep Running | head -1 | awk '{print $1}');
lcl_REPLICA=$(kubectl -n ${KAFKA_NAMESPACE} get statefulset kafka -o jsonpath='{.spec.replicas}')
for line in ${TOPIC_LIST_DETAILS[@]};
do
    CPT=$(($CPT+1))
    SCRIPT=${SCRIPT_PATH}/kafka_init_script${PROC_NUM}.sh
    OUTPUT=${SCRIPT_PATH}/kafka_init_topic${CPT}-in-progress.out
    READY=${SCRIPT_PATH}/kafka_init_topic${CPT}.out
    RESULT=${SCRIPT_PATH}/kafka_init_topic${CPT}.res

    echo "##################################" >>${OUTPUT}
    echo "CURRENT TOPIC (${CPT} / ${NB_TOPIC}) = ${line}" >>${OUTPUT}
    
    lcl_TOPIC_NAME=$(     echo "${line}" | awk -F ':' '{print $1}');
    lcl_TOPIC_PART=$(     echo "${line}" | awk -F ':' '{print $2}');
    lcl_TOPIC_RETENTION=$(echo "${line}" | awk -F ':' '{print $3}');

    echo "lcl_TO_INIT=1" >>${SCRIPT}
    echo "DELETE_STATUS=\"${NC} NOT EXISTS ${NC}\"" >>${SCRIPT}
    echo "INIT_STATUS=\"${RED} UNKNOWN ${NC}\"" >>${SCRIPT}

#    echo "TOPIC: $lcl_TOPIC_NAME"

    if [[ "${WERUM_FAST_KAFKA_INIT}" != "true" ]]
    then
        echo "# Current check" >>${SCRIPT}
        echo "lcl_DOES_NOT_EXISTS=\$(kubectl -n \"${KAFKA_NAMESPACE}\" exec ${KAFKA_POD} -c ${KAFKA_CONTAINER} -- sh ${KAFKA_SCRIPT_TOPIC} --describe --topic ${lcl_TOPIC_NAME} --zookeeper ${ZOOKEEPER_LIST} | grep \"does not exist\" | head -1 | wc -l)" >>${SCRIPT}
        echo "lcl_CURRENT_PART=\$(kubectl -n \"${KAFKA_NAMESPACE}\" exec ${KAFKA_POD} -c ${KAFKA_CONTAINER} -- sh ${KAFKA_SCRIPT_TOPIC} --describe --topic ${lcl_TOPIC_NAME} --zookeeper ${ZOOKEEPER_LIST} | sed 's/ //g' | grep PartitionCount | awk -F\" \" '{print \$2}' | awk 'BEGIN {FS = \":\"}{print \$2}')" >>${SCRIPT}

        echo "# If exists => To delete" >>${SCRIPT}
        echo "if [ \${lcl_DOES_NOT_EXISTS} -ne 1 ]" >>${SCRIPT}
        echo "then" >>${SCRIPT}
        echo "    lcl_TO_INIT=0" >>${SCRIPT}
        #echo "    TOPIC_DELETE_1=\$(kubectl -n \"${KAFKA_NAMESPACE}\" exec ${KAFKA_POD} -c ${KAFKA_CONTAINER} -- sh ${KAFKA_SCRIPT_TOPIC} --delete --topic ${lcl_TOPIC_NAME} --zookeeper ${ZOOKEEPER_LIST} >/dev/null 2>&1)" >>${SCRIPT}
	echo "    TOPIC_DELETE_1=\$(kubectl -n \"${KAFKA_NAMESPACE}\" exec ${KAFKA_POD} -- sh ${KAFKA_SCRIPT_TOPIC} --delete --topic ${lcl_TOPIC_NAME} --zookeeper ${ZOOKEEPER_LIST})" >>${SCRIPT}	
        echo "    TOPIC_DELETE_2=\$(kubectl -n \"${KAFKA_NAMESPACE}\" exec zookeeper-0 -c zookeeper -- sh -c \"echo \\\"deleteall /brokers/topics/${lcl_TOPIC_NAME}\\\"      | /etc/zookeeper/bin/zkCli.sh\" )" >>${SCRIPT}
        echo "    TOPIC_DELETE_3=\$(kubectl -n \"${KAFKA_NAMESPACE}\" exec zookeeper-0 -c zookeeper -- sh -c \"echo \\\"deleteall /admin/delete_topics/${lcl_TOPIC_NAME}\\\" | /etc/zookeeper/bin/zkCli.sh\" )" >>${SCRIPT}
        echo "    lcl_CHECK_DELETE=\$(kubectl -n \"${KAFKA_NAMESPACE}\" exec ${KAFKA_POD} -c ${KAFKA_CONTAINER} -- sh ${KAFKA_SCRIPT_TOPIC} --describe --topic ${lcl_TOPIC_NAME} --zookeeper ${ZOOKEEPER_LIST} | grep \"does not exist\" | head -1 | wc -l)" >>${SCRIPT}
        echo "    if [ \${lcl_CHECK_DELETE} -eq 1 ];" >>${SCRIPT}
        echo "    then" >>${SCRIPT}
        echo "        lcl_TO_INIT=1" >>${SCRIPT}
        echo "        echo -e \"${ORANGE}OK${NC} : topic ${lcl_TOPIC_NAME} (current partition = \${lcl_CURRENT_PART}) has been deleted. (does not exist = \${lcl_CHECK_DELETE})\" >>${OUTPUT}" >>${SCRIPT}
        echo "        DELETE_STATUS=\"${ORANGE} DELETED ${NC}\"" >>${SCRIPT}
        echo "    else" >>${SCRIPT}
        echo "        echo -e \"${RED}KO${NC} : topic ${lcl_TOPIC_NAME} (current partition = \${lcl_CURRENT_PART}) has not been deleted. (does not exist = \${lcl_CHECK_DELETE})\" >>${OUTPUT}" >>${SCRIPT}
        echo "        DELETE_STATUS=\"${RED} NOT DELETED ${NC}\"" >>${SCRIPT}
        echo "    fi" >>${SCRIPT}
        echo "fi" >>${SCRIPT}
    fi

 #   echo $SCRIPT

    echo "# Initialization" >>${SCRIPT}
    echo "if [ \${lcl_TO_INIT} -eq 1 ]" >>${SCRIPT}
    echo "then" >>${SCRIPT}
#        echo "TOPIC_INIT=\$(kubectl -n \"${KAFKA_NAMESPACE}\" exec ${KAFKA_POD} -c ${KAFKA_CONTAINER} -- sh ${KAFKA_SCRIPT_TOPIC} --create --topic ${lcl_TOPIC_NAME} --zookeeper ${ZOOKEEPER_LIST} --replication-factor ${lcl_REPLICA} --partitions ${lcl_TOPIC_PART} >/dev/null 2>&1)" >>${SCRIPT}
#        echo "TOPIC_ALTER=\$(kubectl -n ${KAFKA_NAMESPACE} exec ${KAFKA_POD} -c ${KAFKA_CONTAINER} -- bash ${KAFKA_SCRIPT_CONFIG} --zookeeper ${ZOOKEEPER_LIST} --alter --entity-type topics --entity-name ${lcl_TOPIC_NAME} --add-config retention.ms=${lcl_TOPIC_RETENTION} >/dev/null 2>&1 | grep -v OpenJDK)" >>${SCRIPT}

        echo "TOPIC_INIT=\$(kubectl -n \"${KAFKA_NAMESPACE}\" exec ${KAFKA_POD} -- sh ${KAFKA_SCRIPT_TOPIC} --create --topic ${lcl_TOPIC_NAME} --zookeeper ${ZOOKEEPER_LIST} --replication-factor ${lcl_REPLICA} --partitions ${lcl_TOPIC_PART} )" >>${SCRIPT}
        echo "TOPIC_ALTER=\$(kubectl -n ${KAFKA_NAMESPACE} exec ${KAFKA_POD} -- bash ${KAFKA_SCRIPT_CONFIG} --zookeeper ${ZOOKEEPER_LIST} --alter --entity-type topics --entity-name ${lcl_TOPIC_NAME} --add-config retention.ms=${lcl_TOPIC_RETENTION} | grep -v OpenJDK)" >>${SCRIPT}


        echo "# New check" >>${SCRIPT}
        echo "lcl_CHECK=\$(kubectl -n \"${KAFKA_NAMESPACE}\" exec ${KAFKA_POD} -- sh ${KAFKA_SCRIPT_TOPIC} --describe --topic ${lcl_TOPIC_NAME} --zookeeper ${ZOOKEEPER_LIST} | sed 's/ //g')" >>${SCRIPT}
        echo "lcl_NEW_PART=\$(     echo \"\${lcl_CHECK}\" | grep PartitionCount | awk -F ' ' '{print \$2}' | awk 'BEGIN {FS = \":\"}{print \$2}')" >>${SCRIPT}
        echo "lcl_NEW_RETENTION=\$(echo \"\${lcl_CHECK}\" | grep PartitionCount | awk -F ' ' '{print \$4}' | tr '\r' '=' | awk -F '=' '{print \$2}')" >>${SCRIPT}

        echo "# VERIFICATION" >>${SCRIPT}
        echo "if [ \"\${lcl_NEW_PART}\" == \"${lcl_TOPIC_PART}\" ] && [ \"\${lcl_NEW_RETENTION}\" == \"${lcl_TOPIC_RETENTION}\" ]" >>${SCRIPT}
        echo "then" >>${SCRIPT}
        echo "    echo -e \"${GREEN}OK${NC} : topic ${lcl_TOPIC_NAME} has been initialized => ${lcl_TOPIC_PART} (${lcl_TOPIC_PART}) partitions and \${lcl_NEW_RETENTION}ms (${lcl_TOPIC_RETENTION}ms) retention.\" >>${OUTPUT}" >>${SCRIPT}
        echo "    INIT_STATUS=\"${GREEN} INITIALIZED ${NC} => ${lcl_TOPIC_PART} (${lcl_TOPIC_PART}) partitions and \${lcl_NEW_RETENTION}ms (${lcl_TOPIC_RETENTION}ms) retention.\"" >>${SCRIPT}
        echo "else" >>${SCRIPT}
        echo "    echo -e \"${RED}KO${NC} : topic ${lcl_TOPIC_NAME} has not been initialized => ${lcl_TOPIC_PART} (${lcl_TOPIC_PART}) partitions and \${lcl_NEW_RETENTION}ms (${lcl_TOPIC_RETENTION}ms) retention.\" >>${OUTPUT}" >>${SCRIPT}
        echo "    INIT_STATUS=\"${RED} NOT INITIALIZED ${NC} => ${lcl_TOPIC_PART} (${lcl_TOPIC_PART}) partitions and \${lcl_NEW_RETENTION}ms (${lcl_TOPIC_RETENTION}ms) retention.\"" >>${SCRIPT}
        echo "fi" >>${SCRIPT}
    echo "fi" >>${SCRIPT}
    
    echo "echo \"${lcl_TOPIC_NAME} | \${DELETE_STATUS} | \${INIT_STATUS}\" >>${RESULT}" >>${SCRIPT}
    
    echo "mv ${OUTPUT} ${READY}" >>${SCRIPT}
    
    PROC_NUM=$(($PROC_NUM+1))
    if [ $PROC_NUM -eq $NB_PROC ]; then
        PROC_NUM=0
    fi
done;

for (( PROC_NUM = $NB_PROC - 1; PROC_NUM >= 0; PROC_NUM-- )); do
  #$SCRIPT_PATH/kafka_init_script${PROC_NUM}.sh &>/dev/null &
  $SCRIPT_PATH/kafka_init_script${PROC_NUM}.sh &
done

echo "xxx"

for (( CPT = 1; CPT <= $NB_TOPIC; CPT++ )); do
    while [ ! -f ${SCRIPT_PATH}/kafka_init_topic${CPT}.out ]; do
        sleep 1
    done
    cat ${SCRIPT_PATH}/kafka_init_topic${CPT}.out
done

ALL_RESULTS=""
for (( CPT = 1; CPT <= $NB_TOPIC; CPT++ )); do
    ALL_RESULTS="${ALL_RESULTS}    
$(cat ${SCRIPT_PATH}/kafka_init_topic${CPT}.res)"
done

echo -e "${ALL_RESULTS}" | column -t -s '|';
rm -Rf ${SCRIPT_PATH}

echo Initialized Kafka topics in $(($(date +%s)-$PROFILE_START)) sec.
}

function kafka_init_purge ()
{
    echo "Skipping deletion of kafka partition during init because WERUM_FAST_KAFKA_INIT is set"
    echo "Instead purge kafka cluster ..."
    K_PODS=$(kubectl get pods -n "${KAFKA_NAMESPACE}" | awk '{print $1}' | grep -E "kafka|zookeeper")
    sudo su - rescue -c "/mnt/s1pdgs/scripts/cots/helm_kafka.sh install prod"
    echo "Kafka pods purged and restarted waiting for ready state ..."
    for K_POD in $K_PODS
    do
        kubectl -n "${KAFKA_NAMESPACE}" wait pod/"$K_POD" --for condition=ready
    done
    echo "Kafka purged and restarted"
}

#############################################
##            RESET
#############################################

function get_latest_messages_from_topic_count () 
{
    # usage: get_latest_messages_from_topic_count pdgs kafka-0 kafka kafka-headless.pdgs.svc.cluster.local:9092 t-pdgs-l1-acns-fast /opt/kafka/bin/kafka-run-class.sh

    local lcl_KAFKA_NAMESPACE=$1
    local lcl_KAFKA_POD_NAME=$2
    local lcl_KAFKA_CONTAINER=$3
    local lcl_KAFKA_BOOTSTRAP_SERVER_URI=$4
    local lcl_TOPIC_NAME=$5
    local lcl_RUN_CLASS_SCRIPT_PATH=$6
    local lcl_LATEST=$(kubectl -n ${lcl_KAFKA_NAMESPACE} exec -ti ${lcl_KAFKA_POD_NAME} -c ${lcl_KAFKA_CONTAINER} -- ${lcl_RUN_CLASS_SCRIPT_PATH} kafka.tools.GetOffsetShell --broker-list ${lcl_KAFKA_BOOTSTRAP_SERVER_URI} --topic ${lcl_TOPIC_NAME} --time -1 --offsets 1 | awk -F ":" '{sum += $3} END {print sum}')
    echo ${lcl_LATEST}
}

function get_earliest_messages_from_topic_count () 
{
    # usage: get_earliest_messages_from_topic_count pdgs kafka-0 kafka kafka-headless.pdgs.svc.cluster.local:9092 t-pdgs-l1-acns-fast /opt/kafka/bin/kafka-run-class.sh

    local lcl_KAFKA_NAMESPACE=$1
    local lcl_KAFKA_POD_NAME=$2
    local lcl_KAFKA_CONTAINER=$3
    local lcl_KAFKA_BOOTSTRAP_SERVER_URI=$4
    local lcl_TOPIC_NAME=$5
    local lcl_RUN_CLASS_SCRIPT_PATH=$6

    local lcl_EARLIEST=$(kubectl -n ${lcl_KAFKA_NAMESPACE} exec -ti ${lcl_KAFKA_POD_NAME} -c ${lcl_KAFKA_CONTAINER} -- ${lcl_RUN_CLASS_SCRIPT_PATH} kafka.tools.GetOffsetShell --broker-list ${lcl_KAFKA_BOOTSTRAP_SERVER_URI} --topic ${lcl_TOPIC_NAME} --time -2 --offsets 1 | awk -F ":" '{sum2 += $3} END {print sum2}')
    echo ${lcl_EARLIEST}
}

function get_messages_from_topic_count () 
{
    # usage: get_messages_from_topic_count pdgs kafka-0 kafka kafka-headless.pdgs.svc.cluster.local:9092 t-pdgs-l1-acns-fast /opt/kafka/bin/kafka-run-class.sh

    local lcl_KAFKA_NAMESPACE=$1
    local lcl_KAFKA_POD_NAME=$2
    local lcl_KAFKA_CONTAINER=$3
    local lcl_KAFKA_BOOTSTRAP_SERVER_URI=$4
    local lcl_TOPIC_NAME=$5
    local lcl_RUN_CLASS_SCRIPT_PATH=$6

    local lcl_LATEST=$(get_latest_messages_from_topic_count ${lcl_KAFKA_NAMESPACE} ${lcl_KAFKA_POD_NAME} ${lcl_KAFKA_CONTAINER} ${lcl_KAFKA_BOOTSTRAP_SERVER_URI} ${lcl_TOPIC_NAME} ${lcl_RUN_CLASS_SCRIPT_PATH})
    local lcl_EARLIEST=$(get_earliest_messages_from_topic_count ${lcl_KAFKA_NAMESPACE} ${lcl_KAFKA_POD_NAME} ${lcl_KAFKA_CONTAINER} ${lcl_KAFKA_BOOTSTRAP_SERVER_URI} ${lcl_TOPIC_NAME} ${lcl_RUN_CLASS_SCRIPT_PATH})
    local lcl_TOTAL=$(expr ${lcl_LATEST} - ${lcl_EARLIEST})
    echo ${lcl_TOTAL}
}

function wait_topic_empty ()
{

    # usage: wait_topic_empty pdgs kafka-0 kafka kafka-headless.pdgs.svc.cluster.local:9092 t-pdgs-l1-acns-fast /opt/kafka/bin/kafka-run-class.sh

    local lcl_KAFKA_NAMESPACE=$1
    local lcl_KAFKA_POD_NAME=$2
    local lcl_KAFKA_CONTAINER=$3
    local lcl_KAFKA_BOOTSTRAP_SERVER_URI=$4
    local lcl_TOPIC_NAME=$5
    local lcl_RUN_CLASS_SCRIPT_PATH=$6

    local lcl_TOPIC_EMPTY_CHECK=''

    while [[ "${lcl_TOPIC_EMPTY_CHECK}" != "0" ]]
    do
            lcl_TOPIC_EMPTY_CHECK=$(get_messages_from_topic_count ${lcl_KAFKA_NAMESPACE} ${lcl_KAFKA_POD_NAME} ${lcl_KAFKA_CONTAINER} ${lcl_KAFKA_BOOTSTRAP_SERVER_URI} ${lcl_TOPIC_NAME} ${lcl_RUN_CLASS_SCRIPT_PATH})
    done
    echo ${lcl_TOPIC_NAME} ${lcl_TOPIC_EMPTY_CHECK}
}

function kafka_reset () {

echo -e "${PURPLE}
#################################
#  KAFKA RESET ...
#################################${NC}";

echo -e "${PURPLE}
#################################
#  KAFKA set retention to 1ms ...
#################################${NC}";
NB_TOPIC=$(echo "${TOPIC_LIST_DETAILS}" | wc -l);
CPT=0;
export KAFKA_POD=$(kubectl -n ${KAFKA_NAMESPACE} get pod | grep ${KAFKA_POD_PREFIX} | grep Running | head -1 | awk '{print $1}');
for line in ${TOPIC_LIST_DETAILS[@]};
do
    CPT=$(echo "${CPT} + 1" | bc);
    echo "CURRENT TOPIC (${CPT} / ${NB_TOPIC}) = ${line}";
    lcl_TOPIC_NAME=$(      echo "${line}" | awk -F ':' '{print $1}');
    lcl_TOPIC_PART=$(      echo "${line}" | awk -F ':' '{print $2}');
    lcl_TOPIC_RETENTION=$( echo "${line}" | awk -F ':' '{print $3}');

    # RETENTION to 1ms
    kubectl -n ${KAFKA_NAMESPACE} exec -ti ${KAFKA_POD} -c ${KAFKA_CONTAINER} -- bash ${KAFKA_SCRIPT_CONFIG} --zookeeper ${ZOOKEEPER_LIST} --alter --entity-type topics --entity-name ${lcl_TOPIC_NAME} --add-config retention.ms=1 | grep -v OpenJDK 2>/dev/null;
done;

echo -e "${PURPLE}
#################################
#  KAFKA waiting empty topic ...
#################################${NC}";
CPT=0;
for line in ${TOPIC_LIST_DETAILS[@]};
do
    CPT=$(echo "${CPT} + 1" | bc);
    echo "CURRENT TOPIC (${CPT} / ${NB_TOPIC}) = ${line}";
    lcl_TOPIC_NAME=$(      echo "${line}" | awk -F ':' '{print $1}');
    wait_topic_empty ${KAFKA_NAMESPACE} ${KAFKA_POD} ${KAFKA_CONTAINER} ${KAFKA_URL} ${lcl_TOPIC_NAME} ${KAFKA_RUN_CLASS_SCRIPT_TOPIC}
done;

echo -e "${PURPLE}
#################################
#  KAFKA set original retention ...
#################################${NC}";
CPT=0;
for line in ${TOPIC_LIST_DETAILS[@]};
do
    CPT=$(echo "${CPT} + 1" | bc);
    echo "CURRENT TOPIC (${CPT} / ${NB_TOPIC}) = ${line}";    
    lcl_TOPIC_NAME=$(      echo "${line}" | awk -F ':' '{print $1}');
    lcl_TOPIC_PART=$(      echo "${line}" | awk -F ':' '{print $2}');
    lcl_TOPIC_RETENTION=$( echo "${line}" | awk -F ':' '{print $3}');

    # RETENTION back to original
    kubectl -n ${KAFKA_NAMESPACE} exec -ti ${KAFKA_POD} -c ${KAFKA_CONTAINER} -- bash ${KAFKA_SCRIPT_CONFIG} --zookeeper ${ZOOKEEPER_LIST} --alter --entity-type topics --entity-name ${lcl_TOPIC_NAME} --add-config retention.ms=${lcl_TOPIC_RETENTION} 2>/dev/null | grep -v OpenJDK;
done;
}

#############################################
##            CLEAN
#############################################
function kafka_clean () {
echo -e "${PURPLE}
#################################
#  KAFKA CLEAN ...
#################################${NC}";
if [[ "${WERUM_SKIP_KAFKA_CLEAN}" == "true" ]]
then
  echo "Skipping kafka clean because parameter WERUM_SKIP_KAFKA_CLEAN is set ..."
  return
fi
#kafka_clean_hard;
kafka_clean_soft;
}

function kafka_clean_hard () {
# SCALE UP
kubectl -n ${KAFKA_NAMESPACE} scale sts kafka     --replicas=0
kubectl -n ${KAFKA_NAMESPACE} scale sts zookeeper --replicas=0
 
#kubectl wait -n ${KAFKA_NAMESPACE} --timeout=-1s --for delete pod -l app.kubernetes.io/name=kafka;
#kubectl wait -n ${KAFKA_NAMESPACE} --timeout=-1s --for delete pod -l app.kubernetes.io/name=zookeeper

# DELETE PVC
PVC_ZK=$(   kubectl -n ${KAFKA_NAMESPACE} get pvc | grep data-zookeeper | awk '{print $1}')
PVC_KAFKA=$(kubectl -n ${KAFKA_NAMESPACE} get pvc | grep data-kafka     | awk '{print $1}')
 
for item in ${PVC_ZK[@]};    do   kubectl -n ${KAFKA_NAMESPACE} delete pvc ${item}; done;
for item in ${PVC_KAFKA[@]}; do   kubectl -n ${KAFKA_NAMESPACE} delete pvc ${item}; done;
 
#kubectl wait -n ${KAFKA_NAMESPACE} --timeout=-1s --for delete pvc -l app.kubernetes.io/name=kafka;
#kubectl wait -n ${KAFKA_NAMESPACE} --timeout=-1s --for delete pvc -l app.kubernetes.io/name=zookeeper;
  
# SCALE UP
kubectl -n ${KAFKA_NAMESPACE} scale sts kafka     --replicas=3
#kubectl wait -n ${KAFKA_NAMESPACE} --timeout=-1s --for condition=ready pod -l app.kubernetes.io/name=kafka
 
kubectl -n ${KAFKA_NAMESPACE} scale sts zookeeper --replicas=3
#kubectl wait -n ${KAFKA_NAMESPACE} --timeout=-1s --for condition=ready pod -l app.kubernetes.io/name=zookeeper
}

function kafka_clean_soft () {
NB_TOPIC=$(echo "${TOPIC_LIST}" | wc -l);
CPT=0;
RESULT="";
export KAFKA_POD=$(kubectl -n ${KAFKA_NAMESPACE} get pod | grep ${KAFKA_POD_PREFIX} | grep Running | head -1 | awk '{print $1}');
# PARSE TOPIC LIST
for CURRENT_TOPIC in ${TOPIC_LIST[@]};
do
    CPT=$(echo "${CPT} + 1" | bc);
    lcl_TOPIC_NAME="${CURRENT_TOPIC}";
    echo "##################################";
    echo "CURRENT TOPIC (${CPT} / ${NB_TOPIC}) = ${lcl_TOPIC_NAME}";

    # DELETION
    TOPIC_DELETE_1=$(kubectl -n "${KAFKA_NAMESPACE}" exec -ti ${KAFKA_POD} -c ${KAFKA_CONTAINER} -- sh ${KAFKA_SCRIPT_TOPIC} --delete --topic ${lcl_TOPIC_NAME} --zookeeper ${ZOOKEEPER_LIST} >/dev/null 2>&1);
    TOPIC_DELETE_2=$(kubectl -n "${KAFKA_NAMESPACE}" exec -ti zookeeper-0 -c zookeeper -- sh -c "echo \"deleteall /brokers/topics/${lcl_TOPIC_NAME}\"      | /etc/zookeeper/bin/zkCli.sh" >/dev/null 2>&1);
    TOPIC_DELETE_3=$(kubectl -n "${KAFKA_NAMESPACE}" exec -ti zookeeper-0 -c zookeeper -- sh -c "echo \"deleteall /admin/delete_topics/${lcl_TOPIC_NAME}\" | /etc/zookeeper/bin/zkCli.sh" >/dev/null 2>&1);
    
    lcl_CHECK_DELETE_FULL=$(kubectl -n "${KAFKA_NAMESPACE}" exec -ti ${KAFKA_POD} -c ${KAFKA_CONTAINER} -- sh ${KAFKA_SCRIPT_TOPIC} --describe --topic ${lcl_TOPIC_NAME} --zookeeper ${ZOOKEEPER_LIST} | grep "does not exist" | head -1 | tr '\r' ' ');
    lcl_CHECK_DELETE_WC=$(echo "${lcl_CHECK_DELETE_FULL}" | head -1 | wc -l);

    # VERIFICATION
    if [ ${lcl_CHECK_DELETE_WC} -eq 1 ];
    then
        D="${lcl_TOPIC_NAME} | ${GREEN} DELETED ${NC} (${lcl_CHECK_DELETE_FULL})";
    else
        D="${lcl_TOPIC_NAME} | ${RED} NOT DELETED ${NC} (${lcl_CHECK_DELETE_FULL})";
    fi;
    echo -e "${D}";
    RESULT="${RESULT}
${D}";
done;
echo "";
echo -e "${RESULT}" | column -t -s '|';
}

#############################################
##            CHECK
#############################################
function kafka_check () {
echo -e "${NC}
#  KAFKA CHECK ...${NC}";
# Get TOPIC LIST 
RESULT="TOPIC|ORDER|PARTITION|RETENTION|MESSAGES";
NB_TOPIC=$(echo "${TOPIC_LIST_DETAILS}" | wc -l);
CPT=0;
export KAFKA_POD=$(kubectl -n ${KAFKA_NAMESPACE} get pod | grep ${KAFKA_POD_PREFIX} | grep Running | head -1 | awk '{print $1}');
for line in ${TOPIC_LIST_DETAILS[@]};
do
    CPT=$(echo "${CPT} + 1" | bc);
    lcl_TOPIC_NAME=$(     echo "${line}" | awk -F ':' '{print $1}');
    lcl_TOPIC_PARTITION=$(echo "${line}" | awk -F ':' '{print $2}');
    lcl_TOPIC_RETENTION=$(echo "${line}" | awk -F ':' '{print $3}');

    # Retrieve number of messages
    local lcl_EARLIEST_MESSAGE=$(kubectl -n ${KAFKA_NAMESPACE} exec ${KAFKA_POD} -c ${KAFKA_CONTAINER} -- bash /opt/kafka/bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list ${KAFKA_URL} --topic ${lcl_TOPIC_NAME} --time -2 --offsets 1 2>/dev/null | grep -v OpenJDK | awk -F ":" '{sum2 += $3} END {print sum2}')
    local lcl_LATEST_MESSAGE=$(kubectl -n ${KAFKA_NAMESPACE} exec ${KAFKA_POD} -c ${KAFKA_CONTAINER} -- bash /opt/kafka/bin/kafka-run-class.sh kafka.tools.GetOffsetShell --broker-list ${KAFKA_URL} --topic ${lcl_TOPIC_NAME} --time -1 --offsets 1 2>/dev/null | grep -v OpenJDK | awk -F ":" '{sum += $3} END {print sum}')
    local lcl_TOTAL_MESSAGES=$(expr ${lcl_LATEST_MESSAGE} - ${lcl_EARLIEST_MESSAGE})

    echo -e "CURRENT TOPIC CHECKED (${CPT} / ${NB_TOPIC}) = ${ORANGE} ${lcl_TOPIC_NAME} ${NC} (${lcl_TOPIC_PARTITION} - ${lcl_TOPIC_RETENTION}ms)";
    lcl_TOPIC_DESCRIBE=$(kubectl exec -n ${KAFKA_NAMESPACE} ${KAFKA_POD} -c ${KAFKA_CONTAINER} -- ${KAFKA_SCRIPT_TOPIC} --zookeeper ${ZOOKEEPER_LIST} --describe --topic ${lcl_TOPIC_NAME} 2> /dev/null | grep -v OpenJDK | sed 's/ //g');
    lcl_PARTITION=$(echo "${lcl_TOPIC_DESCRIBE}" | grep PartitionCount | awk -F " " '{print $2}' | awk -F ':' '{print $2}');
    lcl_RETENTION=$(echo "${lcl_TOPIC_DESCRIBE}" | grep retention.ms   | awk -F " " '{print $4}' | awk -F '=' '{print $2}');
    COLOR_PARTITION="${RED}"; if [ "${lcl_PARTITION}" == "${lcl_TOPIC_PARTITION}" ]; then COLOR_PARTITION="${GREEN}"; fi;
    COLOR_RETENTION="${RED}"; if [ "${lcl_RETENTION}" == "${lcl_TOPIC_RETENTION}" ]; then COLOR_RETENTION="${GREEN}"; fi;
    echo -e "${lcl_TOPIC_NAME} | ${COLOR_PARTITION} ${lcl_PARTITION} (${lcl_TOPIC_PARTITION}) ${NC} | ${COLOR_RETENTION} ${lcl_RETENTION}ms (${lcl_TOPIC_RETENTION}ms) ${NC} | ${lcl_TOTAL_MESSAGES}";
    RESULT="${RESULT}
${lcl_TOPIC_NAME} | ${CPT} / ${NB_TOPIC} | ${COLOR_PARTITION} ${lcl_PARTITION} (${lcl_TOPIC_PARTITION}) ${NC} | ${COLOR_RETENTION} ${lcl_RETENTION}ms (${lcl_TOPIC_RETENTION}ms) ${NC} | ${lcl_TOTAL_MESSAGES}";
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
kafka.sh CONF_FILE ACTION
CONF_FILE=path
ACTION=init|reset|clean|check
";

case "${ACTION}" in
    "init")  kafka_init  ;;
    "reset") kafka_reset ;;
    "clean") kafka_clean ;;
    "check") kafka_check ;;
    *) echo "${HELP}"    ;;
esac;

#############################################
##            END
#############################################
