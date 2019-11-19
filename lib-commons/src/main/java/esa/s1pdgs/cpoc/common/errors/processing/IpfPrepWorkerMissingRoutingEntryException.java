package esa.s1pdgs.cpoc.common.errors.processing;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

/**
 * 
 * @author Viveris Technologies
 *
 */
public class IpfPrepWorkerMissingRoutingEntryException extends AbstractCodedException {

    private static final long serialVersionUID = -191284916049233409L;

    /**
     * @param message
     */
    public IpfPrepWorkerMissingRoutingEntryException(final String message) {
        super(ErrorCode.MISSING_ROUTING_ENTRY, message);
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }

}
