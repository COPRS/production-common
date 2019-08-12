package esa.s1pdgs.cpoc.mdcatalog.rest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import esa.s1pdgs.cpoc.common.errors.processing.MetadataMalformedException;
import esa.s1pdgs.cpoc.common.errors.processing.MetadataNotPresentException;
import esa.s1pdgs.cpoc.mdcatalog.es.EsServices;
import esa.s1pdgs.cpoc.metadata.model.L0AcnMetadata;
import esa.s1pdgs.cpoc.metadata.model.L0SliceMetadata;

public class L0SliceMetadataControllerTest extends RestControllerTest {

    @Mock
    private EsServices esServices;

    private L0SliceMetadataController controller;

    @Before
    public void init() throws IOException {
        MockitoAnnotations.initMocks(this);

        this.controller = new L0SliceMetadataController(esServices);
        this.initMockMvc(this.controller);
    }

    private void mockGetL0Slice(L0SliceMetadata response) throws Exception {
        doReturn(response).when(esServices)
                .getL0Slice(Mockito.any(String.class));
    }

    private void mockGetL0Acn(L0AcnMetadata response) throws Exception {
        doReturn(response).when(esServices).getL0Acn(Mockito.any(String.class),
                Mockito.any(String.class), Mockito.any(String.class));
    }

    private void mockGetL0Acn3Responses(L0AcnMetadata response,
            L0AcnMetadata secondResponse, L0AcnMetadata thirdResponse)
            throws Exception {
        doReturn(response, secondResponse, thirdResponse).when(esServices)
                .getL0Acn(Mockito.any(String.class), Mockito.any(String.class),
                        Mockito.any(String.class));
    }

    private void mockGetL0SliceMetadataNotPresentException() throws Exception {
        doThrow(new MetadataNotPresentException("name")).when(esServices)
                .getL0Slice(Mockito.any(String.class));
    }

    private void mockGetL0SliceMetadataMalformedException() throws Exception {
        doThrow(new MetadataMalformedException("url")).when(esServices)
                .getL0Slice(Mockito.any(String.class));
    }

    private void mockGetL0SliceException() throws Exception {
        doThrow(new Exception()).when(esServices)
                .getL0Slice(Mockito.any(String.class));
    }

    private void mockGetL0AcnMetadataMalformedException() throws Exception {
        doThrow(new MetadataMalformedException("url")).when(esServices)
                .getL0Acn(Mockito.any(String.class), Mockito.any(String.class),
                        Mockito.any(String.class));
    }

    private void mockGetL0AcnException() throws Exception {
        doThrow(new Exception()).when(esServices).getL0Acn(
                Mockito.any(String.class), Mockito.any(String.class),
                Mockito.any(String.class));
    }

    @Test
    public void testGetL0SliceMetadata() throws Exception {
        // Expected Result
        L0SliceMetadata expectedResult = new L0SliceMetadata("name",
                "type", "url", "validityStartTime", "validityStopTime",
                "mission", "satellite", "station");
        expectedResult.setInstrumentConfigurationId(0);
        expectedResult.setNumberSlice(2);
        expectedResult.setDatatakeId("datatakeId");

        L0SliceMetadata response = new L0SliceMetadata();
        response.setProductName("name");
        response.setProductType("type");
        response.setKeyObjectStorage("url");
        response.setValidityStart("validityStartTime");
        response.setValidityStop("validityStopTime");
        response.setMissionId("mission");
		response.setSatelliteId("satellite");
		response.setStationCode("station");
        response.setInstrumentConfigurationId(0);
        response.setNumberSlice(2);
        response.setDatatakeId("datatakeId");

        this.mockGetL0Slice(response);

        MvcResult result = request(get("/l0Slice/name"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        assertEquals("Result is not returning the HTTP OK Status code", 200,
                result.getResponse().getStatus());
        assertEquals("Result is different from the expected result",
                expectedResult.toString(),
                result.getResponse().getContentAsString());

    }

    @Test
    public void testGetL0SliceMetadataIsNotPresentException() throws Exception {
        this.mockGetL0SliceMetadataNotPresentException();
        MvcResult result = request(get("/l0Slice/name"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        assertEquals("Result is not returning the HTTP NOT FOUND Status code",
                404, result.getResponse().getStatus());
    }

    @Test
    public void testGetL0SliceMetadataMalformedException() throws Exception {
        this.mockGetL0SliceMetadataMalformedException();
        MvcResult result = request(get("/l0Slice/name"))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError())
                .andReturn();
        assertEquals("Result is not returning the HTTP NOT FOUND Status code",
                500, result.getResponse().getStatus());
    }

    @Test
    public void testGetL0SliceException() throws Exception {
        this.mockGetL0SliceException();
        MvcResult result = request(get("/l0Slice/name"))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError())
                .andReturn();
        assertEquals("Result is not returning the HTTP NOT FOUND Status code",
                500, result.getResponse().getStatus());
    }

    @Test
    public void testGetL0SliceWithoutVariable() throws Exception {
        MvcResult result = request(get("/l0Slice/"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        assertEquals("Result is not returning the HTTP NOT FOUND Status code",
                404, result.getResponse().getStatus());
    }

    @Test
    public void testGetL0SliceWithOneVariable() throws Exception {
        MvcResult result = request(get("/l0Slice/"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        assertEquals("Result is not returning the HTTP NOT FOUND Status code",
                404, result.getResponse().getStatus());
    }

    @Test
    public void testGetL0AcnMetadata() throws Exception {
        L0SliceMetadata l0Slice = new L0SliceMetadata("name0S", "type",
                "url", "validityStartTime", "validityStopTime",
                "mission", "satellite", "station");
        l0Slice.setInstrumentConfigurationId(0);
        l0Slice.setNumberSlice(2);
        l0Slice.setDatatakeId("datatakeId");

        L0SliceMetadata l0SliceResponse = new L0SliceMetadata();
        l0SliceResponse.setProductName("name0S");
        l0SliceResponse.setProductType("type");
        l0SliceResponse.setKeyObjectStorage("url");
        l0SliceResponse.setValidityStart("validityStartTime");
        l0SliceResponse.setValidityStop("validityStopTime");
        l0SliceResponse.setMissionId("mission");
        l0SliceResponse.setSatelliteId("satellite");
        l0SliceResponse.setStationCode("station");
        l0SliceResponse.setInstrumentConfigurationId(0);
        l0SliceResponse.setNumberSlice(2);
        l0SliceResponse.setDatatakeId("datatakeId");

        L0AcnMetadata l0AcnResponse = new L0AcnMetadata();
        l0AcnResponse.setProductName("name0A");
        l0AcnResponse.setProductType("type");
        l0AcnResponse.setKeyObjectStorage("url");
        l0AcnResponse.setValidityStart("validityStartTime");
        l0AcnResponse.setValidityStop("validityStopTime");
        l0AcnResponse.setMissionId("mission");
        l0AcnResponse.setSatelliteId("satellite");
        l0AcnResponse.setStationCode("station");
        l0AcnResponse.setInstrumentConfigurationId(0);
        l0AcnResponse.setNumberOfSlices(2);
        l0AcnResponse.setDatatakeId("datatakeId");

        L0AcnMetadata l0A = new L0AcnMetadata("name0A", "type", "url",
                "validityStartTime", "validityStopTime",
                "mission", "satellite", "station");
        l0A.setDatatakeId("datatakeId");
        l0A.setInstrumentConfigurationId(0);
        l0A.setNumberOfSlices(2);

        // ExpectedResult
        List<L0AcnMetadata> expectedResult =
                new ArrayList<L0AcnMetadata>();
        expectedResult.add(l0A);
        expectedResult.add(l0A);
        expectedResult.add(l0A);

        this.mockGetL0Acn(l0AcnResponse);
        this.mockGetL0Slice(l0SliceResponse);

        MvcResult result = request(get("/l0Slice/name/acns"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        assertEquals("Result is not returning the HTTP OK Status code", 200,
                result.getResponse().getStatus());
        assertEquals("Result is different from the expected result",
                expectedResult.toString().replaceAll(" ", ""),
                result.getResponse().getContentAsString());

    }

    @Test
    public void testGetL0AcnMetadataIsNotPresentException() throws Exception {
        this.mockGetL0SliceMetadataNotPresentException();
        MvcResult result = request(get("/l0Slice/name/acns"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();
        assertEquals("Result is not returning the HTTP NOT FOUND Status code",
                404, result.getResponse().getStatus());
    }

    @Test
    public void testGetL0AcnMetadataMalformedException() throws Exception {
        L0SliceMetadata l0SliceResponse = new L0SliceMetadata();
        l0SliceResponse.setProductName("name0S");
        l0SliceResponse.setProductType("type");
        l0SliceResponse.setKeyObjectStorage("url");
        l0SliceResponse.setValidityStart("validityStartTime");
        l0SliceResponse.setValidityStop("validityStopTime");
        l0SliceResponse.setMissionId("mission");
        l0SliceResponse.setSatelliteId("satellite");
        l0SliceResponse.setStationCode("station");
        l0SliceResponse.setInstrumentConfigurationId(0);
        l0SliceResponse.setNumberSlice(2);
        l0SliceResponse.setDatatakeId("datatakeId");
        this.mockGetL0Slice(l0SliceResponse);
        this.mockGetL0AcnMetadataMalformedException();
        MvcResult result = request(get("/l0Slice/name/acns"))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError())
                .andReturn();
        assertEquals("Result is not returning the HTTP NOT FOUND Status code",
                500, result.getResponse().getStatus());
    }

    @Test
    public void testGetL0AcnException() throws Exception {
        L0SliceMetadata l0SliceResponse = new L0SliceMetadata();
        l0SliceResponse.setProductName("name0S");
        l0SliceResponse.setProductType("type");
        l0SliceResponse.setKeyObjectStorage("url");
        l0SliceResponse.setValidityStart("validityStartTime");
        l0SliceResponse.setValidityStop("validityStopTime");
        l0SliceResponse.setMissionId("mission");
        l0SliceResponse.setSatelliteId("satellite");
        l0SliceResponse.setStationCode("station");
        l0SliceResponse.setInstrumentConfigurationId(0);
        l0SliceResponse.setNumberSlice(2);
        l0SliceResponse.setDatatakeId("datatakeId");
        this.mockGetL0Slice(l0SliceResponse);
        this.mockGetL0AcnException();
        MvcResult result = request(get("/l0Slice/name/acns"))
                .andExpect(MockMvcResultMatchers.status().is5xxServerError())
                .andReturn();
        assertEquals("Result is not returning the HTTP NOT FOUND Status code",
                500, result.getResponse().getStatus());
    }

    @Test
    public void testGetL0AcnMetadataOnlyOneInES() throws Exception {
        L0SliceMetadata l0Slice = new L0SliceMetadata("name0S", "type",
                "url", "validityStartTime", "validityStopTime",
                "mission", "satellite", "station");
        l0Slice.setInstrumentConfigurationId(0);
        l0Slice.setNumberSlice(2);
        l0Slice.setDatatakeId("datatakeId");

        L0SliceMetadata l0SliceResponse = new L0SliceMetadata();
        l0SliceResponse.setProductName("name0S");
        l0SliceResponse.setProductType("type");
        l0SliceResponse.setKeyObjectStorage("url");
        l0SliceResponse.setValidityStart("validityStartTime");
        l0SliceResponse.setValidityStop("validityStopTime");
        l0SliceResponse.setMissionId("mission");
        l0SliceResponse.setSatelliteId("satellite");
        l0SliceResponse.setStationCode("station");
        l0SliceResponse.setInstrumentConfigurationId(0);
        l0SliceResponse.setNumberSlice(2);
        l0SliceResponse.setDatatakeId("datatakeId");

        L0AcnMetadata l0AcnResponse = new L0AcnMetadata();
        l0AcnResponse.setProductName("name0C");
        l0AcnResponse.setProductType("type");
        l0AcnResponse.setKeyObjectStorage("url");
        l0AcnResponse.setValidityStart("validityStartTime");
        l0AcnResponse.setValidityStop("validityStopTime");
        l0AcnResponse.setMissionId("mission");
        l0AcnResponse.setSatelliteId("satellite");
        l0AcnResponse.setStationCode("station");
        l0AcnResponse.setInstrumentConfigurationId(0);
        l0AcnResponse.setNumberOfSlices(2);
        l0AcnResponse.setDatatakeId("datatakeId");

        L0AcnMetadata l0A = new L0AcnMetadata("name0C", "type", "url",
                "validityStartTime", "validityStopTime",
                "mission", "satellite", "station");
        l0A.setDatatakeId("datatakeId");
        l0A.setInstrumentConfigurationId(0);
        l0A.setNumberOfSlices(2);

        // ExpectedResult
        List<L0AcnMetadata> expectedResult =
                new ArrayList<L0AcnMetadata>();
        expectedResult.add(l0A);

        this.mockGetL0Acn3Responses(null, l0AcnResponse, null);
        this.mockGetL0Slice(l0SliceResponse);

        MvcResult result = request(get("/l0Slice/name/acns"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        assertEquals("Result is not returning the HTTP OK Status code", 200,
                result.getResponse().getStatus());
        assertEquals("Result is different from the expected result",
                expectedResult.toString().replaceAll(" ", ""),
                result.getResponse().getContentAsString());

    }

    @Test
    public void testGetL0AcnMetadataOnlyOneReturn() throws Exception {
        L0SliceMetadata l0Slice = new L0SliceMetadata("name0S", "type",
                "url", "validityStartTime", "validityStopTime",
                "mission", "satellite", "station");
        l0Slice.setInstrumentConfigurationId(0);
        l0Slice.setNumberSlice(2);
        l0Slice.setDatatakeId("datatakeId");

        L0SliceMetadata l0SliceResponse = new L0SliceMetadata();
        l0SliceResponse.setProductName("name0S");
        l0SliceResponse.setProductType("type");
        l0SliceResponse.setKeyObjectStorage("url");
        l0SliceResponse.setValidityStart("validityStartTime");
        l0SliceResponse.setValidityStop("validityStopTime");
        l0SliceResponse.setValidityStop("stopDate");
        l0SliceResponse.setMissionId("mission");
        l0SliceResponse.setSatelliteId("satellite");
        l0SliceResponse.setStationCode("station");
        l0SliceResponse.setInstrumentConfigurationId(0);
        l0SliceResponse.setNumberSlice(2);
        l0SliceResponse.setDatatakeId("datatakeId");

        L0AcnMetadata l0AcnResponse = new L0AcnMetadata();
        l0AcnResponse.setProductName("name0C");
        l0AcnResponse.setProductType("type");
        l0AcnResponse.setKeyObjectStorage("url");
        l0AcnResponse.setValidityStart("validityStartTime");
        l0AcnResponse.setValidityStop("validityStopTime");
        l0AcnResponse.setMissionId("mission");
        l0AcnResponse.setSatelliteId("satellite");
        l0AcnResponse.setStationCode("station");
        l0AcnResponse.setInstrumentConfigurationId(0);
        l0AcnResponse.setNumberOfSlices(2);
        l0AcnResponse.setDatatakeId("datatakeId");

        L0AcnMetadata l0A = new L0AcnMetadata("name0C", "type", "url",
                "validityStartTime", "validityStopTime",
                "mission", "satellite", "station");
        l0A.setDatatakeId("datatakeId");
        l0A.setInstrumentConfigurationId(0);
        l0A.setNumberOfSlices(2);

        // ExpectedResult
        List<L0AcnMetadata> expectedResult =
                new ArrayList<L0AcnMetadata>();
        expectedResult.add(l0A);

        this.mockGetL0Acn(l0AcnResponse);
        this.mockGetL0Slice(l0SliceResponse);

        MvcResult result = request(get("/l0Slice/name/acns?mode=ONE"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        assertEquals("Result is not returning the HTTP OK Status code", 200,
                result.getResponse().getStatus());
        assertEquals("Result is different from the expected result",
                expectedResult.toString().replaceAll(" ", ""),
                result.getResponse().getContentAsString());

    }

    @Test
    public void testGetL0AcnMetadataOnlyOneReturn2() throws Exception {
        L0SliceMetadata l0Slice = new L0SliceMetadata("name0S", "type",
                "url", "validityStartTime", "validityStopTime",
                "mission", "satellite", "station");
        l0Slice.setInstrumentConfigurationId(0);
        l0Slice.setNumberSlice(2);
        l0Slice.setDatatakeId("datatakeId");

        L0SliceMetadata l0SliceResponse = new L0SliceMetadata();
        l0SliceResponse.setProductName("name0S");
        l0SliceResponse.setProductType("type");
        l0SliceResponse.setKeyObjectStorage("url");
        l0SliceResponse.setValidityStart("validityStartTime");
        l0SliceResponse.setValidityStop("validityStopTime");
        l0SliceResponse.setMissionId("mission");
        l0SliceResponse.setSatelliteId("satellite");
        l0SliceResponse.setStationCode("station");
        l0SliceResponse.setInstrumentConfigurationId(0);
        l0SliceResponse.setNumberSlice(2);
        l0SliceResponse.setDatatakeId("datatakeId");

        L0AcnMetadata l0AcnResponse = new L0AcnMetadata();
        l0AcnResponse.setProductName("name0C");
        l0AcnResponse.setProductType("type");
        l0AcnResponse.setKeyObjectStorage("url");
        l0AcnResponse.setValidityStart("validityStartTime");
        l0AcnResponse.setValidityStop("validityStopTime");
        l0AcnResponse.setMissionId("mission");
        l0AcnResponse.setSatelliteId("satellite");
        l0AcnResponse.setStationCode("station");
        l0AcnResponse.setInstrumentConfigurationId(0);
        l0AcnResponse.setNumberOfSlices(2);
        l0AcnResponse.setDatatakeId("datatakeId");

        L0AcnMetadata l0A = new L0AcnMetadata("name0C", "type", "url",
                "validityStartTime", "validityStopTime",
                "mission", "satellite", "station");
        l0A.setDatatakeId("datatakeId");
        l0A.setInstrumentConfigurationId(0);
        l0A.setNumberOfSlices(2);

        // ExpectedResult
        List<L0AcnMetadata> expectedResult =
                new ArrayList<L0AcnMetadata>();
        expectedResult.add(l0A);

        this.mockGetL0Acn3Responses(null, l0AcnResponse, null);
        this.mockGetL0Slice(l0SliceResponse);

        MvcResult result = request(get("/l0Slice/name/acns?mode=ONE"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        assertEquals("Result is not returning the HTTP OK Status code", 200,
                result.getResponse().getStatus());
        assertEquals("Result is different from the expected result",
                expectedResult.toString().replaceAll(" ", ""),
                result.getResponse().getContentAsString());

    }

    @Test
    public void testGetL0AcnMetadataOnlyOneReturn3() throws Exception {
        L0SliceMetadata l0Slice = new L0SliceMetadata("name0S", "type",
                "url", "validityStartTime", "validityStopTime",
                "mission", "satellite", "station");
        l0Slice.setInstrumentConfigurationId(0);
        l0Slice.setNumberSlice(2);
        l0Slice.setDatatakeId("datatakeId");

        L0SliceMetadata l0SliceResponse = new L0SliceMetadata();
        l0SliceResponse.setProductName("name0S");
        l0SliceResponse.setProductType("type");
        l0SliceResponse.setKeyObjectStorage("url");
        l0SliceResponse.setValidityStart("validityStartTime");
        l0SliceResponse.setValidityStop("validityStopTime");
        l0SliceResponse.setMissionId("mission");
        l0SliceResponse.setSatelliteId("satellite");
        l0SliceResponse.setStationCode("station");
        l0SliceResponse.setInstrumentConfigurationId(0);
        l0SliceResponse.setNumberSlice(2);
        l0SliceResponse.setDatatakeId("datatakeId");

        L0AcnMetadata l0AcnResponse = new L0AcnMetadata();
        l0AcnResponse.setProductName("name0C");
        l0AcnResponse.setProductType("type");
        l0AcnResponse.setKeyObjectStorage("url");
        l0AcnResponse.setValidityStart("validityStartTime");
        l0AcnResponse.setValidityStop("validityStopTime");
        l0AcnResponse.setMissionId("mission");
        l0AcnResponse.setSatelliteId("satellite");
        l0AcnResponse.setStationCode("station");
        l0AcnResponse.setInstrumentConfigurationId(0);
        l0AcnResponse.setNumberOfSlices(2);
        l0AcnResponse.setDatatakeId("datatakeId");

        L0AcnMetadata l0A = new L0AcnMetadata("name0C", "type", "url",
                "validityStartTime", "validityStopTime",
                "mission", "satellite", "station");
        l0A.setDatatakeId("datatakeId");
        l0A.setInstrumentConfigurationId(0);
        l0A.setNumberOfSlices(2);

        // ExpectedResult
        List<L0AcnMetadata> expectedResult =
                new ArrayList<L0AcnMetadata>();
        expectedResult.add(l0A);

        this.mockGetL0Acn3Responses(null, null, l0AcnResponse);
        this.mockGetL0Slice(l0SliceResponse);

        MvcResult result = request(get("/l0Slice/name/acns?mode=ONE"))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

        assertEquals("Result is not returning the HTTP OK Status code", 200,
                result.getResponse().getStatus());
        assertEquals("Result is different from the expected result",
                expectedResult.toString().replaceAll(" ", ""),
                result.getResponse().getContentAsString());

    }

    @Test
    public void testGetL0AcnMetadataNoACN() throws Exception {
        L0SliceMetadata l0Slice = new L0SliceMetadata("name0S", "type",
                "url", "validityStartTime", "validityStopTime",
                "mission", "satellite", "station");
        l0Slice.setInstrumentConfigurationId(0);
        l0Slice.setNumberSlice(2);
        l0Slice.setDatatakeId("datatakeId");

        L0SliceMetadata l0SliceResponse = new L0SliceMetadata();
        l0SliceResponse.setProductName("name0S");
        l0SliceResponse.setProductType("type");
        l0SliceResponse.setKeyObjectStorage("url");
        l0SliceResponse.setValidityStart("validityStartTime");
        l0SliceResponse.setValidityStop("validityStopTime");
        l0SliceResponse.setInstrumentConfigurationId(0);
        l0SliceResponse.setNumberSlice(2);
        l0SliceResponse.setDatatakeId("datatakeId");

        this.mockGetL0Acn3Responses(null, null, null);
        this.mockGetL0Slice(l0SliceResponse);

        MvcResult result = request(get("/l0Slice/name/acns"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();

        assertEquals("Result is not returning the HTTP NOT FOUND Status code",
                404, result.getResponse().getStatus());

    }

    @Test
    public void testGetL0AcnMetadataL0SliceNULL() throws Exception {

        this.mockGetL0Slice(null);

        MvcResult result = request(get("/l0Slice/name/acns"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andReturn();

        assertEquals("Result is not returning the HTTP NOT FOUND Status code",
                404, result.getResponse().getStatus());

    }
}
