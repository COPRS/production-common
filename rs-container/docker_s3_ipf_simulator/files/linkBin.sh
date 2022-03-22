#!/bin/bash

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
