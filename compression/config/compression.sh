#!/bin/sh

INPUT=$1
OUTPUT=$2

echo "Compressing ${INPUT} to ${OUTPUT}"
#7za a -tzip ${INPUT} ${OUTPUT} 
cp -v ${INPUT} ${OUTPUT}

exit 0
