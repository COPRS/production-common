#!/bin/bash

INPUT=$1

if echo ${INPUT} | egrep -i '\.zip$'; then
  echo "Uncompressing ${INPUT}"
  7za x ./${INPUT}
  result=$?
elif echo ${INPUT} | egrep -i '\.(tar\.gz|tar|tgz)$'; then
  echo "Uncompressing tarred ${INPUT}"
  # since tar 1.15 (2004-12-20), 'z' doesn't need to be provided to properly untar gzipped files
  # see https://www.gnu.org/software/tar/
  tar xf ./${INPUT}
  result=$?
else
  echo "ERROR: Unexpected file to uncompress ${INPUT}. Supported are: .zip, .tar.gz, .tgz and tar"  
  exit 1
fi

if [ ${result} -eq 0 ]
then
  echo "Uncompression script done."
  exit ${result}
else
  echo "Error during uncompression script excecution." >&2
  exit ${result}
fi
