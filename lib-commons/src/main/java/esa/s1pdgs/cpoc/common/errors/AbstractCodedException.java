package esa.s1pdgs.cpoc.common.errors;

/**
 * Abstract custom exception
 * 
 * @author Viveris Technologies
 */
public abstract class AbstractCodedException extends Exception {

    /**
     * UUID
     */
    private static final long serialVersionUID = -3674800585523293639L;

    /**
     * Code identified the error
     */
    private final ErrorCode code;

    /**
     * Constructor
     * 
     * @param code
     * @param message
     */
    public AbstractCodedException(final ErrorCode code, final String message) {
        super(message);
        this.code = code;
    }

    /**
     * Constructor
     * 
     * @param code
     * @param message
     * @param e
     */
    public AbstractCodedException(final ErrorCode code, final String message,
            final Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * @return the code
     */
    public ErrorCode getCode() {
        return code;
    }

    /**
     * The available error codes
     */
    public enum ErrorCode {

        INTERNAL_ERROR(1), UNKNOWN_FAMILY(2), INVALID_PRODUCT_FORMAT(3),

        OBS_UNKOWN_OBJ(50), OBS_ERROR(51), OBS_ALREADY_EXIST(52),
        OBS_PARALLEL_ACCESS(53),

        MQI_PUBLICATION_ERROR(70), MQI_CATEGORY_NOT_AVAILABLE(71),
        MQI_NEXT_API_ERROR(72), MQI_ACK_API_ERROR(73),
        MQI_PUBLISH_API_ERROR(74), MQI_PUBLISH_ERROR(75),
        MQI_STATUS_API_ERROR(76), MQI_STOP_API_ERROR(77),
        MQI_ROUTE_NOT_AVAILABLE(78),

        ES_CREATION_ERROR(90), ES_NOT_PRESENT_ERROR(91),
        ES_INVALID_SEARCH_MODE(92),

        OS_SERVER_NOT_ACTIVE(110), OS_FLOATING_IP_NOT_ACTIVE(111),
        OS_SERVER_NOT_DELETED(112), OS_VOLUME_NOT_CREATED(113),

        K8S_UNKNOWN_RESOURCE(130), K8S_NO_TEMPLATE_POD(131),
        K8S_WRAPPER_STATUS_ERROR(132), K8S_WRAPPER_STOP_ERROR(133),

        INGESTOR_IGNORE_FILE(200), INGESTOR_INVALID_PATH(201),
        INGESTOR_CLEAN(202),

        MAX_NUMBER_CACHED_JOB_REACH(270), MAX_NUMBER_TASKTABLE_REACH(271),
        MAX_NUMBER_CACHED_SESSIONS_REACH(272), MAX_AGE_CACHED_JOB_REACH(273),
        MAX_AGE_CACHED_SESSIONS_REACH(274), MISSING_ROUTING_ENTRY(275),
        JOB_GENERATOR_INIT_FAILED(276), MISSING_INPUT(277),
        JOB_GEN_METADATA_ERROR(278), 

        PROCESS_EXIT_ERROR(290), PROCESS_TIMEOUT(291),

        METADATA_IGNORE_FILE(310), METADATA_FILE_PATH(311),
        METADATA_FILE_EXTENSION(312), METADATA_MALFORMED_ERROR(313),
        METADATA_EXTRACTION_ERROR(313),

        APPCATALOG_MQI_READ_API_ERROR(330), APPCATALOG_MQI_NEXT_API_ERROR(331),
        APPCATALOG_MQI_SEND_API_ERROR(332), APPCATALOG_MQI_ACK_API_ERROR(333),
        APPCATALOG_MQI_GET_OFFSET_API_ERROR(334), APPCATALOG_MQI_GET_API_ERROR(335),
        APPCATALOG_MQI_NB_READ_API_ERROR(336), APPCATALOG_JOB_SEARCH_API_ERROR(337),
        APPCATALOG_MQI_JOB_ONE_API_ERROR(338), APPCATALOG_JOB_NEW_API_ERROR(339),
        APPCATALOG_JOB_DELETE_API_ERROR(340), APPCATALOG_JOB_PATCH_API_ERROR(341),
        APPCATALOG_JOB_PATCH_GEN_API_ERROR(342),
        
        STATUS_PROCESSING_API_ERROR(350);

        /**
         * code
         */
        private final int code;

        /**
         * constructor
         * 
         * @param code
         */
        private ErrorCode(final int code) {
            this.code = code;
        }

        /**
         * @return
         */
        public int getCode() {
            return this.code;
        }
    }

    /**
     * Display exception details (except code) in a specific format: [fieldname1
     * fieldvalue1]... [fieldnamen fieldvaluen] [msg msg]
     * 
     * @return
     */
    public abstract String getLogMessage();

}
