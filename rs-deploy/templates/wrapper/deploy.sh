VERSION=$(cat ${LOCATION}/VERSION.txt)

function makeHelmList()
{
  # assemble list of apps to install via helm, 
  # ignore everything that doesn't start with the expected pattern 'NN_bla'
  for directoryName in $(ls -1 "${LOCATION}"/helm/${ADDON_DIR} | grep -E "^[0-9][0-9]_.*" | sort);
  do
    HELM_NUMBER=$(echo "${directoryName}" | cut -d "_" -f1);
    HELM_APP_NAME=$(echo "${directoryName}" | cut -d "_" -f2- | sed s,_,-,g);
    HELM_NAME="s1pro-${HELM_APP_NAME}";
    echo "${HELM_NUMBER} YES ${HELM_NAME} ${VERSION} ${LOCATION}/helm/${ADDON_DIR}/${directoryName}/values.yaml"
  done
}

# Mapping addon shortcut to actual directory name
case "$ADDON" in
  "s1") ADDON_DIR="processing-sentinel-1" ;;
  "s3") ADDON_DIR="processing-sentinel-3" ;;
  "infra") ADDON_DIR="infrastructure" ;;
  "common")  ADDON_DIR="processing-common" ;;
  "testing")  ADDON_DIR="testing" ;;
esac

if [[ ! -z $ADDON_DIR ]]
then
  echo "Using $ADDON_DIR as chart directory"
fi

export HELM_CONF_REPLICAS="${ENV_DIR}/replicaCount.yaml"
export HELM_CONF_GLOBAL="${ENV_DIR}/values.yaml";

# TODO maybe replaced by the actual invocation of the function in the future
export HELM_LIST="$(makeHelmList)"
