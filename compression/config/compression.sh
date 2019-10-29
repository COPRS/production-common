#!/bin/sh

INPUT=$1
OUTPUT=$2

echo "Compressing ${INPUT} to ${OUTPUT}"
7za a -tzip ${OUTPUT} ./${INPUT}/*

echo "Compression script done."
exit 0