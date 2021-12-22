#!/usr/bin/env python2

import sys
import os
import datetime

import xml.etree.ElementTree as ET

# Parse L0POrder file for information
def parseXML(xmlFile):
    # create tree
    tree = ET.parse(xmlFile)

    # get root element
    root = tree.getroot()

    # yield orbit number
    yield root.find('List_of_Files/File/downlinkorbitnumber').text

    # yield filename
    absolutfilename = root.find('List_of_Files/File/filename').text
    filename = os.path.basename(absolutfilename)
    yield str(filename[0:-1])

    # yield satelliteid
    yield str(filename[2])

    # yield time
    time = root.find('List_of_Files/File/validitystart').text
    d = datetime.datetime.strptime(str(time[0:-4]), '%Y-%m-%d %H:%M:%S')
    yield d.strftime('UTC=%Y-%m-%dT%H:%M:%S')


# Write converted XML-file to disk
def buildNewXML(orbitnumber, filename, satelliteId, time, outputfile):
    root = ET.Element('DIWorkOrderFile')
    createDIOrder(root, satelliteId)
    createTelemetryInput(root)
    createTelemetryOutput(root, filename, satelliteId, orbitnumber)
    createDownlink(root, orbitnumber, time)
    
    et = ET.ElementTree(root)

    myfile = open(outputfile, "w")
    et.write(myfile, encoding='UTF-8')

# Create the DIOrder Part of the newly generated XML-File
def createDIOrder(root, satelliteId):
    order = ET.SubElement(root, 'DIOrder')
    satellite = ET.SubElement(order, 'Satellite')
    satellite.text = 'Sentinel-3' + satelliteId
    mission = ET.SubElement(order, 'Mission')
    mission.text = '1'
    datarate = ET.SubElement(order, 'DataRate')
    datarate.text = '260'
    sensor = ET.SubElement(order, 'Sensor')
    sensor.text = 'OLCI'
    acqstation = ET.SubElement(order, 'AcqStation')
    acqstation.text = 'Matera'
    processstation = ET.SubElement(order, 'ProcessingCentreID')
    processstation.text = 'Matera'
    ET.SubElement(order, 'MessageQueuePath')
    processingstage = ET.SubElement(order, 'ProcessingStage')
    processingstage.text = 'OPER'
    ET.SubElement(order, 'IngestionMode')
    ET.SubElement(order, 'SlotKey')

# Create TelemetryInput Part of the newly generated XML-File
def createTelemetryInput(root):
    input = ET.SubElement(root, 'TelemetryInput')
    path = ET.SubElement(input, 'RFPath')
    id = ET.SubElement(path, 'ID')
    id.text = '1'
    name = ET.SubElement(path, 'Name')
    name.text = 'LEVEL0_ISP_PROC'
    ET.SubElement(input, 'ListOfInput', attrib={'count': '0'})

# Create TelemetryOutput Part of the newly generated XML-File
def createTelemetryOutput(root, filename, satelliteId, downlinkorbitnumber):
    teleOutput = ET.SubElement(root, 'TelemetryOutput')
    ET.SubElement(teleOutput, 'ProcessId')
    outputs = ET.SubElement(teleOutput, 'ListOfOutput', attrib={'count': '2'})
    
    output1 = ET.SubElement(outputs, 'Output')
    channel1 = ET.SubElement(output1, 'Channel')
    channel1.text = '1'
    device1 = ET.SubElement(output1, 'Device')
    pathname1 = ET.SubElement(device1, 'PathName')
    pathname1.text = '/data/NRTAP/WorkingDir/S3' + satelliteId + '/' + downlinkorbitnumber + '/1'
    type1 = ET.SubElement(device1, 'Type')
    type1.text = 'DISK'
    filename1 = ET.SubElement(output1, 'FileName')
    filename1.text = filename + '1'
    datatype1 = ET.SubElement(output1, 'DataType')
    datatype1.text = 'ISP'

    output2 = ET.SubElement(outputs, 'Output')
    channel2 = ET.SubElement(output2, 'Channel')
    channel2.text = '2'
    device2 = ET.SubElement(output2, 'Device')
    pathname2 = ET.SubElement(device2, 'PathName')
    pathname2.text = '/data/NRTAP/WorkingDir/S3' + satelliteId + '/' + downlinkorbitnumber + '/1'
    type2 = ET.SubElement(device2, 'Type')
    type2.text = 'DISK'
    filename2 = ET.SubElement(output2, 'FileName')
    filename2.text = filename + '2'
    datatype2 = ET.SubElement(output2, 'DataType')
    datatype2.text = 'ISP'

# Create Downlink part of the newly generated XML-File
def createDownlink(root, downlinkorbitnumber, time):
    downlink = ET.SubElement(root, 'Downlink')
    starttime = ET.SubElement(downlink, 'StartTime')
    starttime.text = time
    stoptime = ET.SubElement(downlink, 'StopTime')
    stoptime.text = time
    orbit = ET.SubElement(downlink, 'Orbit')
    orbit.text = downlinkorbitnumber
    duration = ET.SubElement(downlink, 'Duration', attrib={ 'unit': 's'})
    duration.text = '0'

# Main method
def main():
    if len(sys.argv) != 3:
        sys.exit('Usage: ' + sys.argv[0] + ' <path to L0POrder-file> <output-file>')

    orbit, filename, satelliteId, time = parseXML(sys.argv[1])
    buildNewXML(orbit, filename, satelliteId, time, sys.argv[2])

if __name__ == "__main__":
    main()
