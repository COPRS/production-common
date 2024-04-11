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

package esa.s1pdgs.cpoc.ingestion.worker.obs;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.AbstractCodedException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.common.errors.obs.ObsUnrecoverableException;
import esa.s1pdgs.cpoc.common.utils.Exceptions;
import esa.s1pdgs.cpoc.common.utils.LogUtils;
import esa.s1pdgs.cpoc.ingestion.worker.inbox.InboxAdapterEntry;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsEmptyFileException;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.ObsServiceException;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.obs_sdk.StreamObsUploadObject;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class ObsAdapter {

    private static final Logger LOG = LogManager.getLogger(ObsAdapter.class);

    private static final int BUFFER_SIZE = 8 * 1024 * 1024; // copy in 8M blocks

    private final ObsClient obsClient;
    private final ReportingFactory reportingFactory;
    private final boolean copyInputStreamToBuffer;
    private final AppStatus appStatus;

    public ObsAdapter(
            final ObsClient obsClient,
            final ReportingFactory reportingFactory,
            final boolean copyInputStreamToBuffer,
            final AppStatus appStatus
            ) {
        this.obsClient = obsClient;
        this.reportingFactory = reportingFactory;
        this.copyInputStreamToBuffer = copyInputStreamToBuffer;
        this.appStatus = appStatus;
    }
    
    public final String getAbsoluteStoragePath(ProductFamily family, String keyObs) {
    	return obsClient.getAbsoluteStoragePath(family, keyObs);
    }

    public final void upload(final ProductFamily family, final List<InboxAdapterEntry> entries, final String obsKey) throws ObsEmptyFileException {
        try {
            obsClient.uploadStreams(toUploadObjects(family, entries), reportingFactory);
        } catch (final ObsUnrecoverableException e) {
            LOG.error("error during upload of {} {}", family, obsKey, e);
            appStatus.getStatus().setFatalError();
            throw new RuntimeException(
                    String.format("Error uploading %s (%s): %s", obsKey, family, LogUtils.toString(e))
            );
        } catch (final AbstractCodedException e) {
            throw new RuntimeException(
                    String.format("Error uploading %s (%s): %s", obsKey, family, LogUtils.toString(e))
            );
        }
    }

    public final long sizeOf(final ProductFamily family, final String obsKey) {
        try {
            final ObsObject obsObject = new ObsObject(family, obsKey);

            //obsObject is a file and exists
            if (obsClient.exists(obsObject)) {
                return obsClient.size(obsObject);
            }

            //obsObject is a directory and exists
            if (obsClient.prefixExists(obsObject)) {
                List<String> list = obsClient.list(family, obsObject.getKey());
                long totalSize = 0;
                for (String key : list) {
                    totalSize += obsClient.size(new ObsObject(family, key));
                }

                return totalSize;
            }

            return -1;
        } catch (ObsException | SdkClientException e) {
            throw new RuntimeException(
                    String.format("Error while retrieving size for obs object with key %s: %s",
                            obsKey,
                            LogUtils.toString(e)));
        }
    }

    private List<StreamObsUploadObject> toUploadObjects(final ProductFamily family, final List<InboxAdapterEntry> entries) {
        return entries.stream()
                .map(e -> new StreamObsUploadObject(family, e.key(), inputStreamOf(e), e.size()))
                .collect(Collectors.toList());
    }

    private InputStream inputStreamOf(final InboxAdapterEntry entry) {
        // S1PRO-2117: Make the buffer explicit here and avoid having too many concurrent open connection
        // for product download
        if (copyInputStreamToBuffer) {
            // No use of retries here as input stream is closed anyway and needs to be re-read,
            // i.e. retries may make sense in ProductServiceImpl
            return copyFromInputStream(entry);
        }
        // old behavior
        return entry.inputStream();
    }

    private static InputStream copyFromInputStream(final InboxAdapterEntry entry) {
        try (final InputStream in = entry.inputStream();
             final ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            final byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            return new ByteArrayInputStream(out.toByteArray());
        } catch (final IOException e) {
            throw new RuntimeException(
                    String.format("Error on downloading '%s': %s", entry.key(), Exceptions.messageOf(e)),
                    e
            );
        }
    }
}
