#!/bin/sh

INPUT=$1
OUTPUT=$2

echo "Compressing ${INPUT} to ${OUTPUT}"
7za a -tzip ${OUTPUT} ./${INPUT}

if [ $? -eq 0 ]
then
  echo "Compression script done."
  exit $?
else
  echo "Error during compression script excecution." >&2
  exit $?
fi
