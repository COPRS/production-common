#!/bin/sh
#
# This script is used by the compression worker in order to generate a tar file
#

INPUT=$1
OUTPUT=$2

if [ $# -lt 2 ]
then
  echo "Error: Invalid parameter provided! Data not compressed"
  echo "Usage: $0 <INPUT> <OUTPUT>"
  exit 1
fi

echo "Using tar on ${INPUT} writing output to ${OUTPUT}"
7za -mx=0 a -tzip ${OUTPUT} ./${INPUT}
result=$?

if [ ${result} -eq 0 ]
then
  echo "Compression script done."
  exit ${result}
else
  echo "Error during compression script excecution." >&2
  exit ${result}
fi
