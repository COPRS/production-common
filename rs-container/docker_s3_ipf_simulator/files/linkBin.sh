#!/bin/bash
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


### helper functions ###

function linkIPFhost() {
  binFiles=$(grep -r -h '.bin' /usr/local/conf/IPFSimulator/TaskTables)
  
  for j in ${binFiles[@]}
  do
    ## remove useless lines
    if [[ "$j" != ":" ]]  &&  [[ "$j" != "ipf1home" ]];
    then
      createLink $j 
    fi
  done

  return 0;
}

function createLink() {
  binPath=$1

  foo=${binPath/<File_Name>/}
  foo=${foo/"</File_Name>"/}
  
  binName=${foo##*/}
  path=${foo/$binName/}
 
  echo $path
  echo $binName
 
  mkdir -p $path
  ln -s /usr/local/conf/IPFSimulator/bin/ipf-sim.sh $path$binName
}

### Main functionality ###
linkIPFhost;

exit 0
