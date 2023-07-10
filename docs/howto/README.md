### Copernicus Reference System
# Common - How to create an RS Add-on
### Reference System version V2.0
---
# Overview

These sections are giving an overview about how to create custom RS Add-ons for the COPRS environment. Basically there are two activities that need to be performed. A docker image needs to be created that is based on an image containing the CFI. This image is expected to contain a self-contained environment that allows the execution of the worker. So it needs to include all the binaries, libraries and configuration that are required in order to execute the processor. On this image another layer needs to be deployed that contains the COPRS Execution Worker that invokes the processor from the workflows and monitor its outcome.

Please note: If you only want to alter existing RS-addons an automatic build pipeline is in place to update the RS-Addons on changes. This is true for the `production-common` repository, as well as for the `processing-sentinel-1` and `processing-sentinel-3` repositories. To rebuild IPF containers located [here](https://github.com/COPRS/production-common/tree/develop/rs-container) append the suffix `#IPF` on your commit message so that the IPF container will be rebuild. This limitation is in place, to prevent overly excessive rebuilds of this costly operation.

The following sections are giving an overview about the procedure to create the docker image as well as the rs add-on itself that includes the configuration of the workflows and the microservices.

# Create the RS Add-on image

## Import the CFI image

The first step is to ensure that the image containing the CFI is available for your local docker instance. This can be eiher archieved by referencing a docker registry (like COPRS artifactory). For the COPRS artifactory you need to get an account in order to login before being able to access the registry. If you want to build the images locally, you can import it into your local docker engine without using a remote registry.

In this case you can simply download the docker images from the [COPRS function 1 environment](https://sentinelprocessors.copernicus.eu/) and put it into a local directory. You can then use docker to import the tarball into your local engine by using the following commands. In this example we are using the S3 SR1 image:

```
docker load < S3_SR1_3.12_2022-09-29_S3IPF_docker_centos-6_SR1_07.03.tar.gz
ceb34b82c2ca: Loading layer [==================================================>]  2.929MB/2.929MB
b375cc3b6270: Loading layer [==================================================>]  227.3MB/227.3MB
Loaded image: s3-ipf/sr1:07.03-centos-6
```

The image is now loaded locally and can be referenced by your docker build command. Keep in mind that you can use the same way also to create custom CFI images if you want to create locally a new image from the scratch or based on a different version. When creating a new package simple create a new docker file from the scratch and start with a base image of your choice and adding own binaries to the file. The creation of such an image is unrelated with COPRS and depends on the binaries you want to add. For more information on how to create a new docker file, please consult the [Docker Documentation](https://docs.docker.com/engine/reference/builder/). If in doubt, please consult the User Manual or Software Release notes of the CFI. 

You might want to use `docker images` to verify that the image was successfully added. That command shall list the images you loaded or build in this step.

## Create a docker file using the template

The next step is to use the base image from the previous section and add the generic COPRS Execution Worker to the image. This is a crucial step as this microservice is used as the interface to SCDF. It contains business logic to download all inputs from OBS, invoke the processing, monitor its operation, collect the produced outputs, upload them into the OBS and create a new message for other microservices to be consumed.

Keep in mind that the Execution Worker does have a few own dependencies. As a Java application it will require a JVM 11 installation within the base image. It is assumed that this is already contained in the CFI image. If this is not the case, you need to install it either manually or use the operating system repositories.

You can find a template for a typical creation of the images [here](https://github.com/COPRS/production-common/tree/develop/rs-container/docker_template/Dockerfile). The idea behind it is simply that the CFI image is used and the Execution Worker copied into the new image. The example provided in the template is best practise and included the creation of working directories and the usage of the tini init wrapper that will ensure that no zombie processes are accidently left behind and causing a higher hardware consumption over time.

If you need more examples on how to build these containers, you can find [here](https://github.com/COPRS/production-common/tree/develop/rs-container/) more examples of IPFs that are supported by the COPRS environment and how they were build. Keep in mind that if you use a local docker image to build everything, you might have to adjust the FROM line to reference the image from your local registry instead of the remote one.

## Build

In order to build the image locally, simply go into the folder of your Docker file and invoke the docker build command on it. For example if you checked out [Production Common](https://github.com/COPRS/production-common) and want to build the SR1, you can do as follow:

```
cd production-common/rs-container/docker_s3_ipf_sr1/
docker build . -t rs-ipf-sr1:myversion
```

The image will be build and added to your local docker engine under the tag `rs-ipf-sr1:myversion` and can be pushed from there into another docker registry in order to share it with other users.

# Create the RS Add-on

The RS add-on is basically a ZIP archive that contains a set of configurations for the workflows and services used within the add-ons. In order to create a new RS add-on, you need to ensure that some configuration elements are defined before building the actual rs add-on. Example configurations for Sentinel-1 can be found [here](https://github.com/COPRS/processing-sentinel-1) or for Sentinel-3 [here](https://github.com/COPRS/processing-sentinel-3). These repositories are using the RS add-ons that are supported by the COPRS environment as well as a build script for building all elements at once locally. If using the official build pipe a commit to the repository will be sufficient and automatically generate the RS add-on and pushing it into the official artifactory registry.

The following sections are giving a brief overview about how to create a new add-on from the scratch. The procedure is however also applicable if you just want to customize parts of it. If you need more information about the structure of the RS add-on, please consult the ICD Reference-System add-on (COPRS-ICD-ADST-001133963) in version 5.0 or later.

## Build

When checking out the repository for [Processing-Sentinel-1](https://github.com/COPRS/processing-sentinel-1) and [Processing-Sentinel-3](https://github.com/COPRS/processing-sentinel-3) at the top level there is a build script that will automatically rebuild all the RS add-ons locally. This can be also reutilized when creating new RS add-ons from the scratch by adding a new folder within the structure with your customized configuration. Please note, that this script will include all folders with the prefix `s1` or `s3` (respective for the specific repository). In order to invoke the build operation simply execute:

```
./build_all.sh /tmp/rsaddons 0.0.1
```
The first parameter is the path to the location where the build RS add-ons will be put. The second parameter is the version for that delivery that will be used within the metadata of the RS add-on. When being finished successfully you can find the finished and ready to use RS add-ons in `/tmp/rsaddons`.

## Manifest

Each RS add-on does contain a so called Manifest that contains some descriptive information about the package itself. Mainly it contains the name and version of the rs-addon as well as the processing levels it is used. It does however also contain a list of files that are included. The generic structure of that file looks like this:

```
{
	"ProcessorID": "template",
	"Version": "<VERSION>",
	"ReleaseDate": "<RELEASE_DATE>",
	"Mission": "template",
	"Level": [
		"template"
	],
	"Description": "The RS Addon contains a template Processing Chain",
	"ReleaseItems": [
		<RELEASE_ITEMS>
	]
}
```

Fill in the fields in order to describe your new processor like Ids, the mission and release dates. Under released items a list of all files are a full example of a manifest for S1 AIOP RS add-ons looks like this:

```
{
	"ProcessorID": "s1_aiop",
	"Version": "1.10.0-rc1",
	"ReleaseDate": "2023-01-27",
	"Mission": "S1",
	"Level": [
		"L0"
	],
	"Description": "The RS Addon contains the S1 All-In-One-Processor",
	"ReleaseItems": [
		"RS_ADDON_S1-L0AIOP_1.10.0-rc1_Executables/additional_resources/tasktable_configmap.yaml","RS_ADDON_S1-L0AIOP_1.10.0-rc1_Executables/stream-application-list.properties","RS_ADDON_S1-L0AIOP_1.10.0-rc1_Executables/stream-definition.properties","RS_ADDON_S1-L0AIOP_1.10.0-rc1_Executables/stream-parameters.properties","Manifest.json","RS_ADDON_S1-L0AIOP_1.10.0-rc1_Release_Note.pdf"
	]
}
```

and is in the file `RS_ADDON_S1-L0AIOP_1.10.0-rc1_Manifest.json`. For the build script, the file `Manifest.json` has to provide a template to be filled by the script in order to automatically create this file.

## Content

The folder `content` does contain the configuration for the RS add-on and defines the payload of the RS add-on. The following files are mandatory:
* stream-application-list.properties
* stream-definition.properties
* stream-parameters.properties

The `stream-application-list.properties` file is defining the SCDF applications and mapping them onto docker images. These images are expected to contain the actually IPF as well as an instance of the execution worker. They are basically the images that had been described above. A typical application file looks like this:

```
processor.myaddon-filter=docker:springcloudstream/filter-processor-kafka:3.1.1
processor.myaddon-preparation=docker:artifactory.coprs.esa-copernicus.eu/werum-docker/rs-core-preparation-worker:0.0.1
processor.myaddon-execution=docker:artifactory.coprs.esa-copernicus.eu/werum-docker/rs-ipf-myaddon:0.0.1
processor.myaddon-housekeep=docker:artifactory.coprs.esa-copernicus.eu/rs-docker/rs-core-preparation-worker:0.0.1
source.myaddon-time=docker:springcloudstream/time-source-kafka:3.2.1
```

Please use a appropriate name for the applications to ensure that their name is unqiue and not interfere with other add-ons. Thus it is a good idea to encode the mission and level into that naming as well.

The `stream-definition.properties` is defining the actual SCDF stream that shall be deployed and defines the order of the application that are called. A typical file looks like this:

```
:catalog-event > message-filter: .myaddon-filter | preparation-worker: .myaddon-preparation | execution-worker: .myaddon-execution > :catalog-job
time: .myaddon-time | housekeep: .myaddon-housekeep > :.myaddon-part1.preparation-worker
```

These two workflows are usually required by all S1 and S3 IPFs. The first line defines the stream for the definition of a systematic workflow that is listening on the topic "catalog-event" that is provided by the COPRS environment and creating an event once a product is added to the Metadata Catalog. The filter is a gate to ensure that just suitable products for this rs add-on is passed on. The preparation worker is adding a job and ensures that all requirements for a processing operational are fullfilled. If this is the case it is generating a job order that is passed to the execution worker that will invoke the actual processing an extracting the outputs. These are then written into the catalog jobs that are consumed by the Metadata Catalog.

The second line is triggered by a timer to invoke the house keeper that will check if there are jobs that run into a timeout and a job might be generated and started even tho it does not fullfill all the requested prerequisites. The house keeping service is basically a preparation worker as well and following the same configuration.

The `stream-parameters.properties` is a property file that contains the configuration for all the SCDF applications. You can find an overview regarding the configuration on this [page](https://github.com/COPRS/processing-sentinel-3/tree/develop/docs/common).

## Additional resources

At the top level there is another optional folder called `additional_resources` that can contain a set of K8s yaml files that might contain additional kubernetes resources that shall be deployed when the RS add-on is deployed using the ansible playbook. This directory might contain one or multiple yaml files as long as they are valid K8s objects.

Especially for S1 and S3 they contain a configmap that includes all the tasktables that are required by the Preparation Worker in order to prepare the processing operation before hands. These tasktables are not in the CFI images and need to be mounted into the Preparation worker service.  Please consult the Software User Manual of the IPF in order to identify the tasktables that needs to be added into such a configmap.

Take note that the configmap needs a unique name that allows to identify which RS add-on the configmap related to. The actual tasktables are added as data text seperated by the file name followed by the actual XML content representing the tasktable. So the structure might look like this:

```
apiVersion: v1
kind: ConfigMap
metadata:
  name: template-tasktables
  namespace: processing

data:
  tasktable.xml: |
  <?xml version='1.0' encoding='UTF-8'?>
    <Ipf_Task_Table xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      <Processor_Name>Template</Processor_Name>
[...]
```

Note that by this way it is possible to add multiple tasktables into that config map. In order to determinate what tasktables are provided by the CFI, please consult the User Manual of your IPF installation kit.

Keep in mind that by adding the tasktable to the additional_resources it will be added to your cluster, you still tho need to mount it into your service to become available. This can be archieved by adding the following configuration into the file "stream-parameters.properties":

```
deployer.preparation-worker.kubernetes.volumes=[{name: tasktablexslt, configMap: {{name: tasktables, configMap: {name: template-tasktables, items: [{key: 'tasktable.xml', path: 'tasktable.xml'}]}}]
```

## Documentation

Each RS add-on shall contain also a Release Document that is giving some overview about what configuration properties are available and what are the requirements for the RS add-on when being deployed wthin a COPRS environment. This information is supposed to be pleased under the folder "doc" as File "ReleaseNote.md". 
