#! /usr/bin/env python2

import sys

sys.path.append('/opt')

from generateDummyProducts import generateDummyProducts
from parseXML import parseXML
import os


# Main method
def main():
    print("## SIMULATOR: Running S3L0PostProcessor Script")

    if len(sys.argv) != 2:
        sys.exit('Usage: ' + sys.argv[0] + ' <path to L0POrder-file>')

    satelliteId, time = parseXML(sys.argv[1])

    outputdir = "/data/NRTAP/LowPriorityOutBasket"
    os.makedirs(outputdir)
    generateDummyProducts(time, satelliteId, outputdir)


if __name__ == "__main__":
    main()
