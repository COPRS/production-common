package esa.s1pdgs.cpoc.scaler.scaling;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.common.errors.k8s.K8sUnknownResourceException;
import esa.s1pdgs.cpoc.common.errors.k8s.PodResourceException;
import esa.s1pdgs.cpoc.common.errors.os.OsEntityException;
import esa.s1pdgs.cpoc.scaler.k8s.K8SAdministration;
import esa.s1pdgs.cpoc.scaler.openstack.OpenStackAdministration;

public class CreateResourcesTest {

    @Mock
    private K8SAdministration k8SAdministration;

    @Mock
    private OpenStackAdministration osAdministration;

    @Before
    public void init() throws OsEntityException, PodResourceException,
            K8sUnknownResourceException {
        MockitoAnnotations.initMocks(this);
        doReturn("serverId").when(osAdministration)
                .createServerForL1Wrappers(Mockito.anyString(), Mockito.any());
        doNothing().when(k8SAdministration)
                .launchWrapperPodsPool(Mockito.anyInt(), Mockito.any());
    }

    @Test
    public void testCall() throws Exception {
        AtomicInteger uVMID = new AtomicInteger(2);
        AtomicInteger uPODID = new AtomicInteger(0);
        CreateResources task = new CreateResources(k8SAdministration,
                osAdministration, uVMID, uPODID);

        String result = task.call();
        assertEquals("serverId", result);
        verify(osAdministration, times(1)).createServerForL1Wrappers(
                Mockito.anyString(), Mockito.eq(uVMID));
        verify(k8SAdministration, times(1))
                .launchWrapperPodsPool(Mockito.eq(1), Mockito.eq(uPODID));
    }

}
