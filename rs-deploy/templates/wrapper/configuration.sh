#!/bin/sh

#############################################
##          ENVIRONMENT SPECIFIC
#############################################
source $(dirname ${BASH_SOURCE})/environment-specific.sh;

#############################################
##          S3 / ES / KAFKA / MONGO
#############################################
source $(dirname ${BASH_SOURCE})/s3.sh;
source $(dirname ${BASH_SOURCE})/es.sh;
source $(dirname ${BASH_SOURCE})/kafka.sh;
source $(dirname ${BASH_SOURCE})/mongo.sh;
source $(dirname ${BASH_SOURCE})/deploy.sh;

#############################################
##            END
#############################################
