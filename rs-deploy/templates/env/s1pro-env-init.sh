#!/bin/sh

# This script needs to be sourcing to specify the environment of the reference system.
# It allows to setup configuration that is needed for most of the scripts and describing
# location or pods. You can either source this file manually, but it is recommend
# to source it automatically in the .bashrc by using
#
# source ~/env/config/env/s1pro-env-init.sh
# 
# Mandatory configuration are marked with an asterix (*) other configuration
# shall be safe to use a given when using the same setup as used in S1PRO.
#

# dont continue on non interactive shells
if [ -z "$PS1" ]; then
        return
fi

# The following configurations are individual for each environment and are mandatory to be set

# (*) Location of the base directory of the environment. This is the env being setup with the rs_init script
# e.g. /home/user/env
export LOCATION=
# (*) Location of the environment specific configuration
# e.g. /home/user/env/config
export ENV_DIR=$LOCATION/config




# The following configurations are set to default configuration as used in S1PRO and shall
# normally not required to be modified if the same environment is used.

# Extend the search path so that the scripts can be directly used
export PATH=$LOCATION/werum/scripts:$LOCATION/werum/rs:$ENV_DIR/:$ENV_DIR/service:$PATH

# Specify the namespace where S1PRO services are running
export S1PRO_NAMESPACE="processing"
# Specify the namespace where kafka is running
# WARNING! This ia duplicate existing in kafka wrapper config as well!
#export KAFKA_NAMESPACE="infra"
export KAFKA_NAMESPACE="processing"
# Specify the namespace where elastic search is running
export ELASTICSEARCH_NAMESPACE="monitoring"

# MongoDB specific configuration
source ${ENV_DIR}/wrapper/mongo.sh
export MONGODB_SECRET_NAME="mongodb";

# Setting additional options for HELM (e.g. TLS)
export HELM_TLS="--tls"

# Specify the elastic search pod that is used to initialize the elastic search cluster indices
export ELASTICSEARCH_POD="elasticsearch-trace-elasticsearch-master-1"

# Specify the url where elastic search can be reached from the cluster. Expecting service name and port.
export ELASTICSEARCH_URL="elasticsearch-trace-elasticsearch-coordinating-only:9200"

# Specify the kafka pod that is used to initialize the kafka topics
export KAFKA_POD="kafka-0"

# Specify the prefix for OBS Buckets

#WERUM_CLUSTER_NUMBER=$(hostname | cut -d '-' -f2 | cut -d 'c' -f2)
#[[ $WERUM_CLUSTER_NUMBER =~ ^[0-9]*$ ]] || echo "**** WARNING: INVALID CLUSTER NUMBER: ${WERUM_CLUSTER_NUMBER} ****"
export OBS_PREFIX="rs" # TAI: Duplicate from s3 wrapper config

# Specify the container from the kafka pod that shall be accesed
export KAFKA_CONTAINER="kafka"

# Specifies the location of the kafka topic script within the container
export KAFKA_TOPIC_PATH="/opt/kafka/bin/kafka-topics.sh"
# Specifies the url where kafka can be reached from the cluster. Expecting service name and port.
export KAFKA_URL="kafka-headless:9092"

# Pointing to a list containing the zookeeper instances within the cluster
export ZOOKEEPER_LIST="zookeeper-0.zookeeper-headless.pdgs.svc.cluster.local:2181,zookeeper-1.zookeeper-headless.pdgs.svc.cluster.local:2181,zookeeper-2.zookeeper-headless.pdgs.svc.cluster.local:2181"

#add some request repo aliases for convenience
alias fr='kubectl -n $S1PRO_NAMESPACE -n $S1PRO_NAMESPACE exec -it s1pro-request-repository-0 -- wget -q -O - --header=ApiKey:LdbEo2020tffcEGS http://s1pro-api-svc/api/v1/failedProcessings'
alias p='kubectl -n $S1PRO_NAMESPACE -n  $S1PRO_NAMESPACE exec -it s1pro-request-repository-0 -- wget -q -O - --header=ApiKey:LdbEo2020tffcEGS http://s1pro-api-svc/api/v1/processings'
alias frb='fr | egrep "summary|^  \"id\" "'

# add some obs aliases for convenience
alias sl='s3cmd ls'
alias s3test='s3cmd -c .s3cfg-testdata'
#alias collect_sl="\
#echo L0_SEG: \$(sl $(sl | grep l0-segments | grep -v zip  | tr -s ' ' | cut -d' ' -f3) | grep -v md5 | wc -l);\
#echo L0_SLI: \$(sl $(sl | grep l0-slices | grep -v zip  | tr -s ' ' | cut -d' ' -f3) | grep -v md5 | wc -l);\
#echo L0_ACN: \$(sl $(sl | grep l0-acns | grep -v zip  | tr -s ' ' | cut -d' ' -f3) | grep -v md5 | wc -l);\
#echo L1_SLI: \$(sl $(sl | grep l1-slices | grep -v zip  | tr -s ' ' | cut -d' ' -f3) | grep -v md5 | wc -l);\
#echo L1_ACN: \$(sl $(sl | grep l1-acns | grep -v zip  | tr -s ' ' | cut -d' ' -f3) | grep -v md5 | wc -l);\
#echo L2_SLI: \$(sl $(sl | grep l2-slices | grep -v zip  | tr -s ' ' | cut -d' ' -f3) | grep -v md5 | wc -l);\
#echo L2_ACN: \$(sl $(sl | grep l2-acns | grep -v zip  | tr -s ' ' | cut -d' ' -f3) | grep -v md5 | wc -l);\
#"

#alias vim='vi'
# Adding some useful aliases
alias kp='kubectl -n $S1PRO_NAMESPACE get pods'
alias kl='kubectl -n $S1PRO_NAMESPACE logs'
alias ke='kubectl -n $S1PRO_NAMESPACE exec -it'
alias kpw='watch kubectl -n $S1PRO_NAMESPACE get pods'
alias kpc="kubectl get pods  -o custom-columns=Name:.metadata.name,Containers:.spec.containers[*].name"
alias kpn="kubectl get pods  -o custom-columns=Name:.metadata.name,IP:.status.podIP,Node:.spec.nodeName"
alias kph="kubectl get pods  -o custom-columns=Name:.metadata.name,hostName:.spec.hostname,domain:.spec.subdomain"
alias co='~/s1pro-validation/checkOutputs.sh'
alias cow='watch -c ~/s1pro-validation/checkOutputs.sh'
alias check_leaders='kubectl exec -n infra kafka-0 -c kafka -- kafka-topics.sh --describe --zookeeper zookeeper.infra:2181  | grep "Leader: none" | cut -d " " -f 2'
alias repair_leaders='for t in $(kubectl exec -n infra kafka-0 -c kafka -- kafka-topics.sh --describe --zookeeper zookeeper.infra:2181  | grep "Leader: none" | sed -e "s;\s; ;g" | cut -d " " -f3); do kafka_clean ${t}; done'
alias loguniq='cut -d"|" -f1 --complement | sort | uniq'

# workaround for elastic search
export elastic_host="elasticsearch-master"
export elastic_port="9200"

#kubectl config set-context --current --namespace=$S1PRO_NAMESPACE

# add cluster version to prompt
#export PS1="[\u@\h (\$(cd $LOCATION;  git rev-parse --abbrev-ref HEAD)) \W]\$ "
