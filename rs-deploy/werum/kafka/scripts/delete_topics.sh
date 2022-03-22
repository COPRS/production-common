#!/bin/bash
# Delete some or all topics
# If there are parameters, the topic in parameters are deleted else the list of topic in the file ./topic_partition.txt are deleted
#
#
# Usage :       ./delete_topics.sh
#                       ./delete_topics.sh topic1 topic2



dirbash="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
listtopicfile="$LOCATION/werum/kafka/topic_partition.txt"
# le while read of the file doesn't work (unable to execute a sh with kubectl -n $KAFKA_NAMESPACE) so I use a for


verifydeletetopic()
{
        topic="${1}"
        existtopic=$(kubectl -n $KAFKA_NAMESPACE exec -ti $KAFKA_POD -- sh $KAFKA_TOPIC_PATH --describe --topic $topic --zookeeper $ZOOKEEPER_LIST)
        #Check if exist
        if [ -z "${existtopic}" ] ; then
                #The topic doesn't exist
                echo "The topic $topic doesn't exist"
        else
                echo "- deleting $topic"
                kubectl -n $KAFKA_NAMESPACE exec -ti $KAFKA_POD -- sh $KAFKA_TOPIC_PATH --delete --topic $topic --zookeeper $ZOOKEEPER_LIST
        fi
}


#If we want to delete specific topic
if [ $# -ne 0 ]; then
        for topic in $*
        do
                verifydeletetopic $topic
        done
else
        old_IFS=$IFS # sauvegarde du sÃ©parateur de champ
        IFS=$'\n'
        for ligne in $(cat $listtopicfile | grep -v "#")
        do
                ligne=$(echo "$ligne" | tr '\t' ' ' | tr -s ' ' )
                topic=$(echo "$ligne" | cut -d " " -f 1)
                partitions=$(echo "$ligne" | cut -d " " -f 2)
                verifydeletetopic $topic
        done
        IFS=$old_IFS
fi
