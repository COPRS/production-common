#!/usr/bin/env python2


import xml.etree.ElementTree as ET


def buildNewXML(orbitnumber, time, satelliteid, type, channel, outputfile):
    root = ET.Element('L0P_Order')
    list_of_files = ET.SubElement(root, 'List_of_Files')
    file = ET.SubElement(list_of_files, 'File')

    filename = ET.SubElement(file, 'filename')
    filename.text = "/data/NRTAP/DICache//" + satelliteid + \
        type + orbitnumber.zfill(10) + "C" + channel
    validitystart = ET.SubElement(file, 'validitystart')
    validitystart.text = time
    validitystop = ET.SubElement(file, 'validitystop')
    validitystop.text = time
    downlinkorbitnumber = ET.SubElement(file, 'downlinkorbitnumber')
    downlinkorbitnumber.text = orbitnumber

    et = ET.ElementTree(root)

    myfile = open(outputfile, "w")
    et.write(myfile, encoding='UTF-8')
