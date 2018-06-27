package fr.viveris.s1pdgs.scaler.k8s.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

public class WrapperNodeMonitorTest {
	
	private WrapperNodeMonitor wrapperNodeMonitor;
	
	@Before
	public void init() {
		this.wrapperNodeMonitor = new WrapperNodeMonitor(new NodeDesc("nodeDescName"));
		List<WrapperPodMonitor> wrapperPods = new ArrayList<>();
		wrapperPods.add(new WrapperPodMonitor(new PodDesc("podDescName1")));
		WrapperPodMonitor wrapperPodMonitor2 = new WrapperPodMonitor(new PodDesc("podDescName2"));
		wrapperPodMonitor2.setLogicalStatus(PodLogicalStatus.PROCESSING);
		wrapperPodMonitor2.setRemainingExecutionTime(3600);
		wrapperPods.add(wrapperPodMonitor2);
		WrapperPodMonitor wrapperPodMonitor3 = new WrapperPodMonitor(new PodDesc("podDescName3"));
		wrapperPodMonitor3.setLogicalStatus(PodLogicalStatus.PROCESSING);
		wrapperPodMonitor3.setRemainingExecutionTime(400);
		wrapperPods.add(wrapperPodMonitor3);
		this.wrapperNodeMonitor.addWrapperPods(wrapperPods);
	}

	@Test
	public void testToString() {
		String str = this.wrapperNodeMonitor.toString();
		assertTrue(str.contains("description: {name: nodeDescName"));
		assertTrue(str.contains("nbWrapperPods: 3"));
		assertTrue(str.contains("nbWrapperPdsPerLogicalStatus: {") && str.contains("STOPPING=1") && str.contains("FATALERROR=0") && str.contains("WAITING=0") && str.contains("PROCESSING=2") && str.contains("ERROR=0"));
		assertTrue(str.contains("maxWrapperPodsRemainingExecTimePerLogicalStatus: {") && str.contains("STOPPING=0") && str.contains("FATALERROR=0") && str.contains("WAITING=0") && str.contains("PROCESSING=3600") && str.contains("ERROR=0"));
	}
	
	@Test
	public void testGetMaxRemainingExecTimeForActivesPods() {
		assertEquals("Max is not the same", 3600, this.wrapperNodeMonitor.getMaxRemainingExecTimeForActivesPods());
	}
	
	@Test
	public void testGetActivesPods() {
		List<WrapperPodMonitor> wrapperPods = new ArrayList<>();
		WrapperPodMonitor wrapperPodMonitor = new WrapperPodMonitor(new PodDesc("podDescName2"));
		wrapperPodMonitor.setLogicalStatus(PodLogicalStatus.PROCESSING);
		wrapperPodMonitor.setRemainingExecutionTime(3600);
		wrapperPods.add(wrapperPodMonitor);
		WrapperPodMonitor wrapperPodMonitor2 = new WrapperPodMonitor(new PodDesc("podDescName3"));
		wrapperPodMonitor2.setLogicalStatus(PodLogicalStatus.PROCESSING);
		wrapperPodMonitor2.setRemainingExecutionTime(400);
		wrapperPods.add(wrapperPodMonitor2);
		assertEquals("Actives pods are differents", wrapperPods, this.wrapperNodeMonitor.getActivesPods());
	}
	
	@Test
	public void testGetNbPodsPerK8SStatus() {
		assertEquals("Number of pod with the same status are differents", 3, this.wrapperNodeMonitor.getNbPodsPerK8SStatus(PodStatus.Unknown));
		assertEquals("Number of pod with the same status are differents", 0, this.wrapperNodeMonitor.getNbPodsPerK8SStatus(PodStatus.Failed));
		assertEquals("Number of pod with the same status are differents", 0, this.wrapperNodeMonitor.getNbPodsPerK8SStatus(PodStatus.Succeeded));
		assertEquals("Number of pod with the same status are differents", 0, this.wrapperNodeMonitor.getNbPodsPerK8SStatus(PodStatus.Running));
		assertEquals("Number of pod with the same status are differents", 0, this.wrapperNodeMonitor.getNbPodsPerK8SStatus(PodStatus.Pending));
	}
	
	/**
	 * Check equals and hascode methods
	 */
	@Test
	public void testEquals() {
		EqualsVerifier.forClass(WrapperNodeMonitor.class).usingGetClass().suppress(Warning.NONFINAL_FIELDS).verify();
	}


}
