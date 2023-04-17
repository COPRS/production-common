# RS-Core - Load balance the lag

In the COPRS the scaling of different services in order to maintain a high performance of the system while simultaniously keeping the resource costs low, is an important feature. To further optimise this behaviour of the system the applications implemented under `rs-core` contain an optional custom implementation of a Kafka Partitioner. By default this implementation is not used, but if necessity arises, it is easy to add it to any of the following applications:

 - `compression-worker`
 - `data-lifecycle-worker`
 - `distribution-worker`
 - `execution-worker`
 - `ingestion-filter`
 - `ingestion-trigger`
 - `ingestion-worker`
 - `metadata-catalog-extraction`
 - `preparation-worker`

To read up more information on how the custom LagBasedPartitioner is working, please refer to [Trade Off "Load Balance the Lag"](https://github.com/COPRS/production-common/wiki/Trade-off-%22Load-Balance-the-Lag%22).

## Configuration of an SCDF application

For any of the above mentioned applications to use the custom implementation of the partitioner the following properties have to be added:

```
app.<application-name>.spring.cloud.stream.kafka.bindings.input.producer.configuration.partitioner.class=esa.s1pdgs.cpoc.message.kafka.LagBasedPartitioner
app.<application-name>.spring.cloud.stream.kafka.binder.producerProperties.lag-based-partitioner.delay-seconds=<integer-value>
app.<application-name>.spring.cloud.stream.kafka.binder.producerProperties.lag-based-partitioner.consumer-group=<string-value>
app.<application-name>.spring.cloud.stream.kafka.binder.producerProperties.lag-based-partitioner.topics-with-priority.<topic-name>=<integer-value>
```

The property `delay-seconds` defines in which interval the PartitionLagFetcher analyses the lag of the configured topics.

The property `consumer-group` defines for which consumer-group of the topics the lags shall be analyzed.

The property `topics-with-priority` contains a map of topic names with a given priority. As in the RS all SCDF applications only listen to one topic, it is only necessary to define one line of this property even though multiple lines are allowed.