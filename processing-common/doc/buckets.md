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
rs-s1-plans-and-reports
rs-s1-l0-slices
rs-l0-acns
rs-s1-l0-segments
rs-s1-l0-blanks
rs-s1-l1-slices
rs-s1-l1-acns
rs-s1-l2-slices
rs-s1-l2-acns
rs-s1-spp-mbu
rs-s1-spp
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
rs-s1-l1-slices-zip
rs-s1-l1-acns-zip
rs-s1-l2-slices-zip
rs-s1-l2-acns-zip
rs-s1-spp-zip
rs-s1-plans-and-reports-zip
rs-s2-aux
rs-s2-l0-gr
rs-s2-l0-ds
rs-s2-aux-zip
rs-s2-l0-gr-zip
rs-s2-l0-ds-zip
rs-s3-granules
rs-s3-aux
rs-s3-l0
rs-s3-l1-nrt
rs-s3-l1-stc
rs-s3-l1-ntc
rs-s3-l2-nrt 
rs-s3-l2-stc
rs-s3-l2-ntc
rs-s3-cal
rs-s3-pug
rs-s3-granules-zip
rs-s3-aux-zip
rs-s3-l0-zip
rs-s3-l1-nrt-zip
rs-s3-l1-stc-zip
rs-s3-l1-ntc-zip
rs-s3-l2-nrt-zip
rs-s3-l2-stc-zip
rs-s3-l2-ntc-zip
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


