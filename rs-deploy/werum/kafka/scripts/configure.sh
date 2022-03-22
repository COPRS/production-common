#!/bin/bash
# Create and configure topics with the number of partitions
# The list of topic and partition in the file ./topic_partition.txt
# If the topic doesn't exist, the script create them
# If the topic exists but the number of partitions is not the same as defined in partition file, it will be deleted and re-created
# If the number of partition is correct, the topic isn't modified
#
# Usage : ./configure.sh

replicas=$(kubectl -n ${KAFKA_NAMESPACE} get statefulset kafka -o jsonpath='{.spec.replicas}')
dirbash="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
listtopicfile="$LOCATION/werum/kafka/topic_partition.txt"

# le while read of the file doesn't work (unable to execute a sh with kubectl -n $KAFKA_NAMESPACE) so I use a for
old_IFS=$IFS # sauvegarde du sÃ©parateur de champ
IFS=$'\n'


for ligne in $(cat $listtopicfile | grep -v "#")
do
        ligne=$(echo "$ligne" | tr '\t' ' ' | tr -s ' ' )
        topic=$(echo "$ligne" | cut -d " " -f 1)
        partitions=$(echo "$ligne" | cut -d " " -f 2)
        ModifTopic=0
        
        echo "Setting up ... $topic"

        #Get Number of actual partitions for topic
        partcount=$(kubectl -n $KAFKA_NAMESPACE exec -ti $KAFKA_POD -- sh $KAFKA_TOPIC_PATH --describe --topic $topic --zookeeper $ZOOKEEPER_LIST | grep PartitionCount | awk -F" " '{print $2}' | awk 'BEGIN {FS = ":"}{print $2}')
        #Check if exist
        if [ -z "${partcount}" ] ; then
                #The topic doesn't exist
                ModifTopic=1
        else
                #the topic exists check if the number of partition must be changed
                if [ $partcount != $partitions ]; then
                        #echo "Change number of partitions for topic $topic"
                        ModifTopic=1
                        kubectl -n $KAFKA_NAMESPACE exec -ti $KAFKA_POD -- sh $KAFKA_TOPIC_PATH --delete --topic $topic --zookeeper $ZOOKEEPER_LIST
                fi
        fi

        if [ $ModifTopic != 0 ] ; then
                echo "Creation of $topic with $partitions partitions"
                kubectl -n $KAFKA_NAMESPACE exec -ti $KAFKA_POD -- sh $KAFKA_TOPIC_PATH --create --topic $topic --zookeeper $ZOOKEEPER_LIST --replication-factor $replicas --partitions $partitions
        fi

done
IFS=$old_IFS

