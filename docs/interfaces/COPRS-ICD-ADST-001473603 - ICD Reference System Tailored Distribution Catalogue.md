# COPRS-ICD-ADST-001473603 - ICD Reference System Tailored Distribution Catalogue

## Document Summary

This document describes the modifications that had been required to tailor the PRIP/DDIP interface for the Reference System. It especially is describing fields or functions that are not described in the PRIP ICD as e.g. quick look productions or if there are known limitations. It is based on the PRIP ICD v1.9.

## Document Change log

| Issue/Revision | Date | Change Requests | Observations |
| --- | --- | --- | --- |
| 01 | 2022/11/07 | N/A | First issue of document |
| 02 | 2023/09/12 | RS-1059 | Implementation of PRIP ICD v1.9 |

## Applicable Documents

| ID | Document title | Version |
| --- | --- | --- |
| ESA-EOPG-EOPGC-IF-3 v1.9 | Copernicus Space Component - Production Interface Delivery Point Specification | 1.9 |

## Known Limitations

Especially for the DDIP not all features that are described within the DDIP ICD (Data Distribution Interface Control Document, v1.2, ESA-EOPG-EOPGC-IF-4) are available within the COPRS system. The used functions are either solved in a different way or was removed from the scope of the project. The following sections are giving an overview about the known limitations.

### DDIP

#### Delete Product

The ICD asks for a delete function that allows to remove products from the catalog. As this function is not supposed to be exposed to the end-user and used internally and also marked as deprecated already, it was decided not to implement it. The COPRS does have a with the Data Lifecycle Manager a component that is automatically expiring outdated products from the system and there are operational procedures that are performed by the system operators if a product needs to be removed.

#### On Demand Processing

The ICD is describing on-demand functionality. However this features was removed from the scope of the project at the beginning and thus the interface is not provided.

#### Quota Management

There is currently no quota management existing for the DDIP system. Quota handling might be added in a future version of the system and and Denial of Service Violations will be handled by the system layer. There is no individual Quota handling for users at the moment.

#### Catalog Export

There is no catalog export existing at the moment for the system. End users can use the OData interface or the STAC native API to paginate throught the whole catalog to get the data. For system operators there are operational procedures in order to create backups of the system databases that are not requiring an individual export of the full catalog.

#### Footprints

The "Footprint" field in OData reponses from the PRIP does not contain a Spatial Reference Identifier as described in the ICD, but contains a GeoJSON String. This field is the same content as "GeoFootprint" and just kept for having a backwards compatibility. The "Footprint" is marked as deprecated and it is high recommended using the "GeoFootprint" attribute instead.

### Quicklook Images

The Product entity has been extended with a navigation property binding to a new entity type Quicklook, allowing to provide multiple preview images with a product. The Quicklook entity has one attribute Image of type String, containing the filename of the preview image as well as acting as the identifier of the entity. A media stream is present under the /$value resource path of a Quicklook to download an image.

### Query Examples

#### Operators Examples

##### AND

A request that looks for products starting with a S1B at the beginning and a specific end in the product name:

<pre>
GET
http://<service-root-uri>/odata/v1/Products$filter=startswith(Name,'S1B') and endswith(Name,'025A63_28CD.SAFE.zip')"
</pre>

##### OR

A request that is looking for products with a product name either containign a "EW_GRDM_1A" or "WV_WAV__0C":

<pre>
GET
http://<service-root-uri>/odata/v1/Products$filter=contains(Name,'EW_GRDM_1A') or contains(Name,'WV_RAW__0C')"
</pre>

##### NOT

Filters for a S1A product that does not contain "WV_RAW__0C":

<pre>
GET
http://<service-root-uri>/odata/v1/Products$filter=startswith(Name,'S1A') and not contains(Name,'WV_RAW__0C')"
</pre>

##### IN

Search for a product that contains a value that is contained in a list:
<pre>
GET
http://<service-root-uri>/odata/v1/Products$filter=Attributes/OData.CSC.StringAttribute/any(att:att/Name eq 'productType' and att/OData.CSC.StringAttribute/Value in ('IW_RAW__0A','IW_RAW__0C','IW_RAW__0N'))"
</pre>

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

### Remarks of ICD 1.9

With the new version 1.9 of the PRIP ICD some changes had been performed that might affect previous versions. These are described in the following sections.

#### Entity Model

In older implementations of the COPRS PRIP version all extended attributes was assigned to type specific attribute lists. E.g. all String attributes was listed in a list containing all string attribute and all Integer was in a specific list for Integers.

This had been modified to be in line with the new ICD version, so that all attributes are contained in the field "attributes" and there is no distinction between their types anymore. Each attribute does contain a type in the attribute "ValueType" allowing to identify the data it represents.

Also note that previous version used the Olingo types starting with the prefix "edm" and now using the types specified in the ICD.

#### Footprint

The attribute "footprint" contains the footprint as geojson and is marked as deprecation and will be removed in further versions. Please use the new field "geofootprint" instead. The data returned is kept and compatible.

#### Origindate

The attribute "origindate" was introduced and will be returned for products. It contains the "t0pdgs" time of the product.

#### Operators

The operators "and", "or", "not" and "in" are supported by the new version. Please check the examples given above to learn how to use them.
