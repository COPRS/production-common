#!/bin/sh

INPUT=$1

echo "Uncompressing ${INPUT}"
7za x ${INPUT}

if [ $? -eq 0 ]
then
  echo "Uncompression script done."
  exit $?
else
  echo "Error during uncompression script excecution." >&2
  exit $?
fi
