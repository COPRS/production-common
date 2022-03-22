#!/bin/bash
# Documenting how to label the nodes in the werum cluster to allow scheduling of S1PRO software...
kubectl label node s3-master node-role.kubernetes.io/worker=infra
kubectl label node s3-node1 node-role.kubernetes.io/worker=infra
kubectl label node s3-node2 node-role.kubernetes.io/worker=infra
kubectl label node s3-node3 node-role.kubernetes.io/worker=infra

# Might be naive, but we don't care about the labeling at all. All nodes can handle everything.
#for node in s3-node{1..3}
#do
#  echo "Labeling $node..."
#  kubectl label node $node node-role.kubernetes.io/processor=zip
#  kubectl label node $node node-role.kubernetes.io/processor=aio
#  kubectl label node $node node-role.kubernetes.io/processor=asp
#  kubectl label node $node node-role.kubernetes.io/processor=l1
#  kubectl label node $node node-role.kubernetes.io/processor --overwrite
#done

kubectl label node s3-node3 node-role.kubernetes.io/processor=zip --overwrite

kubectl label node s3-node2 node-role.kubernetes.io/gateway=std --overwrite
