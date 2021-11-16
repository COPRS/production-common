export MONGO_NAMESPACE="infra"
export MONGO_APP="mongodb"
export MONGO_POD_PREFIX="mongodb-primary-";
#export MONGO_POD_PREFIX="mongodb"
export MONGO_SVC="mongodb-headless.infra.svc.cluster.local"
export MONGO_HOST="mongodb-primary-0.mongodb-headless.infra.svc.cluster.local,mongodb-secondary-0.mongodb-headless.infra.svc.cluster.local,mongodb-secondary-1.mongodb-headless.infra.svc.cluster.local"
export MONGO_PORT="27017"
export MONGO_DB="coprs"

export MONGODB_SECRET_NAME="mongodb";
export MONGODB_ROOT_USER="root";
export MONGODB_ROOT_PASS=$(kubectl get secret -n $MONGO_NAMESPACE mongodb -o json | jq -r '.data."mongodb-root-password"' | base64 -d);
# (*) User for MongoDB
export MONGODB_USER="xxx";
# (*) Password for MongoDB
export MONGODB_PASS="xxx";

