#!/bin/bash

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



