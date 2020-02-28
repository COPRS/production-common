package esa.s1pdgs.cpoc.scaler.config;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import esa.s1pdgs.cpoc.scaler.config.OpenStackConfig;
import esa.s1pdgs.cpoc.scaler.config.OpenStackServerProperties;

public class OpenStackConfigurationTest {

    @Mock
    private OpenStackServerProperties properties;
    
    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        
        doReturn("https://19.16.42.51:6443").when(properties).getEndpoint();
        doReturn("tutu").when(properties).getCredentialUsername();
        doReturn("password").when(properties).getCredentialPassword();
        doReturn("domain-id").when(properties).getDomainId();
        doReturn("project").when(properties).getProjectId();
    }
    
    @Test
    public void testOsClient() {
        OpenStackConfig config = new OpenStackConfig(properties);
        try {
            config.osClient();
            fail("A exception shall be raised during authentication");
        } catch (Exception exc) {
            verify(properties, times(1)).getEndpoint();
            verify(properties, times(1)).getCredentialUsername();
            verify(properties, times(1)).getCredentialPassword();
            verify(properties, times(1)).getDomainId();
            verify(properties, times(1)).getProjectId();
            verifyNoMoreInteractions(properties);
        }
    }
}
