# RS-Core - Connection to Object Storage

In order to simplify changes to the configuration of the connection to the Object Storage (OBS), the configuration is contained in the common properties file obs-stream-properties.properties.
This properties file is then appended to each individual stream in the build pipeline of GitHub.

# Additional setup

## Bucket initialization

It is mandatory to create the buckets that are configured in the properties below before starting any RS Core component that has a connection to the OBS. 
By default the follwing buckets have to be created:

```
rs-s1-aux
rs-session-files
rs-s1-l0-slices
rs-l0-acns
rs-s1-l0-segments
rs-s1-l0-blanks
rs-s1-invalid
rs-s1-ghost
rs-debug
rs-failed-workdir
rs-session-retransfer
rs-s1-aux-zip
rs-s1-l0-slices-zip
rs-s1-l0-acns-zip
rs-s1-l0-segments-zip
rs-s1-l0-blanks-zip
rs-s2-aux
rs-s2-l0-gr
rs-s2-l0-ds
rs-s2-hktm
rs-s2-aux-zip
rs-s2-l0-gr-zip
rs-s2-l0-ds-zip
rs-s2-hktm-zip
rs-s3-granules
rs-s3-aux
rs-s3-l0
rs-s3-cal
rs-s3-pug
rs-s3-granules-zip
rs-s3-aux-zip
rs-s3-l0-zip
rs-s3-cal-zip
rs-s3-pug-zip
```

## Credentials

The credentials for the OBS shall be provided by using a kubernetes secret and configuration of the SCDF application. 
By default all applications connecting to the OBS are configured to use a kubernetes secret `obs` in the namespace `processing` built as follows:

```
USER_ID: <username>
USER_SECRET: <password>
```

This secret will be included as the environment variables `OBS_USERNAME` and `OBS_PASSWORD` by the following SCDF property:

```
deployer.<POD-NAME>.kubernetes.secretKeyRefs=[{ envVarName: 'OBS_USERNAME', secretName: 'obs', dataKey: 'USER_ID'},{ envVarName: 'OBS_PASSWORD', secretName: 'obs', dataKey: 'USER_SECRET'}]
```

# Configuration

| Property | Details |
|-|-|
|`app.*.obs.user-id`| Username used to connect to the OBS (e.g. `access_key` of `.s3cfg`) |
|`app.*.obs.user-secret`| Password used to connect to the OBS (e.g. `secret_key` of `.s3cfg`) |
|`app.*.obs.endpoint`| Service Endpoint for the OBS (e.g. `host_base` of `.s3cfg`) |
|`app.*.obs.endpoint-region`| Signing Region for the OBS (e.g. `bucket_location` of `.s3cfg`) |
|`app.*.obs.multipart-upload-threshold`| Threshold for when to start using multipart upload, in MB (default: 3072) |
|`app.*.obs.min-upload-part-size`| Minimum size for each part, when using multipart upload, in MB (default: 100) |
|`app.*.obs.max-obs-retries`| How many times any operation on the OBS shall be performed before considered failed (default: 10) |
|`app.*.obs.backoff-throttled-base-delay`| Delay between each retry, in ms (default: 500) |
|`app.*.obs.timeout-shutdown`| Time after an Exception was raised to stop waiting for graceful termination, in s (default: 10) |
|`app.*.obs.timeout-down-exec`| On batch download: Timeout for each object, in s (default: 15) |
|`app.*.obs.timeout-up-exec`| On batch upload: Timeout for each object, in s (default: 20) |
|`app.*.obs.disable-chunked-encoding`| Boolean flag to disable chunked encoding. By default chunked encoding is enabled for all PutObjectRequests and UploadPartRequests. When setting this property to true, all requests have chunked encoding disabled. (default: false) |
|`app.*.obs.max-input-stream-buffer-size-mb`| When chunked encoding is disabled, maximum size for object for input stream buffer as UploadObject has to be buffered, in MB (default: 1024) |
|`app.*.obs.upload-cache-location`| Absolute file path to Uplaod Cache (default: /tmp) |
|`app.*.obs.bucket`| Map containing mappings for buckets. Keys are of Enum `ProductFamily`, values of type String |
