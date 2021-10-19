#!/bin/bash

OPENAPI_URL="http://localhost:8888/openapi/v3/doc.yaml"
OUTPUT_DIR="./src/main/resources/"
OUTPUT_FILE="native-api_openapi-gen.yml"

function check_connection() {
    printf "checking connection to springdoc OPENAPI endpoint: %s ...\n" $OPENAPI_URL
    response_code=$(curl --head --write-out %"{response_code}" --silent --output /dev/null "${OPENAPI_URL}")
    
    if [[ 200 -ne "$response_code" ]] ; then
        printf "trying to connect to %s resulted in respone code: %s.\nplease make sure the springdoc OPENAPI endpoint is running and try again.\n" $OPENAPI_URL "$response_code"
        exit 0
    fi
}

function generate_openapidoc() {
    printf "\ngenerating OPENAPI document from %s to %s ...\n" $OPENAPI_URL $OUTPUT_FILE
    mvn springdoc-openapi:generate -Dspringdoc.apiDocsUrl=$OPENAPI_URL -Dspringdoc.outputDir=$OUTPUT_DIR -Dspringdoc.outputFileName=$OUTPUT_FILE -Dgoal=generate
}

check_connection;
generate_openapidoc;
