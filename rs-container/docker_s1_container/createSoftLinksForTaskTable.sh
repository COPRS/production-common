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


#(c) Werum Software & Systems AG, 2012, Author: F. Grosse-Schulte


if [ $# -lt 1 -o $# -gt 1 ]; then
	echo
	echo "Usage: $0 <TaskTable>"
	echo
	exit 1
fi

taskTable=$1
stylesheet=`dirname $0`/extractExecutables.xsl
tasks=`xsltproc ${stylesheet}  ${taskTable}`
numberOfTasks=`echo ${tasks} | tr " " "\n" | wc -l`


createSoftlink(){
	executable=$1
	task=$2
	
	if [ ! -e ${task} ]; then
		mkdir -p `dirname ${task}`
		echo "Creating Softlink '/usr/local/components/IPFSimulator/bin/${executable}' ==>> '${task}'"
	  ln -s /usr/local/components/IPFSimulator/bin/${executable} ${task}
  elif [ -L ${task} ]; then
    echo "Softlink '${task}' already exists."
  elif [ -e ${task} ]; then
    echo "File '${task}' already exists, Softlink cannot be created."
  fi
}

wrapDir=/usr/local/conf/pm-s1/etc/S1IPF_wrapperScripts
wrapDirL2=/usr/local/components/S1IPF_wrapperScripts

for i in $(seq 1 1 ${numberOfTasks})
do

task_i=`echo $tasks | tr " " "\n" | sed -n "${i},${i}p"`

wrapper=0
case `basename ${task_i}` in
 PSC_PreprocMain)
   wrapper=${wrapDir}/PSC_PreprocMain_Wrapper.sh;;
 MDC_MultiSwathDopMain)
   wrapper=${wrapDir}/MDC_MultiSwathDopMain_Wrapper.sh;;
 WPC_ProcMain)
   wrapper=${wrapDir}/WPC_ProcMain_Wrapper.sh;;
 LPC1_ProcMain.sh)
   wrapper=${wrapDir}/LPC1_ProcMain_Wrapper.sh;;
 LPC2_ProcMain.sh)
   wrapper=${wrapDir}/LPC2_ProcMain_Wrapper.sh;;
 LOP_ProcMain)
   wrapper=${wrapDirL2}/LOP_ProcMain_Wrapper.sh;;
 SDC_SingleSwathDopMain)
   wrapper=${wrapDir}/SDC_SingleSwathDopMain_Wrapper.sh;;
 SBE_CntSceMain)
   wrapper=${wrapDir}/SBE_CntSceMain_Wrapper.sh;;
 WVP_WaveProcessMain)
   wrapper=${wrapDir}/WVP_WaveProcessMain_Wrapper.sh;;
 DPP_DopProcMain)
   wrapper=${wrapDir}/DPP_DopProcMain_Wrapper.sh;;
 PPC_PreprocMain)
   wrapper=${wrapDir}/PPC_PreprocMain_Wrapper.sh;; 
 PPC_PreprocMain)
   wrapper=${wrapDir}/PPC_PreprocMain_Wrapper.sh;;
esac

if [ $i == 1 ]; then
  createSoftlink first.sh ${task_i}
  if [ $wrapper != 0 ]; then
    createSoftlink first.sh ${wrapper}
  fi
else
  createSoftlink last.sh ${task_i}
  if [ $wrapper != 0 ]; then
    createSoftlink last.sh ${wrapper}
  fi
fi
	
done



