# COPRS-ICD-ADST-001473603 - ICD Reference System Tailored Distribution Catalogue

## Document Summary

This document describes the modifications that had been required to tailor the PRIP/DDIP interface for the Reference System. It especially is describing fields that are not described in the PRIP ICD as e.g. quick look productions.

## Document Change log

| Issue/Revision | Date | Change Requests | Observations |
| --- | --- | --- | --- |
| 01 | 2022/11/07 | N/A | First issue of document |

## TODO 

Extend Reporting ICD with PRIP Frontend Quicklook Reporting.

New reporting elements:

### rs-prip-frontend

<table>
<tr><th>task</th><th>event</th><th>status</th><th>message</th><th>additional keys</th></tr>
<tr><td>PripTempQuicklookUrl</td><td>begin</td><td></td><td>Creating temporary quicklook URL for obsKey for user</td><td><pre>"input":{
  "filename_strings": [],
  "segment_strings": [],
  "user_name_string": "not defined",
}</pre></td></tr>
<tr><td>PripTempQuicklookUrl</td><td>end</td><td>OK</td><td>Temporary quicklook URL for obsKey for user</td><td><pre>"input":{
  "filename_strings": [],
  "segment_strings": [],
  "user_name_string": "not defined",
}</pre></td></tr>
<tr><td>PripTempQuicklookUrl</td><td>end</td><td>NOK</td><td>Error on creating quicklook URL for obsKey for user:</td><td><pre>"input":{
  "filename_strings": [],
  "segment_strings": [],
  "user_name_string": "not defined",
}
"output":{
  "download_url_string": [<TEMP_QUICKLOOK_URL>]
}</pre></td></tr>
</table>
