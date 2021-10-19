package esa.s1pdgs.cpoc.mqi.server;

public class GenericKafkaUtils {

    public final static String TOPIC_L0_JOBS = "t-pdgs-aio-execution-jobs";
    public final static String TOPIC_L0_PRODUCTS = "t-pdgs-aio-l0-slice-production-events-nrt"; // t-pdgs-l0-slices -> t-pdgs-aio-l0-slice-production-events-nrt
    public final static String TOPIC_L0_REPORTS = "t-pdgs-aio-production-report-events";
    public final static String TOPIC_L1_JOBS = "t-pdgs-l1-execution-jobs-nrt"; // ok t-pdgs-l1-jobs -> t-pdgs-l1-execution-jobs-nrt
    public final static String TOPIC_L1_ACNS = "t-pdgs-l1-acn-production-events-nrt"; // t-pdgs-l1-acns -> t-pdgs-l1-acn-production-events-nrt
    public final static String TOPIC_L1_REPORTS = "t-pdgs-l1-production-report-events";
    public final static String TOPIC_EDRS_SESSIONS = "t-pdgs-session-file-ingestion-events";
    public final static String TOPIC_AUXILIARY_FILES = "t-pdgs-aux-ingestion-events";
    public final static String TOPIC_L0_SEGMENTS = "t-pdgs-aio-l0-segment-production-events";
    
    public final static String TOPIC_L2_JOBS = "t-pdgs-l2-execution-jobs-fast"; // ok t-pdgs-l2-jobs -> t-pdgs-l2-execution-jobs-fast
    public final static String TOPIC_L2_REPORTS = "t-pdgs-l2-production-report-events";
    
}
