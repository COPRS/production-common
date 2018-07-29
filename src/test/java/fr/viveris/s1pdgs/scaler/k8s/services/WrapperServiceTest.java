package fr.viveris.s1pdgs.scaler.k8s.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import fr.viveris.s1pdgs.scaler.k8s.model.PodLogicalStatus;
import fr.viveris.s1pdgs.scaler.k8s.model.WrapperDesc;
import fr.viveris.s1pdgs.scaler.k8s.model.dto.AppState;
import fr.viveris.s1pdgs.scaler.k8s.model.dto.WrapperStatusDto;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.WrapperStatusException;
import fr.viveris.s1pdgs.scaler.k8s.model.exceptions.WrapperStopException;

public class WrapperServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private WrapperService service;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        service = new WrapperService(restTemplate, 82, 500, 3);
    }

    @Test
    public void testBuildWrapperDescFromDTO() throws WrapperStatusException {
        WrapperStatusDto dto = new WrapperStatusDto(AppState.WAITING, 12540, 0);
        WrapperDesc expected = new WrapperDesc("pod-name");
        expected.setErrorCounter(0);
        expected.setTimeSinceLastChange(12540);
        expected.setStatus(PodLogicalStatus.WAITING);
        assertEquals(expected,
                service.buildWrapperDescFromDto(dto, "pod-name", "pod-ip"));

        dto = new WrapperStatusDto(AppState.PROCESSING, 12540, 2);
        expected = new WrapperDesc("pod-name");
        expected.setErrorCounter(2);
        expected.setTimeSinceLastChange(12540);
        expected.setStatus(PodLogicalStatus.PROCESSING);
        assertEquals(expected,
                service.buildWrapperDescFromDto(dto, "pod-name", "pod-ip"));

        dto = new WrapperStatusDto(AppState.STOPPING, 12540, 2);
        expected = new WrapperDesc("pod-name");
        expected.setErrorCounter(2);
        expected.setTimeSinceLastChange(12540);
        expected.setStatus(PodLogicalStatus.STOPPING);
        assertEquals(expected,
                service.buildWrapperDescFromDto(dto, "pod-name", "pod-ip"));

        dto = new WrapperStatusDto(AppState.ERROR, 12540, 2);
        expected = new WrapperDesc("pod-name");
        expected.setErrorCounter(2);
        expected.setTimeSinceLastChange(12540);
        expected.setStatus(PodLogicalStatus.ERROR);
        assertEquals(expected,
                service.buildWrapperDescFromDto(dto, "pod-name", "pod-ip"));
    }

    @Test(expected = WrapperStatusException.class)
    public void testBuildWrapperDescFromDTONull()
            throws WrapperStatusException {
        service.buildWrapperDescFromDto(null, "pod-name", "pod-ip");
    }

    @Test
    public void testGetWrappers() throws WrapperStatusException {
        WrapperStatusDto dto = new WrapperStatusDto(AppState.WAITING, 12540, 0);
        WrapperDesc expected = new WrapperDesc("pod-name");
        expected.setErrorCounter(0);
        expected.setTimeSinceLastChange(12540);
        expected.setStatus(PodLogicalStatus.WAITING);
        doReturn(new ResponseEntity<>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<>(dto, HttpStatus.OK)).when(restTemplate)
                        .exchange(Mockito.anyString(), Mockito.any(),
                                Mockito.any(),
                                Mockito.eq(WrapperStatusDto.class));

        WrapperDesc result = service.getWrapperStatus("pod-name", "pod-ip");
        assertEquals(expected, result);
        verify(restTemplate, times(3)).exchange(
                Mockito.eq("http://pod-ip:82/app/status"),
                Mockito.eq(HttpMethod.GET), Mockito.isNull(),
                Mockito.eq(WrapperStatusDto.class));
    }

    @Test
    public void testGetWrappersInvalmirMaxRetries()
            throws WrapperStatusException {
        service = new WrapperService(restTemplate, 82, 500, -1);
        
        doReturn(new ResponseEntity<>(HttpStatus.BAD_GATEWAY))
                .when(restTemplate).exchange(Mockito.anyString(), Mockito.any(),
                        Mockito.any(), Mockito.eq(WrapperStatusDto.class));

        try {
            service.getWrapperStatus("pod-name", "pod-ip");
            fail("WrapperStatusExcetpion waiting");
        } catch (WrapperStatusException e) {
            assertEquals("pod-name", e.getName());
            assertEquals("pod-ip", e.getIp());
            assertNull(e.getCause());
            verify(restTemplate, times(0)).exchange(
                    Mockito.eq("http://pod-ip:82/app/status"),
                    Mockito.eq(HttpMethod.GET), Mockito.isNull(),
                    Mockito.eq(WrapperStatusDto.class));
        }
    }

    @Test
    public void testGetWrappersK0Timeout() {
        doReturn(new ResponseEntity<>(HttpStatus.BAD_GATEWAY))
                .when(restTemplate).exchange(Mockito.anyString(), Mockito.any(),
                        Mockito.any(), Mockito.eq(WrapperStatusDto.class));

        try {
            service.getWrapperStatus("pod-name", "pod-ip");
            fail("WrapperStatusExcetpion waiting");
        } catch (WrapperStatusException e) {
            assertEquals("pod-name", e.getName());
            assertEquals("pod-ip", e.getIp());
            assertNull(e.getCause());
            verify(restTemplate, times(4)).exchange(
                    Mockito.eq("http://pod-ip:82/app/status"),
                    Mockito.eq(HttpMethod.GET), Mockito.isNull(),
                    Mockito.eq(WrapperStatusDto.class));
        }
    }

    @Test
    public void testGetWrappersExceptionTimeout() {
        doThrow(new RestClientException("message")).when(restTemplate).exchange(
                Mockito.anyString(), Mockito.any(), Mockito.any(),
                Mockito.eq(WrapperStatusDto.class));

        try {
            service.getWrapperStatus("pod-name", "pod-ip");
            fail("WrapperStatusExcetpion waiting");
        } catch (WrapperStatusException e) {
            assertEquals("pod-name", e.getName());
            assertEquals("pod-ip", e.getIp());
            assertNotNull(e.getCause());
            assertEquals("message", e.getCause().getMessage());
            verify(restTemplate, times(4)).exchange(
                    Mockito.eq("http://pod-ip:82/app/status"),
                    Mockito.eq(HttpMethod.GET), Mockito.isNull(),
                    Mockito.eq(WrapperStatusDto.class));
        }
    }

    @Test
    public void testStopWrappers() throws WrapperStopException {
        doReturn(new ResponseEntity<>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<>(HttpStatus.BAD_GATEWAY),
                new ResponseEntity<>("ret", HttpStatus.OK))
                .when(restTemplate).exchange(Mockito.anyString(), Mockito.any(),
                        Mockito.any(), Mockito.eq(String.class));

        service.stopWrapper("pod-ip");
         verify(restTemplate, times(3)).exchange(
                    Mockito.eq("http://pod-ip:82/app/stop"),
                    Mockito.eq(HttpMethod.POST), Mockito.isNull(),
                    Mockito.eq(String.class));
    }

    @Test
    public void testStopWrappersInvalidMaxRetries() throws WrapperStopException {
        service = new WrapperService(restTemplate, 82, 500, -1);
        doReturn(new ResponseEntity<>(HttpStatus.BAD_GATEWAY))
        .when(restTemplate).exchange(Mockito.anyString(), Mockito.any(),
                Mockito.any(), Mockito.eq(String.class));

        try {
            service.stopWrapper("pod-ip");
            fail("WrapperStatusExcetpion waiting");
        } catch (WrapperStopException e) {
            assertEquals("pod-ip", e.getIp());
            assertNull(e.getCause());
            verify(restTemplate, times(0)).exchange(
                    Mockito.eq("http://pod-ip:82/app/stop"),
                    Mockito.eq(HttpMethod.POST), Mockito.isNull(),
                    Mockito.eq(String.class));
        }
    }

    @Test
    public void testStopWrappersK0Timeout() {
        doReturn(new ResponseEntity<>(HttpStatus.BAD_GATEWAY))
                .when(restTemplate).exchange(Mockito.anyString(), Mockito.any(),
                        Mockito.any(), Mockito.eq(String.class));

        try {
            service.stopWrapper("pod-ip");
            fail("WrapperStatusExcetpion waiting");
        } catch (WrapperStopException e) {
            assertEquals("pod-ip", e.getIp());
            assertNull(e.getCause());
            verify(restTemplate, times(4)).exchange(
                    Mockito.eq("http://pod-ip:82/app/stop"),
                    Mockito.eq(HttpMethod.POST), Mockito.isNull(),
                    Mockito.eq(String.class));
        }
    }

    @Test
    public void testStopWrappersExceptionTimeout() {
        doThrow(new RestClientException("message")).when(restTemplate).exchange(
                Mockito.anyString(), Mockito.any(), Mockito.any(),
                Mockito.eq(String.class));

        try {
            service.stopWrapper("pod-ip");
            fail("WrapperStatusExcetpion waiting");
        } catch (WrapperStopException e) {
            assertEquals("pod-ip", e.getIp());
            assertNotNull(e.getCause());
            assertEquals("message", e.getCause().getMessage());
            verify(restTemplate, times(4)).exchange(
                    Mockito.eq("http://pod-ip:82/app/stop"),
                    Mockito.eq(HttpMethod.POST), Mockito.isNull(),
                    Mockito.eq(String.class));
        }
    }
}
