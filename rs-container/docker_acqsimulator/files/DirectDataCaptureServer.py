#!/usr/bin/env python2

import sys

sys.path.append('/opt')

from generateL0OrderFile import buildNewXML
import os
import datetime


def main():
    '''Main Method'''

    print('## SIMULATOR: Running DirectDataCaptureServer Script')

    inputrootpath = '/data/NRTAP/CADU'
    outputpath = '/data/NRTAP/L0Orders'

    s3a = 'S3A'
    s3b = 'S3B'
    s3adir = os.path.join(inputrootpath, s3a)
    s3bdir = os.path.join(inputrootpath, s3b)
    if os.path.isdir(s3adir) and len(os.listdir(s3adir)) > 0:
        print('Looking for input data in folder: ', s3adir)
        inputmission = s3a
        inputpath = s3adir
    elif os.path.isdir(s3bdir) and len(os.listdir(s3bdir)) > 0:
        print('Looking for input data in folder: ', s3bdir)
        inputmission = s3b
        inputpath = s3bdir
    else:
        print('## SIMULATOR: No input products found in ', s3adir, ' or ',
              s3bdir, 'Exiting DirectDataCaptureServer Script')
        return

    print('Found data for mission: ', inputmission)
    inputdircontent = os.listdir(inputpath)
    selectedfoldername = inputdircontent[0]
    print('Found folders: ', inputdircontent)
    print('Using first folder name to build L0JobOrders: ', selectedfoldername)
    orbitnumber = selectedfoldername[-10:-4].lstrip('0')
    satelliteid = selectedfoldername[7:10]
    parsedtime = datetime.datetime.strptime(selectedfoldername[11:23],
                                            '%Y%m%d%H%M%S')
    time = datetime.datetime.strftime(parsedtime, '%Y-%m-%d %H:%M:%S.%f')[:-3]

    if not os.path.exists(outputpath):
        os.mkdir(outputpath)

    print('## SIMULATOR: Create L0OrderISP1.xml')
    buildNewXML(
        orbitnumber,
        time,
        satelliteid,
        'ISP',
        '1',
        os.path.join(outputpath, 'L0OrderISP1.xml'),
    )

    print('## SIMULATOR: Create L0OrderISP2.xml')
    buildNewXML(
        orbitnumber,
        time,
        satelliteid,
        'ISP',
        '1',
        os.path.join(outputpath, 'L0OrderISP2.xml'),
    )

    print('## SIMULATOR: Create L0OrderTransferFrame1.xml')
    buildNewXML(
        orbitnumber,
        time,
        satelliteid,
        'TransferFrame',
        '2',
        os.path.join(outputpath, 'L0OrderTransferFrame1.xml'),
    )

    print('## SIMULATOR: Create L0OrderTransferFrame2.xml')
    buildNewXML(
        orbitnumber,
        time,
        satelliteid,
        'TransferFrame',
        '2',
        os.path.join(outputpath, 'L0OrderTransferFrame2.xml'),
    )


if __name__ == '__main__':
    main()
