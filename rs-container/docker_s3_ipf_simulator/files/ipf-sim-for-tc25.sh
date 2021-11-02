#!/bin/sh                                                                                                                                                                                                                                    
# S3 IPF Simulator 
# Version: 01.00                                                                                                                                                                                                                         
#                                                                                                                                                                                                                                            
# Copyright Werum Software & Systems AG 2012                                                                                                                                                                                                 
#                                                                                                                                                                                                                                            
                                                                                                                                                                                                                                                                                                                                                                               
export BASE_DIR=/usr/local/conf/IPFSimulator
CLASSPATH=$BASE_DIR
multi_call_chk=`dirname $1`/.ipf_called

for i in `ls ${BASE_DIR}/lib/*.jar`
do
  export CLASSPATH=${CLASSPATH}:${i}
done


DESTINATION_DIR=/data/ipf-s3/ipf_analysis_tmp
HOST_NAME=`hostname -s`
PROCESS_ID=$$
child_proc=0
child_status=0


# Clean-up function
#   Arg1: Exit status
clean_up()
{
 if [ $1 -gt 127 ] ;then
   echo ""
 else
   rm Console.log 2> /dev/null
 fi
}


signal_handler()
{
  if [ $child_proc != 0 ]
  then
    kill -15 $child_proc
    wait $child_proc
    child_status=$?
    clean_up $child_status
    exit $child_status
  else
    exit 0
  fi
}

addL1Trigger()
{
	sen3file=$1
	trigger=$2 # RAC SPC NONE
	tmpfile="./tmpfile"
	cd "${sen3file}" || exit 1
	rm -f ${tmpfile} || exit 1
	while read newline || [ -n "$newline" ]
	do
    		echo ${newline} >> ${tmpfile} || exit 1     
    		if echo ${newline} | grep -q '<generalProductInformation'
    		then
        		echo "<L1Triggering sequence=\"S01\" triggers=\"${trigger}\" description=\"RADIOMETRIC CALIBRATION\"></L1Triggering>" >> ${tmpfile} || exit 1
    		fi
	done < "${sen3file}/xfdumanifest.xml"
	if [ $? == 0 ]
	then 
		mv ${tmpfile} "xfdumanifest.xml" || exit 1
	else
		exit 1
	fi
	return 0
}

trap signal_handler SIGTERM SIGQUIT SIGINT

if [ -e "$multi_call_chk" ]
then

  echo "IPF task with JO was called already. Skipping simulation of IPF task."
  exit 0

else

  touch $multi_call_chk

	java -classpath $CLASSPATH -Dpss.main.dir=/usr/local/conf/IPFSimulator -Dpss.hostname=$HOST_NAME -Dpss.ipf.pid=$PROCESS_ID com.werum.sentinel.ipfsimulator.S3IPFSimulator $* &	
	
	child_proc=$!
	wait $child_proc
	retval=$?
	
	#insert L1Triggering field in L0 output files
	#trigger can assume values RAC SPC NONE
	procName=$(grep Processor_Name $1 | cut -d'>' -f2 | cut -d'<' -f1) #procName from JO
	prod1_timetag="20040703T020917_20040703T035017"
	prod2_timetag="20040703T035017_20040703T053117"
	prod3_timetag="20040703T053117_20040703T071217"
	prod4_timetag="20040703T071217_20040703T085317"
	prod5_timetag="20040703T085317_20040703T103417"
	dir=$(dirname $1)
	
	if [[ "${procName}" == "S3A_OL_CR__L0" ]]
	then
		cd ${dir} || exit 1
		echo "pwd=$(pwd)"
		#remove specific output products
		echo "removing unwanted outputs..."
		rm -rf S3A_OL_0_CR1____${prod1_timetag}*
		rm -rf S3A_OL_0_CR1____${prod2_timetag}*
		rm -rf S3A_OL_0_CR1____${prod3_timetag}*
		rm -rf S3A_OL_0_CR0____${prod4_timetag}*
		rm -rf S3A_OL_0_CR0____${prod5_timetag}*
		# purge product.LIST
		echo "purging product.LIST..."
		echo "S3A_OL_0_CR1____${prod1_timetag}"  > ./excludeThose.txt
		echo "S3A_OL_0_CR1____${prod2_timetag}"  >> ./excludeThose.txt
	      	echo "S3A_OL_0_CR1____${prod3_timetag}"  >> ./excludeThose.txt
	        echo "S3A_OL_0_CR0____${prod4_timetag}"  >> ./excludeThose.txt
	        echo "S3A_OL_0_CR0____${prod5_timetag}"  >> ./excludeThose.txt
		cat ./product.LIST | grep -v -f ./excludeThose.txt > ./tmp
		mv ./tmp ./product.LIST 
		# add L1Trigger
		echo "adding L1Trigger..."
		prod=$(ls ${dir} | grep "S3A_OL_0_CR0____${prod1_timetag}" | grep SEN3)
		echo $prod
		if [ -n "${prod}" ]
		then
			addL1Trigger "${dir}/${prod}" "NONE"
		fi 
	        prod=$(ls ${dir} | grep "S3A_OL_0_CR0____${prod2_timetag}" | grep SEN3)
	        echo $prod
		if [ -n "${prod}" ]
	        then
	                addL1Trigger "${dir}/${prod}" "SPC"
	        fi 
	        prod=$(ls ${dir} | grep "S3A_OL_0_CR0____${prod3_timetag}" | grep SEN3)
	        if [ -n "${prod}" ]
	        then
	                addL1Trigger "${dir}/${prod}" "RAC"
	        fi 
	        prod=$(ls ${dir} | grep "S3A_OL_0_CR1____${prod4_timetag}" | grep SEN3)
	        if [ -n "${prod}" ]
	        then
	                addL1Trigger "${dir}/${prod}" "NONE"
	        fi 
	        prod=$(ls ${dir} | grep "S3A_OL_0_CR1____${prod5_timetag}" | grep SEN3)
	        if [ -n "${prod}" ]
	        then
	               addL1Trigger "${dir}/${prod}" "RAC"
	        fi 
		echo "adding L1Trigger done"
	fi
	
	#cp -av `dirname $1` $DESTINATION_DIR
	#find $DESTINATION_DIR -type f -regex '.*dat$' -exec rm {} \; -exec touch {} \;
	#find $DESTINATION_DIR -type f -regex '.*nc$' -exec rm {} \; -exec touch {} \;
	#find $DESTINATION_DIR -type f -regex '.*xsd$' -exec rm {} \; -exec touch {} \;
	#find $DESTINATION_DIR -type f -regex '.*tif$' -exec rm {} \; -exec touch {} \;
	
	rsync -rvu --max-size=20k --timeout=60 "$(dirname $1)" "$DESTINATION_DIR"
	
	if [ $retval -gt 127 ] ;then
	  clean_up $retval
	  exit $retval
	fi
	
	clean_up $retval
	
	exit $retval

fi

                                                                    
