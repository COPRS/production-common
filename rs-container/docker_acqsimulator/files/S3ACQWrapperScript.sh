#!/bin/bash

################################################################################
# Display usage of this script
################################################################################
function display_usage() {
        echo "Wrapper Simulator for the Sentinel-3 Acquisition workflow."
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
# Extract the session_name from local working directory
################################################################################
function prepare_inputs() { 
        # create list of input files (the output list should be empty   
        local workingdir=$(dirname $JOBORDER)

        files=()
        for i in $workingdir/*/*_dat
        do
                files+=("$(basename $i)")
        done

        # Remove duplicates
        INPUT_FILES=()
        while IFS= read -r -d '' x
        do
                INPUT_FILES+=("$x")
        done < <(printf "%s\0" "${files[@]}" | sort -uz)

        # Debug Output to verify all files were found correctly
        #log "Found session file names"
        #for i in ${INPUT_FILES[@]}
        #do
        #       echo "   $i"
        #done
}

################################################################################
# Extract start time and satelliteid from input files
################################################################################
function get_metadata() {
        log "Extract metadata from input file $filename"

        local filename="$1"
        TIMESTAMP=${filename:11:14}
        SATELLITEID=${filename:7:3}

        log "Extracted timestamp: $TIMESTAMP"
        log "Extracted satellite id: $SATELLITEID"
}

################################################################################
# Create dummy products
################################################################################
function create_outputs() {
        log "Create output products"

        ./generateDummyProducts.py $TIMESTAMP $SATELLITEID $(dirname $JOBORDER)

        log "Generated dummy output products"
}

################################################################################
# Create product list file for execution worker
################################################################################
function create_productlist() {
        log "Create product.LIST file"

        # This only works, if the JobOrder was provided with an absolut path
        local workingdir=$(dirname $JOBORDER)
        local listfile=${workingdir}/product.LIST

        # Write product.LIST file with all outputs from directory
        local tmRegex=".*TM_0_NAT.*"

        # Put the TM_0_NAT files up front, so they might get processed earlier
        for entry in $workingdir/*ISIP;
        do
                if [[ $(basename $entry) =~ $tmRegex ]]; then
                        echo "$(basename $entry)" >> $listfile
                fi
        done

        for entry in $workingdir/*ISIP;
        do
                if ! [[ $(basename $entry) =~ $tmRegex ]]; then
                        echo "$(basename $entry)" >> $listfile
                fi
        done

        log "product.LIST file created"
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

prepare_inputs

for i in ${INPUT_FILES[@]}
do
        get_metadata $(basename $i)

        create_outputs

        create_productlist
done
