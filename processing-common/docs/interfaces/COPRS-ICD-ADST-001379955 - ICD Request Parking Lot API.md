# COPRS-ICD-ADST-001379955 - ICD Request Parking Lot API

## Document Summary

This document describes the Request Parking Lot REST API for managing failed processings. The Request Parking Lot is part of the COPRS DLQ sub system. The provided API allows the COPRS operator to list, restart, resubmit and delete failed processings.


## Document Change log

| Issue/Revision | Date | Change Requests | Observations |
| --- | --- | --- | --- |
| 01 | 2022/09/08 | N/A | First issue of document |


## Usage Notes

### API security

The API is protected by a API key. This is a token (string) agreed by server and client and used to authenticate the client. The client needs to provide the (configurable) API key on every request as a HTTP-Header attribute, e.g. `ApiKey: <yourApiKey>`. The server validates the key send by the client and only allows access if the key is known to the server, else a HTTP error 403 is raised.


### View and manage failed processings 

To get a list of failed processings call `GET /failedProcessings`. It will return a list of all failed processings. Operators can review the entries and either restart the processing by `POST /failedProcessings/{id}/restart`, resubmit a failed processing by `POST /failedProcessings/{id}/resubmit` or delete a failed processing by `DELETE /failedProcessings/{id}`. In either case the failed processing is no longer present in the list of failed processings. In case of a restart a failed processing is issued again to the queues eventually resulting in a new processing. In case of a resubmit, the message that preceded a failed processing will be reissued, whereby the message of the originally failed processing will eventually be reissued under new conditions.

A single failed procesing can be viewed by `GET /failedProcessings/{id}`.
      
Batch delete can be performed by `POST /failedProcessings/delete`.
   
Batch restart can be performed by `POST /failedProcessings/restart`.      

## Resources

### Service Root URL

<table>
  <tr>
    <th>Service Root URL</th>
    <td>http://rs-request-parking-lot-svc:8080</td>
  </tr>
</table>


### Resource paths

#### /failedProcessings

<table>
  <tr>
    <th>HTTP Request Method</th>
    <td>GET</td>
  </tr>
  <tr>
    <th>Summary</th>
    <td>Get the list of failed processings ordered by creation time (ascending)</td>
  </tr>
  <tr>
    <th>Parameters</th>
    <td>
        <table>
            <tr>
              <th>Name</th><th>Type</th><th>Location</th><th>Required</th><th>Description</th>
            </tr>
            <tr>
              <td>ApiKey</td><td>string</td><td>header</td><td>Yes</td><td>Api Key</td>
            </tr>
        </table>
    </td>
  </tr>
  <tr>
    <th>Responses</th>
    <td>
        <table>
            <tr>
              <th>Code</th><th>Description</th>
            </tr>
            <tr>
              <td>200</td>
              <td>
                The request succeeded
                <table>
                  <tr>
                    <th>Content Type</th>
                    <td>application/json</td>
                  </tr>
                  <tr>
                    <th>Example Value<br>(Schema)</th>
                    <td>
<pre>[
  {
    "id": "string",
    "topic": "string",
    "missionId": "string",
    "errorLevel": "ERROR",
    "failureDate": "2022-09-07T21:28:11.726Z",
    "failureMessage": "string",
    "stacktrace": "string",
    "message": {
      "uid": "",
      "keyObjectStorage": "",
      "creationDate": "2020-01-20T12:40:37.464424Z",
      "allowedActions": [
        "RESTART"
      ],
      "debug": true,
      "demandType": "EXTERNAL_DEMAND",
      "retryCounter": 1,
      "relativePath": "",
      "productName": "S1A_WV_RAW__0SSV_20200120T124037_20200120T124728_030884_038B5D_3C86.SAFE",
      "productSizeByte": 951324687,
      "stationName": "WILE",
      "mode": "",
      "timeliness": "FAST24"
    },
    "retryCounter": 0
  }
]</pre>                      
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
            <tr>
              <td>400</td><td>Invalid parameters supplied</td>
            </tr>
            <tr>
              <td>403</td><td>Forbidden, e.g. Invalid API key supplied</td>
            </tr>
            <tr>
              <td>500</td><td>Internal Server Error</td>
            </tr>
        </table>
    </td>
  </tr>
</table>


#### /failedProcessings/count

<table>
  <tr>
    <th>HTTP Request Method</th>
    <td>GET</td>
  </tr>
  <tr>
    <th>Summary</th>
    <td>Get the number of failed processings</td>
  </tr>
  <tr>
    <th>Parameters</th>
    <td>
        <table>
            <tr>
              <th>Name</th><th>Type</th><th>Location</th><th>Required</th><th>Description</th>
            </tr>
            <tr>
              <td>ApiKey</td><td>string</td><td>header</td><td>Yes</td><td>Api Key</td>
            </tr>
        </table>
    </td>
  </tr>
  <tr>
    <th>Responses</th>
    <td>
        <table>
            <tr>
              <th>Code</th><th>Description</th>
            </tr>
            <tr>
              <td>200</td>
              <td>
                The request succeeded
                <table>
                  <tr>
                    <th>Content Type</th>
                    <td>application/json</td>
                  </tr>
                  <tr>
                    <th>Example Value<br>(Schema)</th>
                    <td>
<pre>0</pre>
                    </td>
                  </table>
                </td>
            </tr>
            <tr>
              <td>400</td><td>Invalid parameters supplied</td>
            </tr>
            <tr>
              <td>403</td><td>Forbidden, e.g. Invalid API key supplied</td>
            </tr>
            <tr>
              <td>500</td><td>Internal Server Error</td>
            </tr>
        </table>
    </td>
  </tr>
</table>


#### /failedProcessings/{id}

<table>
  <tr>
    <th>HTTP Request Method</th>
    <td>GET</td>
  </tr>
  <tr>
    <th>Summary</th>
    <td>Get failed processing by id</td>
  </tr>
  <tr>
    <th>Parameters</th>
    <td>
        <table>
            <tr>
              <th>Name</th><th>Type</th><th>Location</th><th>Required</th><th>Description</th>
            </tr>
            <tr>
              <td>ApiKey</td><td>string</td><td>header</td><td>Yes</td><td>Api Key</td>
            </tr>
            <tr>
              <td>id</td><td>string</td><td>path</td><td>Yes</td><td>id of FailedProcessing to get</td>
            </tr>
        </table>
    </td>
  </tr>
  <tr>
    <th>Responses</th>
    <td>
        <table>
            <tr>
              <th>Code</th><th>Description</th>
            </tr>
            <tr>
              <td>200</td>
              <td>
                The request succeeded
                <table>
                  <tr>
                    <th>Content Type</th>
                    <td>application/json</td>
                  </tr>
                  <tr>
                    <th>Example Value<br>(Schema)</th>
                    <td>
<pre>{
  "id": "string",
  "topic": "string",
  "missionId": "string",
  "errorLevel": "ERROR",
  "failureDate": "2022-09-07T21:31:08.604Z",
  "failureMessage": "string",
  "stacktrace": "string",
  "message": {
    "uid": "",
    "keyObjectStorage": "",
    "creationDate": "2020-01-20T12:40:37.464424Z",
    "allowedActions": [
      "RESTART"
    ],
    "debug": true,
    "demandType": "EXTERNAL_DEMAND",
    "retryCounter": 1,
    "relativePath": "",
    "productName": "S1A_WV_RAW__0SSV_20200120T124037_20200120T124728_030884_038B5D_3C86.SAFE",
    "productSizeByte": 951324687,
    "stationName": "WILE",
    "mode": "",
    "timeliness": "FAST24"
  },
  "retryCounter": 0
}</pre>
                    </td>
                  </tr>
                </table>
            </tr>
            <tr>
              <td>400</td><td>Invalid parameters supplied</td>
            </tr>
            <tr>
              <td>403</td><td>Forbidden, e.g. Invalid API key supplied</td>
            </tr>
            <tr>
              <td>404</td><td>Item not found</td>
            </tr>
            <tr>
              <td>500</td><td>Internal Server Error</td>
            </tr>
        </table>
    </td>
  </tr>
</table>

<table>
  <tr>
    <th>HTTP Request Method</th>
    <td>DELETE</td>
  </tr>
  <tr>
    <th>Summary</th>
    <td>Deletes a failed processing by id</td>
  </tr>
  <tr>
    <th>Parameters</th>
    <td>
        <table>
            <tr>
              <th>Name</th><th>Type</th><th>Location</th><th>Required</th><th>Description</th>
            </tr>
            <tr>
              <td>ApiKey</td><td>string</td><td>header</td><td>Yes</td><td>Api Key</td>
            </tr>
            <tr>
              <td>id</td><td>string</td><td>path</td><td>Yes</td><td>id of FailedProcessing to delete</td>
            </tr>
        </table>
    </td>
  </tr>
  <tr>
    <th>Responses</th>
    <td>
        <table>
            <tr>
              <th>Code</th><th>Description</th>
            </tr>
            <tr>
              <td>200</td><td>The request succeeded</td>
            </tr>
            <tr>
              <td>400</td><td>Invalid parameters supplied</td>
            </tr>
            <tr>
              <td>403</td><td>Forbidden, e.g. Invalid API key supplied</td>
            </tr>
            <tr>
              <td>404</td><td>Item not found</td>
            </tr>
            <tr>
              <td>500</td><td>Internal Server Error</td>
            </tr>
        </table>
    </td>
  </tr>
</table>


#### /failedProcessings/{id}/restart

<table>
  <tr>
    <th>HTTP Request Method</th>
    <td>POST</td>
  </tr>
  <tr>
    <th>Summary</th>
    <td>Restart a failed processing job. This removes the failed processing from the list and restarts it.</td>
  </tr>
  <tr>
    <th>Parameters</th>
    <td>
        <table>
            <tr>
              <th>Name</th><th>Type</th><th>Location</th><th>Required</th><th>Description</th>
            </tr>
            <tr>
              <td>ApiKey</td><td>string</td><td>header</td><td>Yes</td><td>Api Key</td>
            </tr>
            <tr>
              <td>id</td><td>string</td><td>path</td><td>Yes</td><td>id of FailedProcessing job to restart</td>
            </tr>
        </table>
    </td>
  </tr>
  <tr>
    <th>Responses</th>
    <td>
        <table>
            <tr>
              <th>Code</th><th>Description</th>
            </tr>
            <tr>
              <td>200</td>
              <td>
                The request succeeded
                <table>
                  <tr>
                    <th>Content Type</th>
                    <td>application/json</td>
                  </tr>
                  <tr>
                    <th>Example Value<br>(Schema)</th>
                    <td>
<pre>{
  "entity": "FailedProcessing",
  "action": "restart",
  "idsWithSuccess": [
    "string"
  ],
  "idsSkipped": [
    "string"
  ]
}</pre>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
            <tr>
              <td>400</td><td>Invalid parameters supplied</td>
            </tr>
            <tr>
              <td>403</td><td>Forbidden, e.g. Invalid API key supplied</td>
            </tr>
            <tr>
              <td>404</td><td>Item not found or no restart available</td>
            </tr>
            <tr>
              <td>500</td><td>Internal Server Error</td>
            </tr>
        </table>
    </td>
  </tr>
</table>


#### /failedProcessings/{id}/resubmit

<table>
  <tr>
    <th>HTTP Request Method</th>
    <td>POST</td>
  </tr>
  <tr>
    <th>Summary</th>
    <td>Resubmit a failed processing event. This removes the failed processing from the list and resubmits it.</td>
  </tr>
  <tr>
    <th>Parameters</th>
    <td>
        <table>
            <tr>
              <th>Name</th><th>Type</th><th>Location</th><th>Required</th><th>Description</th>
            </tr>
            <tr>
              <td>ApiKey</td><td>string</td><td>header</td><td>Yes</td><td>Api Key</td>
            </tr>
            <tr>
              <td>id</td><td>string</td><td>path</td><td>Yes</td><td>id of FailedProcessing job to resubmit</td>
            </tr>
        </table>
    </td>
  </tr>
  <tr>
    <th>Responses</th>
    <td>
        <table>
            <tr>
              <th>Code</th><th>Description</th>
            </tr>
            <tr>
              <td>200</td>
              <td>
                The request succeeded
                <table>
                  <tr>
                    <th>Content Type</th>
                    <td>application/json</td>
                  </tr>
                  <tr>
                    <th>Example Value<br>(Schema)</th>
                    <td>
<pre>{
  "entity": "FailedProcessing",
  "action": "resubmit",
  "idsWithSuccess": [
    "string"
  ],
  "idsSkipped": [
    "string"
  ]
}</pre>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
            <tr>
              <td>400</td><td>Invalid parameters supplied</td>
            </tr>
            <tr>
              <td>403</td><td>Forbidden, e.g. Invalid API key supplied</td>
            </tr>
            <tr>
              <td>404</td><td>Item not found or no resubmit available</td>
            </tr>
            <tr>
              <td>500</td><td>Internal Server Error</td>
            </tr>
        </table>
    </td>
  </tr>
</table>


#### /failedProcessings/delete

<table>
  <tr>
    <th>HTTP Request Method</th>
    <td>POST</td>
  </tr>
  <tr>
    <th>Summary</th>
    <td>Delete multiple failed processings in a batch. Ids for not existing failed processings are ignored.</td>
  </tr>
  <tr>
    <th>Parameters</th>
    <td>
        <table>
            <tr>
              <th>Name</th><th>Type</th><th>Location</th><th>Required</th><th>Description</th>
            </tr>
            <tr>
              <td>ApiKey</td><td>string</td><td>header</td><td>Yes</td><td>Api Key</td>
            </tr>
            <tr>
              <td>id</td><td>string</td><td>path</td><td>Yes</td><td>id of FailedProcessing to delete</td>
            </tr>
        </table>
    </td>
  </tr>
  <tr>
    <th>Request Body</th>
    <td>
      <table>
        <tr>
          <th>
            Content Type
          </th>
          <td>
            application/json
          </td>
        </tr> 
        <tr>
          <th>
            Example Value<br>(Schema)
          </th>
          <td>
<pre>{
  "ids": [
    "string"
  ]
}</pre>
          </td>
      </tr>
    </table>
    </td>
  </tr>
  <tr>
    <th>Responses</th>
    <td>
        <table>
            <tr>
              <th>Code</th><th>Description</th>
            </tr>
            <tr>
              <td>200</td>
              <td>
                The request succeeded
                <table>
                  <tr>
                    <th>Content Type</th>
                    <td>application/json</td>
                  </tr>
                  <tr>
                    <th>Example Value<br>(Schema)</th>
                    <td>
<pre>{
  "entity": "FailedProcessing",
  "action": "delete",
  "idsWithSuccess": [
    "string"
  ],
  "idsSkipped": [
    "string"
  ]
}</pre>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
            <tr>
              <td>400</td><td>Invalid parameters supplied</td>
            </tr>
            <tr>
              <td>403</td><td>Forbidden, e.g. Invalid API key supplied</td>
            </tr>
            <tr>
              <td>404</td><td>Item not found</td>
            </tr>
            <tr>
              <td>500</td><td>Internal Server Error</td>
            </tr>
        </table>
    </td>
  </tr>
</table>


#### /failedProcessings/restart

<table>
  <tr>
    <th>HTTP Request Method</th>
    <td>POST</td>
  </tr>
  <tr>
    <th>Summary</th>
    <td>Restart multiple failed processing jobs in a batch. This removes the failed processings from the list and restarts them. Ids for not existing or not restartable failed processings are ignored.</td>
  </tr>
  <tr>
    <th>Parameters</th>
    <td>
        <table>
            <tr>
              <th>Name</th><th>Type</th><th>Location</th><th>Required</th><th>Description</th>
            </tr>
            <tr>
              <td>ApiKey</td><td>string</td><td>header</td><td>Yes</td><td>Api Key</td>
            </tr>
            <tr>
              <td>id</td><td>string</td><td>path</td><td>Yes</td><td>id of FailedProcessing to delete</td>
            </tr>
        </table>
    </td>
  </tr>
  <tr>
    <th>Request Body</th>
    <td>
      <table>
        <tr>
          <th>
            Content Type
          </th>
          <td>
            application/json
          </td>
        </tr> 
        <tr>
          <th>
            Example Value<br>(Schema)
          </th>
          <td>
<pre>{
  "ids": [
    "string"
  ]
}</pre>
          </td>
      </tr>
    </table>
    </td>
  </tr>
  <tr>
    <th>Responses</th>
    <td>
        <table>
            <tr>
              <th>Code</th><th>Description</th>
            </tr>
            <tr>
              <td>200</td>
              <td>
                The request succeeded
                <table>
                  <tr>
                    <th>Content Type</th>
                    <td>application/json</td>
                  </tr>
                  <tr>
                    <th>Example Value<br>(Schema)</th>
                    <td>
<pre>{
  "entity": "FailedProcessing",
  "action": "restart",
  "idsWithSuccess": [
    "string"
  ],
  "idsSkipped": [
    "string"
  ]
}</pre>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
            <tr>
              <td>400</td><td>Invalid parameters supplied</td>
            </tr>
            <tr>
              <td>403</td><td>Forbidden, e.g. Invalid API key supplied</td>
            </tr>
            <tr>
              <td>404</td><td>Item not found</td>
            </tr>
            <tr>
              <td>500</td><td>Internal Server Error</td>
            </tr>
        </table>
    </td>
  </tr>
</table>


#### /failedProcessings/resubmit

<table>
  <tr>
    <th>HTTP Request Method</th>
    <td>POST</td>
  </tr>
  <tr>
    <th>Summary</th>
    <td>Multiple failed processing events in a batch. This removes the failed processings from the list and resubmits them. Ids for not existing or not resubmittable failed processings are ignored.</td>
  </tr>
  <tr>
    <th>Parameters</th>
    <td>
        <table>
            <tr>
              <th>Name</th><th>Type</th><th>Location</th><th>Required</th><th>Description</th>
            </tr>
            <tr>
              <td>ApiKey</td><td>string</td><td>header</td><td>Yes</td><td>Api Key</td>
            </tr>
            <tr>
              <td>id</td><td>string</td><td>path</td><td>Yes</td><td>id of FailedProcessing to delete</td>
            </tr>
        </table>
    </td>
  </tr>
<tr>
    <th>Request Body</th>
    <td>
      <table>
        <tr>
          <th>
            Content Type
          </th>
          <td>
            application/json
          </td>
        </tr> 
        <tr>
          <th>
            Example Value<br>(Schema)
          </th>
          <td>
<pre>{
  "ids": [
    "string"
  ]
}</pre>
          </td>
      </tr>
    </table>
    </td>
  </tr>
  <tr>
    <th>Responses</th>
    <td>
        <table>
            <tr>
              <th>Code</th><th>Description</th>
            </tr>
            <tr>
              <td>200</td>
              <td>
                The request succeeded
                <table>
                  <tr>
                    <th>Content Type</th>
                    <td>application/json</td>
                  </tr>
                  <tr>
                    <th>Example Value<br>(Schema)</th>
                    <td>
<pre>{
  "entity": "FailedProcessing",
  "action": "resubmit",
  "idsWithSuccess": [
    "string"
  ],
  "idsSkipped": [
    "string"
  ]
}</pre>
                    </td>
                  </tr>
                </table>
              </td>
            </tr>
            <tr>
              <td>400</td><td>Invalid parameters supplied</td>
            </tr>
            <tr>
              <td>403</td><td>Forbidden, e.g. Invalid API key supplied</td>
            </tr>
            <tr>
              <td>404</td><td>Item not found</td>
            </tr>
            <tr>
              <td>500</td><td>Internal Server Error</td>
            </tr>
        </table>
    </td>
  </tr>
</table>
