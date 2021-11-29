# RS Deploy
This script can be used to deploy a set of microservices within the Reference System. The different services are grouped by rs addons that can be deployed individually. Please note that there are certain dependency between the different addons as e.g. the S1 processing chain will not work correctly if the common services are not deployed to the cluster.

Please be aware that this script is a wrapper for the existing software solution of the S1PRO and is supposed to be used for V1. All services are deployed during the setup as helm charts. However there is logic involved to setup the values for the helm charts during the deployment process from the environment configuration.

# Prerequisites
The script is supposed to be used in a Reference System environment that is setup correctly already. Do not use this script before the following prerequisites are fullfilled:
- Kubernetes Cluster is setup
- environmental specific configuration had been created
- rs_init was used to generate an Reference System environment

If any of these items are not tackled, the script will likely not work as expected.

# Usage
When correctly setup the script is part of the command search path and can be directly used. It consist of two parameters that needs to be provided:
```
rs_deploy <CMD> <ADDON>
```

The command specifies what action the script shall apply on the RS addon. The following commands are available:
- install
- uninstall
- restart

The usage of the differetn commands are described more verbosely in the following sections.

The addon specifies the set of microservices that shall be handled by the execution. The following official addons are currently available:
- common (common components used by the Reference System)
- s1 (Sentinel-1 workflow specific services)
- s3 (Sentinel-3 workflow specific services)

Be aware that the addons might have certain dependency between each other. E.g. the workflow specific addons are requiring the common addon to be deployed to the server in order to handle requests correctly. However it is possible to undeploy the common addon while the s1 is still running within the cluster. This can be used to update certain services without the need of uninstall all of them.


## install
In order to deploy a RS addon into the cluster the following command  can be used:
```
rs_deploy install <ADDON>
```
For example to deploy the services of the S3 addon this can be archieved by the following command:
```
rs_deploy install s3
```
The output of an successful installation of the S3 addon will look like this:
```
Installing addon s3
Deploying service s3
**** WARNING: INVALID CLUSTER NUMBER: master ****
Using processing-sentinel-3 as chart directory

#################################
#  HELM PROCESSING INIT / UPGRADE ...
#################################
Installing s1pro-s3-acq-production-trigger...
Installing s1pro-s3-acq-ipf-preparation-worker...
Installing s1pro-s3-acq-ipf-execution-worker...
Installing s1pro-s3-l0-production-trigger...
Installing s1pro-s3-l0-ipf-preparation-worker...
Installing s1pro-s3-l0-execution-worker...
Installing s1pro-s3-pug-production-trigger...
Installing s1pro-s3-pug-preparation-worker...
Installing s1pro-s3-pug-execution-worker...
Wait 5 seconds ... before checking

#################################
#  HELM PROCESSING CHECK ...
#################################
Using werum (at https://registry.tools.s1pdgs.eu/chartrepo/werum) to check services ...
Checking ... 41 : YES : s1pro-s3-acq-production-trigger : rs-0.0.1 : /root/env/helm/processing-sentinel-3/41_s3_acq_production_trigger/values.yaml
Checking ... 42 : YES : s1pro-s3-acq-ipf-preparation-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/42_s3_acq_ipf_preparation_worker/values.yaml
Checking ... 43 : YES : s1pro-s3-acq-ipf-execution-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/43_s3_acq_ipf_execution_worker/values.yaml
Checking ... 44 : YES : s1pro-s3-l0-production-trigger : rs-0.0.1 : /root/env/helm/processing-sentinel-3/44_s3_l0_production_trigger/values.yaml
Checking ... 45 : YES : s1pro-s3-l0-ipf-preparation-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/45_s3_l0_ipf_preparation_worker/values.yaml
Checking ... 46 : YES : s1pro-s3-l0-execution-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/46_s3_l0_execution_worker/values.yaml
Checking ... 47 : YES : s1pro-s3-pug-production-trigger : rs-0.0.1 : /root/env/helm/processing-sentinel-3/47_s3_pug_production_trigger/values.yaml
Checking ... 48 : YES : s1pro-s3-pug-preparation-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/48_s3_pug_preparation_worker/values.yaml
Checking ... 49 : YES : s1pro-s3-pug-execution-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/49_s3_pug_execution_worker/values.yaml
s1pro-s3-acq-production-trigger        rs-0.0.1     KO     ::
s1pro-s3-acq-ipf-preparation-worker    rs-0.0.1     KO     ::
s1pro-s3-acq-ipf-execution-worker      rs-0.0.1     KO     ::
s1pro-s3-l0-production-trigger         rs-0.0.1     KO     ::
s1pro-s3-l0-ipf-preparation-worker     rs-0.0.1     KO     ::
s1pro-s3-l0-execution-worker           rs-0.0.1     KO     ::
s1pro-s3-pug-production-trigger        rs-0.0.1     KO     ::
s1pro-s3-pug-preparation-worker        rs-0.0.1     KO     ::
s1pro-s3-pug-execution-worker          rs-0.0.1     KO     ::
done.
```
After the execution you shall be able to find the deployed services within the namespace "processing" of the Kubernetes cluster. Be aware that this just means the services are deployed and might need more time to startup correctly.

## uninstall
In order to undeploy a RS addon from the cluster the following command can be used:
```
rs_deploy uninstall <ADDON>
```
For example to remove the services of the S3 addon this can be archieved by the following command:
```
rs_deploy uninstall s3
```
The successful undeploy action of S3 addon will be looking like this:
```
Uninstalling addon s3
Undeploying service s3
**** WARNING: INVALID CLUSTER NUMBER: master ****
Using processing-sentinel-3 as chart directory

#################################                                               
#  HELM PROCESSING CLEANING ...                                                 
#################################                                               
Using werum (at https://registry.tools.s1pdgs.eu/chartrepo/werum) to undeploy services ...
Undeploying ... 49 : YES : s1pro-s3-pug-execution-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/49_s3_pug_execution_worker/values.yaml
try to delete s1pro-s3-pug-execution-worker
release "s1pro-s3-pug-execution-worker" uninstalled
Undeploying ... 48 : YES : s1pro-s3-pug-preparation-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/48_s3_pug_preparation_worker/values.yaml
try to delete s1pro-s3-pug-preparation-worker
release "s1pro-s3-pug-preparation-worker" uninstalled
Undeploying ... 47 : YES : s1pro-s3-pug-production-trigger : rs-0.0.1 : /root/env/helm/processing-sentinel-3/47_s3_pug_production_trigger/values.yaml
try to delete s1pro-s3-pug-production-trigger
release "s1pro-s3-pug-production-trigger" uninstalled
Undeploying ... 46 : YES : s1pro-s3-l0-execution-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/46_s3_l0_execution_worker/values.yaml
try to delete s1pro-s3-l0-execution-worker
release "s1pro-s3-l0-execution-worker" uninstalled
Undeploying ... 45 : YES : s1pro-s3-l0-ipf-preparation-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/45_s3_l0_ipf_preparation_worker/values.yaml
try to delete s1pro-s3-l0-ipf-preparation-worker
release "s1pro-s3-l0-ipf-preparation-worker" uninstalled
Undeploying ... 44 : YES : s1pro-s3-l0-production-trigger : rs-0.0.1 : /root/env/helm/processing-sentinel-3/44_s3_l0_production_trigger/values.yaml
try to delete s1pro-s3-l0-production-trigger
release "s1pro-s3-l0-production-trigger" uninstalled
Undeploying ... 43 : YES : s1pro-s3-acq-ipf-execution-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/43_s3_acq_ipf_execution_worker/values.yaml
try to delete s1pro-s3-acq-ipf-execution-worker
release "s1pro-s3-acq-ipf-execution-worker" uninstalled
Undeploying ... 42 : YES : s1pro-s3-acq-ipf-preparation-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/42_s3_acq_ipf_preparation_worker/values.yaml
try to delete s1pro-s3-acq-ipf-preparation-worker
release "s1pro-s3-acq-ipf-preparation-worker" uninstalled
Undeploying ... 41 : YES : s1pro-s3-acq-production-trigger : rs-0.0.1 : /root/env/helm/processing-sentinel-3/41_s3_acq_production_trigger/values.yaml
try to delete s1pro-s3-acq-production-trigger
release "s1pro-s3-acq-production-trigger" uninstalled
Wait 5 seconds ... before checking

#################################                                               
#  HELM PROCESSING CHECK ...                                                   
#################################                                               
Using werum (at https://registry.tools.s1pdgs.eu/chartrepo/werum) to check services ...
Checking ... 41 : YES : s1pro-s3-acq-production-trigger : rs-0.0.1 : /root/env/helm/processing-sentinel-3/41_s3_acq_production_trigger/values.yaml
Checking ... 42 : YES : s1pro-s3-acq-ipf-preparation-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/42_s3_acq_ipf_preparation_worker/values.yaml
Checking ... 43 : YES : s1pro-s3-acq-ipf-execution-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/43_s3_acq_ipf_execution_worker/values.yaml
Checking ... 44 : YES : s1pro-s3-l0-production-trigger : rs-0.0.1 : /root/env/helm/processing-sentinel-3/44_s3_l0_production_trigger/values.yaml
Checking ... 45 : YES : s1pro-s3-l0-ipf-preparation-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/45_s3_l0_ipf_preparation_worker/values.yaml
Checking ... 46 : YES : s1pro-s3-l0-execution-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/46_s3_l0_execution_worker/values.yaml
Checking ... 47 : YES : s1pro-s3-pug-production-trigger : rs-0.0.1 : /root/env/helm/processing-sentinel-3/47_s3_pug_production_trigger/values.yaml
Checking ... 48 : YES : s1pro-s3-pug-preparation-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/48_s3_pug_preparation_worker/values.yaml
Checking ... 49 : YES : s1pro-s3-pug-execution-worker : rs-0.0.1 : /root/env/helm/processing-sentinel-3/49_s3_pug_execution_worker/values.yaml
s1pro-s3-acq-production-trigger        rs-0.0.1     KO     ::
s1pro-s3-acq-ipf-preparation-worker    rs-0.0.1     KO     ::
s1pro-s3-acq-ipf-execution-worker      rs-0.0.1     KO     ::
s1pro-s3-l0-production-trigger         rs-0.0.1     KO     ::
s1pro-s3-l0-ipf-preparation-worker     rs-0.0.1     KO     ::
s1pro-s3-l0-execution-worker           rs-0.0.1     KO     ::
s1pro-s3-pug-production-trigger        rs-0.0.1     KO     ::
s1pro-s3-pug-preparation-worker        rs-0.0.1     KO     ::
s1pro-s3-pug-execution-worker          rs-0.0.1     KO     ::
done.
```
Please note that it might take a few seconds until the pods of the addon are actually removed from the cluster.

Be aware that the script will just remove the helm charts from the cluster and this causing kubernetes to shutdown running systems. It will not automatically remove artifacts within queues or databases that had been created during runtime. It just ensures that the services are removed from Kubernetes.

## restart
Is basically just a comfort function to unit the commands install and uninstall. So when restarting a addon it will be first uninstalled from the cluster and then immediatly redeployed again. This can be used to force and update of the service configuration as the existing configuration is removed and the new one deployed.

# Possible pitfalls between different versions
Keep in mind that the rs_deploy script will just tackle microservices that are existing in the current version of the configuration. Thus it is highly recommended to undeploy the services before moving to a new version and redeploy it afterwards. If you uninstall after the upgrade it might be that a service that had been removed is still running in the Kubernetes cluster and will be ignored by rs_deploy.