# COPRS-ICD-ADST-001473603 - ICD Reference System Tailored Distribution Catalogue

## Document Summary

This document describes the modifications that had been required to tailor the PRIP/DDIP interface for the Reference System. It especially is describing fields that are not described in the PRIP ICD as e.g. quick look productions.

## Document Change log

| Issue/Revision | Date | Change Requests | Observations |
| --- | --- | --- | --- |
| 01 | 2022/11/07 | N/A | First issue of document |

## Quicklook Images

The Product entity has been extended with a navigation property binding to a new entity type Quicklook, allowing to provide multiple preview images with a product. The Quicklook entity has one attribute Image of type String, containing the filename of the preview image as well as acting as the identifier of the entity. A media stream is present under the /$value resource path of a Quicklook to download an image.

### Query Examples

#### Product List Query

##### Example Request:

The following query shows the request for, as an example, the list of products limited to max. one search result using the $top parameter. It 'expands' the result to include quicklook images:

<pre>
GET
http://<service-root-uri>/odata/v1/Products?$top=1&$expand=Quicklooks
</pre>

##### Example Response (JSON format):

In this example response, a single product with one quicklook image is returned:

<pre>
HTTP/1.1 200 OK
{
    "@odata.context": "$metadata#Products",
    "value": [
        {
            "@odata.mediaContentType": "application/octet-stream",
            "Id": "df5f2fb4-b32e-4098-9513-8c7f2037f0a1",
            "Name": "S1A_IW_GRDH_1SDV_20200120T124746_20200120T124818_030884_038B5E_C9A1.SAFE.zip",
            "ContentType": "application/octet-stream",
            "ContentLength": 841321,
            "PublicationDate": "2022-11-08T18:57:07.717Z",
            "EvictionDate": "2022-11-15T18:57:07.717Z",
            "Checksum": [
                {
                    "Algorithm": "MD5",
                    "Value": "2d3956df814ff74128d880c081f02578",
                    "ChecksumDate": "2022-11-08T18:57:07Z"
                }
            ],
            "ProductionType": "systematic_production",
            "ContentDate": {
                "Start": "2020-01-20T12:47:46Z",
                "End": "2020-01-20T12:48:18.4Z"
            },
            "Footprint": {
                "type": "Polygon",
                "coordinates": [
                    [
                        [
                            -23.9539,
                            -11.3875
                        ],
                        [
                            -14.3129,
                            -11.3875
                        ],
                        [
                            -14.3129,
                            -14.3875
                        ],
                        [
                            -23.9539,
                            -14.4743
                        ],
                        [
                            -23.9539,
                            -11.3875
                        ]
                    ]
                ],
                "crs": {
                    "type": "name",
                    "properties": {
                        "name": "EPSG:4326"
                    }
                }
            },
            "Quicklooks": [
                {
                    "Image": "S1A_IW_GRDH_1SDV_20200120T124746_20200120T124818_030884_038B5E_C9A1.SAFE_bwi.png"
                }
            ]
        }
    ]
}
</pre>

#### Single Product Query

##### Example Request:

The following query shows the request for a single Product by its Id:

<pre>
GET
http://<service-root-uri>/odata/v1/Products(81fd477f-6503-4887-8b5b-30e07dca179c)?$expand=Quicklooks
</pre>

##### Example Response (JSON format):

In case a Product for a requested Id exists, the response is a single Product including its Quicklook images if present:

<pre>
HTTP/1.1 200 OK
{
  "@odata.context": "$metadata#Products/$entity",
  "@odata.mediaContentType": "application/octet-stream",
  "Id": "81fd477f-6503-4887-8b5b-30e07dca179c",
  "Name": "S1A_IW_GRDH_1SDV_20200120T124926_20200120T124958_030884_038B5E_F0D1.SAFE.zip",
  "ContentType": "application/octet-stream",
  "ContentLength": 841323,
  "PublicationDate": "2022-11-08T19:00:43.716Z",
  "EvictionDate": "2022-11-15T19:00:43.716Z",
  "Checksum": [
    {
      "Algorithm": "MD5",
      "Value": "23a68958fdb7d94da1fb03ab70c05484",
      "ChecksumDate": "2022-11-08T19:00:43Z"
    }
  ],
  "ProductionType": "systematic_production",
  "ContentDate": {
    "Start": "2020-01-20T12:49:26Z",
    "End": "2020-01-20T12:49:58.4Z"
  },
  "Footprint": {
    "type": "Polygon",
    "coordinates": [
      [
        [
          -53.71,
          -11.3875
        ],
        [
          -44.069,
          -11.3875
        ],
        [
          -44.069,
          -14.6556
        ],
        [
          -53.71,
          -14.7424
        ],
        [
          -53.71,
          -11.3875
        ]
      ]
    ],
    "crs": {
      "type": "name",
      "properties": {
        "name": "EPSG:4326"
      }
    }
  },
  "Quicklooks": [
    {
      "Image": "S1A_IW_GRDH_1SDV_20200120T124926_20200120T124958_030884_038B5E_F0D1.SAFE_bwi.png"
    }
  ]
}
</pre>

#### Query Quicklooks of a Product

##### Example Request:

The following query shows a request for the list of Quicklook images of a single Product by its Id:

<pre>
GET
http://<service-root-uri>/odata/v1/Products(81fd477f-6503-4887-8b5b-30e07dca179c)/Quicklooks
</pre>

##### Example Response (JSON format):

A list of all Quicklooks of the Product is returned:

<pre>
HTTP/1.1 200 OK
{
  "@odata.context": "$metadata#Quicklooks",
  "value": [
    {
      "Image": "S1A_IW_GRDH_1SDV_20200120T124926_20200120T124958_030884_038B5E_F0D1.SAFE_bwi.png"
    }
  ]
}
</pre>

#### Single Quicklook Query

##### Example Request:

The following query shows a request for a single Quicklook by Product Id and Quicklook Id:

<pre>
GET
http://<service-root-uri>/odata/v1/Products(81fd477f-6503-4887-8b5b-30e07dca179c)/Quicklooks('S1A_IW_GRDH_1SDV_20200120T124926_20200120T124958_030884_038B5E_F0D1.SAFE_bwi.png')
</pre>

##### Example Response (JSON format):

Details of the Quicklook will be returned:

<pre>
HTTP/1.1 200 OK
{
  "@odata.context": "$metadata#Quicklooks/$entity",
  "Image": "S1A_IW_GRDH_1SDV_20200120T124926_20200120T124958_030884_038B5E_F0D1.SAFE_bwi.png"
}
</pre>

#### Downloading a Quicklook Media Stream

##### Example Request:

The following query shows a download request for a Quicklook media stream (binary image data) by Product Id and Quicklook Id:

<pre>
GET
http://<service-root-uri>/odata/v1/Products(81fd477f-6503-4887-8b5b-30e07dca179c)/Quicklooks('S1A_IW_GRDH_1SDV_20200120T124926_20200120T124958_030884_038B5E_F0D1.SAFE_bwi.png')/$value
</pre>