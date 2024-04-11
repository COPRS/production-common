#!/usr/bin/env python2
# Copyright 2023 Airbus
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.



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
