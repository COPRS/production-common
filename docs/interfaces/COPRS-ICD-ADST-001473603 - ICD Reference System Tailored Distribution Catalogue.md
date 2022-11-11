# COPRS-ICD-ADST-001473603 - ICD Reference System Tailored Distribution Catalogue

## Document Summary

This document describes the modifications that had been required to tailor the PRIP/DDIP interface for the Reference System. It especially is describing fields that are not described in the PRIP ICD as e.g. quick look productions.

## Document Change log

| Issue/Revision | Date | Change Requests | Observations |
| --- | --- | --- | --- |
| 01 | 2022/11/07 | N/A | First issue of document |

## Reporting

PRIP Frontend Reporting has been extendend with the reporting messages:

### rs-prip-frontend

<table>
<tr><th>task</th><th>event</th><th>status</th><th>message</th><th>additional keys</th></tr>
<tr><td>PripTempQuicklookUrl</td><td>begin</td><td></td><td>Creating temporary quicklook URL for obsKey &lt;KEY&gt; for user &lt;USERNAME&gt;</td><td><pre>"input":{
  "filename_strings": [&lt;FILENAME&gt;],
  "segment_strings": [&lt;FILENAME&gt;],
  "user_name_string": &lt;SERNAME&gt;,
}</pre></td></tr>
<tr><td>PripTempQuicklookUrl</td><td>end</td><td>OK</td><td>Temporary quicklook URL for obsKey &lt;KEY&gt; for user &lt;USERNAME&gt;</td><td><pre>"input":{
  "filename_strings": [&lt;FILENAME&gt;],
  "segment_strings": [&lt;FILENAME&gt;],
  "user_name_string": &lt;USERNAME&gt;",
}</pre></td></tr>
<tr><td>PripTempQuicklookUrl</td><td>end</td><td>NOK</td><td>Error on creating quicklook URL for obsKey &lt;KEY&gt; for user &lt;USERNAME&gt;: &lt;ERRMESS&gt;</td><td><pre>"input":{
  "filename_strings": [&lt;FILENAME&gt;],
  "segment_strings": [&lt;FILENAME&gt;],
  "user_name_string": &lt;USERNAME&gt;,
}
"output":{
  "download_url_string": [&lt;TEMP_QUICKLOOK_URL&gt;]
}</pre></td></tr>
</table>
