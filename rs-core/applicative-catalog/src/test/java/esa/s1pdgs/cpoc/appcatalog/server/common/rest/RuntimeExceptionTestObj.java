package esa.s1pdgs.cpoc.appcatalog.server.common.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INSUFFICIENT_STORAGE)
public class RuntimeExceptionTestObj extends RuntimeException {

    /**
     * 
     */
    private static final long serialVersionUID = 3802480528997941253L;

    public RuntimeExceptionTestObj(String message) {
        super(message);
    }
}
