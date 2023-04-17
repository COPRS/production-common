#!/bin/bash
#
# This script aims to wrap the following components:
#  - DDC (Direct Data Capture Server)
#  - L0 Pre Processor
#  - L0 Post Processor

## Structure of this script
# 
# In order to wrap the complete functionality of the ACQ-Workflow the following
# steps need to be performed:
# 
#  1. Link input file directory from the local working directory into the CADU folder
#  2. Start the DDC
#  3. Poll the directory 'L0Orders' for output files of the DDC
#  4. Create DIWorkOrder files based on the L0Order files for L0 Pre and Post Processor
#  5. Start the L0 Pre Processor 
#    a. with a DIWorkorder-File for the ISP part of the preprocessing (generating granules)
#    b. with a DIWorkorder-File for the TransferFrame part of the preprocessing (generating HKTM-Files)
#  6. Start the L0 Post Processor with the file from step 4a
#  7. Link all outputs into the local working directory of the execution worker
#  8. Create a product list with all resulting output files
#  9. Clean the working directories of the three wrapped processors
#
# The two processing chains (Pre-Post and Pre) can be run in parallel. Step 7 should then be executed, when both 
# chains finished successfully. Currently the processing chains are run sequentially.
#
## Error handling
# 
# TBD 
#

################################################################################
# Display usage of this script
################################################################################
function display_usage() {
	echo "Wrapper Script for the Sentinel-3 Acquisition workflow."
	echo -e "\nUsage: $0 [path to joborder] \n"
}

################################################################################
# Logging function in order to provide more information for each logging 
# statement
################################################################################
function log() {
	local message="$1"
	NOW=$(date +"%Y-%m-%dT%H:%M:%S.%N")

	# There was a case, when /bin/date failed... 	
	if [ ${#NOW} -gt 0 ]; then
		echo "${NOW::${#NOW}-3} - $message"
	else
		echo $message
	fi
}

################################################################################
# Local XML-Reader to extract filenames from arbitrary ACQ-Wrapper JobOrder
################################################################################
function read_dom() {
	local IFS=\>
        read -d \< ENTITY CONTENT
}

################################################################################
# Soft link the input files from the Exec Worker working dir to the 
# DDC working directory
################################################################################
function link_inputs() {	
    local workingdir=$(dirname $JOBORDER)
    
    echo "Preparing products in working directory $workingdir"
    
    # We attempt to detect the satellite id from the DSIB of the first channel
    DSIB_FILE=$(find $workingdir/ch01 -iname "*.xml")
    if [[ $DSIB_FILE == *"S3A"* ]]
    then
      SATID="S3A"
    else
      SATID="S3B"
    fi
    echo "Detected SAT id $SATID"

    # Extract session name from DSIB file 
    DSIB_FILE=$(basename $DSIB_FILE)
	SESSION_NAME=$(echo $DSIB_FILE | cut -d '_' -f 1-4)
    
    # Create a directories for the DDC
    TARGET_DIR="/data/NRTAP/CADU/$SATID/${SESSION_NAME}_dat/"
    mkdir -p ${TARGET_DIR}
    
    # Hard link the channels from working directory to DDC input directory
    echo "Preparing channel 1"
    ln -s $workingdir/ch01 $TARGET_DIR/ch_1
    echo "Preparing channel 2"
    ln -s $workingdir/ch02 $TARGET_DIR/ch_2
    
    # Just for debugging purposes
    echo "Listing target directory $TARGET_DIR"
    find $TARGET_DIR
}

################################################################################
# Start the DDC Processor
################################################################################
function start_ddc() {
	log "Start DirectDataCaptureServer:"
	log "################################################################################"

	/usr/local/components/DDC/bin/DirectDataCaptureServer

	log "################################################################################"

	local status=$?

	if [ $status -eq 0 ];
	then
		log "DirectDataCaptureServer started successfully"
	else
		log "DirectDataCaptureServer finished with status $status. Abort further execution!"
		exit 128
	fi
}

################################################################################
# Poll for output files of the DDC to continue execution
################################################################################
function poll_ddcoutput() {
	# Check L0Orders folder for generated xml files
	local iteration=0

	while [ $iteration -le 99 ] ; do
		iteration=$((iteration+1))
		log "Check if DDC produced L0Order files ($iteration/100)"

		local count=$(ls -l /data/NRTAP/L0Orders | wc -l)
		if [ $count -eq 5 ] ; then
			log "Found the expected number of files in the folder /data/NRTAP/L0Orders (4)"
			break
		fi
		
		count=$((count-1))
		log "Found $count out of the expected 4 files. Keep waiting..."
		sleep 10s
	done

	if [ $iteration -eq 100 ] ; then
		log "Maximum iterations reached. There may be something wrong with the DDC. Please refer the logs for more information."
		exit 129
	fi	
}

################################################################################
# Create joborders for L0Pre and L0Post Processor (ISP and TransferFrame)
################################################################################
function create_l0pp_joborders() {
	log "Prepare working directories for L0Pre and L0Post processor"

	# All files are there. Generate DIWorkOrder files
        local isp_files=(/data/NRTAP/L0Orders/*ISP*)
        local tf_files=(/data/NRTAP/L0Orders/*TransferFrame*)

        # Determine orbit number
        while read_dom; do
                if [[ $ENTITY = "downlinkorbitnumber" ]]
                then
                        orbitnumber=$CONTENT
                        break
                fi
        done < ${isp_files[0]}

	log "Extracted orbit number $orbitnumber from file ${isp_files[0]}"

        # Determine working dir path
        local filename=$(basename ${isp_files[0]})

        if [[ $filename == *"S3A"* ]]; then
		WORKINGDIR_PATH=/data/NRTAP/WorkingDir/S3A/$orbitnumber
        fi

        if [[ $filename == *"S3B"* ]]; then
		WORKINGDIR_PATH=/data/NRTAP/WorkingDir/S3B/$orbitnumber
        fi

	log "Create working directories ($WORKINGDIR_PATH)"
	# Create Working directories for ISP and Transfer Frames
	mkdir -p $WORKINGDIR_PATH/1
        mkdir -p $WORKINGDIR_PATH/2

	log "Create symbolic links for binary files from DDC"
	# Link all binary files from DDC into respective working directories
        ln -s /data/NRTAP/DICache/*ISP* $WORKINGDIR_PATH/1/
        ln -s /data/NRTAP/DICache/*TransferFrame* $WORKINGDIR_PATH/2/

	log "Create DIWorkOrders for L0Pre and L0Post processor in working directories"
	# Create JobOrder for L0Pre and L0Post processors
	/opt/convertL0OrderISP.py ${isp_files[0]} $WORKINGDIR_PATH/1/JobOrder.xml
    /opt/convertL0OrderTF.py ${tf_files[0]} $WORKINGDIR_PATH/2/JobOrder.xml

	log "Finished preparing working directories for L0Pre processor"
}

################################################################################
# Start L0Pre and L0Post for ISP and TransferFrame processing
################################################################################
function start_processors() {
	local current_path=$PWD

	log "Start L0Pre Processor for ISP"
	log "################################################################################"
	cd $WORKINGDIR_PATH/1/
	/usr/local/components/L0PreProc/bin/S3L0PreProcessor JobOrder.xml
	log "################################################################################"
	log "L0Pre Processor for ISP finished"

	log "Start L0Pre Processor for TransferFrames"
	log "################################################################################"
	cd $WORKINGDIR_PATH/2/
	/usr/local/components/L0PreProc/bin/S3L0PreProcessor JobOrder.xml
	log "################################################################################"
	log "L0Pre Processor for TransferFrames finished"

	log "Start L0Post Processor for ISP"
	log "################################################################################"
	cd $WORKINGDIR_PATH/1/
	/usr/local/components/L0PostProc/bin/S3L0PostProcessor JobOrder.xml
	log "################################################################################"
	log "L0Post Processor for ISP finished"

	cd $current_path
}

################################################################################
# Create list of created products and link them into the working dir of the 
# execution worker
################################################################################
function create_productlist() {
	# This only works, if the JobOrder was provided with an absolut path
	local workingdir=$(dirname $JOBORDER)
	local listfile=${workingdir}/product.LIST

	# Write product.LIST file with all outputs from
	for entry in /data/NRTAP/LowPriorityOutBasket/*ISIP /data/NRTAP/HighPriorityOutBasket/*ISIP;
	do
		# Above list will return a *ISIP-entry when no matching products where found in folder
		if [[ "$entry" != *"*"* ]];  then
			local newfilename=$(basename $entry)
			mv $entry $newfilename
			echo "$newfilename" >> $listfile
		fi
	done

	# Outputs of the TransferFrame part of the processing chain
	# Currently not used
#	for entry in /data/NRTAP/HighPriorityOutBasket/*;
#	do
#		local newfilename=$(basename $entry)
#
#		echo "mv $entry $newfilename"
#
#		mv $entry $newfilename
#		echo "$newfilename" >> $listfile
#	done
}

################################################################################
# Clean all temporary files from NRTAP-folder
################################################################################
function clean_acq_working_dirs() {
	echo "Clean NRTAP-folder..."

	rm -rf /data/NRTAP/CADU/S3A/*
	rm -rf /data/NRTAP/CADU/S3B/*
	rm -rf /data/NRTAP/CADU_Cache/S3A/*
	rm -rf /data/NRTAP/CADU_Cache/S3B/*
	rm -rf /data/NRTAP/DICache/*
	rm -rf /data/NRTAP/HighPriorityOutBasket/*
	rm -rf /data/NRTAP/L0Orders/*
	rm -rf /data/NRTAP/LowPriorityOutBasket/*
	rm -rf /data/NRTAP/WorkingDir/S3A/*
	rm -rf /data/NRTAP/WorkingDir/S3B/*

	echo "Finished clean up NRTAP-folder"
}

################################################################################
# Kill dangling processes
################################################################################
function kill_dangling_processes() {
	if pgrep -f "DirectDataCaptureServer" > /dev/null
	then
		kill -9 $(pgrep -f DirectDataCaptureServer)
	fi

	if pgrep -f "S3L0PreProcessor" > /dev/null
	then
		kill -9 $(pgrep -f S3L0PreProcessor)
	fi

	if pgrep -f "S3L0PostProcessor" > /dev/null
	then
		kill -9 $(pgrep -f S3L0PostProcessor)
	fi
}

################################################################################
# Main script
################################################################################
JOBORDER=$1

# if less than two arguments supplied, display usage 
if [  $# -le 0 ] 
then 
	display_usage
	exit 254
fi 
 
# check whether user had supplied -h or --help . If yes display usage 
if [[ ( $# == "--help") ||  $# == "-h" ]] 
then 
	display_usage
	exit 0
fi

# check if file exists
if [ ! -f "$JOBORDER" ]
then
	echo "Provided path to JobOrder does not contain a file."
	display_usage
	exit 255
fi

# DDC checks for the existance of an host configuration file and will log error messages
# if not finding it. We are creating an empty file to supress this error. If it does exist,
# nothing happens
cat <<EOF > /data/ACQ/conf/$(hostname).xml
<?xml version="1.0" encoding="UTF-8" standalone="yes" ?>
<root>
</root>
EOF

# RS-512: Make sure the environment is clean, so no old unfinished processing intervenes with the current one
kill_dangling_processes
clean_acq_working_dirs

link_inputs

# The DDC binary terminates, but the process will still be running in the background
start_ddc

poll_ddcoutput

create_l0pp_joborders

start_processors

create_productlist

clean_acq_working_dirs

kill_dangling_processes

log "Acquisition finished!"

