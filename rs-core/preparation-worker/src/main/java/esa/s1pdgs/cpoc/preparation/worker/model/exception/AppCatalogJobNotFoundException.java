/*
 * Copyright 2023 Airbus
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package esa.s1pdgs.cpoc.preparation.worker.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author Viveris Technologies
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "No such job")
public class AppCatalogJobNotFoundException extends AbstractAppDataException {

    /**
     * 
     */
    private static final long serialVersionUID = -5220416714429436808L;

    /**
     * Job identifier
     */
    private final long jobId;

    /**
     * Constructor
     * 
     * @param id
     */
    public AppCatalogJobNotFoundException(final long jobId) {
        super(ErrorCode.JOB_NOT_FOUND, "Job not found " + jobId);
        this.jobId = jobId;
    }

    public long getJobId() {
        return jobId;
    }

    /**
     * 
     */
    @Override
    public String getLogMessage() {
        return String.format("[jobId %s] [msg %s]", jobId, getMessage());
    }
}
