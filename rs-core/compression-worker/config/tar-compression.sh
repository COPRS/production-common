#!/bin/sh
# Copyright 2023 Airbus
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

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
tar -cf ${OUTPUT} ./${INPUT}
result=$?

if [ ${result} -eq 0 ]
then
  echo "Compression script done."
  exit ${result}
else
  echo "Error during compression script excecution." >&2
  exit ${result}
fi
