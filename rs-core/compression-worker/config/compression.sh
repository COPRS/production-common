#!/bin/sh

INPUT=$1
OUTPUT=$2

echo "Compressing ${INPUT} to ${OUTPUT}"
7zr a -tzip ${OUTPUT} ./${INPUT}
result=$?

if [ ${result} -eq 0 ]
then
  echo "Compression script done."
  exit ${result}
else
  echo "Error during compression script excecution." >&2
  exit ${result}
fi
