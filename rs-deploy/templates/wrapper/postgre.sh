export PGSQL_INFRA_NAMESPACE="infra"
export PGSQL_INFRA_POD="postgresql-${PGSQL_INFRA_NAMESPACE}-postgresql-master-0"
export PGSQL_INFRA_CONTAINER="postgresql-${PGSQL_INFRA_NAMESPACE}"
export PGSQL_INFRA_SVC="postgresql-infra"
export PGSQL_AMALFI_SECRET_NAME="postgresql-amalfi"

export PGSQL_INFRA_USER="postgres"
export PGSQL_INFRA_PASSWORD=$(kubectl get secret --namespace ${PGSQL_INFRA_NAMESPACE} postgresql-${PGSQL_INFRA_NAMESPACE} -o jsonpath="{.data.postgresql-password}" | base64 --decode)
export PGSQL_INFRA_DB="postgres"

export PGSQL_AMALFI_DB="amalfi"
export PGSQL_AMALFI_USER="amalfi"
export PGSQL_AMALFI_USER_PASSWORD="amalfi"

export PGSQL_AMALFI_SQL_CREATE_TABLE_SCRIPT_PATH="${LOCATION}/postgresql/amalfi/amalfi_create_table.sql"
export PGSQL_AMALFI_SQL_DROP_TABLE_SCRIPT_PATH="${LOCATION}/postgresql/amalfi/amalfi_drop_table.sql"
export PGSQL_AMALFI_HOST="${PGSQL_INFRA_POD}.${PGSQL_INFRA_SVC}.${PGSQL_INFRA_NAMESPACE}.svc.cluster.local"