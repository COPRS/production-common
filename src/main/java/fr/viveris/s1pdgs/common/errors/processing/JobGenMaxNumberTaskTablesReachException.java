/**
 * 
 */
package fr.viveris.s1pdgs.common.errors.processing;

import fr.viveris.s1pdgs.common.errors.AbstractCodedException;

/**
 * Exception occurred during job generation
 * 
 * @author Cyrielle Gailliard
 */
public class JobGenMaxNumberTaskTablesReachException
        extends AbstractCodedException {

    private static final long serialVersionUID = -7488001919910076897L;

    /**
     * @param message
     */
    public JobGenMaxNumberTaskTablesReachException(final String message) {
        super(ErrorCode.MAX_NUMBER_TASKTABLE_REACH, message);
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }

}
