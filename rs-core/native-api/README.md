# RS Native API
The Native API is an outgoing interface of the Copernicus Reference System (COPRS). It enables users (e.g. the user web client) to search for processed satellite data (products) and download it. Together with the outgoing interfaces PRIP and DDIP it forms the Reference System API (RS-API).
<figure>
  <img src="media/rs-api_native-api.png" alt="RS-API diagram" style="width:100%"><figcaption align="center"><b>The Native API and its environment (outlined in blue) as part of the RS-API</b></figcaption>
</figure>

## Interface Context
### Outgoing Interfaces
The outgoing interface of the Native API is described in form of an OpenAPI document that is readable by humans and machines alike. The document is used by the Native API itself to validate ingoing requests to its endpoints.
<figure>
  <img src="media/native-api_openapi.png" alt="Native API OpenAPI Overview" style="width:100%"><figcaption align="center"><b>A render of the Native API OpenAPI document showing an overview of its endpoints</b></figcaption>
</figure>

The API is partitioned into the two parts ```Metadata``` and ```Products```, the former containing endpoints which allow to ask for metadata used to query products on endpoints in the latter part.

The current OpenAPI document can be retrieved from the running Native API service in JSON and YAML format via these URLs:
* ```[API_BASE_URL]/openapi/v3/doc```
* ```[API_BASE_URL]/openapi/v3/doc.yaml```

<figure>
  <img src="media/native-api_retrieve-openapi-yaml.png" alt="Screenshot retrieving Native API OpenAPI document in YAML format" style="width:100%"><figcaption align="center"><b>Example retrieving the Native API OpenAPI document in YAML format</b></figcaption>
</figure>

For a more informal hands-on interaction with the outgoing interface of the Native API a [Postman collection](RS-Native-API.postman_collection.json) is provided that can be imported into [Postman](https://www.postman.com/) to explore the API.

<figure>
  <img src="media/native-api_postman.png" alt="Screenshot of Native API Postman collection" style="width:100%"><figcaption align = "center"><b>Native API Postman collection</b></figcaption>
</figure>

### Internal Interfaces
Inwards the Native API interfaces to the ```PRIP storage``` for product metadata and the ```OBS storage``` for the actual product data, via the S1PRO components:
* ```prip-client```
* ```obs-sdk```

The reasons for the Native API to directly face the PRIP storage instead of the PRIP OData interface are:
* no additional network connection (```user - Native API - PRIP storage``` instead of ```user - Native API - PRIP Frontend - PRIP storage```)
* no additional mapping (```JSON/Native API model/S1PRO model/PRIP Elasticsearch``` instead of (```JSON/Native API model/PRIP OData/S1PRO model/PRIP Elasticsearch```))
* no additional parsing (```Native API query - S1PRO filter model - Elasticsearch query``` instead of ```Native API query - PRIP OData query - S1PRO filter model - Elasticsearch query```)
* flexibility: e.g. returning metadata from PRIP storage which is not returned by PRIP OData interface
