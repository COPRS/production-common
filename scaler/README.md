S1-PDGS Cloud POC - Scaler
==========================

This module is in charge of scaling resources according to the predicted time for processing the last job with the current active wrappers and remaining Kafka messages.
If this time is superior or inferior of predefined thresholds, the system increases or decreases the number processor L1. 
For an ascendant scaling: 
* the scaler asks to Open Stack to create a new virtual machine
* the scaler asks to Kubernetes to create a new pod with a process L1
* the new process L1 gets job in the queue and begins its treatment
For a descendant scaling, 
* the scaler choses a pod "ready to be stopped"
* it asks to its pod to finish its task, and not take a new one
* when the task is finished the scaler asks to Kubernetes to stop the pod
* The scaler asks to OpenStack to delete the Virtual Machine.


<div style="text-align:center"><img alt="Design" src="build/design_scaler.jpg" align="center"/></div>

The scaler periodically executes the following steps:
1.	Delete the wrappers in succeeded state (i.e. terminated with success)
2.	Monitor Kafka information: l1-jobs topic and l1-wrappers groups
3.	Monitor Kubernetes and wrappers information: l1 wrappers and workers dedicated to them 
4.	Calculate the comparison value: the predicated time for processing the last job with the current active wrappers and remaining Kafka messages
5.	Determinate and apply the scaling action (allocate resources, freeing resources, nothing)
6.	Removed unused resources: Open Stack VMs


### Sources

The module has 4 packages:
* k8s: for monitoring and managing the pods
* kafka: for monitoring the topics
* openstack: for managing virtual machines
* scaling: for the scaling functionality


### Builds

This project is a maven, java and spring project.

##### IDE

You can use STS (Spring Tools Suite) or Eclipse.
Required java version is >= 1.8

##### Internal dependencies

This project depends on:
* [commons](https://conf.geohub.space/wo7/lib-commons) library
* [app-catalog-client](https://conf.geohub.space/wo7/obs-sdk) library

Please install these dependencies in your local repository before building project

##### External dependency
This project depends on:
* spring-boot
* spring-log4j2
* kafka_2.11 and kafka-clients
* spring-kafka
* kubernetes-client
* openstack4j
	
### Configuration

##### application.yml
Below the parameters to configure for the production

Parameter                                        | Description
------------------------------------------------ | ------------- 
TODO