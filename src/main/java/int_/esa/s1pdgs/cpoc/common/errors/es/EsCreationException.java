package int_.esa.s1pdgs.cpoc.common.errors.es;

import int_.esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * @author Viveris Technologies
 */
public class EsCreationException extends AbstractCodedException {

    /**
     * UUID
     */
    private static final long serialVersionUID = 1086920560648012203L;

    /**
     * Generic message
     */
    private static final String MESSAGE = "Metadata not created";

    /**
     * Elastic status
     */
    private final String status;

    /**
     * Elastic result
     */
    private final String result;

    /**
     * Constructor
     * 
     * @param result
     * @param status
     */
    public EsCreationException(final String result, final String status) {
        super(ErrorCode.ES_CREATION_ERROR, MESSAGE);
        this.status = status;
        this.result = result;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[status %s] [result %s] [msg %s]", status, result,
                getMessage());
    }

}
