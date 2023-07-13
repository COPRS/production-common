# RS-Core - Scaling of applications

One important feature of Spring Cloud Dataflow (SCDF) is the ability to independently scale applications inside a stream.
There are two different methods to scale applications with SCDF: Setup properties before first deploying a stream, and dynamically scale applications up and down, while they are running.

## Preconditions

As we are using Kafka as a MessageBroker it is important to understand the concept of topics and partitions before thinking about scaling your applications.
Each topic in Kafka is divided in a number of partitions. Each partition can only be assigned to one consumer at a time, while each consumer can assign itself to multiple partitions.
The maximum number of parallel consumers is therefore limited by the number of partitions the topics the consumer is listening to has.

By default SCDF will automatically create the topics with the number of partitions matching the maximum instance count of any consumer group.
This means, that without additional configuration, an upscaling is possible but will not have any effect, as not enough partitions are available to be provided to the newly created consumers.

It is therefore necessary to create the kafka topics with the maximum number of consumers in one consumer group in mind.

The important SCDF properties on the producer side are:

```
app.<application-name>.spring.cloud.stream.kafka.binder.autoAddPartitions
app.<applicaition-name>.spring.cloud.stream.kafka.binder.minPartitionCount
```

`autoAddPartitions` should be set to `true`, while `minPartitionCount` should be set to the maximum number of consumers in one consumer group listening to the output topic of this application.

When SCDF is responsible for creating the topics it might be necessary to tweak the maximum sizes of messages on the topic. This can be done by adding the properties:

```
app.<producer-application-name>.spring.cloud.stream.kafka.bindings.output.producer.topic.properties.max.message.bytes

app.<consumer-application-name>.spring.cloud.stream.kafka.bindings.input.consumer.configuration.max.partition.fetch.bytes
```

For example the topic between the ingestion-trigger (producer) and the ingestion-filter (consumer) contains big messages if the ingestion point listened to lists a lot of input files at once.

## Start a stream with more than one instance

In order to start a stream by default with more than one instance of a particular application in that stream, the following property should be defined:

```
deployer.<application-name>.count
```

This will define the number of instances for the application on deploying this chain.

## Dynamically scale an application while running

SCDF provided a seperate command in order to dynamically scale an application up or down.
This is necessary as the deployer properties are only considered ondeploying the stream, not when updating a stream.
If one is using the SCDF Shell, the folliwing command will scale an application inside one stream to the desired number of instances:

```
stream app instances --name <stream-name> --applicationName <application-name> --count <instance-count>
```

SCDF also provides a REST-interface to execute this scaling mechanism. This is interesting to use in combination with external monitoring tools (e.g. Prometheus).
For more information on how to use the REST-interface please refer to the SCDF documentation mentioned under **Additional resources**

## Additional resources

- [Manual scaling](https://dataflow.spring.io/docs/recipes/scaling/manual-scaling/)
- [Autoscaling](https://dataflow.spring.io/docs/recipes/scaling/autoscaling/)
