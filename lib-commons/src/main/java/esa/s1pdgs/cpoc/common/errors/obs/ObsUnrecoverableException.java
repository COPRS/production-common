package esa.s1pdgs.cpoc.common.errors.obs;

import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;

public class ObsUnrecoverableException extends AbstractCodedException {

    public ObsUnrecoverableException(Throwable cause) {
        super(ErrorCode.OBS_UNRECOVERABLE, cause.getMessage(), cause);
    }

    @Override
    public String getLogMessage() {
        return String.format("[msg %s]", getMessage());
    }
}
