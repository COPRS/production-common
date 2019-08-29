#!/bin/bash

LIST_FILE=AIOProc.LIST
touch $LIST_FILE

ISIP=$(ls -d PT/S1*.ISIP 2>/dev/null)
if [ ! -z "$ISIP" ]
then
        for i in $ISIP
        do
                #move PT to NRT because of wrapper unknown mode error
                mv $i NRT/
                #echo $i >> $LIST_FILE
        done
fi

ISIP=$(ls -d NRT/S1*.ISIP 2>/dev/null)
if [ ! -z "$ISIP" ]
then
        for i in $ISIP
        do
                echo $i >> $LIST_FILE
        done
fi

ISIP=$(ls -d FAST24/S1*.ISIP 2>/dev/null)
if [ ! -z "$ISIP" ]
then
        for i in $ISIP
        do
                echo $i >> $LIST_FILE
        done
fi

EFEP_REPORTS=$(ls REPORTS/S1*EOF | grep REP_ 2>/dev/null)
if [ ! -z "$EFEP_REPORTS" ]
then
        for i in $EFEP_REPORTS
        do
                echo $i >> $LIST_FILE
        done
fi

ACQ_PROC_REPORTS=$(ls REPORTS/S1*xml | grep REP_ACQ 2>/dev/null)
if [ ! -z "$ACQ_PROC_REPORTS" ]
then
        for i in $ACQ_PROC_REPORTS
        do
                echo $i >> $LIST_FILE
        done
fi

for ISIP in $(cat $LIST_FILE | grep ISIP)
do
        #Replace partialType PARTIAL with BEGIN, MIDDLE or END in manifest.safe
        name=$(echo ${ISIP} | cut -d / -f 2 | cut -d . -f 1)
        if [ "$(grep productConsolidation ${ISIP}/${name}.SAFE/manifest.safe | grep PARTIAL)" != "" ]
        then
                partialType="$(grep partialType ${ISIP}/${name}.SAFE_iif.xml | cut -d '>' -f 2 | cut -d '<' -f 1)"
                commande="sed 's#<productConsolidation>PARTIAL</productConsolidation>#<productConsolidation>${partialType}</productConsolidation>#' -i ${ISIP}/${name}.SAFE/manifest.safe"
                eval ${commande}
        fi

        #Ghost filtering
        file=${ISIP}
        ignoreModeTime="RF"
        product="$(echo ${file} | cut -d '/' -f 2)"
        mode="${product:4:2}"

        if [ "${mode}" == "WV" ]
        then
                duration=30
        else
                duration=2
        fi

        if [ "$(echo ${ignoreModeTime} | grep ${mode})" != "" ]
        then
                toCheckTime=false
        else
                toCheckTime=true
        fi

        if $toCheckTime
        then
                # All but RF products
                startY="${product:17:4}"
                startm="${product:21:2}"
                startd="${product:23:2}"
                startH="${product:26:2}"
                startM="${product:28:2}"
                startS="${product:30:2}"
                endY="${product:33:4}"
                endm="${product:37:2}"
                endd="${product:39:2}"
                endH="${product:42:2}"
                endM="${product:44:2}"
                endS="${product:46:2}"

                dateStart=$(date -d "${startY}-${startm}-${startd} ${startH}:${startM}:${startS}" +%s)
                dateEnd=$(date -d "${endY}-${endm}-${endd} ${endH}:${endM}:${endS}" +%s)
                datediff=$(expr ${dateEnd} - ${dateStart})

                if [ ${datediff} -le ${duration} ]
                then
                        toRemove=true
                else
                        toRemove=false
                fi
        else
                # RF products
                # We check if they are FULL else discard
                manifestFile=$(find ${file} -name "*manifest.safe")
                productConsolidation="$(grep productConsolidation ${manifestFile} | cut -d '>' -f 2 | cut -d '<' -f 1)"

                if [ "${productConsolidation}" != "FULL" ]
                then
                        toRemove=true
                else
                        toRemove=false
                fi
        fi

        if $toRemove
        then
                #removeCMD="sed -i '/${product}/d' AIOProc.LIST"
                #eval ${removeCMD}
                # FSi: 2019-08-29 Removing ghost removal, so that the wrapper can use the new ghost logic
                echo "Ignoring ghost removal for ${product}"
        fi
done

#Insert Only the reports of the ISIP Products
#for prod in $ISIP
#do
#       echo $prod >> $LIST_FILE
#       SUBSTRING=${prod:0:67}
#       for i in $ACQ_PROC_REPORTS
#       do
#               RES=$(grep -l $SUBSTRING $i)
#               if [ $? == 0 ]
#               then
#                       echo $RES >> $LIST_FILE
#               fi
#       done
#done


