package esa.s1pdgs.cpoc.mqi.server.service;

import static java.lang.String.valueOf;
import static java.lang.Thread.sleep;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.support.Acknowledgment;

import esa.s1pdgs.cpoc.appcatalog.rest.AppCatMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.AppCatSendMessageDto;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.mqi.model.queue.ProductionEvent;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.server.config.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.config.PersistenceConfiguration;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.GenericConsumer;

public class InMemoryMessagePersistenceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private KafkaProperties kafkaProperties;

    @Mock
    private KafkaProperties.KafkaConsumerProperties consumerProperties;

    @Mock
    private GenericConsumer<ProductionEvent> genericConsumer;

    @Mock
    private PersistenceConfiguration.InMemoryMessagePersistenceConfiguration configuration;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);

        when(kafkaProperties.getHostname()).thenReturn("pod1");
        when(kafkaProperties.getConsumer()).thenReturn(consumerProperties);
        when(consumerProperties.getGroupId()).thenReturn("group1");
        when(configuration.getDefaultOffset()).thenReturn(-3);
        when(configuration.getInMemoryPersistenceHighThreshold()).thenReturn(5);
        when(genericConsumer.isPaused()).thenReturn(false);
        when(genericConsumer.getTopic()).thenReturn("topic");
    }

    @Test
    public void readAndNext() {

        InMemoryMessagePersistence<ProductionEvent> messagePersistence = new InMemoryMessagePersistence<>(kafkaProperties, configuration);

        final ProductionEvent event1 = new ProductionEvent("prod1", "prod1", ProductFamily.AUXILIARY_FILE);
        final ConsumerRecord<String, ProductionEvent> record1 = new ConsumerRecord<>("topic", 1, 1, "string", event1);
        Acknowledgment acknowledgement1 = Mockito.mock(Acknowledgment.class);

        final ProductionEvent event2 = new ProductionEvent("prod2", "prod2", ProductFamily.AUXILIARY_FILE);
        final ConsumerRecord<String, ProductionEvent> record2 = new ConsumerRecord<>("topic", 1, 2, "string", event2);
        Acknowledgment acknowledgement2 = Mockito.mock(Acknowledgment.class);

        final ProductionEvent event3 = new ProductionEvent("prod3", "prod3", ProductFamily.AUXILIARY_FILE);
        final ConsumerRecord<String, ProductionEvent> record3 = new ConsumerRecord<>("topic", 1, 3, "string", event3);
        Acknowledgment acknowledgement3 = Mockito.mock(Acknowledgment.class);

        messagePersistence.read(record1, acknowledgement1, genericConsumer, ProductCategory.AUXILIARY_FILES);
        messagePersistence.read(record2, acknowledgement2, genericConsumer, ProductCategory.AUXILIARY_FILES);
        messagePersistence.read(record3, acknowledgement3, genericConsumer, ProductCategory.AUXILIARY_FILES);

        verifyZeroInteractions(acknowledgement1);
        verifyZeroInteractions(acknowledgement2);
        verifyZeroInteractions(acknowledgement3);

        final List<AppCatMessageDto<ProductionEvent>> messagesPod1 = messagePersistence.next(ProductCategory.AUXILIARY_FILES, "pod1");
        assertThat(messagesPod1, is(notNullValue()));
        assertThat(messagesPod1.size(), is(3));
        assertThat(messagesPod1.get(0).getTopic(), is(equalTo("topic")));
        assertThat(messagesPod1.get(0).getReadingPod(), is(equalTo("pod1")));
        assertThat(messagesPod1.get(0).getDto(), is(equalTo(event1)));
        assertThat(messagesPod1.get(1).getTopic(), is(equalTo("topic")));
        assertThat(messagesPod1.get(1).getReadingPod(), is(equalTo("pod1")));
        assertThat(messagesPod1.get(1).getDto(), is(equalTo(event2)));
        assertThat(messagesPod1.get(2).getTopic(), is(equalTo("topic")));
        assertThat(messagesPod1.get(2).getReadingPod(), is(equalTo("pod1")));
        assertThat(messagesPod1.get(2).getDto(), is(equalTo(event3)));
        assertThat(messagesPod1.stream().map(AppCatMessageDto::getId).distinct().count(), is(3L)); //unique ids

        final List<AppCatMessageDto<ProductionEvent>> messagesPod2 = messagePersistence.next(ProductCategory.AUXILIARY_FILES, "pod2");
        assertThat(messagesPod2, is(notNullValue()));
        assertThat(messagesPod2.size(), is(0));

        final List<AppCatMessageDto<ProductionEvent>> messagesAnotherCategory = messagePersistence.next(ProductCategory.LEVEL_PRODUCTS, "pod1");
        assertThat(messagesAnotherCategory, is(notNullValue()));
        assertThat(messagesAnotherCategory.size(), is(0));

    }

    @Test
    public void readShouldPauseConsumptionOnThreshold() {
        InMemoryMessagePersistence<ProductionEvent> messagePersistence = new InMemoryMessagePersistence<>(kafkaProperties, configuration);

        final RecordAndAcknowledgement eventTopic1 = RecordAndAcknowledgement.createNew("topic", 1, 1);
        final RecordAndAcknowledgement eventTopic2= RecordAndAcknowledgement.createNew("topic", 1, 2 );
        final RecordAndAcknowledgement eventTropic1= RecordAndAcknowledgement.createNew("tropic", 1, 3 ); //event with other topic should not increase message count
        final RecordAndAcknowledgement eventTopic3= RecordAndAcknowledgement.createNew("topic", 1, 4);
        final RecordAndAcknowledgement eventTropic2= RecordAndAcknowledgement.createNew("tropic", 1, 5 ); //event with other topic should not increase message count
        final RecordAndAcknowledgement eventTopic4= RecordAndAcknowledgement.createNew("topic", 1, 6);
        final RecordAndAcknowledgement eventTopic5= RecordAndAcknowledgement.createNew("topic", 1, 7);

        messagePersistence.read(eventTopic1.record, eventTopic1.acknowledgment, genericConsumer, ProductCategory.AUXILIARY_FILES);
        messagePersistence.read(eventTopic2.record, eventTopic2.acknowledgment, genericConsumer, ProductCategory.AUXILIARY_FILES);
        messagePersistence.read(eventTropic1.record, eventTropic1.acknowledgment, genericConsumer, ProductCategory.AUXILIARY_FILES);
        messagePersistence.read(eventTopic3.record, eventTopic3.acknowledgment, genericConsumer, ProductCategory.AUXILIARY_FILES);
        messagePersistence.read(eventTropic2.record, eventTropic2.acknowledgment, genericConsumer, ProductCategory.AUXILIARY_FILES);
        messagePersistence.read(eventTopic4.record, eventTopic4.acknowledgment, genericConsumer, ProductCategory.AUXILIARY_FILES);

        verify(genericConsumer, times(0)).pause();

        messagePersistence.read(eventTopic5.record, eventTopic5.acknowledgment, genericConsumer, ProductCategory.AUXILIARY_FILES);

        verify(genericConsumer, times(1)).pause();
    }

    @Test
    public void sendShouldReturnTrue() {

        InMemoryMessagePersistence<ProductionEvent> messagePersistence = new InMemoryMessagePersistence<>(kafkaProperties, configuration);

        final ProductionEvent event1 = new ProductionEvent("prod1", "prod1", ProductFamily.AUXILIARY_FILE);
        final ConsumerRecord<String, ProductionEvent> record1 = new ConsumerRecord<>("topic", 1, 1, "string", event1);
        Acknowledgment acknowledgement1 = Mockito.mock(Acknowledgment.class);


        messagePersistence.read(record1, acknowledgement1, genericConsumer, ProductCategory.AUXILIARY_FILES);

        verifyZeroInteractions(acknowledgement1);

        final List<AppCatMessageDto<ProductionEvent>> messagesPod1 = messagePersistence.next(ProductCategory.AUXILIARY_FILES, "pod1");
        final AppCatMessageDto<ProductionEvent> message = messagesPod1.get(0);

        boolean sendResult = messagePersistence.send(ProductCategory.AUXILIARY_FILES,
                message.getId(),
                new AppCatSendMessageDto(kafkaProperties.getHostname(), false));

        assertThat(sendResult, is(true));
    }

    @Test
    public void sendMissingMessageWithIdShouldFail() {
        {
            InMemoryMessagePersistence<ProductionEvent> messagePersistence = new InMemoryMessagePersistence<>(kafkaProperties, configuration);

            final ProductionEvent event1 = new ProductionEvent("prod1", "prod1", ProductFamily.AUXILIARY_FILE);
            final ConsumerRecord<String, ProductionEvent> record1 = new ConsumerRecord<>("topic", 1, 1, "string", event1);
            Acknowledgment acknowledgement1 = Mockito.mock(Acknowledgment.class);

            messagePersistence.read(record1, acknowledgement1, genericConsumer, ProductCategory.AUXILIARY_FILES);

            verifyZeroInteractions(acknowledgement1);

            final List<AppCatMessageDto<ProductionEvent>> messagesPod1 = messagePersistence.next(ProductCategory.AUXILIARY_FILES, "pod1");
            final AppCatMessageDto<ProductionEvent> message = messagesPod1.get(0);

            thrown.expect(IllegalArgumentException.class);
            thrown.expectMessage(containsString(valueOf(message.getId() + 31)));
            thrown.expectMessage(containsString("not found"));

            messagePersistence.send(ProductCategory.AUXILIARY_FILES,
                    message.getId() + 31, //random id
                    new AppCatSendMessageDto(kafkaProperties.getHostname(), false));

        }
    }

    @Test
    public void ackShouldCallKafkaAcknowledgeAndMessageRemoved() {

        InMemoryMessagePersistence<ProductionEvent> messagePersistence = new InMemoryMessagePersistence<>(kafkaProperties, configuration);

        final ProductionEvent event1 = new ProductionEvent("prod1", "prod1", ProductFamily.AUXILIARY_FILE);
        final ConsumerRecord<String, ProductionEvent> record1 = new ConsumerRecord<>("topic", 1, 1, "string", event1);
        Acknowledgment acknowledgement1 = Mockito.mock(Acknowledgment.class);

        messagePersistence.read(record1, acknowledgement1, genericConsumer, ProductCategory.AUXILIARY_FILES);

        verifyZeroInteractions(acknowledgement1);

        final List<AppCatMessageDto<ProductionEvent>> messagesPod1 = messagePersistence.next(ProductCategory.AUXILIARY_FILES, "pod1");
        final AppCatMessageDto<ProductionEvent> message = messagesPod1.get(0);

        boolean ackResult = messagePersistence.ack(ProductCategory.AUXILIARY_FILES, message.getId(), Ack.OK);
        assertThat(ackResult, is(true));
        verify(acknowledgement1, times(1)).acknowledge();

        final List<AppCatMessageDto<ProductionEvent>> messagesPod1AfterAck = messagePersistence.next(ProductCategory.AUXILIARY_FILES, "pod1");
        assertThat(messagesPod1AfterAck, is(notNullValue()));
        assertThat(messagesPod1AfterAck.size(), is(0));
    }

    @Test
    public void ackWithMissingMessageShouldReturnFalseMessageNotRemoved() {

        InMemoryMessagePersistence<ProductionEvent> messagePersistence = new InMemoryMessagePersistence<>(kafkaProperties, configuration);

        final ProductionEvent event1 = new ProductionEvent("prod1", "prod1", ProductFamily.AUXILIARY_FILE);
        final ConsumerRecord<String, ProductionEvent> record1 = new ConsumerRecord<>("topic", 1, 1, "string", event1);
        Acknowledgment acknowledgement1 = Mockito.mock(Acknowledgment.class);

        messagePersistence.read(record1, acknowledgement1, genericConsumer, ProductCategory.AUXILIARY_FILES);

        verifyZeroInteractions(acknowledgement1);

        final List<AppCatMessageDto<ProductionEvent>> messagesPod1 = messagePersistence.next(ProductCategory.AUXILIARY_FILES, "pod1");
        final AppCatMessageDto<ProductionEvent> message = messagesPod1.get(0);

        boolean ackResult = messagePersistence.ack(ProductCategory.AUXILIARY_FILES, message.getId() + 45, Ack.OK); //wrong id
        assertThat(ackResult, is(false));
        verifyZeroInteractions(acknowledgement1);

        final List<AppCatMessageDto<ProductionEvent>> messagesPod1AfterAck = messagePersistence.next(ProductCategory.AUXILIARY_FILES, "pod1");
        assertThat(messagesPod1AfterAck, is(notNullValue()));
        assertThat(messagesPod1AfterAck.size(), is(1));
    }

    @Test
    public void getNbReadingMessagesByTopicAndPod() {
        {

            InMemoryMessagePersistence<ProductionEvent> messagePersistence = new InMemoryMessagePersistence<>(kafkaProperties, configuration);

            final ProductionEvent event1 = new ProductionEvent("prod1", "prod1", ProductFamily.AUXILIARY_FILE);
            final ConsumerRecord<String, ProductionEvent> record1 = new ConsumerRecord<>("topic", 1, 1, "string", event1);
            Acknowledgment acknowledgement1 = Mockito.mock(Acknowledgment.class);

            final ProductionEvent event2 = new ProductionEvent("prod2", "prod2", ProductFamily.AUXILIARY_FILE);
            final ConsumerRecord<String, ProductionEvent> record2 = new ConsumerRecord<>("topic", 1, 2, "string", event2);
            Acknowledgment acknowledgement2 = Mockito.mock(Acknowledgment.class);

            final ProductionEvent event3 = new ProductionEvent("prod3", "prod2", ProductFamily.AUXILIARY_FILE);
            final ConsumerRecord<String, ProductionEvent> record3 = new ConsumerRecord<>("tropic", 1, 3, "string", event3);
            Acknowledgment acknowledgement3 = Mockito.mock(Acknowledgment.class);

            messagePersistence.read(record1, acknowledgement1, genericConsumer, ProductCategory.AUXILIARY_FILES);
            messagePersistence.read(record2, acknowledgement2, genericConsumer, ProductCategory.AUXILIARY_FILES);
            messagePersistence.read(record3, acknowledgement3, genericConsumer, ProductCategory.AUXILIARY_FILES);

            final int nbReadingMessagesTopicPod1 = messagePersistence.getNbReadingMessages("topic", "pod1");
            final int nbReadingMessagesTropicPod1 = messagePersistence.getNbReadingMessages("tropic", "pod1");
            final int nbReadingMessagesTopicPod2 = messagePersistence.getNbReadingMessages("topic", "pod2");
            final int nbReadingMessagesTropicPod2 = messagePersistence.getNbReadingMessages("tropic", "pod2");

            assertThat(nbReadingMessagesTopicPod1, is(2));
            assertThat(nbReadingMessagesTropicPod1, is(1));
            assertThat(nbReadingMessagesTopicPod2, is(0));
            assertThat(nbReadingMessagesTropicPod2, is(0));
        }

    }

    @Test
    public void getEarliestOffsetWithMessagesRead() throws InterruptedException {

        InMemoryMessagePersistence<ProductionEvent> messagePersistence = new InMemoryMessagePersistence<>(kafkaProperties, configuration);

        final ProductionEvent event1 = new ProductionEvent("prod1", "prod1", ProductFamily.AUXILIARY_FILE);
        final ConsumerRecord<String, ProductionEvent> record1 = new ConsumerRecord<>("tropic", 1, 156, "string", event1);
        Acknowledgment acknowledgement1 = Mockito.mock(Acknowledgment.class);

        final ProductionEvent event2 = new ProductionEvent("prod2", "prod2", ProductFamily.AUXILIARY_FILE);
        final ConsumerRecord<String, ProductionEvent> record2 = new ConsumerRecord<>("topic", 1, 277, "string", event2);
        Acknowledgment acknowledgement2 = Mockito.mock(Acknowledgment.class);

        final ProductionEvent event3 = new ProductionEvent("prod3", "prod3", ProductFamily.AUXILIARY_FILE);
        final ConsumerRecord<String, ProductionEvent> record3 = new ConsumerRecord<>("topic", 1, 3003, "string", event3);
        Acknowledgment acknowledgement3 = Mockito.mock(Acknowledgment.class);

        messagePersistence.read(record1, acknowledgement1, genericConsumer, ProductCategory.AUXILIARY_FILES);
        sleep(100); //assure different timestamps
        messagePersistence.read(record2, acknowledgement2, genericConsumer, ProductCategory.AUXILIARY_FILES);
        sleep(100);
        messagePersistence.read(record3, acknowledgement3, genericConsumer, ProductCategory.AUXILIARY_FILES);

        final long earliestOffsetTopic = messagePersistence.getEarliestOffset("topic", 1, "group1");
        final long earliestOffsetTropic = messagePersistence.getEarliestOffset("tropic", 1, "group1");
        final long earliestOffsetTopicOtherPartition = messagePersistence.getEarliestOffset("tropic", 2, "group1");

        assertThat(earliestOffsetTopic, is(277L));
        assertThat(earliestOffsetTropic, is(156L));
        assertThat(earliestOffsetTopicOtherPartition, is(-3L)); //default offset
    }

    @Test
    public void getEarliestOffsetWithNoMessagesShouldReturnDefaultOffset() {

        InMemoryMessagePersistence<ProductionEvent> messagePersistence = new InMemoryMessagePersistence<>(kafkaProperties, configuration);

        final long earliestOffsetTopic = messagePersistence.getEarliestOffset("topic", 1, "group1");
        final long earliestOffsetTropic = messagePersistence.getEarliestOffset("tropic", 1, "group1");
        final long earliestOffsetTopicOtherPartition = messagePersistence.getEarliestOffset("tropic", 2, "group1");

        assertThat(earliestOffsetTopic, is(-3L));
        assertThat(earliestOffsetTropic, is(-3L));
        assertThat(earliestOffsetTopicOtherPartition, is(-3L));
    }

    private static class RecordAndAcknowledgement {
        private final ConsumerRecord<String, ProductionEvent> record;
        private final Acknowledgment acknowledgment;

        private RecordAndAcknowledgement(ConsumerRecord<String, ProductionEvent> record, Acknowledgment acknowledgment) {
            this.record = record;
            this.acknowledgment = acknowledgment;
        }

        private static RecordAndAcknowledgement createNew(final String topic, final int partition, final int offset) {
            final ProductionEvent event = new ProductionEvent("prod", "prod", ProductFamily.AUXILIARY_FILE);
            final ConsumerRecord<String, ProductionEvent> record = new ConsumerRecord<>(topic, partition, offset, "string", event);
            Acknowledgment acknowledgement = Mockito.mock(Acknowledgment.class);
            return new RecordAndAcknowledgement(record, acknowledgement);
        }
    }

}