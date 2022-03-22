#!/bin/sh

for topic in `kubectl -n $KAFKA_NAMESPACE exec -it $KAFKA_POD -- sh $KAFKA_TOPIC_PATH --list --zookeeper $ZOOKEEPER_LIST | grep -v "consumer_offsets"`
do
  echo "Purging topic ... $topic"
   kubectl -n $KAFKA_NAMESPACE exec -ti $KAFKA_POD -- sh $KAFKA_TOPIC_PATH --delete --topic $topic --zookeeper $ZOOKEEPER_LIST
done
