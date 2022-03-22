#!/bin/bash

# an easy wrapper for the wrapper modules to avoid permanently providing the configuration.
# it can be used to execute a module directly with providing the module name and the action.
# 2021-08-05 FSi

if [ "$#" -ne 2 ]; then
    echo "$0 [MODULE:deploy|es|kafka|mongo|s3|secrets] [ACTION:init|clean]"
    exit 1
fi

cd $LOCATION
./modules/$1.sh $ENV_DIR/wrapper/configuration.sh $2
