package esa.s1pdgs.cpoc.mqi.server.consumption;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;

import esa.s1pdgs.cpoc.appcatalog.client.mqi.GenericAppCatalogMqiService;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiGenericMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiLightMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiSendMessageDto;
import esa.s1pdgs.cpoc.appcatalog.rest.MqiStateMessageEnum;
import esa.s1pdgs.cpoc.common.ProductCategory;
import esa.s1pdgs.cpoc.common.ResumeDetails;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.mqi.MqiCategoryNotAvailable;
import esa.s1pdgs.cpoc.mqi.model.queue.AuxiliaryFileDto;
import esa.s1pdgs.cpoc.mqi.model.queue.EdrsSessionDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelJobDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelProductDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelReportDto;
import esa.s1pdgs.cpoc.mqi.model.queue.LevelSegmentDto;
import esa.s1pdgs.cpoc.mqi.model.rest.Ack;
import esa.s1pdgs.cpoc.mqi.model.rest.GenericMessageDto;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties.ProductCategoryConsumptionProperties;
import esa.s1pdgs.cpoc.mqi.server.ApplicationProperties.ProductCategoryProperties;
import esa.s1pdgs.cpoc.mqi.server.KafkaProperties;
import esa.s1pdgs.cpoc.mqi.server.consumption.kafka.consumer.GenericConsumer;
import esa.s1pdgs.cpoc.mqi.server.persistence.OtherApplicationService;
import esa.s1pdgs.cpoc.mqi.server.status.AppStatus;

/**
 * Manager of consumers
 * 
 * @author Viveris Technologies
 */
@Controller
public class MessageConsumptionController {

    /**
     * Logger
     */
    protected static final Logger LOGGER =
            LogManager.getLogger(MessageConsumptionController.class);

    /**
     * List of consumers
     */
    protected final Map<ProductCategory, Map<String, GenericConsumer<?>>> consumers;

    /**
     * Application properties
     */
    private final ApplicationProperties appProperties;

    /**
     * Kafka properties
     */
    private final KafkaProperties kafkaProperties;

    /**
     * Services
     */
    private final Map<ProductCategory, GenericAppCatalogMqiService<?>> persistServices;

    /**
     * Service for AUXILIARY_FILES
     */
    private final GenericAppCatalogMqiService<AuxiliaryFileDto> persistAuxiliaryFilesService;

    /**
     * Service for EDRS_SESSIONS
     */
    private final GenericAppCatalogMqiService<EdrsSessionDto> persistEdrsSessionsService;

    /**
     * Service for LEVEL_JOBS
     */
    private final GenericAppCatalogMqiService<LevelJobDto> persistLevelJobsService;

    /**
     * Service for LEVEL_PRODUCTS
     */
    private final GenericAppCatalogMqiService<LevelProductDto> persistLevelProductsService;

    /**
     * Service for LEVEL_REPORTS
     */
    private final GenericAppCatalogMqiService<LevelReportDto> persistLevelReportsService;

    /**
     * Service for LEVEL_SEGMENTS
     */
    private final GenericAppCatalogMqiService<LevelSegmentDto> persistLevelSegmentsService;

    /**
     * Service for checking if a message is processing or not by another
     */
    private final OtherApplicationService otherAppService;

    /**
     * Application status
     */
    private final AppStatus appStatus;

    /**
     * Constructor
     * 
     * @param appProperties
     * @param kafkaProperties
     */
    @Autowired
    public MessageConsumptionController(
            final ApplicationProperties appProperties,
            final KafkaProperties kafkaProperties,
            @Qualifier("persistenceServiceForAuxiliaryFiles") final GenericAppCatalogMqiService<AuxiliaryFileDto> persistAuxiliaryFilesService,
            @Qualifier("persistenceServiceForEdrsSessions") final GenericAppCatalogMqiService<EdrsSessionDto> persistEdrsSessionsService,
            @Qualifier("persistenceServiceForLevelJobs") final GenericAppCatalogMqiService<LevelJobDto> persistLevelJobsService,
            @Qualifier("persistenceServiceForLevelProducts") final GenericAppCatalogMqiService<LevelProductDto> persistLevelProductsService,
            @Qualifier("persistenceServiceForLevelReports") final GenericAppCatalogMqiService<LevelReportDto> persistLevelReportsService,
            @Qualifier("persistenceServiceForLevelSegments") final GenericAppCatalogMqiService<LevelSegmentDto> persistLevelSegmentsService,
            final OtherApplicationService otherAppService,
            final AppStatus appStatus) {
        this.consumers = new HashMap<>();
        this.appProperties = appProperties;
        this.kafkaProperties = kafkaProperties;
        this.otherAppService = otherAppService;
        this.persistServices = new HashMap<>();
        this.persistAuxiliaryFilesService = persistAuxiliaryFilesService;
        this.persistEdrsSessionsService = persistEdrsSessionsService;
        this.persistLevelJobsService = persistLevelJobsService;
        this.persistLevelProductsService = persistLevelProductsService;
        this.persistLevelReportsService = persistLevelReportsService;
        this.persistLevelSegmentsService = persistLevelSegmentsService;
        this.persistServices.put(ProductCategory.AUXILIARY_FILES,
                this.persistAuxiliaryFilesService);
        this.persistServices.put(ProductCategory.EDRS_SESSIONS,
                this.persistEdrsSessionsService);
        this.persistServices.put(ProductCategory.LEVEL_JOBS,
                this.persistLevelJobsService);
        this.persistServices.put(ProductCategory.LEVEL_PRODUCTS,
                this.persistLevelProductsService);
        this.persistServices.put(ProductCategory.LEVEL_REPORTS,
                this.persistLevelReportsService);
        this.persistServices.put(ProductCategory.LEVEL_SEGMENTS,
                this.persistLevelSegmentsService);
        this.appStatus = appStatus;
    }

    /**
     * Start consumers according the configuration
     */
    @PostConstruct
    public void startConsumers() {

        // Init the list of consumers
        for (ProductCategory cat : appProperties.getProductCategories()
                .keySet()) {
            ProductCategoryProperties catProp =
                    appProperties.getProductCategories().get(cat);
            ProductCategoryConsumptionProperties prop =
                    catProp.getConsumption();
            if (prop.isEnable()) {
                LOGGER.info(
                        "Creating consumers on topics {} with for category {}",
                        prop.getTopicsWithPriority(), cat);
                Map<String, GenericConsumer<?>> catConsumers = new HashMap<>();
                for (String topic : prop.getTopicsWithPriority().keySet()) {
                    switch (cat) {
                        case AUXILIARY_FILES:
                            catConsumers.put(topic,
                                    new GenericConsumer<AuxiliaryFileDto>(
                                            kafkaProperties,
                                            persistAuxiliaryFilesService,
                                            otherAppService, appStatus, topic,
                                            prop.getTopicsWithPriority().get(topic),
                                            AuxiliaryFileDto.class));
                            break;
                        case EDRS_SESSIONS:
                            catConsumers.put(topic,
                                    new GenericConsumer<EdrsSessionDto>(
                                            kafkaProperties,
                                            persistEdrsSessionsService,
                                            otherAppService, appStatus, topic,
                                            prop.getTopicsWithPriority().get(topic),
                                            EdrsSessionDto.class));
                            break;
                        case LEVEL_JOBS:
                            catConsumers.put(topic,
                                    new GenericConsumer<LevelJobDto>(
                                            kafkaProperties,
                                            persistLevelJobsService,
                                            otherAppService, appStatus, topic,
                                            prop.getTopicsWithPriority().get(topic),
                                            LevelJobDto.class));
                            break;
                        case LEVEL_PRODUCTS:
                            catConsumers.put(topic,
                                    new GenericConsumer<LevelProductDto>(
                                            kafkaProperties,
                                            persistLevelProductsService,
                                            otherAppService, appStatus, topic,
                                            prop.getTopicsWithPriority().get(topic),
                                            LevelProductDto.class));
                            break;
                        case LEVEL_REPORTS:
                            catConsumers.put(topic,
                                    new GenericConsumer<LevelReportDto>(
                                            kafkaProperties,
                                            persistLevelReportsService,
                                            otherAppService, appStatus, topic,
                                            prop.getTopicsWithPriority().get(topic),
                                            LevelReportDto.class));
                            break;
                        case LEVEL_SEGMENTS:
                            catConsumers.put(topic,
                                    new GenericConsumer<LevelSegmentDto>(
                                            kafkaProperties,
                                            persistLevelSegmentsService,
                                            otherAppService, appStatus, topic,
                                            prop.getTopicsWithPriority().get(topic),
                                            LevelSegmentDto.class));
                            break;
                    }
                }
                consumers.put(cat, catConsumers);
            }
        }
        // Start the consumers
        for (Map<String, GenericConsumer<?>> catConsumers : consumers
                .values()) {
            for (GenericConsumer<?> consumer : catConsumers.values()) {
                LOGGER.info("Starting consumer on topic {}",
                        consumer.getTopic());
                consumer.start();
            }
        }
    }

    /**
     * Get the next message for a given category
     * 
     * @param category
     * @return
     * @throws AbstractCodedException
     */
    public GenericMessageDto<?> nextMessage(final ProductCategory category)
            throws AbstractCodedException {
        GenericMessageDto<?> message = null;
        if (consumers.containsKey(category)) {
            switch (category) {
                case EDRS_SESSIONS:
                    message = nextEdrsSessionsMessage();
                    break;
                case LEVEL_JOBS:
                    message = nextLevelJobsMessage();
                    break;
                case LEVEL_PRODUCTS:
                    message = nextLevelProductsMessage();
                    break;
                case LEVEL_REPORTS:
                    message = nextLevelReportsMessage();
                    break;
                case LEVEL_SEGMENTS:
                    message = nextLevelSegmentsMessage();
                    break;
                default:
                    message = nextAuxiliaryFilesMessage();
                    break;
            }
            // if no message and consumer is pause => resume it
            if (message == null) {
                for(GenericConsumer<?> consumer: consumers.get(category).values()) {
                    consumer.resume();
                }
            }
        } else {
            throw new MqiCategoryNotAvailable(category, "consumer");
        }
        return message;
    }

    /**
     * Get the next message for auxiliary files
     * 
     * @return
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    protected GenericMessageDto<AuxiliaryFileDto> nextAuxiliaryFilesMessage()
            throws AbstractCodedException {
        List<MqiGenericMessageDto<AuxiliaryFileDto>> messages =
                persistAuxiliaryFilesService.next(appProperties.getHostname());
        MqiGenericMessageDto<AuxiliaryFileDto> result = null;
        if (!CollectionUtils.isEmpty(messages)) {
            messages.sort(new Comparator<MqiGenericMessageDto<AuxiliaryFileDto>>() {
                @Override
                public int compare(MqiGenericMessageDto<AuxiliaryFileDto> o1,
                        MqiGenericMessageDto<AuxiliaryFileDto> o2) {
                    if(consumers.get(ProductCategory.AUXILIARY_FILES).get(o1.getTopic()).getPriority() >
                        consumers.get(ProductCategory.AUXILIARY_FILES).get(o2.getTopic()).getPriority()) {
                        return -1;
                    } else if(consumers.get(ProductCategory.AUXILIARY_FILES).get(o1.getTopic()).getPriority() ==
                            consumers.get(ProductCategory.AUXILIARY_FILES).get(o2.getTopic()).getPriority()) {
                        if(o1.getCreationDate()==null) {
                            return 1;
                        } else if(o2.getCreationDate()==null) {
                            return -1;
                        } else {
                            return o1.getCreationDate().compareTo(o2.getCreationDate());
                        }
                    } else {
                        return 1;
                    }
                }                
            });
            for (MqiGenericMessageDto<AuxiliaryFileDto> tmpMessage : messages) {
                if (send(persistAuxiliaryFilesService,
                        (MqiLightMessageDto) tmpMessage)) {
                    result = tmpMessage;
                    break;
                }
            }
        }
        return (GenericMessageDto<AuxiliaryFileDto>) convertToRestDto(result);
    }

    /**
     * Get the next message for edrs sessions
     * 
     * @return
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    protected GenericMessageDto<EdrsSessionDto> nextEdrsSessionsMessage()
            throws AbstractCodedException {
        List<MqiGenericMessageDto<EdrsSessionDto>> messages =
                persistEdrsSessionsService.next(appProperties.getHostname());
        MqiGenericMessageDto<EdrsSessionDto> result = null;
        if (!CollectionUtils.isEmpty(messages)) {
            messages.sort(new Comparator<MqiGenericMessageDto<EdrsSessionDto>>() {
                @Override
                public int compare(MqiGenericMessageDto<EdrsSessionDto> o1,
                        MqiGenericMessageDto<EdrsSessionDto> o2) {
                    if(consumers.get(ProductCategory.EDRS_SESSIONS).get(o1.getTopic()).getPriority() >
                        consumers.get(ProductCategory.EDRS_SESSIONS).get(o2.getTopic()).getPriority()) {
                        return -1;
                    } else if(consumers.get(ProductCategory.EDRS_SESSIONS).get(o1.getTopic()).getPriority() ==
                            consumers.get(ProductCategory.EDRS_SESSIONS).get(o2.getTopic()).getPriority()) {
                        if(o1.getCreationDate()==null) {
                            return 1;
                        } else if(o2.getCreationDate()==null) {
                            return -1;
                        } else {
                            return o1.getCreationDate().compareTo(o2.getCreationDate());
                        }
                    } else {
                        return 1;
                    }
                }                
            });
            for (MqiGenericMessageDto<EdrsSessionDto> tmpMessage : messages) {
                if (send(persistEdrsSessionsService,
                        (MqiLightMessageDto) tmpMessage)) {
                    result = tmpMessage;
                    break;
                }
            }
        }
        return (GenericMessageDto<EdrsSessionDto>) convertToRestDto(result);
    }

    /**
     * Get the next message for product jobs
     * 
     * @return
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    protected GenericMessageDto<LevelJobDto> nextLevelJobsMessage()
            throws AbstractCodedException {
        List<MqiGenericMessageDto<LevelJobDto>> messages =
                persistLevelJobsService.next(appProperties.getHostname());
        MqiGenericMessageDto<LevelJobDto> result = null;
        if (!CollectionUtils.isEmpty(messages)) {
            messages.sort(new Comparator<MqiGenericMessageDto<LevelJobDto>>() {
                @Override
                public int compare(MqiGenericMessageDto<LevelJobDto> o1,
                        MqiGenericMessageDto<LevelJobDto> o2) {
                    if(consumers.get(ProductCategory.LEVEL_JOBS).get(o1.getTopic()).getPriority() >
                        consumers.get(ProductCategory.LEVEL_JOBS).get(o2.getTopic()).getPriority()) {
                        return -1;
                    } else if(consumers.get(ProductCategory.LEVEL_JOBS).get(o1.getTopic()).getPriority() ==
                            consumers.get(ProductCategory.LEVEL_JOBS).get(o2.getTopic()).getPriority()) {
                        if(o1.getCreationDate()==null) {
                            return 1;
                        } else if(o2.getCreationDate()==null) {
                            return -1;
                        } else {
                            return o1.getCreationDate().compareTo(o2.getCreationDate());
                        }
                    } else {
                        return 1;
                    }
                }                
            });
            for (MqiGenericMessageDto<LevelJobDto> tmpMessage : messages) {
                if (send(persistLevelJobsService,
                        (MqiLightMessageDto) tmpMessage)) {
                    result = tmpMessage;
                    break;
                }
            }
        }
        return (GenericMessageDto<LevelJobDto>) convertToRestDto(result);
    }

    /**
     * Get the next message for level products
     * 
     * @return
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    protected GenericMessageDto<LevelProductDto> nextLevelProductsMessage()
            throws AbstractCodedException {
        List<MqiGenericMessageDto<LevelProductDto>> messages =
                persistLevelProductsService.next(appProperties.getHostname());
        MqiGenericMessageDto<LevelProductDto> result = null;
        if (!CollectionUtils.isEmpty(messages)) {
            messages.sort(new Comparator<MqiGenericMessageDto<LevelProductDto>>() {
                @Override
                public int compare(MqiGenericMessageDto<LevelProductDto> o1,
                        MqiGenericMessageDto<LevelProductDto> o2) {
                    if(consumers.get(ProductCategory.LEVEL_PRODUCTS).get(o1.getTopic()).getPriority() >
                        consumers.get(ProductCategory.LEVEL_PRODUCTS).get(o2.getTopic()).getPriority()) {
                        return -1;
                    } else if(consumers.get(ProductCategory.LEVEL_PRODUCTS).get(o1.getTopic()).getPriority() ==
                            consumers.get(ProductCategory.LEVEL_PRODUCTS).get(o2.getTopic()).getPriority()) {
                        if(o1.getCreationDate()==null) {
                            return 1;
                        } else if(o2.getCreationDate()==null) {
                            return -1;
                        } else {
                            return o1.getCreationDate().compareTo(o2.getCreationDate());
                        }
                    } else {
                        return 1;
                    }
                }                
            });
            for (MqiGenericMessageDto<LevelProductDto> tmpMessage : messages) {
                if (send(persistLevelProductsService,
                        (MqiLightMessageDto) tmpMessage)) {
                    result = tmpMessage;
                    break;
                }
            }
        }
        return (GenericMessageDto<LevelProductDto>) convertToRestDto(result);
    }

    /**
     * Get the next message for level segments
     * 
     * @return
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    protected GenericMessageDto<LevelSegmentDto> nextLevelSegmentsMessage()
            throws AbstractCodedException {
        List<MqiGenericMessageDto<LevelSegmentDto>> messages =
                persistLevelSegmentsService.next(appProperties.getHostname());
        MqiGenericMessageDto<LevelSegmentDto> result = null;
        if (!CollectionUtils.isEmpty(messages)) {
            messages.sort(new Comparator<MqiGenericMessageDto<LevelSegmentDto>>() {
                @Override
                public int compare(MqiGenericMessageDto<LevelSegmentDto> o1,
                        MqiGenericMessageDto<LevelSegmentDto> o2) {
                    if(consumers.get(ProductCategory.LEVEL_PRODUCTS).get(o1.getTopic()).getPriority() >
                        consumers.get(ProductCategory.LEVEL_PRODUCTS).get(o2.getTopic()).getPriority()) {
                        return -1;
                    } else if(consumers.get(ProductCategory.LEVEL_PRODUCTS).get(o1.getTopic()).getPriority() ==
                            consumers.get(ProductCategory.LEVEL_PRODUCTS).get(o2.getTopic()).getPriority()) {
                        if(o1.getCreationDate()==null) {
                            return 1;
                        } else if(o2.getCreationDate()==null) {
                            return -1;
                        } else {
                            return o1.getCreationDate().compareTo(o2.getCreationDate());
                        }
                    } else {
                        return 1;
                    }
                }                
            });
            for (MqiGenericMessageDto<LevelSegmentDto> tmpMessage : messages) {
                if (send(persistLevelSegmentsService,
                        (MqiLightMessageDto) tmpMessage)) {
                    result = tmpMessage;
                    break;
                }
            }
        }
        return (GenericMessageDto<LevelSegmentDto>) convertToRestDto(result);
    }

    /**
     * Get the next message for level reports
     * 
     * @return
     * @throws AbstractCodedException
     */
    @SuppressWarnings("unchecked")
    protected GenericMessageDto<LevelReportDto> nextLevelReportsMessage()
            throws AbstractCodedException {
        List<MqiGenericMessageDto<LevelReportDto>> messages =
                persistLevelReportsService.next(appProperties.getHostname());
        MqiGenericMessageDto<LevelReportDto> result = null;
        if (!CollectionUtils.isEmpty(messages)) {
            messages.sort(new Comparator<MqiGenericMessageDto<LevelReportDto>>() {
                @Override
                public int compare(MqiGenericMessageDto<LevelReportDto> o1,
                        MqiGenericMessageDto<LevelReportDto> o2) {
                    if(consumers.get(ProductCategory.LEVEL_REPORTS).get(o1.getTopic()).getPriority() >
                        consumers.get(ProductCategory.LEVEL_REPORTS).get(o2.getTopic()).getPriority()) {
                        return -1;
                    } else if(consumers.get(ProductCategory.LEVEL_REPORTS).get(o1.getTopic()).getPriority() ==
                            consumers.get(ProductCategory.LEVEL_REPORTS).get(o2.getTopic()).getPriority()) {
                        if(o1.getCreationDate()==null) {
                            return 1;
                        } else if(o2.getCreationDate()==null) {
                            return -1;
                        } else {
                            return o1.getCreationDate().compareTo(o2.getCreationDate());
                        }
                    } else {
                        return 1;
                    }
                }                
            });
            for (MqiGenericMessageDto<LevelReportDto> tmpMessage : messages) {
                if (send(persistLevelReportsService,
                        (MqiLightMessageDto) tmpMessage)) {
                    result = tmpMessage;
                    break;
                }
            }
        }
        return (GenericMessageDto<LevelReportDto>) convertToRestDto(result);
    }

    /**
     * @param service
     * @param messageId
     * @param force
     * @return
     * @throws AbstractCodedException
     */
    protected boolean send(final GenericAppCatalogMqiService<?> service,
            final MqiLightMessageDto message) throws AbstractCodedException {
        boolean ret = false;
        if (message.getState() == MqiStateMessageEnum.SEND) {
            if (isSameSendingPod(message.getSendingPod())) {

                ret = service.send(message.getIdentifier(),
                        new MqiSendMessageDto(appProperties.getHostname(),
                                false));
            } else {
                boolean isProcessing = false;
                try {
                    isProcessing = otherAppService.isProcessing(
                            message.getSendingPod(), service.getCategory(),
                            message.getIdentifier());
                } catch (AbstractCodedException ace) {
                    isProcessing = false;
                    LOGGER.warn(
                            "{} No response from the other application, consider it as dead",
                            ace.getLogMessage());
                }
                if (!isProcessing) {
                    ret = service.send(message.getIdentifier(),
                            new MqiSendMessageDto(appProperties.getHostname(),
                                    true));
                } else {
                    ret = false;
                }
            }
        } else {
            // We return this message after persisting it as sending
            ret = service.send(message.getIdentifier(),
                    new MqiSendMessageDto(appProperties.getHostname(), false));
        }

        return ret;
    }

    /**
     * @param sendingPod
     * @return
     */
    protected boolean isSameSendingPod(final String sendingPod) {
        return appProperties.getHostname().equals(sendingPod);
    }

    /**
     * Convert an app catalog rest object into mqi rest object from next API
     * 
     * @param object
     * @return
     */
    private GenericMessageDto<?> convertToRestDto(
            final MqiGenericMessageDto<?> object) {
        if (object == null) {
            return null;
        }
        return new GenericMessageDto<>(object.getIdentifier(),
                object.getTopic(), object.getDto());
    }

    /**
     * Acknowledge a message
     * 
     * @param identifier
     * @param ack
     * @param message
     * @throws AbstractCodedException
     */
    public ResumeDetails ackMessage(final ProductCategory category,
            final long identifier, final Ack ack, final boolean stop)
            throws AbstractCodedException {
        ResumeDetails ret = null;
        int nbReadingMsg = 0;
        MqiGenericMessageDto<?> message = null;
        if (consumers.containsKey(category)) {
            try {
                // Ack message
                persistServices.get(category).ack(identifier, ack);
                // Get resume details and topic
                message = persistServices.get(category).get(identifier);
                // Get remaining message read
                nbReadingMsg = persistServices.get(category)
                        .getNbReadingMessages(message.getTopic(),
                                appProperties.getHostname());
                ret = new ResumeDetails(message.getTopic(), message.getDto());
            } finally {
                // Resume consumer of concerned topic
                if (!stop && nbReadingMsg <= 0 && message != null) {
                    // Resume consumer
                    if (consumers.get(category)
                            .containsKey(message.getTopic())) {
                        consumers.get(category).get(message.getTopic())
                                .resume();
                    } else {
                        LOGGER.warn(
                                "[category {}] [messageCannot resume consumer for this topic because does not exist: {}",
                                category, message);
                    }
                }
            }
        } else {
            throw new MqiCategoryNotAvailable(category, "consumer");
        }
        return ret;
    }

}
