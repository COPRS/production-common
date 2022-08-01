# RS-Core - Handling of long-running applications

By default Kafka will only allow the processing of one specific message to take at most 5 minutes. As some of the applications in the Reference System are taking more time by design and technical restrictions, it is necessary to be able to configure those applications to be allowed to process as long as they need at most. This configuration has to be divided into two seperate parts: Firstly Kafka shall be configured to allow longer processing in those specific cases and secondly after a timeout occured and the application is excluded from the consumer group by the Kafka broker, the application shall not be processing further in order to prevent duplication of messages.

## Configuration of Kafka consumer

```
app.*.spring.cloud.stream.kafka.bindings.input.consumer.configuration.max.poll.records=1
app.*.spring.cloud.stream.kafka.bindings.input.consumer.configuration.max.poll.interval.ms=3600000
```

To prevent Kafka of excluding a long processing application from the consumer group early, two properties are important: 
``max.poll.interval.ms`` overwrites the default maximum length of 5 minutes. In this example the maximum time a application is allowed processing one message is 1 hour. The second property ``max.poll.records`` has to be set to 1 for applications that have the possibility of inducing a timeout in order to prevent them of continuing processign the next message even after the application was excluded from the consumer group. 

## Configuration of the application

```
app.*.process.tm-proc-stop-s=300
app.*.process.tm-proc-one-task-s=600
app.*.process.tm-proc-all-tasks-s=3600
app.*.process.tm-proc-check-stop-s=60
```

In order to prevent the application of publishing the result of the processing after a timeout occured, an additional configuration of the application is advised. In the above example an execution-worker was configured to identify a processing as failed after 1 hour (``tm-proc-all-tasks-s``). This configuration makes sure, that the processing of the message will be terminated and a failed processing is signaled. As this configuration is handled by the specific applications please refer to the different application documentations for more information on how affected applications handle this timeout scenario.