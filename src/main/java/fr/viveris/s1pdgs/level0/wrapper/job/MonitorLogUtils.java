package fr.viveris.s1pdgs.level0.wrapper.job;

import fr.viveris.s1pdgs.mqi.model.queue.LevelJobDto;

public final class MonitorLogUtils {

    /**
     * Identifier for default monitor logs
     */
    public static final String LOG_DFT = "default";
    /**
     * Identifier for end monitor logs
     */
    public static final String LOG_BEGIN = "begin";
    /**
     * Identifier for end monitor logs
     */
    public static final String LOG_READ = "read";
    /**
     * Identifier for processes execution monitor logs
     */
    public static final String LOG_PROCESS = "process";
    /**
     * Identifier for input download monitor logs
     */
    public static final String LOG_INPUT = "input";
    /**
     * Identifier for output processing monitor logs
     */
    public static final String LOG_OUTPUT = "output";
    /**
     * Identifier for erasing monitor logs
     */
    public static final String LOG_ERASE = "erase";
    /**
     * Identifier for resuming monitor logs
     */
    public static final String LOG_ACK = "ack";
    /**
     * Identifier for status monitor logs
     */
    public static final String LOG_STATUS = "status";
    /**
     * Identifier for end monitor logs
     */
    public static final String LOG_END = "end";
    /**
     * Identifier for error monitor logs
     */
    public static final String LOG_ERROR = "error";

    /**
     * Get the prefix for monitor logs according the step
     * 
     * @param step
     * @return
     */
    public static String getPrefixMonitorLog(final String step,
            final LevelJobDto job) {
        String ret;
        switch (step) {
            case LOG_ERROR:
                ret = String.format("[productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_BEGIN:
                ret = String.format(
                        "[MONITOR] [step 0] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_READ:
                ret = String.format(
                        "[MONITOR] [step 1] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_PROCESS:
                ret = String.format(
                        "[MONITOR] [step 3] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_INPUT:
                ret = String.format(
                        "[MONITOR] [step 2] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_OUTPUT:
                ret = String.format(
                        "[MONITOR] [step 4] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_ACK:
                ret = String.format(
                        "[MONITOR] [step 5] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_ERASE:
                ret = String.format(
                        "[MONITOR] [step 6] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_STATUS:
                ret = String.format(
                        "[MONITOR] [step 7] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            case LOG_END:
                ret = String.format(
                        "[MONITOR] [step 0] [productName %s] [workDir %s]",
                        job.getProductIdentifier(), job.getWorkDirectory());
                break;
            default:
                ret = "[MONITOR]";
                break;
        }
        return ret;
    }

}
