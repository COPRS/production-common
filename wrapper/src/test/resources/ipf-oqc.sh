#!/bin/bash

if [[ $# -ne 1 ]]
then
  echo "Expected JobOrder, please provide it!"
fi 


echo "Doing simulated OQC..."
dd if=/dev/urandom of=./reports/oqc-report.pdf bs=1024 count=5

cat << EOF > ./reports/oqc-report.xml
<report date="2019-10-16T08:55:59" xmlns="http://www.gael.fr/amalfi">
   <item class="http://http://www.esa.int/s1#IWlevel1product" className="SENTINEL-1 Interferometric Wide Swath Level 1 Product" name="S1B_IW_SLC__1SDV_20181001T143402_20181001T143430_012960_017EFD_530E.SAFE" url="/data/localWD/39/S1B_IW_SLC__1SDV_20181001T143402_20181001T143430_012960_017EFD_530E.SAFE/"/>
   <inspection creation="2019-10-16T08:55:58" duration="0.823" execution="2019-10-16T08:55:58" item="S1B_IW_SLC__1SDV_20181001T143402_20181001T143430_012960_017EFD_530E.SAFE" itemURL="/data/localWD/39/S1B_IW_SLC__1SDV_20181001T143402_20181001T143430_012960_017EFD_530E.SAFE/" name="All Applicable Inspections Plan (Automatic)" priority="5" processingStatus="Done" status="Passed">
   <!-- Basically here would be more data -->
   </inspection>
</report>   
EOF
echo "done."
