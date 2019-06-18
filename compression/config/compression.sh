#!/bin/sh

INPUT=$1
OUTPUT=$2

echo "Compressing ${INPUT} to ${OUTPUT}"
7za a -tzip ${OUTPUT} ${INPUT}
#cp -rv ${INPUT} ${OUTPUT}

echo "Compression script done."
exit 0