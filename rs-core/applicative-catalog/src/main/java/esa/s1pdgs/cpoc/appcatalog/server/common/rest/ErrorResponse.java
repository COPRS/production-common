package esa.s1pdgs.cpoc.appcatalog.server.common.rest;

/**
 * Generic error response
 * @author Viveris Technologies
 *
 */
public class ErrorResponse {
    
    /**
     * HTTP status
     */
    private int status;

    /**
     * Internal error code
     */
    private int code;

    /**
     * Message
     */
	private String message;

    /**
     * @param error
     * @param message
     * @param code
     */
    public ErrorResponse(final int status, final String message, final int code) {
        super();
        this.status = status;
        this.message = message;
        this.code = code;
    }

    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(final int status) {
        this.status = status;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(final String message) {
        this.message = message;
    }

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * @param code the code to set
     */
    public void setCode(final int code) {
        this.code = code;
    }

}
