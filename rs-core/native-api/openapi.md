---
title: RS Native API v1.12.0-rc1
language_tabs:
  - shell: Shell
  - http: HTTP
  - javascript: JavaScript
  - ruby: Ruby
  - python: Python
  - php: PHP
  - java: Java
  - go: Go
toc_footers: []
includes: []
search: true
highlight_theme: darkula
headingLevel: 2

---

<!-- Generator: Widdershins v4.0.1 -->

<h1 id="rs-native-api">RS Native API v1.12.0-rc1</h1>

> Scroll down for code samples, example requests and responses. Select a language for code samples from the tabs above or the mobile navigation menu.

The Native API of the Copernicus Reference System (COPRS) that can be used to query and download product data.

Base URLs:

* <a href="http://localhost:8080">http://localhost:8080</a>

<h1 id="rs-native-api-default">Default</h1>

## servers__stac

> Code samples

```shell
# You can also use wget
curl -X SERVERS http://localhost:8080/stac

```

```http
SERVERS http://localhost:8080/stac HTTP/1.1
Host: localhost:8080

```

```javascript

fetch('http://localhost:8080/stac',
{
  method: 'SERVERS'

})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

result = RestClient.servers 'http://localhost:8080/stac',
  params: {
  }

p JSON.parse(result)

```

```python
import requests

r = requests.servers('http://localhost:8080/stac')

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('SERVERS','http://localhost:8080/stac', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/stac");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("SERVERS");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("SERVERS", "http://localhost:8080/stac", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`SERVERS /stac`

<h3 id="servers__stac-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|

<aside class="success">
This operation does not require authentication
</aside>

## servers__stac_{missionId}

> Code samples

```shell
# You can also use wget
curl -X SERVERS http://localhost:8080/stac/{missionId}

```

```http
SERVERS http://localhost:8080/stac/{missionId} HTTP/1.1
Host: localhost:8080

```

```javascript

fetch('http://localhost:8080/stac/{missionId}',
{
  method: 'SERVERS'

})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

result = RestClient.servers 'http://localhost:8080/stac/{missionId}',
  params: {
  }

p JSON.parse(result)

```

```python
import requests

r = requests.servers('http://localhost:8080/stac/{missionId}')

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('SERVERS','http://localhost:8080/stac/{missionId}', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/stac/{missionId}");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("SERVERS");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("SERVERS", "http://localhost:8080/stac/{missionId}", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`SERVERS /stac/{missionId}`

<h3 id="servers__stac_{missionid}-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|

<aside class="success">
This operation does not require authentication
</aside>

## servers__stac_{missionId}_collections

> Code samples

```shell
# You can also use wget
curl -X SERVERS http://localhost:8080/stac/{missionId}/collections

```

```http
SERVERS http://localhost:8080/stac/{missionId}/collections HTTP/1.1
Host: localhost:8080

```

```javascript

fetch('http://localhost:8080/stac/{missionId}/collections',
{
  method: 'SERVERS'

})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

result = RestClient.servers 'http://localhost:8080/stac/{missionId}/collections',
  params: {
  }

p JSON.parse(result)

```

```python
import requests

r = requests.servers('http://localhost:8080/stac/{missionId}/collections')

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('SERVERS','http://localhost:8080/stac/{missionId}/collections', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/stac/{missionId}/collections");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("SERVERS");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("SERVERS", "http://localhost:8080/stac/{missionId}/collections", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`SERVERS /stac/{missionId}/collections`

<h3 id="servers__stac_{missionid}_collections-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|

<aside class="success">
This operation does not require authentication
</aside>

## servers__stac_{missionId}_collections_{productType}

> Code samples

```shell
# You can also use wget
curl -X SERVERS http://localhost:8080/stac/{missionId}/collections/{productType}

```

```http
SERVERS http://localhost:8080/stac/{missionId}/collections/{productType} HTTP/1.1
Host: localhost:8080

```

```javascript

fetch('http://localhost:8080/stac/{missionId}/collections/{productType}',
{
  method: 'SERVERS'

})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

result = RestClient.servers 'http://localhost:8080/stac/{missionId}/collections/{productType}',
  params: {
  }

p JSON.parse(result)

```

```python
import requests

r = requests.servers('http://localhost:8080/stac/{missionId}/collections/{productType}')

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('SERVERS','http://localhost:8080/stac/{missionId}/collections/{productType}', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/stac/{missionId}/collections/{productType}");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("SERVERS");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("SERVERS", "http://localhost:8080/stac/{missionId}/collections/{productType}", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`SERVERS /stac/{missionId}/collections/{productType}`

<h3 id="servers__stac_{missionid}_collections_{producttype}-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|

<aside class="success">
This operation does not require authentication
</aside>

<h1 id="rs-native-api-stac-interface">STAC Interface</h1>

STAC interface to query items available on the RS

## get__stac

> Code samples

```shell
# You can also use wget
curl -X GET http://localhost:8080/stac \
  -H 'Accept: application/json'

```

```http
GET http://localhost:8080/stac HTTP/1.1
Host: localhost:8080
Accept: application/json

```

```javascript

const headers = {
  'Accept':'application/json'
};

fetch('http://localhost:8080/stac',
{
  method: 'GET',

  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Accept' => 'application/json'
}

result = RestClient.get 'http://localhost:8080/stac',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Accept': 'application/json'
}

r = requests.get('http://localhost:8080/stac', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Accept' => 'application/json',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('GET','http://localhost:8080/stac', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/stac");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("GET");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Accept": []string{"application/json"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("GET", "http://localhost:8080/stac", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`GET /stac`

*Landing page for the STAC interface*

> Example responses

> 200 Response

```json
{
  "stac_version": "string",
  "collections": [
    "string"
  ],
  "description": "string",
  "links": [
    {
      "rel": "string",
      "href": "string",
      "type": "string",
      "title": "string"
    }
  ],
  "id": "string",
  "conformsTo": [
    "string"
  ],
  "type": "string",
  "title": "string",
  "stac_extensions": [
    "string"
  ]
}
```

<h3 id="get__stac-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Returns landing page of the STAC interface|Inline|

<h3 id="get__stac-responseschema">Response Schema</h3>

Status Code **200**

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|» stac_version|string|false|none|none|
|» collections|[string]|false|none|none|
|» description|string|false|none|none|
|» links|[object]|false|none|none|
|»» rel|string|false|none|none|
|»» href|string|false|none|none|
|»» type|string|false|none|none|
|»» title|string|false|none|none|
|» id|string|false|none|none|
|» conformsTo|[string]|false|none|none|
|» type|string|false|none|none|
|» title|string|false|none|none|
|» stac_extensions|[string]|false|none|none|

<aside class="success">
This operation does not require authentication
</aside>

## get__stac_{missionId}

> Code samples

```shell
# You can also use wget
curl -X GET http://localhost:8080/stac/{missionId} \
  -H 'Accept: application/json'

```

```http
GET http://localhost:8080/stac/{missionId} HTTP/1.1
Host: localhost:8080
Accept: application/json

```

```javascript

const headers = {
  'Accept':'application/json'
};

fetch('http://localhost:8080/stac/{missionId}',
{
  method: 'GET',

  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Accept' => 'application/json'
}

result = RestClient.get 'http://localhost:8080/stac/{missionId}',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Accept': 'application/json'
}

r = requests.get('http://localhost:8080/stac/{missionId}', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Accept' => 'application/json',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('GET','http://localhost:8080/stac/{missionId}', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/stac/{missionId}");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("GET");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Accept": []string{"application/json"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("GET", "http://localhost:8080/stac/{missionId}", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`GET /stac/{missionId}`

*Retrieve SubCatalog for specific mission*

<h3 id="get__stac_{missionid}-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|missionId|path|string|true|Mission identifier to specify which Sentinel mission shall be queried. |

#### Enumerated Values

|Parameter|Value|
|---|---|
|missionId|S1|
|missionId|S2|
|missionId|S3|

> Example responses

> 200 Response

```json
{
  "stac_version": "string",
  "collections": [
    "string"
  ],
  "description": "string",
  "links": [
    {
      "rel": "string",
      "href": "string",
      "type": "string",
      "title": "string"
    }
  ],
  "id": "string",
  "type": "string",
  "title": "string",
  "stac_extensions": [
    "string"
  ]
}
```

<h3 id="get__stac_{missionid}-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Returns STAC conform catalog containing further links|Inline|

<h3 id="get__stac_{missionid}-responseschema">Response Schema</h3>

Status Code **200**

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|» stac_version|string|false|none|none|
|» collections|[string]|false|none|none|
|» description|string|false|none|none|
|» links|[object]|false|none|none|
|»» rel|string|false|none|none|
|»» href|string|false|none|none|
|»» type|string|false|none|none|
|»» title|string|false|none|none|
|» id|string|false|none|none|
|» type|string|false|none|none|
|» title|string|false|none|none|
|» stac_extensions|[string]|false|none|none|

<aside class="success">
This operation does not require authentication
</aside>

## get__stac_{missionId}_collections

> Code samples

```shell
# You can also use wget
curl -X GET http://localhost:8080/stac/{missionId}/collections \
  -H 'Accept: application/json'

```

```http
GET http://localhost:8080/stac/{missionId}/collections HTTP/1.1
Host: localhost:8080
Accept: application/json

```

```javascript

const headers = {
  'Accept':'application/json'
};

fetch('http://localhost:8080/stac/{missionId}/collections',
{
  method: 'GET',

  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Accept' => 'application/json'
}

result = RestClient.get 'http://localhost:8080/stac/{missionId}/collections',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Accept': 'application/json'
}

r = requests.get('http://localhost:8080/stac/{missionId}/collections', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Accept' => 'application/json',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('GET','http://localhost:8080/stac/{missionId}/collections', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/stac/{missionId}/collections");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("GET");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Accept": []string{"application/json"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("GET", "http://localhost:8080/stac/{missionId}/collections", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`GET /stac/{missionId}/collections`

*Retrieve list of collections for mission*

<h3 id="get__stac_{missionid}_collections-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|missionId|path|string|true|Mission identifier to specify which Sentinel mission shall be queried. |

#### Enumerated Values

|Parameter|Value|
|---|---|
|missionId|S1|
|missionId|S2|
|missionId|S3|

> Example responses

> 200 Response

```json
{
  "stac_version": "string",
  "collections": [
    "string"
  ],
  "description": "string",
  "links": [
    {
      "rel": "string",
      "href": "string",
      "type": "string",
      "title": "string"
    }
  ],
  "id": "string",
  "type": "string",
  "title": "string",
  "stac_extensions": [
    "string"
  ]
}
```

<h3 id="get__stac_{missionid}_collections-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Returns list of collections available for this mission. Collections map to product type.|Inline|

<h3 id="get__stac_{missionid}_collections-responseschema">Response Schema</h3>

Status Code **200**

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|» stac_version|string|false|none|none|
|» collections|[string]|false|none|none|
|» description|string|false|none|none|
|» links|[object]|false|none|none|
|»» rel|string|false|none|none|
|»» href|string|false|none|none|
|»» type|string|false|none|none|
|»» title|string|false|none|none|
|» id|string|false|none|none|
|» type|string|false|none|none|
|» title|string|false|none|none|
|» stac_extensions|[string]|false|none|none|

<aside class="success">
This operation does not require authentication
</aside>

## get__stac_{missionId}_collections_{productType}

> Code samples

```shell
# You can also use wget
curl -X GET http://localhost:8080/stac/{missionId}/collections/{productType} \
  -H 'Accept: application/json'

```

```http
GET http://localhost:8080/stac/{missionId}/collections/{productType} HTTP/1.1
Host: localhost:8080
Accept: application/json

```

```javascript

const headers = {
  'Accept':'application/json'
};

fetch('http://localhost:8080/stac/{missionId}/collections/{productType}',
{
  method: 'GET',

  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Accept' => 'application/json'
}

result = RestClient.get 'http://localhost:8080/stac/{missionId}/collections/{productType}',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Accept': 'application/json'
}

r = requests.get('http://localhost:8080/stac/{missionId}/collections/{productType}', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Accept' => 'application/json',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('GET','http://localhost:8080/stac/{missionId}/collections/{productType}', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/stac/{missionId}/collections/{productType}");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("GET");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Accept": []string{"application/json"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("GET", "http://localhost:8080/stac/{missionId}/collections/{productType}", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`GET /stac/{missionId}/collections/{productType}`

Auto generated using Swagger Inspector

<h3 id="get__stac_{missionid}_collections_{producttype}-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|missionId|path|string|true|Mission identifier to specify which Sentinel mission shall be queried. |
|productType|path|string|true|Product type to get the items of. |

#### Enumerated Values

|Parameter|Value|
|---|---|
|missionId|S1|
|missionId|S2|
|missionId|S3|

> Example responses

> 200 Response

```json
{
  "extent": null,
  "stac_version": "string",
  "keywords": [
    "string"
  ],
  "description": "string",
  "type": "string",
  "title": "string",
  "license": "string",
  "assets": {},
  "links": [
    {
      "rel": "string",
      "href": "string",
      "type": "string",
      "title": "string"
    }
  ],
  "id": "string",
  "stac_extensions": [
    "string"
  ],
  "providers": [
    "string"
  ],
  "summaries": {}
}
```

<h3 id="get__stac_{missionid}_collections_{producttype}-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|Auto generated using Swagger Inspector|Inline|

<h3 id="get__stac_{missionid}_collections_{producttype}-responseschema">Response Schema</h3>

Status Code **200**

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|» extent|any|false|none|none|
|» stac_version|string|false|none|none|
|» keywords|[string]|false|none|none|
|» description|string|false|none|none|
|» type|string|false|none|none|
|» title|string|false|none|none|
|» license|string|false|none|none|
|» assets|object|false|none|none|
|» links|[object]|false|none|none|
|»» rel|string|false|none|none|
|»» href|string|false|none|none|
|»» type|string|false|none|none|
|»» title|string|false|none|none|
|» id|string|false|none|none|
|» stac_extensions|[string]|false|none|none|
|» providers|[string]|false|none|none|
|» summaries|object|false|none|none|

<aside class="success">
This operation does not require authentication
</aside>

## getItemSearch

<a id="opIdgetItemSearch"></a>

> Code samples

```shell
# You can also use wget
curl -X GET http://localhost:8080/stac/search \
  -H 'Accept: application/geo+json'

```

```http
GET http://localhost:8080/stac/search HTTP/1.1
Host: localhost:8080
Accept: application/geo+json

```

```javascript

const headers = {
  'Accept':'application/geo+json'
};

fetch('http://localhost:8080/stac/search',
{
  method: 'GET',

  headers: headers
})
.then(function(res) {
    return res.json();
}).then(function(body) {
    console.log(body);
});

```

```ruby
require 'rest-client'
require 'json'

headers = {
  'Accept' => 'application/geo+json'
}

result = RestClient.get 'http://localhost:8080/stac/search',
  params: {
  }, headers: headers

p JSON.parse(result)

```

```python
import requests
headers = {
  'Accept': 'application/geo+json'
}

r = requests.get('http://localhost:8080/stac/search', headers = headers)

print(r.json())

```

```php
<?php

require 'vendor/autoload.php';

$headers = array(
    'Accept' => 'application/geo+json',
);

$client = new \GuzzleHttp\Client();

// Define array of request body.
$request_body = array();

try {
    $response = $client->request('GET','http://localhost:8080/stac/search', array(
        'headers' => $headers,
        'json' => $request_body,
       )
    );
    print_r($response->getBody()->getContents());
 }
 catch (\GuzzleHttp\Exception\BadResponseException $e) {
    // handle exception or api errors.
    print_r($e->getMessage());
 }

 // ...

```

```java
URL obj = new URL("http://localhost:8080/stac/search");
HttpURLConnection con = (HttpURLConnection) obj.openConnection();
con.setRequestMethod("GET");
int responseCode = con.getResponseCode();
BufferedReader in = new BufferedReader(
    new InputStreamReader(con.getInputStream()));
String inputLine;
StringBuffer response = new StringBuffer();
while ((inputLine = in.readLine()) != null) {
    response.append(inputLine);
}
in.close();
System.out.println(response.toString());

```

```go
package main

import (
       "bytes"
       "net/http"
)

func main() {

    headers := map[string][]string{
        "Accept": []string{"application/geo+json"},
    }

    data := bytes.NewBuffer([]byte{jsonReq})
    req, err := http.NewRequest("GET", "http://localhost:8080/stac/search", data)
    req.Header = headers

    client := &http.Client{}
    resp, err := client.Do(req)
    // ...
}

```

`GET /stac/search`

*Search STAC items with simple filtering.*

Retrieve Items matching filters. Intended as a shorthand API for simple
queries.

<h3 id="getitemsearch-parameters">Parameters</h3>

|Name|In|Type|Required|Description|
|---|---|---|---|---|
|ids|query|string|false|UUID value for a specific item.|
|bbox|query|string|false|4 points (longitude, latitude) describing a polygon. Longitude and |
|point|query|string|false|1 point (logitude, latitude). Longitude and latitude have to be |
|line|query|string|false|2 points (logitude, latitude) describing a line. Longitude and latitude |
|productname|query|string|false|Part of the productname that should be included in the retrieved items.|
|collections|query|string|false|Exact name of the product type which items shall be queried for.|
|cloudcover|query|string|false|Percentage value interval in which the items are filtered for. Has to be |
|datetime|query|string|false|Interval, open or closed. Date and time expressions adhere to RFC 3339. Open intervals are expressed using double-dots.|
|publicationdate|query|string|false|Interval, open or closed. Date and time expressions adhere to RFC 3339. |
|polarisation|query|string|false|Polarisation to filter for.|
|page|query|integer|false|Number of page used for pagination. First page is page number 1.|
|limit|query|integer|false|Maximum number of results that shall be retrieved. Too big values might |

#### Detailed descriptions

**ids**: UUID value for a specific item.
Example:
* "808dc636-5bd6-43d9-ad52-3b2b589c2d80"

**bbox**: 4 points (longitude, latitude) describing a polygon. Longitude and 
latitude have to be seperated by an URL encoded space ("%20"), points 
have to be seperated by a comma. Items with intersecting footprints 
will be returned.
Example:
* "76.036377%20-75.654331,76.57106%20-75.654331, 76.57196%20-75.594357,76.036377%20-75.594357"

**point**: 1 point (logitude, latitude). Longitude and latitude have to be 
seperated by an URL encoded space ("%20"). Items with a footprint 
containing the point will be returned.
Example:
* 76.036377%20-75.654331

**line**: 2 points (logitude, latitude) describing a line. Longitude and latitude 
have to be seperated by an URL encoded space ("%20"), points have to be 
seperated by a comma. Items with a footprint intersecting the line will 
be returned.
Example:
* 76.036377%20-75.654331,76.57106%20-75.654331

**productname**: Part of the productname that should be included in the retrieved items.
Example:
* S1A_EW_RAW__0NDH

**collections**: Exact name of the product type which items shall be queried for.
Example:
* OL_1_EFR___

**cloudcover**: Percentage value interval in which the items are filtered for. Has to be 
provided in the form min/max where empty values may be left out or 
replaced by "..".
Examples:
* Closed interval: "5.0/10.0"
* Open interval (minimum open): "../10.0"
* Open interval (maximum open): 90.0/..

**datetime**: Interval, open or closed. Date and time expressions adhere to RFC 3339. Open intervals are expressed using double-dots.

Examples:
* A closed interval: "2018-02-12T00:00:00Z/2018-03-18T12:31:12Z"
* Open intervals: "2018-02-12T00:00:00Z/.." or "../2018-03-18T12:31:12Z"

Only features that have a temporal property of `ContentDate` that 
intersects the value of `datetime` are selected.

**publicationdate**: Interval, open or closed. Date and time expressions adhere to RFC 3339. 
Open intervals are expressed using double-dots.

Examples:
* A closed interval: "2018-02-12T00:00:00Z/2018-03-18T12:31:12Z"
* Open intervals: "2018-02-12T00:00:00Z/.." or "../2018-03-18T12:31:12Z"

Only features that have a temporal property of `CreationDate` that 
intersects the value of `datetime` are selected.

**polarisation**: Polarisation to filter for.
Example:
* DV

**limit**: Maximum number of results that shall be retrieved. Too big values might 
be cut down to maximum value allowed by API.

> Example responses

> 200 Response

```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "stac_version": "1.0.0",
      "stac_extensions": [],
      "type": "Feature",
      "id": "808dc636-5bd6-43d9-ad52-3b2b589c2d80",
      "bbox": [
        -122.59750209,
        37.48803556,
        -122.2880486,
        37.613537207
      ],
      "geometry": {
        "type": "Polygon",
        "coordinates": [
          [
            [
              -122.308150179,
              37.488035566
            ],
            [
              -122.597502109,
              37.538869539
            ],
            [
              -122.576687533,
              37.613537207
            ],
            [
              -122.2880486,
              37.562818007
            ],
            [
              -122.308150179,
              37.488035566
            ]
          ]
        ]
      },
      "properties": {
        "datetime": null,
        "start_datetime": "2020-01-20T19:00:11.000Z",
        "end_datetime": "2020-01-20T19:02:09.000Z",
        "PublicationDate": "2020-01-21T13:58:58.187Z",
        "Name": "S1A_EW_RAW__0NDH_20200120T190011_20200120T190209_030888_038B84_FCD9.SAFE.zip",
        "ProductionType": "systematic_production",
        "ContentLength": 3545,
        "ContentDate": {
          "Start": "2020-01-20T19:00:11.000Z",
          "End": "2020-01-20T19:02:09.000Z"
        },
        "Checksum": [
          {
            "Algorithm": "MD5",
            "Value": "5f7aaadd0275c9e0e932c39f81fd1445",
            "ChecksumDate": "2020-01-20T13:58:52Z"
          }
        ],
        "AdditionalAttributes": {
          "StringAttributes": {
            "orbitDirection": "ASCENDING",
            "productType": "EW_RAW__0N"
          },
          "IntegerAttributes": {
            "missionDatatakeID": 232324,
            "orbitNumber": 385
          },
          "DoubleAttributes": {
            "completionTimeFromAscendingNode": 7654321,
            "startTimeFromAscendingNode": 1234567
          },
          "BooleanAttributes": {
            "valid": true,
            "extended": false
          },
          "DateTimeOffsetAttributes": {
            "beginningDateTime": "2020-01-20T19:00:11Z",
            "endingDateTime": "2020-01-20T19:02:09Z"
          }
        }
      },
      "links": [
        {
          "rel": "self",
          "href": "http://cool-sat.com/prip/odata/v1/Products(808dc636-5bd6-43d9-ad52-3b2b589c2d80)?$format=JSON",
          "description": "metadata for S1A_EW_RAW__0NDH_20200120T190011_20200120T190209_030888_038B84_FCD9.SAFE.zip",
          "type": "application/json"
        }
      ],
      "assets": {
        "product": {
          "href": "http://cool-sat.com/prip/odata/v1/Products(808dc636-5bd6-43d9-ad52-3b2b589c2d80)/$value",
          "title": "S1A_EW_RAW__0NDH_20200120T190011_20200120T190209_030888_038B84_FCD9.SAFE.zip",
          "description": "download link for product data",
          "type": "application/zip"
        }
      }
    }
  ]
}
```

<h3 id="getitemsearch-responses">Responses</h3>

|Status|Meaning|Description|Schema|
|---|---|---|---|
|200|[OK](https://tools.ietf.org/html/rfc7231#section-6.3.1)|A feature collection.|Inline|
|default|Default|An error occurred.|[exception](#schemaexception)|

<h3 id="getitemsearch-responseschema">Response Schema</h3>

Status Code **200**

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|» type|string|true|none|none|
|» features|[[item](#schemaitem)]|true|none|[A GeoJSON Feature augmented with foreign members that contain values relevant to a STAC entity]|
|»» stac_version|[stac_version](#schemastac_version)|true|none|none|
|»» stac_extensions|[anyOf]|false|none|none|

*anyOf*

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|»»» *anonymous*|string(uri)|false|none|none|

*or*

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|»»» *anonymous*|string|false|none|none|

*continued*

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|»» id|[itemId](#schemaitemid)|true|none|Provider identifier, a unique ID.|
|»» bbox|[number]|true|none|Only features that have a geometry that intersects the bounding box are<br>selected. The bounding box is provided as four or six numbers,<br>depending on whether the coordinate reference system includes a<br>vertical axis (elevation or depth):<br><br>* Lower left corner, coordinate axis 1<br>* Lower left corner, coordinate axis 2  <br>* Lower left corner, coordinate axis 3 (optional) <br>* Upper right corner, coordinate axis 1 <br>* Upper right corner, coordinate axis 2 <br>* Upper right corner, coordinate axis 3 (optional)<br><br>The coordinate reference system of the values is WGS84<br>longitude/latitude (http://www.opengis.net/def/crs/OGC/1.3/CRS84).<br><br>For WGS84 longitude/latitude the values are in most cases the sequence<br>of minimum longitude, minimum latitude, maximum longitude and maximum<br>latitude. However, in cases where the box spans the antimeridian the<br>first value (west-most box edge) is larger than the third value<br>(east-most box edge).<br><br>If a feature has multiple spatial geometry properties, it is the<br>decision of the server whether only a single spatial geometry property<br>is used to determine the extent or all relevant geometries.<br><br>Example: The bounding box of the New Zealand Exclusive Economic Zone in<br>WGS 84 (from 160.6°E to 170°W and from 55.95°S to 25.89°S) would be<br>represented in JSON as `[160.6, -55.95, -170, -25.89]` and in a query as<br>`bbox=160.6,-55.95,-170,-25.89`.|
|»» geometry|any|true|none|none|

*oneOf*

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|»»» *anonymous*|[pointGeoJSON](#schemapointgeojson)|false|none|none|
|»»»» type|string|true|none|none|
|»»»» coordinates|[number]|true|none|none|

*xor*

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|»»» *anonymous*|[multipointGeoJSON](#schemamultipointgeojson)|false|none|none|
|»»»» type|string|true|none|none|
|»»»» coordinates|[array]|true|none|none|

*xor*

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|»»» *anonymous*|[linestringGeoJSON](#schemalinestringgeojson)|false|none|none|
|»»»» type|string|true|none|none|
|»»»» coordinates|[array]|true|none|none|

*xor*

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|»»» *anonymous*|[multilinestringGeoJSON](#schemamultilinestringgeojson)|false|none|none|
|»»»» type|string|true|none|none|
|»»»» coordinates|[array]|true|none|none|

*xor*

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|»»» *anonymous*|[polygonGeoJSON](#schemapolygongeojson)|false|none|none|
|»»»» type|string|true|none|none|
|»»»» coordinates|[array]|true|none|none|

*xor*

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|»»» *anonymous*|[multipolygonGeoJSON](#schemamultipolygongeojson)|false|none|none|
|»»»» type|string|true|none|none|
|»»»» coordinates|[array]|true|none|none|

*xor*

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|»»» *anonymous*|[geometrycollectionGeoJSON](#schemageometrycollectiongeojson)|false|none|none|
|»»»» type|string|true|none|none|
|»»»» geometries|[oneOf]|true|none|none|

*xor*

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|»»» *anonymous*|any|false|none|none|

*continued*

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|»» type|[itemType](#schemaitemtype)|true|none|The GeoJSON type|
|»» links|[[link](#schemalink)]|true|none|none|
|»»» Link|[link](#schemalink)|false|none|none|
|»»»» href|string(uri)|true|none|The location of the resource|
|»»»» rel|string|true|none|Relation type of the link|
|»»»» type|string|false|none|The media type of the resource|
|»»»» title|string|false|none|Title of the resource|
|»»»» method|string|false|none|Specifies the HTTP method that the resource expects|
|»»»» headers|object|false|none|Object key values pairs they map to headers|
|»»»» body|object|false|none|For POST requests, the resource can specify the HTTP body as a JSON object.|
|»»»» merge|boolean|false|none|This is only valid when the server is responding to POST request.<br><br>If merge is true, the client is expected to merge the body value<br>into the current request body before following the link.<br>This avoids passing large post bodies back and forth when following<br>links, particularly for navigating pages through the `POST /search`<br>endpoint.<br><br>NOTE: To support form encoding it is expected that a client be able<br>to merge in the key value pairs specified as JSON<br>`{"next": "token"}` will become `&next=token`.|
|»» properties|[properties](#schemaproperties)|true|none|provides the core metadata fields plus extensions|
|»»» **additionalProperties**|any|false|none|Any additional properties added in via Item specification or extensions.|
|»»» datetime|[datetime](#schemadatetime)(date-time)¦null|true|none|The searchable date and time of the assets, in UTC.<br>It is formatted according to [RFC 3339, section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6).<br>`null` is allowed, but requires `start_datetime` and `end_datetime` from common metadata to be set.|
|»»» start_datetime|[datetime](#schemadatetime)(date-time)¦null|false|none|The searchable date and time of the assets, in UTC.<br>It is formatted according to [RFC 3339, section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6).<br>`null` is allowed, but requires `start_datetime` and `end_datetime` from common metadata to be set.|
|»»» end_datetime|[datetime](#schemadatetime)(date-time)¦null|false|none|The searchable date and time of the assets, in UTC.<br>It is formatted according to [RFC 3339, section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6).<br>`null` is allowed, but requires `start_datetime` and `end_datetime` from common metadata to be set.|
|»»» PublicationDate|string|false|none|none|
|»»» EvictionDate|string|false|none|none|
|»»» Checksum|[Checksum](#schemachecksum)|false|none|checksum object containing the checksum value for the product file|
|»»»» Algorithm|string|false|none|the hash function used for the calculation of the checksum value|
|»»»» Value|string|false|none|the checksum value for the product file|
|»»»» ChecksumDate|string|false|none|the date and time the checksum was calculated|
|»»» ContentDate|[ContentDate](#schemacontentdate)|false|none|object containing the start and end dates of the product|
|»»»» Start|string|false|none|the start date and time of the product|
|»»»» End|string|false|none|the end date and time of the product|
|»»» Name|string|false|none|none|
|»»» ContentType|string|false|none|none|
|»»» ProductionType|string|false|none|none|
|»»» Online|boolean|false|none|none|
|»»» ContentLength|integer|false|none|none|
|»»» AdditionalAttributes|[AdditionalAttributes](#schemaadditionalattributes)|false|none|additional attributes/values segmented by value type|
|»»»» StringAttributes|object|false|none|attributes with values of type string|
|»»»»» **additionalProperties**|string|false|none|none|
|»»»» IntegerAttributes|object|false|none|attributes with values of type integer|
|»»»»» **additionalProperties**|integer|false|none|none|
|»»»» DoubleAttributes|object|false|none|attributes with values of type number|
|»»»»» **additionalProperties**|number|false|none|none|
|»»»» DateTimeOffsetAttributes|object|false|none|attributes with date+time values of type string|
|»»»»» **additionalProperties**|string|false|none|none|
|»»»» BooleanAttributes|object|false|none|attributes with values of type boolean|
|»»»»» **additionalProperties**|boolean|false|none|none|
|»» assets|[assets](#schemaassets)|true|none|none|
|»»» **additionalProperties**|object|false|none|none|
|»»»» href|string(url)|true|none|Link to the asset object|
|»»»» title|string|false|none|Displayed title|
|»»»» description|string|false|none|Multi-line description to explain the asset.<br><br>[CommonMark 0.29](http://commonmark.org/) syntax MAY be used for rich text representation.|
|»»»» type|string|false|none|Media type of the asset|
|»»»» roles|[string]|false|none|Purposes of the asset|

#### Enumerated Values

|Property|Value|
|---|---|
|type|FeatureCollection|
|type|Point|
|type|MultiPoint|
|type|LineString|
|type|MultiLineString|
|type|Polygon|
|type|MultiPolygon|
|type|GeometryCollection|
|type|Feature|
|method|GET|
|method|POST|

<aside class="success">
This operation does not require authentication
</aside>

# Schemas

<h2 id="tocS_Checksum">Checksum</h2>
<!-- backwards compatibility -->
<a id="schemachecksum"></a>
<a id="schema_Checksum"></a>
<a id="tocSchecksum"></a>
<a id="tocschecksum"></a>

```json
{
  "Algorithm": "MD5",
  "Value": "71f920fa275127a7b60fa4d4d41432a3",
  "ChecksumDate": "2021-09-09T18:00:00.000Z"
}

```

checksum object containing the checksum value for the product file

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|Algorithm|string|false|none|the hash function used for the calculation of the checksum value|
|Value|string|false|none|the checksum value for the product file|
|ChecksumDate|string|false|none|the date and time the checksum was calculated|

<h2 id="tocS_ContentDate">ContentDate</h2>
<!-- backwards compatibility -->
<a id="schemacontentdate"></a>
<a id="schema_ContentDate"></a>
<a id="tocScontentdate"></a>
<a id="tocscontentdate"></a>

```json
{
  "Start": "2021-09-09T18:00:00.000Z",
  "End": "2021-09-09T18:00:00.000Z"
}

```

object containing the start and end dates of the product

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|Start|string|false|none|the start date and time of the product|
|End|string|false|none|the end date and time of the product|

<h2 id="tocS_AdditionalAttributes">AdditionalAttributes</h2>
<!-- backwards compatibility -->
<a id="schemaadditionalattributes"></a>
<a id="schema_AdditionalAttributes"></a>
<a id="tocSadditionalattributes"></a>
<a id="tocsadditionalattributes"></a>

```json
{
  "StringAttributes": {
    "property1": "string",
    "property2": "string"
  },
  "IntegerAttributes": {
    "property1": 0,
    "property2": 0
  },
  "DoubleAttributes": {
    "property1": 0,
    "property2": 0
  },
  "DateTimeOffsetAttributes": {
    "property1": "string",
    "property2": "string"
  },
  "BooleanAttributes": {
    "property1": true,
    "property2": true
  }
}

```

additional attributes/values segmented by value type

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|StringAttributes|object|false|none|attributes with values of type string|
|» **additionalProperties**|string|false|none|none|
|IntegerAttributes|object|false|none|attributes with values of type integer|
|» **additionalProperties**|integer|false|none|none|
|DoubleAttributes|object|false|none|attributes with values of type number|
|» **additionalProperties**|number|false|none|none|
|DateTimeOffsetAttributes|object|false|none|attributes with date+time values of type string|
|» **additionalProperties**|string|false|none|none|
|BooleanAttributes|object|false|none|attributes with values of type boolean|
|» **additionalProperties**|boolean|false|none|none|

<h2 id="tocS_exception">exception</h2>
<!-- backwards compatibility -->
<a id="schemaexception"></a>
<a id="schema_exception"></a>
<a id="tocSexception"></a>
<a id="tocsexception"></a>

```json
{
  "code": "string",
  "description": "string"
}

```

Information about the exception: an error code plus an optional description.

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|code|string|true|none|none|
|description|string|false|none|none|

<h2 id="tocS_datetimeFilter">datetimeFilter</h2>
<!-- backwards compatibility -->
<a id="schemadatetimefilter"></a>
<a id="schema_datetimeFilter"></a>
<a id="tocSdatetimefilter"></a>
<a id="tocsdatetimefilter"></a>

```json
{
  "datetime": "2018-02-12T00:00:00Z/2018-03-18T12:31:12Z"
}

```

An object representing a date+time based filter.

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|datetime|[datetime_interval](#schemadatetime_interval)|false|none|Interval, open or closed. Date and time<br>expressions adhere to RFC 3339. Open intervals are expressed using double-dots.<br>Examples:<br>* A closed interval: "2018-02-12T00:00:00Z/2018-03-18T12:31:12Z"<br>* Open intervals: "2018-02-12T00:00:00Z/.." or "../2018-03-18T12:31:12Z"|

<h2 id="tocS_datetime_interval">datetime_interval</h2>
<!-- backwards compatibility -->
<a id="schemadatetime_interval"></a>
<a id="schema_datetime_interval"></a>
<a id="tocSdatetime_interval"></a>
<a id="tocsdatetime_interval"></a>

```json
"2018-02-12T00:00:00Z/2018-03-18T12:31:12Z"

```

Interval, open or closed. Date and time
expressions adhere to RFC 3339. Open intervals are expressed using double-dots.
Examples:
* A closed interval: "2018-02-12T00:00:00Z/2018-03-18T12:31:12Z"
* Open intervals: "2018-02-12T00:00:00Z/.." or "../2018-03-18T12:31:12Z"

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|string|false|none|Interval, open or closed. Date and time<br>expressions adhere to RFC 3339. Open intervals are expressed using double-dots.<br>Examples:<br>* A closed interval: "2018-02-12T00:00:00Z/2018-03-18T12:31:12Z"<br>* Open intervals: "2018-02-12T00:00:00Z/.." or "../2018-03-18T12:31:12Z"|

<h2 id="tocS_pointGeoJSON">pointGeoJSON</h2>
<!-- backwards compatibility -->
<a id="schemapointgeojson"></a>
<a id="schema_pointGeoJSON"></a>
<a id="tocSpointgeojson"></a>
<a id="tocspointgeojson"></a>

```json
{
  "type": "Point",
  "coordinates": [
    0,
    0
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|none|
|coordinates|[number]|true|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|Point|

<h2 id="tocS_multipointGeoJSON">multipointGeoJSON</h2>
<!-- backwards compatibility -->
<a id="schemamultipointgeojson"></a>
<a id="schema_multipointGeoJSON"></a>
<a id="tocSmultipointgeojson"></a>
<a id="tocsmultipointgeojson"></a>

```json
{
  "type": "MultiPoint",
  "coordinates": [
    [
      0,
      0
    ]
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|none|
|coordinates|[array]|true|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|MultiPoint|

<h2 id="tocS_linestringGeoJSON">linestringGeoJSON</h2>
<!-- backwards compatibility -->
<a id="schemalinestringgeojson"></a>
<a id="schema_linestringGeoJSON"></a>
<a id="tocSlinestringgeojson"></a>
<a id="tocslinestringgeojson"></a>

```json
{
  "type": "LineString",
  "coordinates": [
    [
      0,
      0
    ],
    [
      0,
      0
    ]
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|none|
|coordinates|[array]|true|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|LineString|

<h2 id="tocS_multilinestringGeoJSON">multilinestringGeoJSON</h2>
<!-- backwards compatibility -->
<a id="schemamultilinestringgeojson"></a>
<a id="schema_multilinestringGeoJSON"></a>
<a id="tocSmultilinestringgeojson"></a>
<a id="tocsmultilinestringgeojson"></a>

```json
{
  "type": "MultiLineString",
  "coordinates": [
    [
      [
        0,
        0
      ],
      [
        0,
        0
      ]
    ]
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|none|
|coordinates|[array]|true|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|MultiLineString|

<h2 id="tocS_polygonGeoJSON">polygonGeoJSON</h2>
<!-- backwards compatibility -->
<a id="schemapolygongeojson"></a>
<a id="schema_polygonGeoJSON"></a>
<a id="tocSpolygongeojson"></a>
<a id="tocspolygongeojson"></a>

```json
{
  "type": "Polygon",
  "coordinates": [
    [
      [
        0,
        0
      ],
      [
        0,
        0
      ],
      [
        0,
        0
      ],
      [
        0,
        0
      ]
    ]
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|none|
|coordinates|[array]|true|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|Polygon|

<h2 id="tocS_multipolygonGeoJSON">multipolygonGeoJSON</h2>
<!-- backwards compatibility -->
<a id="schemamultipolygongeojson"></a>
<a id="schema_multipolygonGeoJSON"></a>
<a id="tocSmultipolygongeojson"></a>
<a id="tocsmultipolygongeojson"></a>

```json
{
  "type": "MultiPolygon",
  "coordinates": [
    [
      [
        [
          0,
          0
        ],
        [
          0,
          0
        ],
        [
          0,
          0
        ],
        [
          0,
          0
        ]
      ]
    ]
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|none|
|coordinates|[array]|true|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|MultiPolygon|

<h2 id="tocS_geometryGeoJSON">geometryGeoJSON</h2>
<!-- backwards compatibility -->
<a id="schemageometrygeojson"></a>
<a id="schema_geometryGeoJSON"></a>
<a id="tocSgeometrygeojson"></a>
<a id="tocsgeometrygeojson"></a>

```json
{
  "type": "Point",
  "coordinates": [
    0,
    0
  ]
}

```

### Properties

oneOf

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[pointGeoJSON](#schemapointgeojson)|false|none|none|

xor

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[multipointGeoJSON](#schemamultipointgeojson)|false|none|none|

xor

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[linestringGeoJSON](#schemalinestringgeojson)|false|none|none|

xor

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[multilinestringGeoJSON](#schemamultilinestringgeojson)|false|none|none|

xor

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[polygonGeoJSON](#schemapolygongeojson)|false|none|none|

xor

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[multipolygonGeoJSON](#schemamultipolygongeojson)|false|none|none|

xor

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[geometrycollectionGeoJSON](#schemageometrycollectiongeojson)|false|none|none|

<h2 id="tocS_geometrycollectionGeoJSON">geometrycollectionGeoJSON</h2>
<!-- backwards compatibility -->
<a id="schemageometrycollectiongeojson"></a>
<a id="schema_geometrycollectionGeoJSON"></a>
<a id="tocSgeometrycollectiongeojson"></a>
<a id="tocsgeometrycollectiongeojson"></a>

```json
{
  "type": "GeometryCollection",
  "geometries": [
    {
      "type": "Point",
      "coordinates": [
        0,
        0
      ]
    }
  ]
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|none|
|geometries|[[geometryGeoJSON](#schemageometrygeojson)]|true|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|GeometryCollection|

<h2 id="tocS_geojson-bbox">geojson-bbox</h2>
<!-- backwards compatibility -->
<a id="schemageojson-bbox"></a>
<a id="schema_geojson-bbox"></a>
<a id="tocSgeojson-bbox"></a>
<a id="tocsgeojson-bbox"></a>

```json
[
  0,
  0,
  0,
  0
]

```

### Properties

*None*

<h2 id="tocS_point">point</h2>
<!-- backwards compatibility -->
<a id="schemapoint"></a>
<a id="schema_point"></a>
<a id="tocSpoint"></a>
<a id="tocspoint"></a>

```json
{
  "type": "Point",
  "coordinates": [
    0,
    0
  ],
  "bbox": [
    0,
    0,
    0,
    0
  ]
}

```

GeoJSON Point

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|none|
|coordinates|[number]|true|none|none|
|bbox|[geojson-bbox](#schemageojson-bbox)|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|Point|

<h2 id="tocS_linestring">linestring</h2>
<!-- backwards compatibility -->
<a id="schemalinestring"></a>
<a id="schema_linestring"></a>
<a id="tocSlinestring"></a>
<a id="tocslinestring"></a>

```json
{
  "type": "LineString",
  "coordinates": [
    [
      0,
      0
    ],
    [
      0,
      0
    ]
  ],
  "bbox": [
    0,
    0,
    0,
    0
  ]
}

```

GeoJSON LineString

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|none|
|coordinates|[array]|true|none|none|
|bbox|[geojson-bbox](#schemageojson-bbox)|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|LineString|

<h2 id="tocS_polygon">polygon</h2>
<!-- backwards compatibility -->
<a id="schemapolygon"></a>
<a id="schema_polygon"></a>
<a id="tocSpolygon"></a>
<a id="tocspolygon"></a>

```json
{
  "type": "Polygon",
  "coordinates": [
    [
      [
        0,
        0
      ],
      [
        0,
        0
      ],
      [
        0,
        0
      ],
      [
        0,
        0
      ]
    ]
  ],
  "bbox": [
    0,
    0,
    0,
    0
  ]
}

```

GeoJSON Polygon

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|none|
|coordinates|[array]|true|none|none|
|bbox|[geojson-bbox](#schemageojson-bbox)|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|Polygon|

<h2 id="tocS_multipoint">multipoint</h2>
<!-- backwards compatibility -->
<a id="schemamultipoint"></a>
<a id="schema_multipoint"></a>
<a id="tocSmultipoint"></a>
<a id="tocsmultipoint"></a>

```json
{
  "type": "MultiPoint",
  "coordinates": [
    [
      0,
      0
    ]
  ],
  "bbox": [
    0,
    0,
    0,
    0
  ]
}

```

GeoJSON MultiPoint

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|none|
|coordinates|[array]|true|none|none|
|bbox|[geojson-bbox](#schemageojson-bbox)|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|MultiPoint|

<h2 id="tocS_multilinestring">multilinestring</h2>
<!-- backwards compatibility -->
<a id="schemamultilinestring"></a>
<a id="schema_multilinestring"></a>
<a id="tocSmultilinestring"></a>
<a id="tocsmultilinestring"></a>

```json
{
  "type": "MultiLineString",
  "coordinates": [
    [
      [
        0,
        0
      ],
      [
        0,
        0
      ]
    ]
  ],
  "bbox": [
    0,
    0,
    0,
    0
  ]
}

```

GeoJSON MultiLineString

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|none|
|coordinates|[array]|true|none|none|
|bbox|[geojson-bbox](#schemageojson-bbox)|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|MultiLineString|

<h2 id="tocS_multipolygon">multipolygon</h2>
<!-- backwards compatibility -->
<a id="schemamultipolygon"></a>
<a id="schema_multipolygon"></a>
<a id="tocSmultipolygon"></a>
<a id="tocsmultipolygon"></a>

```json
{
  "type": "MultiPolygon",
  "coordinates": [
    [
      [
        [
          0,
          0
        ],
        [
          0,
          0
        ],
        [
          0,
          0
        ],
        [
          0,
          0
        ]
      ]
    ]
  ],
  "bbox": [
    0,
    0,
    0,
    0
  ]
}

```

GeoJSON MultiPolygon

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|none|
|coordinates|[array]|true|none|none|
|bbox|[geojson-bbox](#schemageojson-bbox)|false|none|none|

#### Enumerated Values

|Property|Value|
|---|---|
|type|MultiPolygon|

<h2 id="tocS_geometryLiteral">geometryLiteral</h2>
<!-- backwards compatibility -->
<a id="schemageometryliteral"></a>
<a id="schema_geometryLiteral"></a>
<a id="tocSgeometryliteral"></a>
<a id="tocsgeometryliteral"></a>

```json
{
  "type": "Point",
  "coordinates": [
    0,
    0
  ],
  "bbox": [
    0,
    0,
    0,
    0
  ]
}

```

### Properties

oneOf

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[point](#schemapoint)|false|none|none|

xor

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[linestring](#schemalinestring)|false|none|none|

xor

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[polygon](#schemapolygon)|false|none|none|

xor

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[multipoint](#schemamultipoint)|false|none|none|

xor

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[multilinestring](#schemamultilinestring)|false|none|none|

xor

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[multipolygon](#schemamultipolygon)|false|none|none|

<h2 id="tocS_bbox">bbox</h2>
<!-- backwards compatibility -->
<a id="schemabbox"></a>
<a id="schema_bbox"></a>
<a id="tocSbbox"></a>
<a id="tocsbbox"></a>

```json
[
  0
]

```

### Properties

oneOf

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|array|false|none|none|

xor

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|array|false|none|none|

<h2 id="tocS_stac_version">stac_version</h2>
<!-- backwards compatibility -->
<a id="schemastac_version"></a>
<a id="schema_stac_version"></a>
<a id="tocSstac_version"></a>
<a id="tocsstac_version"></a>

```json
"1.0.0"

```

STAC version

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|STAC version|string|false|none|none|

<h2 id="tocS_stac_extensions">stac_extensions</h2>
<!-- backwards compatibility -->
<a id="schemastac_extensions"></a>
<a id="schema_stac_extensions"></a>
<a id="tocSstac_extensions"></a>
<a id="tocsstac_extensions"></a>

```json
[
  "http://example.com"
]

```

STAC extensions

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|STAC extensions|[anyOf]|false|none|none|

anyOf

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|string(uri)|false|none|none|

or

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|string|false|none|none|

<h2 id="tocS_itemId">itemId</h2>
<!-- backwards compatibility -->
<a id="schemaitemid"></a>
<a id="schema_itemId"></a>
<a id="tocSitemid"></a>
<a id="tocsitemid"></a>

```json
"string"

```

Provider identifier, a unique ID.

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|string|false|none|Provider identifier, a unique ID.|

<h2 id="tocS_itemType">itemType</h2>
<!-- backwards compatibility -->
<a id="schemaitemtype"></a>
<a id="schema_itemType"></a>
<a id="tocSitemtype"></a>
<a id="tocsitemtype"></a>

```json
"Feature"

```

The GeoJSON type

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|string|false|none|The GeoJSON type|

#### Enumerated Values

|Property|Value|
|---|---|
|*anonymous*|Feature|

<h2 id="tocS_link">link</h2>
<!-- backwards compatibility -->
<a id="schemalink"></a>
<a id="schema_link"></a>
<a id="tocSlink"></a>
<a id="tocslink"></a>

```json
{
  "href": "http://example.com",
  "rel": "string",
  "type": "string",
  "title": "string",
  "method": "GET",
  "headers": {
    "Accept": "application/json"
  },
  "body": {},
  "merge": false
}

```

Link

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|href|string(uri)|true|none|The location of the resource|
|rel|string|true|none|Relation type of the link|
|type|string|false|none|The media type of the resource|
|title|string|false|none|Title of the resource|
|method|string|false|none|Specifies the HTTP method that the resource expects|
|headers|object|false|none|Object key values pairs they map to headers|
|body|object|false|none|For POST requests, the resource can specify the HTTP body as a JSON object.|
|merge|boolean|false|none|This is only valid when the server is responding to POST request.<br><br>If merge is true, the client is expected to merge the body value<br>into the current request body before following the link.<br>This avoids passing large post bodies back and forth when following<br>links, particularly for navigating pages through the `POST /search`<br>endpoint.<br><br>NOTE: To support form encoding it is expected that a client be able<br>to merge in the key value pairs specified as JSON<br>`{"next": "token"}` will become `&next=token`.|

#### Enumerated Values

|Property|Value|
|---|---|
|method|GET|
|method|POST|

<h2 id="tocS_links">links</h2>
<!-- backwards compatibility -->
<a id="schemalinks"></a>
<a id="schema_links"></a>
<a id="tocSlinks"></a>
<a id="tocslinks"></a>

```json
[
  {
    "href": "http://example.com",
    "rel": "string",
    "type": "string",
    "title": "string",
    "method": "GET",
    "headers": {
      "Accept": "application/json"
    },
    "body": {},
    "merge": false
  }
]

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|[[link](#schemalink)]|false|none|none|

<h2 id="tocS_datetime">datetime</h2>
<!-- backwards compatibility -->
<a id="schemadatetime"></a>
<a id="schema_datetime"></a>
<a id="tocSdatetime"></a>
<a id="tocsdatetime"></a>

```json
"2018-02-12T00:00:00Z"

```

The searchable date and time of the assets, in UTC.
It is formatted according to [RFC 3339, section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6).
`null` is allowed, but requires `start_datetime` and `end_datetime` from common metadata to be set.

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|*anonymous*|string(date-time)¦null|false|none|The searchable date and time of the assets, in UTC.<br>It is formatted according to [RFC 3339, section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6).<br>`null` is allowed, but requires `start_datetime` and `end_datetime` from common metadata to be set.|

<h2 id="tocS_properties">properties</h2>
<!-- backwards compatibility -->
<a id="schemaproperties"></a>
<a id="schema_properties"></a>
<a id="tocSproperties"></a>
<a id="tocsproperties"></a>

```json
{
  "datetime": "2018-02-12T00:00:00Z",
  "start_datetime": "2018-02-12T00:00:00Z",
  "end_datetime": "2018-02-12T00:00:00Z",
  "PublicationDate": "string",
  "EvictionDate": "string",
  "Checksum": {
    "Algorithm": "MD5",
    "Value": "71f920fa275127a7b60fa4d4d41432a3",
    "ChecksumDate": "2021-09-09T18:00:00.000Z"
  },
  "ContentDate": {
    "Start": "2021-09-09T18:00:00.000Z",
    "End": "2021-09-09T18:00:00.000Z"
  },
  "Name": "string",
  "ContentType": "string",
  "ProductionType": "string",
  "Online": true,
  "ContentLength": 0,
  "AdditionalAttributes": {
    "StringAttributes": {
      "property1": "string",
      "property2": "string"
    },
    "IntegerAttributes": {
      "property1": 0,
      "property2": 0
    },
    "DoubleAttributes": {
      "property1": 0,
      "property2": 0
    },
    "DateTimeOffsetAttributes": {
      "property1": "string",
      "property2": "string"
    },
    "BooleanAttributes": {
      "property1": true,
      "property2": true
    }
  },
  "property1": null,
  "property2": null
}

```

provides the core metadata fields plus extensions

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|**additionalProperties**|any|false|none|Any additional properties added in via Item specification or extensions.|
|datetime|[datetime](#schemadatetime)|true|none|The searchable date and time of the assets, in UTC.<br>It is formatted according to [RFC 3339, section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6).<br>`null` is allowed, but requires `start_datetime` and `end_datetime` from common metadata to be set.|
|start_datetime|[datetime](#schemadatetime)|false|none|The searchable date and time of the assets, in UTC.<br>It is formatted according to [RFC 3339, section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6).<br>`null` is allowed, but requires `start_datetime` and `end_datetime` from common metadata to be set.|
|end_datetime|[datetime](#schemadatetime)|false|none|The searchable date and time of the assets, in UTC.<br>It is formatted according to [RFC 3339, section 5.6](https://tools.ietf.org/html/rfc3339#section-5.6).<br>`null` is allowed, but requires `start_datetime` and `end_datetime` from common metadata to be set.|
|PublicationDate|string|false|none|none|
|EvictionDate|string|false|none|none|
|Checksum|[Checksum](#schemachecksum)|false|none|checksum object containing the checksum value for the product file|
|ContentDate|[ContentDate](#schemacontentdate)|false|none|object containing the start and end dates of the product|
|Name|string|false|none|none|
|ContentType|string|false|none|none|
|ProductionType|string|false|none|none|
|Online|boolean|false|none|none|
|ContentLength|integer|false|none|none|
|AdditionalAttributes|[AdditionalAttributes](#schemaadditionalattributes)|false|none|additional attributes/values segmented by value type|

<h2 id="tocS_assets">assets</h2>
<!-- backwards compatibility -->
<a id="schemaassets"></a>
<a id="schema_assets"></a>
<a id="tocSassets"></a>
<a id="tocsassets"></a>

```json
{
  "property1": {
    "href": "http://cool-sat.com/catalog/collections/cs/items/CS3-20160503_132130_04/thumb.png",
    "title": "Thumbnail",
    "description": "Small 256x256px PNG thumbnail for a preview.",
    "type": "image/png",
    "roles": [
      "thumbnail"
    ]
  },
  "property2": {
    "href": "http://cool-sat.com/catalog/collections/cs/items/CS3-20160503_132130_04/thumb.png",
    "title": "Thumbnail",
    "description": "Small 256x256px PNG thumbnail for a preview.",
    "type": "image/png",
    "roles": [
      "thumbnail"
    ]
  }
}

```

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|**additionalProperties**|object|false|none|none|
|» href|string(url)|true|none|Link to the asset object|
|» title|string|false|none|Displayed title|
|» description|string|false|none|Multi-line description to explain the asset.<br><br>[CommonMark 0.29](http://commonmark.org/) syntax MAY be used for rich text representation.|
|» type|string|false|none|Media type of the asset|
|» roles|[string]|false|none|Purposes of the asset|

<h2 id="tocS_item">item</h2>
<!-- backwards compatibility -->
<a id="schemaitem"></a>
<a id="schema_item"></a>
<a id="tocSitem"></a>
<a id="tocsitem"></a>

```json
{
  "stac_version": "1.0.0",
  "stac_extensions": [],
  "type": "Feature",
  "id": "808dc636-5bd6-43d9-ad52-3b2b589c2d80",
  "bbox": [
    -122.59750209,
    37.48803556,
    -122.2880486,
    37.613537207
  ],
  "geometry": {
    "type": "Polygon",
    "coordinates": [
      [
        [
          -122.308150179,
          37.488035566
        ],
        [
          -122.597502109,
          37.538869539
        ],
        [
          -122.576687533,
          37.613537207
        ],
        [
          -122.2880486,
          37.562818007
        ],
        [
          -122.308150179,
          37.488035566
        ]
      ]
    ]
  },
  "properties": {
    "datetime": null,
    "start_datetime": "2020-01-20T19:00:11.000Z",
    "end_datetime": "2020-01-20T19:02:09.000Z",
    "PublicationDate": "2020-01-21T13:58:58.187Z",
    "Name": "S1A_EW_RAW__0NDH_20200120T190011_20200120T190209_030888_038B84_FCD9.SAFE.zip",
    "ProductionType": "systematic_production",
    "ContentLength": 3545,
    "ContentDate": {
      "Start": "2020-01-20T19:00:11.000Z",
      "End": "2020-01-20T19:02:09.000Z"
    },
    "Checksum": [
      {
        "Algorithm": "MD5",
        "Value": "5f7aaadd0275c9e0e932c39f81fd1445",
        "ChecksumDate": "2020-01-20T13:58:52Z"
      }
    ],
    "AdditionalAttributes": {
      "StringAttributes": {
        "orbitDirection": "ASCENDING",
        "productType": "EW_RAW__0N"
      },
      "IntegerAttributes": {
        "missionDatatakeID": 232324,
        "orbitNumber": 385
      },
      "DoubleAttributes": {
        "completionTimeFromAscendingNode": 7654321,
        "startTimeFromAscendingNode": 1234567
      },
      "BooleanAttributes": {
        "valid": true,
        "extended": false
      },
      "DateTimeOffsetAttributes": {
        "beginningDateTime": "2020-01-20T19:00:11Z",
        "endingDateTime": "2020-01-20T19:02:09Z"
      }
    }
  },
  "links": [
    {
      "rel": "self",
      "href": "http://cool-sat.com/prip/odata/v1/Products(808dc636-5bd6-43d9-ad52-3b2b589c2d80)?$format=JSON",
      "description": "metadata for S1A_EW_RAW__0NDH_20200120T190011_20200120T190209_030888_038B84_FCD9.SAFE.zip",
      "type": "application/json"
    }
  ],
  "assets": {
    "product": {
      "href": "http://cool-sat.com/prip/odata/v1/Products(808dc636-5bd6-43d9-ad52-3b2b589c2d80)/$value",
      "title": "S1A_EW_RAW__0NDH_20200120T190011_20200120T190209_030888_038B84_FCD9.SAFE.zip",
      "description": "download link for product data",
      "type": "application/zip"
    }
  }
}

```

A GeoJSON Feature augmented with foreign members that contain values relevant to a STAC entity

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|stac_version|[stac_version](#schemastac_version)|true|none|none|
|stac_extensions|[stac_extensions](#schemastac_extensions)|false|none|none|
|id|[itemId](#schemaitemid)|true|none|Provider identifier, a unique ID.|
|bbox|[schemas-bbox](#schemaschemas-bbox)|true|none|Only features that have a geometry that intersects the bounding box are<br>selected. The bounding box is provided as four or six numbers,<br>depending on whether the coordinate reference system includes a<br>vertical axis (elevation or depth):<br><br>* Lower left corner, coordinate axis 1<br>* Lower left corner, coordinate axis 2  <br>* Lower left corner, coordinate axis 3 (optional) <br>* Upper right corner, coordinate axis 1 <br>* Upper right corner, coordinate axis 2 <br>* Upper right corner, coordinate axis 3 (optional)<br><br>The coordinate reference system of the values is WGS84<br>longitude/latitude (http://www.opengis.net/def/crs/OGC/1.3/CRS84).<br><br>For WGS84 longitude/latitude the values are in most cases the sequence<br>of minimum longitude, minimum latitude, maximum longitude and maximum<br>latitude. However, in cases where the box spans the antimeridian the<br>first value (west-most box edge) is larger than the third value<br>(east-most box edge).<br><br>If a feature has multiple spatial geometry properties, it is the<br>decision of the server whether only a single spatial geometry property<br>is used to determine the extent or all relevant geometries.<br><br>Example: The bounding box of the New Zealand Exclusive Economic Zone in<br>WGS 84 (from 160.6°E to 170°W and from 55.95°S to 25.89°S) would be<br>represented in JSON as `[160.6, -55.95, -170, -25.89]` and in a query as<br>`bbox=160.6,-55.95,-170,-25.89`.|
|geometry|[geometryGeoJSON](#schemageometrygeojson)|true|none|none|
|type|[itemType](#schemaitemtype)|true|none|The GeoJSON type|
|links|[links](#schemalinks)|true|none|none|
|properties|[properties](#schemaproperties)|true|none|provides the core metadata fields plus extensions|
|assets|[assets](#schemaassets)|true|none|none|

<h2 id="tocS_itemCollection">itemCollection</h2>
<!-- backwards compatibility -->
<a id="schemaitemcollection"></a>
<a id="schema_itemCollection"></a>
<a id="tocSitemcollection"></a>
<a id="tocsitemcollection"></a>

```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "stac_version": "1.0.0",
      "stac_extensions": [],
      "type": "Feature",
      "id": "808dc636-5bd6-43d9-ad52-3b2b589c2d80",
      "bbox": [
        -122.59750209,
        37.48803556,
        -122.2880486,
        37.613537207
      ],
      "geometry": {
        "type": "Polygon",
        "coordinates": [
          [
            [
              -122.308150179,
              37.488035566
            ],
            [
              -122.597502109,
              37.538869539
            ],
            [
              -122.576687533,
              37.613537207
            ],
            [
              -122.2880486,
              37.562818007
            ],
            [
              -122.308150179,
              37.488035566
            ]
          ]
        ]
      },
      "properties": {
        "datetime": null,
        "start_datetime": "2020-01-20T19:00:11.000Z",
        "end_datetime": "2020-01-20T19:02:09.000Z",
        "PublicationDate": "2020-01-21T13:58:58.187Z",
        "Name": "S1A_EW_RAW__0NDH_20200120T190011_20200120T190209_030888_038B84_FCD9.SAFE.zip",
        "ProductionType": "systematic_production",
        "ContentLength": 3545,
        "ContentDate": {
          "Start": "2020-01-20T19:00:11.000Z",
          "End": "2020-01-20T19:02:09.000Z"
        },
        "Checksum": [
          {
            "Algorithm": "MD5",
            "Value": "5f7aaadd0275c9e0e932c39f81fd1445",
            "ChecksumDate": "2020-01-20T13:58:52Z"
          }
        ],
        "AdditionalAttributes": {
          "StringAttributes": {
            "orbitDirection": "ASCENDING",
            "productType": "EW_RAW__0N"
          },
          "IntegerAttributes": {
            "missionDatatakeID": 232324,
            "orbitNumber": 385
          },
          "DoubleAttributes": {
            "completionTimeFromAscendingNode": 7654321,
            "startTimeFromAscendingNode": 1234567
          },
          "BooleanAttributes": {
            "valid": true,
            "extended": false
          },
          "DateTimeOffsetAttributes": {
            "beginningDateTime": "2020-01-20T19:00:11Z",
            "endingDateTime": "2020-01-20T19:02:09Z"
          }
        }
      },
      "links": [
        {
          "rel": "self",
          "href": "http://cool-sat.com/prip/odata/v1/Products(808dc636-5bd6-43d9-ad52-3b2b589c2d80)?$format=JSON",
          "description": "metadata for S1A_EW_RAW__0NDH_20200120T190011_20200120T190209_030888_038B84_FCD9.SAFE.zip",
          "type": "application/json"
        }
      ],
      "assets": {
        "product": {
          "href": "http://cool-sat.com/prip/odata/v1/Products(808dc636-5bd6-43d9-ad52-3b2b589c2d80)/$value",
          "title": "S1A_EW_RAW__0NDH_20200120T190011_20200120T190209_030888_038B84_FCD9.SAFE.zip",
          "description": "download link for product data",
          "type": "application/zip"
        }
      }
    }
  ]
}

```

A GeoJSON FeatureCollection augmented with foreign members that contain values relevant to a STAC entity

### Properties

|Name|Type|Required|Restrictions|Description|
|---|---|---|---|---|
|type|string|true|none|none|
|features|[[item](#schemaitem)]|true|none|[A GeoJSON Feature augmented with foreign members that contain values relevant to a STAC entity]|

#### Enumerated Values

|Property|Value|
|---|---|
|type|FeatureCollection|

<h2 id="tocS_schemas-bbox">schemas-bbox</h2>
<!-- backwards compatibility -->
<a id="schemaschemas-bbox"></a>
<a id="schema_schemas-bbox"></a>
<a id="tocSschemas-bbox"></a>
<a id="tocsschemas-bbox"></a>

```json
[
  -110,
  39.5,
  -105,
  40.5
]

```

Only features that have a geometry that intersects the bounding box are
selected. The bounding box is provided as four or six numbers,
depending on whether the coordinate reference system includes a
vertical axis (elevation or depth):

* Lower left corner, coordinate axis 1
* Lower left corner, coordinate axis 2  
* Lower left corner, coordinate axis 3 (optional) 
* Upper right corner, coordinate axis 1 
* Upper right corner, coordinate axis 2 
* Upper right corner, coordinate axis 3 (optional)

The coordinate reference system of the values is WGS84
longitude/latitude (http://www.opengis.net/def/crs/OGC/1.3/CRS84).

For WGS84 longitude/latitude the values are in most cases the sequence
of minimum longitude, minimum latitude, maximum longitude and maximum
latitude. However, in cases where the box spans the antimeridian the
first value (west-most box edge) is larger than the third value
(east-most box edge).

If a feature has multiple spatial geometry properties, it is the
decision of the server whether only a single spatial geometry property
is used to determine the extent or all relevant geometries.

Example: The bounding box of the New Zealand Exclusive Economic Zone in
WGS 84 (from 160.6°E to 170°W and from 55.95°S to 25.89°S) would be
represented in JSON as `[160.6, -55.95, -170, -25.89]` and in a query as
`bbox=160.6,-55.95,-170,-25.89`.

### Properties

*None*

