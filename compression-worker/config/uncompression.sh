#!/bin/sh

INPUT=$1

echo "Uncompressing ${INPUT}"
7za x ${INPUT}

echo "Uncompression script done."
exit 0
