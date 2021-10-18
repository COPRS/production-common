#!/bin/bash

# A simple script / documentation to deploy or undeploy elastic search in the werum cluster
# 2021-08-05 FSi

if [ "$#" -ne 1 ]; then
    echo "$0 [DEPLOY|UNDEPLOY]"
    exit 1
fi

if [[ $1 == "DEPLOY" ]]
then
  echo "Deploying elastic search"
  #helm install -n monitoring --set service.type=LoadBalancer elasticsearch elastic/elasticsearch
  #helm install -n monitoring --set name=elasticsearch,master.replicas=3,coordinating.service.type=LoadBalancer elasticsearch bitnami/elasticsearch
  helm install -n monitoring elasticsearch elastic/elasticsearch
  exit 0
fi

if [[ $1 == "UNDEPLOY" ]]
then
  echo "Undeploying elastic search"
  helm delete -n monitoring elasticsearch
  
  exit 0
fi

echo "Unknown argument"
exit 1
