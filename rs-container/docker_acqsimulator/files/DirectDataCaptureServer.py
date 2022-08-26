#!/usr/bin/env python2

import sys
sys.path.append('/opt')
from generateL0OrderFile import buildNewXML
import os
import datetime



def main():
    '''Main Method'''

    print("## SIMULATOR: Running DirectDataCaptureServer Script")

    inputpath = "/data/NRTAP/CADU"
    outputpath = "/data/NRTAP/L0Orders"

    print("Looking for input data in folder: ", inputpath)
    inputdir = os.listdir(inputpath)
    foldername = inputdir[0]
    print("Found folders: " + inputdir)
    print("Using first folder name to build L0JobOrders: ", foldername)
    orbitnumber = foldername[-10:-4].lstrip("0")
    satelliteid = foldername[7:10]
    parsedtime = datetime.datetime.strptime(foldername[11:23], '%Y%m%d%H%M%S')
    time = datetime.datetime.strftime(parsedtime, '%Y-%m-%d %H:%M:%S.%f')[:-3]

    if not os.path.exists(outputpath):
        os.mkdir(outputpath)

    print("## SIMULATOR: Create L0OrderISP1.xml")
    buildNewXML(orbitnumber, time, satelliteid, "ISP", "1",
                os.path.join(outputpath, "L0OrderISP1.xml"))

    print("## SIMULATOR: Create L0OrderISP2.xml")
    buildNewXML(orbitnumber, time, satelliteid, "ISP", "1",
                os.path.join(outputpath, "L0OrderISP2.xml"))

    print("## SIMULATOR: Create L0OrderTransferFrame1.xml")
    buildNewXML(orbitnumber, time, satelliteid, "TransferFrame", "2",
                os.path.join(outputpath, "L0OrderTransferFrame1.xml"))

    print("## SIMULATOR: Create L0OrderTransferFrame2.xml")
    buildNewXML(orbitnumber, time, satelliteid, "TransferFrame", "2",
                os.path.join(outputpath, "L0OrderTransferFrame2.xml"))


if __name__ == "__main__":
    main()
