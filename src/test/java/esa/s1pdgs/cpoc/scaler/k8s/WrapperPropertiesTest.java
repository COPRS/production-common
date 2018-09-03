package esa.s1pdgs.cpoc.scaler.k8s;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import esa.s1pdgs.cpoc.scaler.k8s.WrapperProperties;

@RunWith(SpringRunner.class)
@SpringBootTest
@DirtiesContext

public class WrapperPropertiesTest {

	@Autowired
	private WrapperProperties wrapperProperties;
	
	@Test
	public void wrapperPropertiesTest(){
		assertEquals("Invalid Label Wrapper Config label", "wrapperconfig", wrapperProperties.getLabelWrapperConfig().getLabel());
		assertEquals("Invalid Label Wrapper Config value", "l1", wrapperProperties.getLabelWrapperConfig().getValue());
		assertEquals("Invalid Label Wrapper State Used label", "wrapperstate", wrapperProperties.getLabelWrapperStateUsed().getLabel());
		assertEquals("Invalid Label Wrapper State Used value", "used", wrapperProperties.getLabelWrapperStateUsed().getValue());
		assertEquals("Invalid Label Wrapper State Unused label", "wrapperstate", wrapperProperties.getLabelWrapperStateUnused().getLabel());
		assertEquals("Invalid Label Wrapper State Unused value", "unused", wrapperProperties.getLabelWrapperStateUnused().getValue());
		assertEquals("Invalid Label Wrapper app label", "app", wrapperProperties.getLabelWrapperApp().getLabel());
		assertEquals("Invalid Label Wrapper app value", "l1-wrapper", wrapperProperties.getLabelWrapperApp().getValue());
		assertEquals("Invalid Label Wrapper NbPoolingPods", 3, wrapperProperties.getNbPoolingPods());
		assertEquals("Invalid Label Wrapper NbMinServers", 3, wrapperProperties.getNbMinServers());
		assertEquals("Invalid Label Wrapper NbMaxServers", 30, wrapperProperties.getNbMaxServers());
		assertEquals("Invalid Label Wrapper NbPodsPerServer", 1, wrapperProperties.getNbPodsPerServer());
		assertEquals("Invalid Label Wrapper PodTemplateFile", "config/template_l1_wrapper_pod.yml", wrapperProperties.getPodTemplateFile());
		assertEquals("Invalid Label Wrapper ExecutionTime Average S", 900, wrapperProperties.getExecutionTime().getAverageS());
		assertEquals("Invalid Label Wrapper ExecutionTime MinThreshold S", 900.0, wrapperProperties.getExecutionTime().getMinThresholdS(), 0.0);
		assertEquals("Invalid Label Wrapper ExecutionTime MaxThreshold S", 1800.0, wrapperProperties.getExecutionTime().getMaxThresholdS(), 0.0);
		assertEquals("Invalid Label Wrapper TempoPooling Ms", 500, wrapperProperties.getTempoPoolingMs());
		assertEquals("Invalid Label Wrapper TempoScaling S", 600, wrapperProperties.getTempoScalingS());
		assertEquals("Invalid Label Wrapper TempoDeleteResources S", 600, wrapperProperties.getTempoDeleteResourcesS());
		assertEquals("Invalid Label Wrapper WaitPodDeletion Ms", 10000, wrapperProperties.getWaitPodDeletionMs());
		assertEquals("Invalid Label Wrapper RestApi Port", 8080, wrapperProperties.getRestApi().getPort());
	}
	

}
