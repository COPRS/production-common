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

    filename = root.find('TelemetryOutput/ListOfOutput/Output/FileName').text

    # yield satelliteid
    yield str(filename[2])

    # yield time
    yield root.find('Downlink/StartTime').text
