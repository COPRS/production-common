#!/bin/bash
find /data/oqcWD -mindepth 1 -maxdepth 1 -type d | xargs -I{} cp -l /usr/local/components/OQCSimulator/reports/* {}/reports/
