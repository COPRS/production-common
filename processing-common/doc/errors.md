# RS-Core - Error Management 

One important common topic in the Reference System is the handling of errors within the system. This feature is common within all RS core chains and RS addon chains. 

## Message retry with Spring Cloud Dataflow (SCDF)

When an error occurs within one application, SCDF handles this error by retrying the message on the same application for a configurable number of times. This behaviour should prevent that small issues regarding the unavailability of required other applications or any network issues will immediately result in a failed message. The following parameters are commonly used to configure the retry mechanism by SCDF:

```
app.*.spring.cloud.stream.bindings.input.consumer.maxAttempts=3
app.*.spring.cloud.stream.kafka.bindings.input.consumer.enableDlq=true
app.*.spring.cloud.stream.kafka.bindings.input.consumer.dlqName=error-warning
```

From top to bottom the properties have the following effects: ``maxAttempts`` defines how many times a message shall be processed before flagged as a "failed" message. The retry is handled within the SCDF layer and will not reingest the message onto the incoming Kafka topic, meaning no message duplication will occur. 
``enableDlq`` will enable the Dead-Letter-Queue mechanism from SCDF, meaning, that after the retries are exhausted, the failed message will be sent to one "Dead-Letter-Queue" (DLQ). By default the topic would be determined by the name of the input topic and therefore each topic would have its own DLQ. To harmonize this and make the further processing easier the parameter ``dlqName`` can be used to configure the name of the DLQ-topic. In this case all failed messages within the system will be sent to the topic ``error-warning``. 

When forwarding a message to the DLQ the Kafka message headers are enriched with additional information regarding the original topic and the number of retries. This information is then available for all future applications by accessing the headers. 

## Dead-Letter-Queue mechanism (DLQ)

In the Reference System once a message landed in the DLQ an application called "DLQ manager" is handling the further processing of the different messages. It is using a configurable ruleset to determine whether a message should be reprocessed, retried or sent to the "parking lot" for further manual handling by an operator.

The configuration for the DLQ manager can be found [here](../dlq/doc/SRN.md).