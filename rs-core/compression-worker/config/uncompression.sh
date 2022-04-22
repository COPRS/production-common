#!/bin/sh

INPUT=$1

echo "Uncompressing ${INPUT}"
7zr x ./${INPUT}
result=$?

if [ ${result} -eq 0 ]
then
  echo "Uncompression script done."
  exit ${result}
else
  echo "Error during uncompression script excecution." >&2
  exit ${result}
fi
