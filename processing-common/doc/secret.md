# RS-Core - Secret configuration

Especially when tackle with sensible information as user credentials it is not recommended to use these information directly in the property file "stream-parameters.properties". Instead it is recommended to create a secret within Kubernetes and make it available in the property file using variables.

What exactly needs to be setup is different for the setups. This documentation is trying to give an example on how to make a secret available.

## AUXIP

Initially a new secret needs to be generated. This can be done using the tool kubectl from your cluster by using the following command line:

``kubectl create secret generic auxip --from-literal=USERNAME=<USER_ACCOUNT> --from-literal=PASSWORD=<USER_PASSWORD> --from-literal=CLIENT_ID=<CLIENT_ID --from-literal=CLIENT_SECRET=<CLIENT_SECRET>``

In this case a new secret with the name "auxip" will be generated that contains the fields "USERNAME", "PASSWORD", "CLIENT_ID" and "CLIENT_SECRET". This is a typical setup as it would be used in AUXIP as not just the login credentials will be required, but also addtional client information for OAuth2.

Please verify that the secret had been created successfully by using:
``kubectl describe secret auxip``

You should see something like:
```
Name:         auxip
Namespace:    processing
Labels:       <none>
Annotations:  <none>

Type:  Opaque

Data
====
PASSWORD:       20 bytes
USERNAME:       18 bytes
CLIENT_ID:      13 bytes
CLIENT_SECRET:  36 bytes
```

After the secret was created in the cluster, you can map the secret into environment variables in your "stream-parameters.properties" by using the following deployer configuration:

``deployer.ingestion-auxip-trigger.kubernetes.secretKeyRefs=[{ envVarName: 'AUXIP_USERNAME', secretName: 'auxip', dataKey: 'USERNAME' },{ envVarName: 'AUXIP_PASSWORD', secretName: 'auxip', dataKey: 'PASSWORD' },{envVarName: 'AUXIP_CLIENT_ID', secretName: 'auxip', dataKey: 'CLIENT_ID' },{envVarName: 'AUXIP_CLIENT_SECRET', secretName: 'auxip', dataKey: 'CLIENT_SECRET' }]``

In this case 4 new environmental variables are created. All will be taken from the secret "auxip" that had been created before. The datakey is the name of the datafield within the datafield that will be mapped to the envVarName. Now these environmental variables can be used within the same property file as value, e.g.
```
app.ingestion-auxip-trigger.auxip.host-configs.host1.user=${AUXIP_USERNAME}
app.ingestion-auxip-trigger.auxip.host-configs.host1.pass=${AUXIP_PASSWORD}
```
## MongoDB

Some application of the COPRS like the ingestion or preparation worker are requiring MongoDB as persistence layer to store runtime information. The MongoDB instance is part of the infrastructure. Further information please see [https://github.com/COPRS/infrastructure].

By default it is assumed that the RS Core Components Ingestion and the RS Add-ons (with the Preparation worker) are not sharing the same secrets and for each one a own secret is generated. Even tho the credentials used to login to the MongoDB can be the same. The procedure in order to generate the sescrets is however indentically.

In order to generate a secrets for the components, you can use the following commands:
``kubectl create secret generic mongodlq --from-literal=USERNAME=<MONGO_USER> --from-literal=PASSWORD=<MONGO_PASSWORD>``
``kubectl create secret generic mongoingestion --from-literal=USERNAME=<MONGO_USER> --from-literal=PASSWORD=<MONGO_PASSWORD>``
``kubectl create secret generic mongopreparation --from-literal=USERNAME=<MONGO_USER> --from-literal=PASSWORD=<MONGO_PASSWORD>``

Be aware that it might be required to generate the user and set its password with MongoDB after the database had been generated. Please consult the documentation for the components for further information.