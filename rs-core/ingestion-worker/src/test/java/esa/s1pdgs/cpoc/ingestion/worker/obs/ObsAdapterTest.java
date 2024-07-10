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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.appstatus.AppStatus;
import esa.s1pdgs.cpoc.common.ProductFamily;
import esa.s1pdgs.cpoc.common.errors.obs.ObsException;
import esa.s1pdgs.cpoc.obs_sdk.ObsClient;
import esa.s1pdgs.cpoc.obs_sdk.ObsObject;
import esa.s1pdgs.cpoc.obs_sdk.SdkClientException;
import esa.s1pdgs.cpoc.report.ReportingFactory;

public class ObsAdapterTest {

    @Mock
    ObsClient obsClient;

    @Mock
    AppStatus appStatus;

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void upload() {
        //TODO
    }

    @Test
    public void sizeOfSingleFile() throws SdkClientException, ObsException {
        when(obsClient.exists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"))).thenReturn(true);
        when(obsClient.size(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"))).thenReturn(559967L);

        ObsAdapter uut = new ObsAdapter(obsClient, ReportingFactory.NULL, false, appStatus);

        assertThat(uut.sizeOf(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"), is(equalTo(559967L)));

        verify(obsClient).exists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"));
        verify(obsClient, times(0)).prefixExists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"));
        verify(obsClient).size(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"));
    }

    @Test
    public void sizeOfDirectoryProduct() throws SdkClientException, ObsException {
        when(obsClient.exists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE"))).thenReturn(false);
        when(obsClient.prefixExists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE"))).thenReturn(true);
        when(obsClient.list(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE")).thenReturn(
                Arrays.asList(
                        "AUX2.SAFE/data/s1a-aux-cal.xml",
                        "AUX2.SAFE/manifest.safe",
                        "AUX2.SAFE/support/s1-aux-cal.xsd",
                        "AUX2.SAFE/support/s1-object-types.xsd"));
        when(obsClient.size(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE/data/s1a-aux-cal.xml"))).thenReturn(1558640L);
        when(obsClient.size(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE/manifest.safe"))).thenReturn(3509L);
        when(obsClient.size(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE/support/s1-aux-cal.xsd"))).thenReturn(9723L);
        when(obsClient.size(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE/support/s1-object-types.xsd"))).thenReturn(63114L);

        ObsAdapter uut = new ObsAdapter(obsClient, ReportingFactory.NULL, false, appStatus);

        assertThat(uut.sizeOf(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE"), is(equalTo(1558640L + 3509L + 9723L + 63114L)));

        verify(obsClient).exists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE"));
        verify(obsClient).prefixExists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE"));
        verify(obsClient).list(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE");
        verify(obsClient).size(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE/data/s1a-aux-cal.xml"));
        verify(obsClient).size(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE/manifest.safe"));
        verify(obsClient).size(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE/support/s1-aux-cal.xsd"));
        verify(obsClient).size(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE/support/s1-object-types.xsd"));
    }

    @Test
    public void sizeOfNonExisting() throws SdkClientException, ObsException {
        when(obsClient.exists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"))).thenReturn(false);

        ObsAdapter uut = new ObsAdapter(obsClient, ReportingFactory.NULL, false, appStatus);

        assertThat(uut.sizeOf(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"), is(equalTo(-1L)));

        verify(obsClient).exists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"));
        verify(obsClient).prefixExists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"));
        verify(obsClient, times(0)).list(any(), any());
        verify(obsClient, times(0)).size(any());
    }

    @Test
    public void sizeOfWithExceptionDuringExists() throws SdkClientException {
        when(obsClient.exists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"))).thenThrow(new SdkClientException("ERROR"));
        ObsAdapter uut = new ObsAdapter(obsClient, ReportingFactory.NULL, false, appStatus);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> uut.sizeOf(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"));
        assertThat(exception.getMessage(), startsWith("Error while retrieving size for obs object with key AUX1.EOF:"));
    }

    @Test
    public void sizeOfWithExceptionDuringPrefixExists() throws SdkClientException {
        when(obsClient.exists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE"))).thenReturn(false);
        when(obsClient.prefixExists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE"))).thenThrow(new SdkClientException("ERROR"));

        ObsAdapter uut = new ObsAdapter(obsClient, ReportingFactory.NULL, false, appStatus);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> uut.sizeOf(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE"));
        assertThat(exception.getMessage(), startsWith("Error while retrieving size for obs object with key AUX2.SAFE:"));
    }

    @Test
    public void sizeOfWithExceptionDuringSize() throws SdkClientException, ObsException {
        when(obsClient.exists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"))).thenReturn(true);
        when(obsClient.size(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"))).thenThrow(new ObsException(ProductFamily.AUXILIARY_FILE, "AUX1.EOF", new RuntimeException("ERROR")));

        ObsAdapter uut = new ObsAdapter(obsClient, ReportingFactory.NULL, false, appStatus);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> uut.sizeOf(ProductFamily.AUXILIARY_FILE, "AUX1.EOF"));
        assertThat(exception.getMessage(), startsWith("Error while retrieving size for obs object with key AUX1.EOF:"));
    }

    @Test
    public void sizeOfWithExceptionDuringList() throws SdkClientException {
        when(obsClient.exists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE"))).thenReturn(false);
        when(obsClient.prefixExists(new ObsObject(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE"))).thenReturn(true);
        when(obsClient.list(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE")).thenThrow(new SdkClientException("ERROR"));

        ObsAdapter uut = new ObsAdapter(obsClient, ReportingFactory.NULL, false, appStatus);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> uut.sizeOf(ProductFamily.AUXILIARY_FILE, "AUX2.SAFE"));
        assertThat(exception.getMessage(), startsWith("Error while retrieving size for obs object with key AUX2.SAFE:"));
    }
}